package Model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import DeepLearning.NeuralNet;
import DeepLearning.PgnGameAnalyzer;

public class BasicEngine extends Position implements IEngine, Comparator<Move> {

	private static Logger logger = LogManager.getLogger();

	private boolean pvSearch = false;

	private Move bestMove = null;

	private long timeOfSearch;

	private volatile boolean interrupted = false;
	private volatile int depth = 5;

	private int sortingDepth = 3;

	public static int PAWN_VALUE = 100;
	public static int KNIGHT_VALUE = 300;
	public static int BISHOP_VALUE = 320;
	public static int ROOK_VALUE = 450;
	public static int QUEEN_VALUE = 900;
	public static int KING_VALUE = 100000000;

	private final int MAX_PLY = 100;
	private final int MAX_MOVE_BUFFER = 3800;

	private int Material = 0;

	private TranspositionTable transTable = new TranspositionTable();

	private Move[] currentSearch = new Move[MAX_MOVE_BUFFER];
	private BinaryMoveTree moveTree = new BinaryMoveTree(6);
	private BinaryMoveNode currentNode = null;
	private int[] moveBufLen = new int[MAX_PLY];
	private Killer[][] killers = new Killer[MAX_PLY][2];

	private int lastNodeType = EvalNode.PVNODE;
	private long currentHash = 0L;
	private long currentHash2 = 0L;

	private long targetBitboard;
	private long freeSquares;
	private long tempPiece;
	private long tempMove;
	private Move move = new Move();
	private int from, to, king;
	private byte fBitstate6;
	private byte rBitstate6;
	private byte diaga8h1Bitstate6;
	private byte diaga1h8Bitstate6;

	private int numberOfPvAlphaBetas = 0;
	private int numberOfPvAlphaBetaWithoutPvs = 0;
	private int numberOfQuiescences = 0;
	private int numberOfBetaCutoffs = 0;

	private long toBitMap;
	private int piece;
	private int captured;

	private long fromBitMap;
	private long fromToBitMap;

	private int compare1;
	private int compare2;

	public BasicEngine() {

		super();

		int i, j;

		for (i = 0; i < MAX_MOVE_BUFFER; i++) {
			currentSearch[i] = new Move();

		}

		move.clear();
		move.setPiec(W_KNIGHT);
		move.setFrom(55);
		// move.setCapt(EMPTY);
		move.setTosq(63);
		for (i = 0; i < MAX_PLY; i++) {
			killers[i][0] = new Killer();
			killers[i][0].setMoveInt(move.getMoveInt());
			killers[i][1] = new Killer();
			killers[i][1].setMoveInt(move.getMoveInt());
		}

		moveBufLen[0] = 0;

		initialiseHash();
	}

	private void initialiseHash() {

		for (int i = Bitboards.HASH_SHORT_CASTLE_WHITE; i < Bitboards.HASH_LONG_CASTLE_WHITE + 4; i++) {
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[i];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[i];
		}

		tempPiece = whiteKnights;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_KNIGHT * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_KNIGHT * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = whiteBishops;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_BISHOP * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_BISHOP * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = whiteRooks;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_ROOK * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_ROOK * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}

		tempPiece = blackKnights;

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_KNIGHT * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_KNIGHT * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = blackBishops;

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_BISHOP * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_BISHOP * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = blackRooks;

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_ROOK * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_ROOK * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}

	}

	public void pvFindBestMove() {
		interrupted = false;
		this.numberOfPvAlphaBetas = 0;
		this.numberOfPvAlphaBetaWithoutPvs = 0;
		this.numberOfQuiescences = 0;
		this.numberOfBetaCutoffs = 0;

		this.transTable.clear();

		currentNode = moveTree.root;

		timeOfSearch = System.currentTimeMillis();
		bestMove = null;

		int ply = 0;
		moveBufLen[ply] = 0;
		// We assume it's not checkmate
		moveBufLen[ply + 1] = pseudoLegalMoveGenerator(moveBufLen[ply]);

		int value = -BasicEngine.KING_VALUE;

		boolean pvfound = false;

		int alpha;
		int beta;

		int best;

		// after each iteration drop moves that are not good
		// how do you do this??

		outer: for (int currentDepth = 3; currentDepth <= depth; ++currentDepth) {

			sortingDepth = currentDepth - 1;

			System.out.println("There are  " + (moveBufLen[ply + 1] - moveBufLen[ply]) + " many moves");
			Arrays.sort(currentSearch, moveBufLen[ply], moveBufLen[ply + 1], this);

			best = -BasicEngine.KING_VALUE;
			pvfound = false;
			alpha = -BasicEngine.KING_VALUE;
			beta = BasicEngine.KING_VALUE;

			for (int i = moveBufLen[ply]; i < moveBufLen[ply + 1]; ++i) {

				if (interrupted && (bestMove != null)) {
					return;
				}

				makeMove(currentSearch[i]);
				System.out.println(currentSearch[i]);

				currentNode = currentNode.rightChild;
				currentNode.value.setMoveInt(currentSearch[i].getMoveInt());

				if (pvfound) {
					value = -pvAlphaBeta(ply + 1, currentDepth - 1, -alpha - BasicEngine.PAWN_VALUE, -alpha);
					if (value > alpha && value < beta) {
						value = -pvAlphaBeta(ply + 1, currentDepth - 1, -beta, -value);
					}

				} else {
					value = -pvAlphaBeta(ply + 1, currentDepth - 1, -beta, -alpha);
				}

				transTable.put(currentHash, currentHash2, value, currentDepth, lastNodeType);

				unmakeMove(currentSearch[i]);

				if (currentSearch[i].toString().contentEquals("Tf1-f4")) {
					System.out.println("The move Tf1-f4 has evaluation " + value);
				}
				if (value == BasicEngine.KING_VALUE) {
					System.out.println("Checkmate was found");
				}
				if (value > best) {
					if (value >= beta) {
						System.out.println("Value >=beta at ply 0");
						System.exit(1);
					}
					if (value >= KING_VALUE - currentDepth) {
						System.out.println("Checkmate wass found");
						for (int k = 0; k <= currentDepth; ++k) {
							if (value == KING_VALUE - k)
								System.out.println("It's mate in " + k + " moves!");
						}

					}
					best = value;
					if (value > alpha) {
						alpha = value;
						pvfound = true;

						currentNode.setPrincipalVariation();

					}
				} // end if value > best

				currentNode = currentNode.father;

			} // end iterating through first moves

			bestMove = moveTree.getPrincipalMove();
			// triangularArray[0][0]
			// points to
			// a copy of currentSearch[i] where currentSearch[i] is always
			// modified by the move Generator

			if (best >= BasicEngine.KING_VALUE - currentDepth) {
				break outer;
			}

		}
	}

	// the best move so far should be played or not
	@Override
	public void findBestMove() {
		if(isCheckmate())
			return;
		if (pvSearch)
			pvFindBestMove();
		else
			normalFindBestMove();
	}// end do in Background

	private void normalFindBestMove() {

		interrupted = false;
		this.numberOfPvAlphaBetas = 0;
		this.numberOfPvAlphaBetaWithoutPvs = 0;
		this.numberOfQuiescences = 0;
		this.numberOfBetaCutoffs = 0;

		this.transTable.clear();

		currentNode = moveTree.root;

		timeOfSearch = System.currentTimeMillis();
		bestMove = null;

		int ply = 0;
		moveBufLen[ply] = 0;
		// We assume it's not checkmate
		moveBufLen[ply + 1] = pseudoLegalMoveGenerator(moveBufLen[ply]);

		int value = -BasicEngine.KING_VALUE;

		int alpha;
		int beta;

		int best;

		// after each iteration drop moves that are not good
		// how do you do this??

		outer: for (int currentDepth = 3; currentDepth <= depth; ++currentDepth) {

			sortingDepth = currentDepth - 1;

			System.out.println("There are  " + (moveBufLen[ply + 1] - moveBufLen[ply]) + " many moves");
			Arrays.sort(currentSearch, moveBufLen[ply], moveBufLen[ply + 1], this);

			best = -BasicEngine.KING_VALUE;
			alpha = -BasicEngine.KING_VALUE;
			beta = BasicEngine.KING_VALUE;

			for (int i = moveBufLen[ply]; i < moveBufLen[ply + 1]; ++i) {

				if (interrupted && (bestMove != null)) {
					return;
				}

				makeMove(currentSearch[i]);
				System.out.println(currentSearch[i]);

				currentNode = currentNode.rightChild;
				currentNode.value.setMoveInt(currentSearch[i].getMoveInt());

				value = -alphaBeta(ply + 1, currentDepth - 1, -beta, -alpha);
				if(value >= KING_VALUE - currentDepth) {
					--value; //We need one move to reach the evaluation!
				}
				System.out.println( currentSearch[i].toString() + " has evaluation " + value );
				
				transTable.put(currentHash, currentHash2, value, currentDepth, lastNodeType);

				unmakeMove(currentSearch[i]);

				if (currentSearch[i].toString().contentEquals("Tf1-f4")) {
					System.out.println("The move Tf1-f4 has evaluation " + value);
				}
				
				if (value > best) {
					if (value >= beta) {
						System.out.println("Value >=beta at ply 0");
						System.exit(1);
					}
					if (value >= KING_VALUE - currentDepth) {
						System.out.println("Checkmate was found");
						for (int k = 0; k <= currentDepth; ++k) {
							if (value == KING_VALUE - k)
								System.out.println("It's mate in " + k + " moves!");
						}

					}
					best = value;
					if (value > alpha) {
						alpha = value;
						currentNode.setPrincipalVariation();
					}
				} // end if value > best

				currentNode = currentNode.father;

			} // end iterating through first moves

			bestMove = moveTree.getPrincipalMove();
			// triangularArray[0][0]
			// points to
			// a copy of currentSearch[i] where currentSearch[i] is always
			// modified by the move Generator

			if (best >= BasicEngine.KING_VALUE - currentDepth) {
				break outer;
			}

		}
	}

	private int alphaBeta(int ply, int depth, int alpha, int beta) {
		if (isCheckmate()) {
			return -BasicEngine.KING_VALUE;
		}
		++numberOfPvAlphaBetas;

		// at the beginning, we are assuming no move exceeds alpha
		// lastNodeType needs to be a local variable here!!!
		int nodeType = EvalNode.ALLNODE;

		if (depth == 0) {

			// Nothing need to be set here, quiecence set lastNodeType correctly
			return staticEvaluation();

		}

		int value;

		int best = -BasicEngine.KING_VALUE;

		moveBufLen[ply + 1] = moveBufLen[ply];

		if (interrupted && (bestMove != null)) {
			return alpha;
		}

		moveBufLen[ply + 1] = pseudoLegalMoveGenerator(moveBufLen[ply]);
		sortingDepth = depth - 1;

		Arrays.sort(currentSearch, moveBufLen[ply], moveBufLen[ply + 1], this);

		for (int i = moveBufLen[ply]; i < moveBufLen[ply + 1]; i++) {

			makeMove(currentSearch[i]);

			currentNode = currentNode.rightChild;
			currentNode.value.setMoveInt(currentSearch[i].getMoveInt());

			value = -alphaBeta(ply + 1, depth - 1, -beta, -alpha);
			if(value >= KING_VALUE - depth) {
				--value; //We need one move to reach the evaluation
			}
			
			unmakeMove(currentSearch[i]);

			if (value > best) {
				best = value;
				if (value >= beta) {
					++numberOfBetaCutoffs;

					lastNodeType = EvalNode.CUTNODE;
					currentNode = currentNode.father;
					addKiller(currentSearch[i], ply, value - Material());
					return best;
				}

				if (value > alpha) {
					nodeType = EvalNode.PVNODE;
					addKiller(currentSearch[i], ply, value - Material());
					alpha = value;

					currentNode.setPrincipalVariation();

				}
			}

			currentNode = currentNode.father;
		}

		lastNodeType = nodeType;
		return best;
	}

	private int pvAlphaBeta(int ply, int depth, int alpha, int beta) {

		if (isCheckmate()) {
			return -BasicEngine.KING_VALUE + ply;
		}
		++numberOfPvAlphaBetas;

		// at the beginning, we are assuming no move exceeds alpha
		// lastNodeType needs to be a local variable here!!!
		int nodeType = EvalNode.ALLNODE;

		if (depth == 0) {

			// Nothing need to be set here, quiecence set lastNodeType correctly
			return staticEvaluation();

		}

		int value;

		int best = alpha;
		boolean pvfound = false;

		moveBufLen[ply + 1] = moveBufLen[ply];

		// We ignore killers for the moment
		/*
		 * for (int i = 0; i < 2; i++) {
		 * 
		 * // What node type is this??? if (interrupted && (bestMove != null)) { return
		 * alpha + 1; }
		 * 
		 * if (isPseudoLegalMove(killers[ply][i].getMove())) {
		 * 
		 * makeMove(killers[ply][i].getMove());
		 * 
		 * currentNode = currentNode.rightChild;
		 * 
		 * currentNode.value.setMoveInt(killers[ply][i].getMove().getMoveInt());
		 * 
		 * if (!positionRepeated()) {
		 * 
		 * 
		 * 
		 * if (pvfound) { value = -pvAlphaBeta(ply + 1, depth - 1, -alpha - PAWN_VALUE,
		 * -alpha); if (value > alpha && value < beta) { value = -pvAlphaBeta(ply + 1,
		 * depth - 1, -beta, -value); }
		 * 
		 * } else { value = -pvAlphaBeta(ply + 1, depth - 1, -beta, -alpha); }
		 * 
		 * transTable.put(currentHash, currentHash2, value, depth, lastNodeType);
		 * 
		 * } // End if position is not repeated else { // If position is repeated, then
		 * evaluation is 0 value = 0; }
		 * 
		 * unmakeMove(killers[ply][i].getMove());
		 * 
		 * if (value > best) { if (value >= beta) { ++numberOfBetaCutoffs;
		 * 
		 * lastNodeType = EvalNode.CUTNODE; currentNode = currentNode.father; return
		 * value; } best = value; if (value > alpha) { nodeType = EvalNode.PVNODE; alpha
		 * = value; pvfound = true;
		 * 
		 * currentNode.setPrincipalVariation();
		 * 
		 * } // end if value > alpha } // end if value > best
		 * 
		 * currentNode = currentNode.father; } // end if this killer is a
		 * pseudoLegalMove
		 * 
		 * } // end iterating through killers
		 */

		if (interrupted && (bestMove != null)) {
			return alpha;
		}

		moveBufLen[ply + 1] = pseudoLegalMoveGenerator(moveBufLen[ply]);
		sortingDepth = depth - 1;

		Arrays.sort(currentSearch, moveBufLen[ply], moveBufLen[ply + 1], this);

		for (int i = moveBufLen[ply]; i < moveBufLen[ply + 1]; i++) {

			makeMove(currentSearch[i]);

			currentNode = currentNode.rightChild;
			currentNode.value.setMoveInt(currentSearch[i].getMoveInt());

			if (!positionRepeated()) {
				/*
				 * valueEvalNode = transTable.getEvaluation(currentHash, currentHash2, depth);
				 * value = valueEvalNode.evaluation;
				 * 
				 * if (value == TranspositionTable.NOENTRY || (valueEvalNode.nodeType ==
				 * EvalNode.CUTNODE && value > beta) || (valueEvalNode.nodeType ==
				 * EvalNode.ALLNODE && value < beta)) {
				 */

				if (pvfound) {
					value = -pvAlphaBeta(ply + 1, depth - 1, -alpha - PAWN_VALUE, -alpha);
					if (value > alpha && value < beta) {
						value = -pvAlphaBeta(ply + 1, depth - 1, -beta, -value);
					}

				} else {
					value = -pvAlphaBeta(ply + 1, depth - 1, -beta, -alpha);
				}

				transTable.put(currentHash, currentHash2, value, depth, lastNodeType);

			} // end if position is not repeated
			else {
				value = 0;
			}

			unmakeMove(currentSearch[i]);

			if (value > best) {
				best = value;
				if (value >= beta) {
					++numberOfBetaCutoffs;

					lastNodeType = EvalNode.CUTNODE;
					currentNode = currentNode.father;
					addKiller(currentSearch[i], ply, value - Material());
					return best;
				}

				if (value > alpha) {
					nodeType = EvalNode.PVNODE;
					addKiller(currentSearch[i], ply, value - Material());
					alpha = value;
					pvfound = true;

					currentNode.setPrincipalVariation();

				}
			}

			currentNode = currentNode.father;
		}

		lastNodeType = nodeType;
		return best;

	}

	public String printPrincipalVariation() {
		return moveTree.getPrincipalVariation();
	}

	private int pvAlphaBetaWithoutPv(int ply, int depth, int alpha, int beta) {

		++numberOfPvAlphaBetaWithoutPvs;

		int nodeType = EvalNode.ALLNODE;

		if (depth == 0) {
			return staticEvaluation();

		}

		int value;

		int best = -BasicEngine.KING_VALUE;
		boolean pvfound = false;

		moveBufLen[ply + 1] = moveBufLen[ply];

		for (int i = 0; i < 2; i++) {

			if (interrupted && (bestMove != null)) {
				return alpha + 1;
			}

			if (isPseudoLegalMove(killers[ply][i].getMove())) {

				makeMove(killers[ply][i].getMove());

				if (!positionRepeated()) {
					/*
					 * valueEvalNode = transTable.getEvaluation(currentHash, currentHash2, depth);
					 * value = valueEvalNode.evaluation;
					 * 
					 * if (value == TranspositionTable.NOENTRY || (valueEvalNode.nodeType ==
					 * EvalNode.CUTNODE && value > best) || (valueEvalNode.nodeType ==
					 * EvalNode.ALLNODE && value < beta)) {
					 */

					if (pvfound) {
						value = -pvAlphaBetaWithoutPv(ply + 1, depth - 1, -alpha - PAWN_VALUE, -alpha);
						if (value > alpha && value < beta) {
							value = -pvAlphaBetaWithoutPv(ply + 1, depth - 1, -beta, -value);
						}

					} else {
						value = -pvAlphaBetaWithoutPv(ply + 1, depth - 1, -beta, -alpha);
					}

					transTable.put(currentHash, currentHash2, value, depth, lastNodeType);

				} // end if position is not repeated
				else {
					value = 0;
				}

				unmakeMove(killers[ply][i].getMove());

				if (value > best) {
					if (value >= beta) {
						++numberOfBetaCutoffs;
						lastNodeType = EvalNode.CUTNODE;
						return value;
					}
					best = value;
					if (value > alpha) {
						nodeType = EvalNode.PVNODE;
						alpha = value;
						pvfound = true;

					}
				}

			}

		}

		if (interrupted && (bestMove != null)) {
			return alpha + 1;
		}

		moveBufLen[ply + 1] = pseudoLegalMoveGenerator(moveBufLen[ply]);

		sortingDepth = depth - 1;

		Arrays.sort(currentSearch, moveBufLen[ply], moveBufLen[ply + 1], this);

		for (int i = moveBufLen[ply]; i < moveBufLen[ply + 1]; i++) {

			makeMove(currentSearch[i]);

			if (!positionRepeated()) {
				/*
				 * valueEvalNode = transTable.getEvaluation(currentHash, currentHash2, depth);
				 * value = valueEvalNode.evaluation; if (value == TranspositionTable.NOENTRY ||
				 * (valueEvalNode.nodeType == EvalNode.CUTNODE && value > best) ||
				 * (valueEvalNode.nodeType == EvalNode.ALLNODE && value < beta)) {
				 */
				if (pvfound) {
					value = -pvAlphaBetaWithoutPv(ply + 1, depth - 1, -alpha - PAWN_VALUE, -alpha);
					if (value > alpha && value < beta) {
						value = -pvAlphaBetaWithoutPv(ply + 1, depth - 1, -beta, -value);
					}

				} else {
					value = -pvAlphaBetaWithoutPv(ply + 1, depth - 1, -beta, -alpha);
				}

				transTable.put(currentHash, currentHash2, value, depth, lastNodeType);

			} // end if position not repeated
			else {
				value = 0;
			}

			unmakeMove(currentSearch[i]);

			if (value > best) {
				if (value >= beta) {
					++numberOfBetaCutoffs;
					lastNodeType = EvalNode.CUTNODE;
					addKiller(currentSearch[i], ply, value - Material());
					return value;
				}
				best = value;
				if (value > alpha) {
					nodeType = EvalNode.PVNODE;
					addKiller(currentSearch[i], ply, value - Material());
					alpha = value;
					pvfound = true;

				}
			}
		}

		if (best == -BasicEngine.KING_VALUE) {
			best = 0;
		}

		lastNodeType = nodeType;
		return best;

	}

	private int staticEvaluation() {

		return 0;
		/*
		 * int [] i = new int[3]; long l = whitePieces >>
		 * Long.numberOfTrailingZeros(whitePieces); tempMove = l; for(int j=0; j < 3;
		 * ++j) { i[j] = Long.numberOfTrailingZeros(tempMove); tempMove ^=
		 * Bitboards.BITSET[i[j]]; } int w = i[0] + i[1] + i[2]; w += i[0] % 8 + i[1] %
		 * 8 + i[2] % 8;
		 * 
		 * l = blackPieces >> Long.numberOfTrailingZeros(blackPieces); tempMove = l;
		 * for(int j=0; j < 3; ++j) { i[j] = Long.numberOfLeadingZeros(tempMove);
		 * tempMove ^= Bitboards.BITSET[i[j]]; } int b = i[0] + i[1] + i[2]; b += i[0] %
		 * 8 + i[1] % 8 + i[2] % 8;
		 * 
		 * return nextMove * (b-w)*100;
		 */
	}

	@Override
	public void stop() {

		interrupted = true;
	}

	private int bonusBishops() {

		return 0;
	}

	private int bonusKnights() {

		int whiteKnightEval = 0;
		whiteKnightEval += 10 * Long.bitCount(whiteKnights & Bitboards.CENTRE);
		whiteKnightEval += 7 * Long.bitCount(whiteKnights & Bitboards.ADVANDED_CENTRE);
		whiteKnightEval -= 8 * Long.bitCount(whiteKnights & Bitboards.EDGE_OF_BOARD);

		int blackKnightEval = 0;
		blackKnightEval += 10 * Long.bitCount(blackKnights & Bitboards.CENTRE);
		blackKnightEval += 7 * Long.bitCount(blackKnights & Bitboards.ADVANDED_CENTRE);
		blackKnightEval -= 8 * Long.bitCount(blackKnights & Bitboards.EDGE_OF_BOARD);

		return nextMove * (whiteKnightEval - blackKnightEval);
	}

	private int rookBonus() {

		return 0;

	}

	public void makeMove(Move move)

	{

		gameLine[endOfSearch].getMove().setMoveInt(move.getMoveInt());

		gameLine[endOfSearch].setHash(currentHash);

		gameLine[endOfSearch].setHash2(currentHash2);

		endOfSearch++;

		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_BLACK_TO_MOVE];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_BLACK_TO_MOVE];

		from = move.getFrom();
		to = move.getTosq();
		piece = move.getPiec();

		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[piece * 64 + from];
		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[piece * 64 + to];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[piece * 64 + from];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[piece * 64 + to];

		fromBitMap = Bitboards.BITSET[from];
		fromToBitMap = fromBitMap | Bitboards.BITSET[to];

		switch (piece)

		{

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_KNIGHT;

			occupiedSquares ^= fromToBitMap;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_BISHOP;

			occupiedSquares ^= fromToBitMap;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_ROOK;

			occupiedSquares ^= fromToBitMap;

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_KNIGHT;

			occupiedSquares ^= fromToBitMap;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_BISHOP;

			occupiedSquares ^= fromToBitMap;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_ROOK;

			occupiedSquares ^= fromToBitMap;

			break;

		}

		nextMove *= (-1);

	}

	public int Material() {
		return nextMove * Material;
	}

	private int pseudoLegalMoveGenerator(int index) {

		if (isCheckmate()) {
			return index;
		}

		move.clear();
		freeSquares = ~occupiedSquares;

		targetBitboard = ~occupiedSquares;

		if (nextMove == 1) {
			move.setPiec(W_KNIGHT);

			tempPiece = whiteKnights;
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(W_ROOK);
			tempPiece = getWhiteRooks();

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
						& targetBitboard;

				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];

				}

				tempPiece ^= Bitboards.BITSET[from];
			}

			move.setPiec(W_BISHOP);
			tempPiece = getWhiteBishops();

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[(int) from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[(int) from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		} else {

			move.setPiec(B_KNIGHT);

			tempPiece = blackKnights;
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(B_ROOK);
			tempPiece = getBlackRooks();

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
						& targetBitboard;

				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);

					move.setTosq(to);
					// move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];

				}

				tempPiece ^= Bitboards.BITSET[from];
			}

			move.setPiec(B_BISHOP);
			tempPiece = getBlackBishops();

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);

				tempMove = (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;

				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		}

		return index;

	}

	@Override
	public void unmakeMove(Move move)

	{

		piece = move.getPiec();

		from = move.getFrom();

		to = move.getTosq();

		fromBitMap = Bitboards.BITSET[from];
		fromToBitMap = fromBitMap | Bitboards.BITSET[to];

		switch (piece)

		{

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_KNIGHT;

			square[to] = EMPTY;

			occupiedSquares ^= fromToBitMap;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_BISHOP;

			square[to] = EMPTY;

			occupiedSquares ^= fromToBitMap;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_ROOK;

			square[to] = EMPTY;

			occupiedSquares ^= fromToBitMap;

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_KNIGHT;

			square[to] = EMPTY;

			occupiedSquares ^= fromToBitMap;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_BISHOP;

			square[to] = EMPTY;

			occupiedSquares ^= fromToBitMap;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_ROOK;

			square[to] = EMPTY;

			occupiedSquares ^= fromToBitMap;

			break;

		}

		endOfSearch--;

		currentHash = gameLine[endOfSearch].getHash();

		currentHash2 = gameLine[endOfSearch].getHash2();

		nextMove *= (-1);

	}

	private LinkedList<Move> calculateMovesOfPiece(String piece, int comingFrom) {

		long targetBitboard = ~whitePieces, freeSquares;
		long tempPiece, tempMove;
		freeSquares = ~occupiedSquares;

		Move move = new Move();
		int to;
		byte fBitstate6;
		byte rBitstate6;
		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;

		LinkedList<Move> list = new LinkedList<Move>();

		if (nextMove == 1) {
			if ("whiteKnights".equals(piece)) {

				move.setPiec(W_KNIGHT);

				move.setFrom(comingFrom);
				tempMove = Bitboards.KNIGHT_ATTACKS[comingFrom] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					list.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

			}

			else if ("whiteRooks".equals(piece)) {

				move.setPiec(W_ROOK);

				move.setFrom(comingFrom);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[comingFrom])
						* Bitboards.FILEMAGIC[comingFrom]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares
						& Bitboards.RANKMASK[comingFrom]) >>> Bitboards.RANKSHIFT[comingFrom]);

				tempMove = (Bitboards.RANK_ATTACKS[comingFrom][rBitstate6]
						| Bitboards.FILE_ATTACKS[comingFrom][fBitstate6]) & targetBitboard;

				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					list.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];

				}

			}

			else if ("whiteBishops".equals(piece)) {

				move.setPiec(W_BISHOP);

				move.setFrom(comingFrom);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[(int) comingFrom])
						* Bitboards.DIAGA8H1MAGIC[comingFrom] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[(int) comingFrom])
						* Bitboards.DIAGA1H8MAGIC[comingFrom] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[comingFrom][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[comingFrom][diaga8h1Bitstate6]) & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					// move.setCapt(square[to]);
					list.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

			}
		} else {
			// not yet implemented
		}

		return list;
	}

	@Override
	public void setDepth(int depth) {
		this.depth = depth;

	}

	public int getDepth() {
		return depth;
	}

	@Override
	public Move getBestMove() {
		return bestMove;
	}

	private void setRedundantBittboardsAndStuff() {
		currentHash = 0;

		whitePieces = whiteKnights | whiteBishops | whiteRooks;
		blackPieces = blackKnights | blackBishops | blackRooks;
		occupiedSquares = whitePieces | blackPieces;

		if (nextMove == -1) {
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_BLACK_TO_MOVE];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_BLACK_TO_MOVE];
		}

		// Enpassent hash not beachtet so far

		int from;
		long tempPiece;
		for (int i = 0; i < 64; i++) {
			square[i] = 0;
		}

		tempPiece = whiteKnights;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = W_KNIGHT;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_KNIGHT * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_KNIGHT * 64 + from];
			Material += KNIGHT_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = whiteBishops;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = W_BISHOP;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_BISHOP * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_BISHOP * 64 + from];
			Material += BISHOP_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = whiteRooks;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = W_ROOK;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_ROOK * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_ROOK * 64 + from];
			Material += ROOK_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}

		tempPiece = blackKnights;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = B_KNIGHT;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_KNIGHT * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_KNIGHT * 64 + from];
			Material -= KNIGHT_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = blackBishops;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = B_BISHOP;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_BISHOP * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_BISHOP * 64 + from];
			Material -= BISHOP_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = blackRooks;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = B_ROOK;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_ROOK * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_ROOK * 64 + from];
			Material -= ROOK_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}

	}

	public void setPosition(Position position) {

		whiteKnights = position.whiteKnights;
		whiteBishops = position.whiteBishops;
		whiteRooks = position.whiteRooks;
		blackKnights = position.blackKnights;
		blackBishops = position.blackBishops;
		blackRooks = position.blackRooks;

		nextMove = position.nextMove; // white or black Move

		endOfSearch = 0;

		setRedundantBittboardsAndStuff();

	}

	@Override
	public int compare(Move m1, Move m2) {

		makeMove(m1);
		compare1 = transTable.getEvaluation(currentHash, currentHash2, sortingDepth).evaluation;
		unmakeMove(m1);

		makeMove(m2);
		compare2 = transTable.getEvaluation(currentHash, currentHash2, sortingDepth).evaluation;
		unmakeMove(m2);

		if (compare1 == TranspositionTable.NOENTRY && compare2 == TranspositionTable.NOENTRY) {
			return 0;

		} else {
			compare1 *= (-1);
			compare2 *= (-1);

			return compare2 - compare1;

		}

	}

	@Override
	public void setPositionFromFen(String fenString) {

		super.setPositionFromFen(fenString);

		setRedundantBittboardsAndStuff();

	}

	public long getHash() {
		return currentHash;
	}

	public long getHash2() {
		return currentHash2;
	}

	private void addKiller(Move move, int ply, int radicalChange) {
		killers[ply][1].setMoveInt(killers[ply][0].getMove().getMoveInt());
		killers[ply][1].setRadicalChange(killers[ply][0].getRadicalChange());
		killers[ply][0].setMoveInt(move.getMoveInt());
		killers[ply][0].setRadicalChange(radicalChange);
	}

	public void printBoard() {

		int j = 1;
		int sq;
		int aFile, aRank;

		while (j <= 64) {

			aFile = 1 + ((j - 1) % 8);
			aRank = 8 - ((j - 1) / 8);
			sq = (aRank - 1) * 8 + aFile - 1;

			if (sq % 8 == 0) {
				System.out.println();
			}

			switch (square[sq]) {

			case EMPTY:
				System.out.print("  ");
				break;
			case W_KNIGHT:
				System.out.print("N ");
				break;
			case B_KNIGHT:
				System.out.print("n ");
				break;
			case W_BISHOP:
				System.out.print("B ");
				break;
			case B_BISHOP:
				System.out.print("b ");
				break;
			case W_ROOK:
				System.out.print("R ");
				break;
			case B_ROOK:
				System.out.print("r ");
				break;

			default:
				;

			}

			j++;

		}

		System.out.println();

	}

	private boolean isPseudoLegalMove(Move move) {

		if (isCheckmate())
			return false;
		from = move.getFrom();
		piece = move.getPiec();
		to = move.getTosq();
		// captured = move.getCapt();

		// quick check first!!
		if (square[from] != piece) {
			return false;
		}

		if (nextMove == 1) {

			switch (piece) {

			case W_KNIGHT:

				return true;

			case W_BISHOP:
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & Bitboards.BITSET[to];

				return (tempMove != 0);

			case W_ROOK:
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
						& Bitboards.BITSET[to];
				return (tempMove != 0);

			default:
				return false;

			}

		} else {
			switch (piece) {

			case B_KNIGHT:
				return true;
			case B_BISHOP:
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & Bitboards.BITSET[to];

				return (tempMove != 0);

			case B_ROOK:
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
						& Bitboards.BITSET[to];
				return (tempMove != 0);

			default:
				return false;

			}

		}

	}

	private boolean positionRepeated() {
		return false;

		/*
		 * int fiftyMoveCopy = fiftyMove; fiftyMoveCopy = fiftyMoveCopy - 2;
		 * 
		 * // The check for i >=0 is necessary, because we might not have gotten // the
		 * initial position from // the starting position plus a few moves
		 * 
		 * for (int i = endOfSearch - 2; fiftyMoveCopy >= 0 && i >= 0; i = i - 2,
		 * fiftyMoveCopy = fiftyMoveCopy - 2) { if (gameLine[i].getHash() ==
		 * currentHash) { return true; }
		 * 
		 * }
		 * 
		 * return false;
		 */
	}

	@Override
	public void setPosition(List<Move> moves) {

		reset();

		if (moves == null)
			return;
		for (Move m : moves) {
			makeMove(m);
		}

	}

	public void reset() {

		super.reset();

		initialiseHash();

	}

	@Override
	public void quit() {
		// This sould release all the memory!!!
		// This does the garbage collector

	}

}
