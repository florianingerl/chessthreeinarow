package Model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import DeepLearning.NeuralNet;
import DeepLearning.PgnGameAnalyzer;

public class BasicEngine extends Position implements IEngine, Comparator<Move> {

	private static Logger logger = Logger.getLogger(BasicEngine.class);

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
	public static int KING_VALUE = 1000000;

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

	private NeuralNet net = new NeuralNet(68, 60, 1) {
		{

			try {
				InputStream in = new FileInputStream("NeuralNet");
				load(in);
				in.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(1);
			}
		}
	};

	public BasicEngine() {

		super();

		int i, j;

		for (i = 0; i < MAX_MOVE_BUFFER; i++) {
			currentSearch[i] = new Move();

		}

		move.clear();
		move.setPiec(W_PAWN);
		move.setFrom(55);
		move.setCapt(EMPTY);
		move.setTosq(63);
		move.setProm(W_QUEEN);
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

		tempPiece = whitePawns;

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_PAWN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_PAWN * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
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
		tempPiece = whiteQueens;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_QUEEN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_QUEEN * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = blackPawns;

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_PAWN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_PAWN * 64 + from];
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
		tempPiece = blackQueens;

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_QUEEN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_QUEEN * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}

		tempPiece = blackKing;

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_KING * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_KING * 64 + from];
			tempPiece ^= Bitboards.BITSET[from];
		}

	}

	// the best move so far should be played or not
	@Override
	public void findBestMove() {

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
				if (isOtherKingAttacked()) {
					unmakeMove(currentSearch[i]);
					continue;
				}

				currentNode = currentNode.rightChild;
				currentNode.value.setMoveInt(currentSearch[i].getMoveInt());

				/*
				 * valueEvalNode = transTable.getEvaluation(currentHash,
				 * currentHash2, currentDepth); value =
				 * valueEvalNode.evaluation;
				 * 
				 * If there is no entry or NodeType is a cutNode, that means the
				 * position might be worse, d.h if its a cutNode and not worse
				 * than best
				 * 
				 * Normally also if(valueNode.nodeType == EvalNode.ALLNODE &&
				 * value < beta), because if value >= beta, it might be even
				 * better but it's already a cut if (value ==
				 * TranspositionTable.NOENTRY ||
				 * (valueEvalNode.nodeType==EvalNode.CUTNODE && value > best) ||
				 * valueEvalNode.nodeType == EvalNode.ALLNODE) {
				 */

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

				if (value > best) {
					if (value >= beta) {
						logger.fatal("Value >=beta at ply 0");
						System.exit(1);
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

			if (best == -BasicEngine.KING_VALUE) {
				nextMove *= (-1);
				if (isOtherKingAttacked()) {
					// stateOfSearch = BasicEngine.CHECKMATE;
				} else {
					// stateOfSearch = BasicEngine.STALEMATE;
				}
				nextMove *= (-1);
				break outer;
			} else if (value == BasicEngine.KING_VALUE - 1) {
				// stateOfSearch = BasicEngine.COMPUTER_CHECKMATE;
				break outer;
			} else {
			}

		}

	}// end do in Background

	private int pvAlphaBeta(int ply, int depth, int alpha, int beta) {

		++numberOfPvAlphaBetas;

		// at the beginning, we are assuming no move exceeds alpha
		// lastNodeType needs to be a local variable here!!!
		int nodeType = EvalNode.ALLNODE;

		if (depth == 0) {
			// Nothing need to be set here, quiecence set lastNodeType correctly
			return quiescence(ply, alpha, beta);

		}

		int value;

		int best = -BasicEngine.KING_VALUE;
		boolean pvfound = false;

		moveBufLen[ply + 1] = moveBufLen[ply];

		for (int i = 0; i < 2; i++) {

			// What node type is this???
			if (interrupted && (bestMove != null)) {
				return alpha + 1;
			}

			if (isPseudoLegalMove(killers[ply][i].getMove())) {

				makeMove(killers[ply][i].getMove());
				if (isOtherKingAttacked()) {
					unmakeMove(killers[ply][i].getMove());
					continue;
				}

				currentNode = currentNode.rightChild;

				currentNode.value.setMoveInt(killers[ply][i].getMove().getMoveInt());

				if (!positionRepeated()) {

					/*
					 * valueEvalNode = transTable.getEvaluation(currentHash,
					 * currentHash2, depth); value = valueEvalNode.evaluation;
					 * 
					 * if (value == TranspositionTable.NOENTRY ||
					 * (valueEvalNode.nodeType == EvalNode.CUTNODE && value >
					 * beta) || (valueEvalNode.nodeType== EvalNode.ALLNODE &&
					 * value < beta)) {
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

				} // End if position is not repeated
				else {
					// If position is repeated, then evaluation is 0
					value = 0;
				}

				unmakeMove(killers[ply][i].getMove());

				if (value > best) {
					if (value >= beta) {
						++numberOfBetaCutoffs;

						lastNodeType = EvalNode.CUTNODE;
						currentNode = currentNode.father;
						return value;
					}
					best = value;
					if (value > alpha) {
						nodeType = EvalNode.PVNODE;
						alpha = value;
						pvfound = true;

						currentNode.setPrincipalVariation();

					} // end if value > alpha
				} // end if value > best

				currentNode = currentNode.father;
			} // end if this killer is a pseudoLegalMove

		} // end iterating through killers

		if (interrupted && (bestMove != null)) {
			return alpha + 1;
		}

		moveBufLen[ply + 1] = pseudoLegalMoveGenerator(moveBufLen[ply]);
		sortingDepth = depth - 1;

		Arrays.sort(currentSearch, moveBufLen[ply], moveBufLen[ply + 1], this);

		for (int i = moveBufLen[ply]; i < moveBufLen[ply + 1]; i++) {

			makeMove(currentSearch[i]);
			if (isOtherKingAttacked()) {
				unmakeMove(currentSearch[i]);
				continue;
			}

			currentNode = currentNode.rightChild;
			currentNode.value.setMoveInt(currentSearch[i].getMoveInt());

			if (!positionRepeated()) {
				/*
				 * valueEvalNode = transTable.getEvaluation(currentHash,
				 * currentHash2, depth); value = valueEvalNode.evaluation;
				 * 
				 * if (value == TranspositionTable.NOENTRY ||
				 * (valueEvalNode.nodeType == EvalNode.CUTNODE && value > beta)
				 * || (valueEvalNode.nodeType == EvalNode.ALLNODE && value <
				 * beta)) {
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
				if (value >= beta) {
					++numberOfBetaCutoffs;

					lastNodeType = EvalNode.CUTNODE;
					currentNode = currentNode.father;
					addKiller(currentSearch[i], ply, value - Material());
					return value;
				}
				best = value;
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

		if (best == -BasicEngine.KING_VALUE) {
			nextMove *= (-1);
			if (isOtherKingAttacked()) {
				best = best + ply;
			} else {
				best = 0;
			}
			nextMove *= (-1);
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
			return quiescence(ply, alpha, beta);

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
				if (isOtherKingAttacked()) {
					unmakeMove(killers[ply][i].getMove());
					continue;
				}

				if (!positionRepeated()) {
					/*
					 * valueEvalNode = transTable.getEvaluation(currentHash,
					 * currentHash2, depth); value = valueEvalNode.evaluation;
					 * 
					 * if (value == TranspositionTable.NOENTRY ||
					 * (valueEvalNode.nodeType == EvalNode.CUTNODE && value >
					 * best) || (valueEvalNode.nodeType == EvalNode.ALLNODE &&
					 * value < beta)) {
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
			if (isOtherKingAttacked()) {
				unmakeMove(currentSearch[i]);
				continue;
			}

			if (!positionRepeated()) {
				/*
				 * valueEvalNode = transTable.getEvaluation(currentHash,
				 * currentHash2, depth); value = valueEvalNode.evaluation; if
				 * (value == TranspositionTable.NOENTRY ||
				 * (valueEvalNode.nodeType == EvalNode.CUTNODE && value > best)
				 * || (valueEvalNode.nodeType == EvalNode.ALLNODE && value <
				 * beta)) {
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
			nextMove *= (-1);
			if (isOtherKingAttacked()) {
				best = best + ply;
			} else {
				best = 0;
			}
			nextMove *= (-1);
		}

		lastNodeType = nodeType;
		return best;

	}

	private int quiescence(int ply, int alpha, int beta) {

		++numberOfQuiescences;

		if (ply < 8 && isCheck()) {
			// Nothing needs to be set for lastNodeType, because
			// pvAlphaBetaWithoutPv does it
			return pvAlphaBetaWithoutPv(ply, 1, alpha, beta);
		}

		int value;

		int stand_pat = staticEvaluation();

		if (stand_pat >= beta) {
			++numberOfBetaCutoffs;
			lastNodeType = EvalNode.CUTNODE;
			// Koennte man auch stand_pat returnen
			// Return stand_pat is not tested
			return stand_pat;
		}

		int nodeType = EvalNode.ALLNODE;

		if (stand_pat > alpha) {
			alpha = stand_pat;
		}

		if (interrupted && bestMove != null) {
			return alpha + 1;
		}

		moveBufLen[ply + 1] = pseudoLegalCaptureGenerator(moveBufLen[ply]);
		for (int i = moveBufLen[ply]; i < moveBufLen[ply + 1]; i++) {
			makeMove(currentSearch[i]);
			if (isOtherKingAttacked()) {
				unmakeMove(currentSearch[i]);
				continue;
			}

			// Here, the position has not repeated
			/*
			 * valueEvalNode = transTable.getEvaluation(currentHash,
			 * currentHash2, 0); value = valueEvalNode.evaluation;
			 * 
			 * if (value == TranspositionTable.NOENTRY ||
			 * (valueEvalNode.nodeType == EvalNode.CUTNODE && value > alpha) ||
			 * (valueEvalNode.nodeType == EvalNode.ALLNODE && value < beta)) {
			 */
			value = -quiescence(ply + 1, -beta, -alpha);
			transTable.put(currentHash, currentHash2, value, 0, lastNodeType);

			unmakeMove(currentSearch[i]);

			// Why don't make a beta cutoff here

			if (value > alpha) {
				alpha = value;
				nodeType = EvalNode.PVNODE;

			}

		}
		if (ply < 8) {
			if (interrupted && bestMove != null) {
				return alpha + 1;
			}

			int old = moveBufLen[ply + 1];
			moveBufLen[ply + 1] = pseudoLegalCheckNotHeavyCaptureGenerator(old);
			for (int i = old; i < moveBufLen[ply + 1]; i++) {
				makeMove(currentSearch[i]);
				if (isOtherKingAttacked()) {
					unmakeMove(currentSearch[i]);
					continue;
				}

				if (!positionRepeated()) {
					/*
					 * valueEvalNode = transTable.getEvaluation(currentHash,
					 * currentHash2, 0); value = valueEvalNode.evaluation; if
					 * (value == TranspositionTable.NOENTRY ||
					 * (valueEvalNode.nodeType == EvalNode.CUTNODE && value >
					 * alpha) || (valueEvalNode.nodeType == EvalNode.ALLNODE &&
					 * value < beta)) {
					 */
					value = -pvAlphaBetaWithoutPv(ply + 1, 1, -beta, -alpha);
					transTable.put(currentHash, occupiedSquares, value, 0, lastNodeType);

				} else {
					value = 0;
				}
				unmakeMove(currentSearch[i]);

				if (value > alpha) {
					alpha = value;
					nodeType = EvalNode.PVNODE;
				}
			}

		}

		lastNodeType = nodeType;
		return alpha;

	}

	private int staticEvaluation() {
		//return (int)(net.computeOutputVector( PgnGameAnalyzer.getInputVectorForNeuralNet(this ) ).getEntry(0, 0) * 100);
		return Material() + bonusPawns() + bonusKnights() + kingSafetyBonus() + rookBonus();
	}

	@Override
	public void stop() {

		interrupted = true;
	}

	private int bonusPawns() {

		return 10 * nextMove * (Long.bitCount(blackPawns & (blackPawns >>> 8) & (blackPawns >>> 16))
				- Long.bitCount(whitePawns & (whitePawns << 8) & (whitePawns << 16)));

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

	private int kingSafetyBonus() {

		if (whiteQueens != 0 || blackQueens != 0) {
			int whiteKingSafety = 0;
			if ((Bitboards.BEST_WHITE_KING & whiteKing) != 0) {
				whiteKingSafety += 25;
			}
			int blackKingSafety = 0;
			if ((Bitboards.BEST_BLACK_KING & blackKing) != 0) {
				blackKingSafety += 25;
			}

			return nextMove * (whiteKingSafety - blackKingSafety);
		} else {
			return 0;
		}
	}

	private int rookBonus() {

		int whiteRookBonus = 0;
		tempPiece = whiteRooks;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			if ((Bitboards.FILE[from] & whitePawns) == 0) {
				whiteRookBonus += 15;
			}
			// bonus for seventh rank!!
			if (from >= 48) {
				whiteRookBonus += 7;
			}
			tempPiece ^= Bitboards.BITSET[from];
		}
		int blackRookBonus = 0;
		tempPiece = blackRooks;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			if ((Bitboards.FILE[from] & blackPawns) == 0) {
				blackRookBonus += 15;
			}
			// bonus for seventh rank!!!
			if (from <= 16) {
				blackRookBonus += 7;
			}
			tempPiece ^= Bitboards.BITSET[from];
		}

		return nextMove * (whiteRookBonus - blackRookBonus);

	}

	private int beweglichkeit() {

		int beweglichkeit = 0;

		long targetBitboard, freeSquares;
		long tempPiece, tempMove;
		freeSquares = ~occupiedSquares;
		int from;
		byte fBitstate6;
		byte rBitstate6;
		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;

		targetBitboard = ~whitePieces;

		tempMove = (getWhitePawns() << 8) & freeSquares;

		beweglichkeit += Long.bitCount(tempMove);

		tempMove = ((((getWhitePawns() & 65280L) << 8) & freeSquares) << 8) & freeSquares;

		beweglichkeit += Long.bitCount(tempMove);

		tempMove = ((getWhitePawns() & 9187201950435737471L) << 9) & blackPieces;

		beweglichkeit += Long.bitCount(tempMove);

		tempMove = ((getWhitePawns() & (-72340172838076674L)) << 7) & blackPieces;

		beweglichkeit += Long.bitCount(tempMove);

		tempPiece = getWhiteKnights();

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
			beweglichkeit += Long.bitCount(tempMove);
			tempPiece ^= Bitboards.BITSET[from];

		}

		tempPiece = getWhiteKing();
		if (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			tempMove = Bitboards.KING_ATTACKS[from] & targetBitboard;
			beweglichkeit += Long.bitCount(tempMove);

		}

		tempPiece = getWhiteRooks();

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);

			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

			tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
					& targetBitboard;

			beweglichkeit += Long.bitCount(tempMove);

			tempPiece ^= Bitboards.BITSET[from];
		}

		tempPiece = getWhiteQueens();

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[(int) from])
					* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[(int) from])
					* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
			tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
					| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
			beweglichkeit += Long.bitCount(tempMove);
			tempPiece ^= Bitboards.BITSET[from];

		}

		tempPiece = getWhiteBishops();

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[(int) from])
					* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[(int) from])
					* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
			tempMove = (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
			beweglichkeit += Long.bitCount(tempMove);
			tempPiece ^= Bitboards.BITSET[from];

		}

		targetBitboard = ~blackPieces;

		tempMove = (getBlackPawns() >>> 8) & freeSquares;

		beweglichkeit -= Long.bitCount(tempMove);

		tempMove = ((((getBlackPawns() & ((65280L) << (5 * 8))) >>> 8) & freeSquares) >>> 8) & freeSquares;

		beweglichkeit -= Long.bitCount(tempMove);

		tempMove = ((getBlackPawns() & 9187201950435737471L) >>> 7) & whitePieces;

		beweglichkeit -= Long.bitCount(tempMove);

		tempMove = ((getBlackPawns() & (-72340172838076674L)) >>> 9) & whitePieces;

		beweglichkeit -= Long.bitCount(tempMove);

		tempPiece = getBlackKnights();
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
			beweglichkeit -= Long.bitCount(tempMove);
			tempPiece ^= Bitboards.BITSET[from];

		}

		tempPiece = getBlackKing();
		if (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			tempMove = Bitboards.KING_ATTACKS[from] & targetBitboard;
			beweglichkeit -= Long.bitCount(tempMove);
		}

		tempPiece = getBlackRooks();

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);

			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

			tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
					& targetBitboard;

			beweglichkeit -= Long.bitCount(tempMove);

			tempPiece ^= Bitboards.BITSET[from];
		}

		tempPiece = getBlackQueens();

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

			tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
					| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
			beweglichkeit -= Long.bitCount(tempMove);
			tempPiece ^= Bitboards.BITSET[from];

		}

		tempPiece = getBlackBishops();

		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from] >>> 57);

			tempMove = (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;

			beweglichkeit -= Long.bitCount(tempMove);
			tempPiece ^= Bitboards.BITSET[from];

		}

		return nextMove * beweglichkeit;

	}

	@Override
	public boolean isCheck() {
		tempMove = 0L;

		if (nextMove == -1) {
			from = Long.numberOfTrailingZeros(blackKing);

			diaga8h1Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from]) >>> 57);

			diaga1h8Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from]) >>> 57);

			tempMove |= (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & (whiteQueens | whiteBishops);

			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

			tempMove |= (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
					& (whiteQueens | whiteRooks);

			tempMove |= Bitboards.KNIGHT_ATTACKS[from] & whiteKnights;

			tempMove |= Bitboards.KING_ATTACKS[from] & whiteKing;

			tempMove |= ((blackKing & 9187201950435737471L) >>> 7) & whitePawns;

			tempMove |= ((blackKing & (-72340172838076674L)) >>> 9) & whitePawns;

		}

		else {

			from = Long.numberOfTrailingZeros(whiteKing);

			diaga8h1Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from]) >>> 57);

			diaga1h8Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from]) >>> 57);

			tempMove |= (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & (blackQueens | blackBishops);

			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

			tempMove |= (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
					& (blackQueens | blackRooks);

			tempMove |= Bitboards.KNIGHT_ATTACKS[from] & blackKnights;

			tempMove |= Bitboards.KING_ATTACKS[from] & blackKing;

			tempMove |= ((whiteKing & 9187201950435737471L) << 9) & blackPawns;

			tempMove |= ((whiteKing & (-72340172838076674L)) << 7) & blackPawns;

		}

		if (tempMove != 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public boolean isOtherKingAttacked() {

		tempMove = 0L;

		if (nextMove == 1) {
			from = Long.numberOfTrailingZeros(blackKing);

			diaga8h1Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from]) >>> 57);

			diaga1h8Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from]) >>> 57);

			tempMove |= (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & (whiteQueens | whiteBishops);

			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

			tempMove |= (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
					& (whiteQueens | whiteRooks);

			tempMove |= Bitboards.KNIGHT_ATTACKS[from] & whiteKnights;

			tempMove |= Bitboards.KING_ATTACKS[from] & whiteKing;

			tempMove |= ((blackKing & 9187201950435737471L) >>> 7) & whitePawns;

			tempMove |= ((blackKing & (-72340172838076674L)) >>> 9) & whitePawns;

		}

		else {

			from = Long.numberOfTrailingZeros(whiteKing);

			diaga8h1Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from]) >>> 57);

			diaga1h8Bitstate6 = (byte) (((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from]) >>> 57);

			tempMove |= (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & (blackQueens | blackBishops);

			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

			tempMove |= (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
					& (blackQueens | blackRooks);

			tempMove |= Bitboards.KNIGHT_ATTACKS[from] & blackKnights;

			tempMove |= Bitboards.KING_ATTACKS[from] & blackKing;

			tempMove |= ((whiteKing & 9187201950435737471L) << 9) & blackPawns;

			tempMove |= ((whiteKing & (-72340172838076674L)) << 7) & blackPawns;

		}

		if (tempMove != 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void makeBlackPromotion(int prom, int to)

	{

		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_PAWN * 64 + to];
		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[prom * 64 + to];

		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_PAWN * 64 + to];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[prom * 64 + to];

		toBitMap = Bitboards.BITSET[to];

		setBlackPawns(getBlackPawns() ^ toBitMap);

		Material += PAWN_VALUE;

		switch (prom) {
		case B_QUEEN:
			setBlackQueens(getBlackQueens() ^ toBitMap);

			Material -= QUEEN_VALUE;
			break;
		case B_ROOK:
			setBlackRooks(getBlackRooks() ^ toBitMap);

			Material -= ROOK_VALUE;
			break;
		case B_BISHOP:
			setBlackBishops(getBlackBishops() ^ toBitMap);

			Material -= BISHOP_VALUE;
			break;
		case B_KNIGHT:
			setBlackKnights(getBlackKnights() ^ toBitMap);

			Material -= KNIGHT_VALUE;
			break;
		default:
		}

	}

	public void makeCapture(int captured, int to)

	{

		// deals with all captures, except en-passant
		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[captured * 64 + to];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[captured * 64 + to];

		toBitMap = Bitboards.BITSET[to];

		switch (captured)

		{

		case W_PAWN: // white pawn:

			setWhitePawns(getWhitePawns() ^ toBitMap);

			whitePieces ^= toBitMap;

			Material -= BasicEngine.PAWN_VALUE;

			break;

		case W_KING: // white king:

			logger.fatal("Move attempted a white king capture! Captured: " + captured + " Tosq: " + to);

			setWhiteKing(getWhiteKing() ^ toBitMap);

			whitePieces ^= toBitMap;

			Material -= BasicEngine.KING_VALUE;

			break;

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ toBitMap);

			whitePieces ^= toBitMap;

			Material -= BasicEngine.KNIGHT_VALUE;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ toBitMap);

			whitePieces ^= toBitMap;

			Material -= BasicEngine.BISHOP_VALUE;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ toBitMap);

			whitePieces ^= toBitMap;

			Material -= BasicEngine.ROOK_VALUE;

			if (to == 0) {
				if ((castleWhite & CANCASTLEOOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_WHITE];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_WHITE];
				}

				castleWhite &= ~CANCASTLEOOO;
			}
			if (to == 7) {
				if ((castleWhite & CANCASTLEOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_WHITE];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_WHITE];
				}

				castleWhite &= ~CANCASTLEOO;
			}
			break;

		case W_QUEEN: // white queen:

			setWhiteQueens(getWhiteQueens() ^ toBitMap);

			whitePieces ^= toBitMap;

			Material -= BasicEngine.QUEEN_VALUE;

			break;

		case B_PAWN: // black pawn:

			setBlackPawns(getBlackPawns() ^ toBitMap);

			blackPieces ^= toBitMap;

			Material += BasicEngine.PAWN_VALUE;

			break;

		case B_KING: // black king:

			logger.fatal("Move attempted a black king capture! Captured: " + captured + " Tosq: " + to);

			setBlackKing(getBlackKing() ^ toBitMap);

			blackPieces ^= toBitMap;

			Material += BasicEngine.KING_VALUE;

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ toBitMap);

			blackPieces ^= toBitMap;

			Material += BasicEngine.KNIGHT_VALUE;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ toBitMap);

			blackPieces ^= toBitMap;

			Material += BasicEngine.BISHOP_VALUE;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ toBitMap);

			blackPieces ^= toBitMap;

			Material += BasicEngine.ROOK_VALUE;

			if (to == 56) {
				if ((castleBlack & CANCASTLEOOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_BLACK];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_BLACK];
				}

				castleBlack &= ~CANCASTLEOOO;
			}
			if (to == 63) {
				if ((castleBlack & CANCASTLEOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_BLACK];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_BLACK];
				}

				castleBlack &= ~CANCASTLEOO;
			}
			break;

		case B_QUEEN: // black queen:

			setBlackQueens(getBlackQueens() ^ toBitMap);

			blackPieces ^= toBitMap;

			Material += BasicEngine.QUEEN_VALUE;

			break;

		}

		fiftyMove = 0;

	}

	public void makeMove(Move move)

	{

		gameLine[endOfSearch].getMove().setMoveInt(move.getMoveInt());

		gameLine[endOfSearch].setCastleWhite(castleWhite);

		gameLine[endOfSearch].setCastleBlack(castleBlack);

		gameLine[endOfSearch].setFiftyMove(fiftyMove);

		gameLine[endOfSearch].setEpSquare(epSquare);

		gameLine[endOfSearch].setHash(currentHash);

		gameLine[endOfSearch].setHash2(currentHash2);

		endOfSearch++;

		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_BLACK_TO_MOVE];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_BLACK_TO_MOVE];

		if (epSquare != 0) {
			if (nextMove == 1) {
				currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_EP_SQUARE + epSquare - 40];
				currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_EP_SQUARE + epSquare - 40];
			} else {
				currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_EP_SQUARE + epSquare - 16];
				currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_EP_SQUARE + epSquare - 16];
			}

		}

		from = move.getFrom();
		to = move.getTosq();
		piece = move.getPiec();
		captured = move.getCapt();

		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[piece * 64 + from];
		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[piece * 64 + to];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[piece * 64 + from];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[piece * 64 + to];

		fromBitMap = Bitboards.BITSET[from];
		fromToBitMap = fromBitMap | Bitboards.BITSET[to];

		switch (piece)

		{

		case W_PAWN: // white pawn:

			setWhitePawns(getWhitePawns() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_PAWN;

			epSquare = 0;

			fiftyMove = 0;

			if (Bitboards.RANKS[from] == 2) {

				if (Bitboards.RANKS[to] == 4) {
					epSquare = from + 8;
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_EP_SQUARE + from - 8];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_EP_SQUARE + from - 8];
				}
			}
			if (captured != 0)

			{

				if (move.isEnpassant())

				{

					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_PAWN * 64 + (to - 8)];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_PAWN * 64 + (to - 8)];

					setBlackPawns(getBlackPawns() ^ Bitboards.BITSET[to - 8]);

					blackPieces ^= Bitboards.BITSET[to - 8];

					occupiedSquares ^= fromToBitMap | Bitboards.BITSET[to - 8];

					square[to - 8] = EMPTY;

					Material += PAWN_VALUE;

				}

				else

				{

					makeCapture(captured, to);

					occupiedSquares ^= fromBitMap;

				}

			}

			else {
				occupiedSquares ^= fromToBitMap;
			}

			if (move.isPromotion())

			{

				makeWhitePromotion(move.getProm(), to);

				square[to] = move.getProm();

			}

			break;

		case W_KING: // white king:

			setWhiteKing(getWhiteKing() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_KING;

			epSquare = 0;

			fiftyMove++;

			if ((castleWhite & Position.CANCASTLEOO) != 0) {
				currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_WHITE];
				currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_WHITE];
			}
			if ((castleWhite & Position.CANCASTLEOOO) != 0) {
				currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_WHITE];
				currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_WHITE];
			}
			castleWhite = 0;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			if (move.isCastle())

			{

				if (move.isCastleOO())

				{

					setWhiteRooks(getWhiteRooks() ^ (Bitboards.BITSET[7] | Bitboards.BITSET[5]));

					whitePieces ^= Bitboards.BITSET[7] | Bitboards.BITSET[5];

					occupiedSquares ^= Bitboards.BITSET[7] | Bitboards.BITSET[5];

					square[7] = EMPTY;

					square[5] = W_ROOK;

				}

				else

				{

					setWhiteRooks(getWhiteRooks() ^ (Bitboards.BITSET[0] | Bitboards.BITSET[3]));

					whitePieces ^= Bitboards.BITSET[0] | Bitboards.BITSET[3];

					occupiedSquares ^= Bitboards.BITSET[0] | Bitboards.BITSET[3];

					square[0] = EMPTY;

					square[3] = W_ROOK;

				}

			}

			break;

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_KNIGHT;

			epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_BISHOP;

			epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_ROOK;

			epSquare = 0;

			fiftyMove++;

			if (from == 0) {
				if ((castleWhite & CANCASTLEOOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_WHITE];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_WHITE];
				}
				castleWhite &= ~CANCASTLEOOO;

			}

			if (from == 7) {
				if ((castleWhite & CANCASTLEOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_WHITE];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_WHITE];
				}

				castleWhite &= ~CANCASTLEOO;

			}

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case W_QUEEN: // white queen:

			setWhiteQueens(getWhiteQueens() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_QUEEN;

			epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_PAWN: // black pawn:

			setBlackPawns(getBlackPawns() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_PAWN;

			epSquare = 0;

			fiftyMove = 0;

			if (Bitboards.RANKS[from] == 7) {

				if (Bitboards.RANKS[to] == 5) {
					epSquare = from - 8;
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_EP_SQUARE + from - 48];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_EP_SQUARE + from - 48];
				}

			}
			if (captured != 0)

			{

				if (move.isEnpassant())

				{

					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_PAWN * 64 + to + 8];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_PAWN * 64 + to + 8];
					setWhitePawns(getWhitePawns() ^ Bitboards.BITSET[to + 8]);

					whitePieces ^= Bitboards.BITSET[to + 8];

					occupiedSquares ^= fromToBitMap | Bitboards.BITSET[to + 8];

					square[to + 8] = EMPTY;

					Material -= PAWN_VALUE;

				}

				else

				{

					makeCapture(captured, to);

					occupiedSquares ^= fromBitMap;

				}

			}

			else
				occupiedSquares ^= fromToBitMap;

			if (move.isPromotion())

			{

				makeBlackPromotion((int) move.getProm(), to);

				square[to] = move.getProm();

			}

			break;

		case B_KING: // black king:

			setBlackKing(getBlackKing() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_KING;

			epSquare = 0;

			fiftyMove++;

			if ((castleBlack & Position.CANCASTLEOO) != 0) {
				currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_BLACK];
				currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_BLACK];
			}
			if ((castleBlack & Position.CANCASTLEOOO) != 0) {
				currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_BLACK];
				currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_BLACK];
			}

			castleBlack = 0;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			if (move.isCastle())

			{

				if (move.isCastleOO())

				{

					setBlackRooks(getBlackRooks() ^ (Bitboards.BITSET[63] | Bitboards.BITSET[61]));

					blackPieces ^= Bitboards.BITSET[63] | Bitboards.BITSET[61];

					occupiedSquares ^= Bitboards.BITSET[63] | Bitboards.BITSET[61];

					square[63] = EMPTY;

					square[61] = B_ROOK;

				}

				else

				{

					setBlackRooks(getBlackRooks() ^ (Bitboards.BITSET[56] | Bitboards.BITSET[59]));

					blackPieces ^= Bitboards.BITSET[56] | Bitboards.BITSET[59];

					occupiedSquares ^= Bitboards.BITSET[56] | Bitboards.BITSET[59];

					square[56] = EMPTY;

					square[59] = B_ROOK;

				}

			}

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_KNIGHT;

			epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_BISHOP;

			epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_ROOK;

			epSquare = 0;

			fiftyMove++;

			if (from == 56) {
				if ((castleBlack & CANCASTLEOOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_BLACK];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_BLACK];
				}

				castleBlack &= ~CANCASTLEOOO;
			}
			if (from == 63) {
				if ((castleBlack & CANCASTLEOO) != 0) {
					currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_BLACK];
					currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_BLACK];
				}

				castleBlack &= ~CANCASTLEOO;
			}
			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_QUEEN: // black queen:

			setBlackQueens(getBlackQueens() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_QUEEN;

			epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		}

		nextMove *= (-1);

	}

	@Override
	protected void makeWhitePromotion(int prom, int to)

	{

		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_PAWN * 64 + to];
		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[prom * 64 + to];

		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_PAWN * 64 + to];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[prom * 64 + to];

		toBitMap = Bitboards.BITSET[to];

		setWhitePawns(getWhitePawns() ^ toBitMap);

		Material -= PAWN_VALUE;

		switch (prom) {
		case W_QUEEN:
			setWhiteQueens(getWhiteQueens() ^ toBitMap);

			Material += QUEEN_VALUE;
			break;
		case W_ROOK:
			setWhiteRooks(getWhiteRooks() ^ toBitMap);

			Material += ROOK_VALUE;
			break;
		case W_BISHOP:
			setWhiteBishops(getWhiteBishops() ^ toBitMap);

			Material += BISHOP_VALUE;
			break;
		case W_KNIGHT:
			setWhiteKnights(getWhiteKnights() ^ toBitMap);

			Material += KNIGHT_VALUE;
			break;
		default:
		}

	}

	public int Material() {
		return nextMove * Material;
	}

	private int pseudoLegalCaptureGenerator(int index) {

		freeSquares = ~occupiedSquares;
		move.clear();

		if (nextMove == 1) {

			targetBitboard = blackPieces & (~blackPawns);

			move.setPiec(W_PAWN);

			tempMove = ((whitePawns & 71776119061217280L) << 8) & freeSquares;

			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 8);
				move.setTosq(to);
				move.setProm(W_QUEEN);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(W_ROOK);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(W_BISHOP);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(W_KNIGHT);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(EMPTY);

				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((whitePawns & 9187201950435737471L) << 9);
			// Also note that we can not capture pawns on the last rank
			tempMove &= targetBitboard;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 9);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 8) {
					move.setProm(W_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];
			}

			tempMove = ((whitePawns & (-72340172838076674L)) << 7);

			tempMove &= targetBitboard;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 7);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 8) {
					move.setProm(W_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			move.setPiec(W_KNIGHT);

			tempPiece = whiteKnights;
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(W_KING);

			tempPiece = whiteKing;
			if (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KING_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}

			}

			move.setPiec(W_ROOK);
			tempPiece = whiteRooks;

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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];

				}

				tempPiece ^= Bitboards.BITSET[from];
			}

			move.setPiec(W_QUEEN);
			tempPiece = whiteQueens;

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[(int) from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[(int) from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
						| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(W_BISHOP);
			tempPiece = whiteBishops;

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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		} else {

			targetBitboard = whitePieces & (~whitePawns);
			move.setPiec(B_PAWN);

			tempMove = ((blackPawns & 65280L) >>> 8) & freeSquares;

			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 8);
				move.setTosq(to);
				move.setProm(B_QUEEN);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(B_ROOK);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(B_BISHOP);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(B_KNIGHT);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				move.setProm(EMPTY);

				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((blackPawns & 9187201950435737471L) >>> 7);
			tempMove &= targetBitboard;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 7);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 1) {
					move.setProm(B_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());

					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];
			}

			tempMove = ((getBlackPawns() & (-72340172838076674L)) >>> 9);
			tempMove &= targetBitboard;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 9);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 1) {
					move.setProm(B_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());

					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			move.setPiec(B_KNIGHT);

			tempPiece = blackKnights;
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(B_KING);
			tempPiece = blackKing;
			if (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KING_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}

			}

			move.setPiec(B_ROOK);
			tempPiece = blackRooks;

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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];

				}

				tempPiece ^= Bitboards.BITSET[from];
			}

			move.setPiec(B_QUEEN);
			tempPiece = blackQueens;

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
						| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(B_BISHOP);
			tempPiece = blackBishops;

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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		}

		return index;

	}

	private int pseudoLegalMoveGenerator(int index) {

		move.clear();
		freeSquares = ~occupiedSquares;

		if (nextMove == 1) {

			targetBitboard = ~whitePieces;

			move.setPiec(W_PAWN);

			tempMove = (getWhitePawns() << 8) & freeSquares;

			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 8);
				move.setTosq(to);
				if (Bitboards.RANKS[to] == 8) {
					move.setProm(W_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index].setMoveInt(move.getMoveInt());
					index++;
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((((getWhitePawns() & 65280L) << 8) & freeSquares) << 8) & freeSquares;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 16);
				move.setTosq(to);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((getWhitePawns() & 9187201950435737471L) << 9);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare - 9);
					move.setTosq(epSquare);
					move.setCapt(B_PAWN);
					move.setProm(W_PAWN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				}
			}
			tempMove &= blackPieces;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 9);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 8) {
					move.setProm(W_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];
			}

			tempMove = ((getWhitePawns() & (-72340172838076674L)) << 7);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare - 7);
					move.setTosq(epSquare);
					move.setCapt(B_PAWN);
					move.setProm(W_PAWN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				}
			}
			tempMove &= blackPieces;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 7);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 8) {
					move.setProm(W_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(W_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			move.setPiec(W_KNIGHT);

			tempPiece = whiteKnights;
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(W_KING);

			tempPiece = getWhiteKing();
			if (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KING_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}

				if (((castleWhite & CANCASTLEOO) != 0) && ((Bitboards.maskFG[0] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskEG[0])) {
					currentSearch[index++].setMoveInt(Bitboards.WHITE_OO_CASTL.getMoveInt()); // predefined
				}

				// White 0-0-0 Castling:

				if (((castleWhite & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[0] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskCE[0])) {
					currentSearch[index++].setMoveInt(Bitboards.WHITE_OOO_CASTL.getMoveInt()); // predefined

				}
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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];

				}

				tempPiece ^= Bitboards.BITSET[from];
			}

			move.setPiec(W_QUEEN);
			tempPiece = getWhiteQueens();

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[(int) from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[(int) from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
						| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		} else {
			targetBitboard = ~blackPieces;

			move.setPiec(B_PAWN);

			tempMove = (getBlackPawns() >>> 8) & freeSquares;

			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 8);
				move.setTosq(to);
				if (Bitboards.RANKS[to] == 1) {
					move.setProm(B_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index].setMoveInt(move.getMoveInt());
					index++;
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((((getBlackPawns() & ((65280L) << (5 * 8))) >>> 8) & freeSquares) >>> 8) & freeSquares;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 16);
				move.setTosq(to);
				currentSearch[index++].setMoveInt(move.getMoveInt());
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((getBlackPawns() & 9187201950435737471L) >>> 7);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare + 7);
					move.setTosq(epSquare);
					move.setCapt(W_PAWN);
					move.setProm(B_PAWN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				}
			}
			tempMove &= whitePieces;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 7);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 1) {
					move.setProm(B_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];
			}

			tempMove = ((getBlackPawns() & (-72340172838076674L)) >>> 9);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare + 9);
					move.setTosq(epSquare);
					move.setCapt(W_PAWN);
					move.setProm(B_PAWN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				}
			}
			tempMove &= whitePieces;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 9);
				move.setTosq(to);
				move.setCapt(square[to]);
				if (Bitboards.RANKS[to] == 1) {
					move.setProm(B_QUEEN);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_ROOK);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_BISHOP);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(B_KNIGHT);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					move.setProm(EMPTY);
				} else {
					currentSearch[index++].setMoveInt(move.getMoveInt());
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			move.setPiec(B_KNIGHT);

			tempPiece = blackKnights;
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

			move.setPiec(B_KING);
			tempPiece = getBlackKing();
			if (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KING_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}

				if (((castleBlack & CANCASTLEOO) != 0) && ((Bitboards.maskFG[1] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskEG[1])) {
					currentSearch[index++].setMoveInt(Bitboards.BLACK_OO_CASTL.getMoveInt()); // predefined
				}

				if (((castleBlack & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[1] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskCE[1])) {
					currentSearch[index++].setMoveInt(Bitboards.BLACK_OOO_CASTL.getMoveInt()); // predefined
				}

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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];

				}

				tempPiece ^= Bitboards.BITSET[from];
			}

			move.setPiec(B_QUEEN);
			tempPiece = getBlackQueens();

			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
						| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
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
					move.setCapt(square[to]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		}

		return index;

	}

	@Override
	protected void unmakeBlackPromotion(int prom, int to)

	{

		// Why has is missing here
		// currentHash ^= Data::ZOBRIST_HASH_RANDOMS[B_PAWN * 64 + to];
		// currentHash ^= Data::ZOBRIST_HASH_RANDOMS[prom * 64 + to];

		toBitMap = Bitboards.BITSET[to];

		setBlackPawns(getBlackPawns() ^ toBitMap);

		Material -= BasicEngine.PAWN_VALUE;

		if (prom == B_QUEEN)

		{

			setBlackQueens(getBlackQueens() ^ toBitMap);

			Material += BasicEngine.QUEEN_VALUE;

		} else if (prom == B_KNIGHT)

		{

			setBlackKnights(getBlackKnights() ^ toBitMap);

			Material += BasicEngine.KNIGHT_VALUE;

		}

		else if (prom == B_ROOK)

		{

			setBlackRooks(getBlackRooks() ^ toBitMap);

			Material += BasicEngine.ROOK_VALUE;

		}

		else if (prom == B_BISHOP)

		{

			setBlackBishops(getBlackBishops() ^ toBitMap);

			Material += BasicEngine.BISHOP_VALUE;

		}

	}

	@Override
	protected void unmakeCapture(int captured, int to)

	{

		toBitMap = Bitboards.BITSET[to];

		switch (captured)

		{

		case W_PAWN: // white pawn:

			setWhitePawns(getWhitePawns() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_PAWN;

			Material += BasicEngine.PAWN_VALUE;

			break;

		case W_KING: // white king:

			setWhiteKing(getWhiteKing() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_KING;

			Material += BasicEngine.KING_VALUE;

			break;

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_KNIGHT;

			Material += BasicEngine.KNIGHT_VALUE;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_BISHOP;

			Material += BasicEngine.BISHOP_VALUE;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_ROOK;

			Material += BasicEngine.ROOK_VALUE;

			break;

		case W_QUEEN: // white queen:

			setWhiteQueens(getWhiteQueens() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_QUEEN;

			Material += BasicEngine.QUEEN_VALUE;

			break;

		case B_PAWN: // black pawn:

			setBlackPawns(getBlackPawns() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_PAWN;

			Material -= BasicEngine.PAWN_VALUE;

			break;

		case B_KING: // black king:

			setBlackKing(getBlackKing() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_KING;

			Material -= BasicEngine.KING_VALUE;

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_KNIGHT;

			Material -= BasicEngine.KNIGHT_VALUE;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_BISHOP;

			Material -= BasicEngine.BISHOP_VALUE;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_ROOK;

			Material -= BasicEngine.ROOK_VALUE;

			break;

		case B_QUEEN: // black queen:

			setBlackQueens(getBlackQueens() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_QUEEN;

			Material -= BasicEngine.QUEEN_VALUE;

			break;

		}

	}

	@Override
	public void unmakeMove(Move move)

	{

		piece = move.getPiec();

		captured = move.getCapt();

		from = move.getFrom();

		to = move.getTosq();

		fromBitMap = Bitboards.BITSET[from];
		fromToBitMap = fromBitMap | Bitboards.BITSET[to];

		switch (piece)

		{

		case W_PAWN: // white pawn:

			setWhitePawns(getWhitePawns() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_PAWN;

			square[to] = EMPTY;

			if (captured != 0)

			{

				if (move.isEnpassant())

				{

					setBlackPawns(getBlackPawns() ^ Bitboards.BITSET[to - 8]);

					blackPieces ^= Bitboards.BITSET[to - 8];

					occupiedSquares ^= fromToBitMap | Bitboards.BITSET[to - 8];

					square[to - 8] = B_PAWN;

					Material -= PAWN_VALUE;

				}

				else

				{

					unmakeCapture(captured, to);

					occupiedSquares ^= fromBitMap;

				}

			}

			else
				occupiedSquares ^= fromToBitMap;

			if (move.isPromotion())

			{

				unmakeWhitePromotion(move.getProm(), to);

			}

			break;

		case W_KING: // white king:

			setWhiteKing(getWhiteKing() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_KING;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			if (move.isCastle())

			{

				if (move.isCastleOO())

				{

					setWhiteRooks(getWhiteRooks() ^ (Bitboards.BITSET[7] | Bitboards.BITSET[5]));

					whitePieces ^= Bitboards.BITSET[7] | Bitboards.BITSET[5];

					occupiedSquares ^= Bitboards.BITSET[7] | Bitboards.BITSET[5];

					square[7] = W_ROOK;

					square[5] = EMPTY;

				}

				else

				{

					setWhiteRooks(getWhiteRooks() ^ (Bitboards.BITSET[0] | Bitboards.BITSET[3]));

					whitePieces ^= Bitboards.BITSET[0] | Bitboards.BITSET[3];

					occupiedSquares ^= Bitboards.BITSET[0] | Bitboards.BITSET[3];

					square[0] = W_ROOK;

					square[3] = EMPTY;

				}

			}

			break;

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_KNIGHT;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_BISHOP;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_ROOK;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case W_QUEEN: // white queen:

			setWhiteQueens(getWhiteQueens() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_QUEEN;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_PAWN: // black pawn:

			setBlackPawns(getBlackPawns() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_PAWN;

			square[to] = EMPTY;

			if (captured != 0)

			{

				if (move.isEnpassant())

				{

					setWhitePawns(getWhitePawns() ^ Bitboards.BITSET[to + 8]);

					whitePieces ^= Bitboards.BITSET[to + 8];

					occupiedSquares ^= fromToBitMap | Bitboards.BITSET[to + 8];

					square[to + 8] = W_PAWN;

					Material += PAWN_VALUE;

				}

				else

				{

					unmakeCapture(captured, to);

					occupiedSquares ^= fromBitMap;

				}

			}

			else
				occupiedSquares ^= fromToBitMap;

			if (move.isPromotion())

			{

				unmakeBlackPromotion((int) move.getProm(), to);

			}

			break;

		case B_KING: // black king:

			setBlackKing(getBlackKing() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_KING;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			if (move.isCastle())

			{

				if (move.isCastleOO())

				{

					setBlackRooks(getBlackRooks() ^ (Bitboards.BITSET[63] | Bitboards.BITSET[61]));

					blackPieces ^= Bitboards.BITSET[63] | Bitboards.BITSET[61];

					occupiedSquares ^= Bitboards.BITSET[63] | Bitboards.BITSET[61];

					square[63] = B_ROOK;

					square[61] = EMPTY;

				}

				else

				{

					setBlackRooks(getBlackRooks() ^ (Bitboards.BITSET[56] | Bitboards.BITSET[59]));

					blackPieces ^= Bitboards.BITSET[56] | Bitboards.BITSET[59];

					occupiedSquares ^= Bitboards.BITSET[56] | Bitboards.BITSET[59];

					square[56] = B_ROOK;

					square[59] = EMPTY;

				}

			}

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_KNIGHT;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_BISHOP;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_ROOK;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		case B_QUEEN: // black queen:

			setBlackQueens(getBlackQueens() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_QUEEN;

			square[to] = EMPTY;

			if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else
				occupiedSquares ^= fromToBitMap;

			break;

		}

		endOfSearch--;

		castleWhite = gameLine[endOfSearch].getCastleWhite();

		castleBlack = gameLine[endOfSearch].getCastleBlack();

		epSquare = gameLine[endOfSearch].getEpSquare();

		fiftyMove = gameLine[endOfSearch].getFiftyMove();

		currentHash = gameLine[endOfSearch].getHash();

		currentHash2 = gameLine[endOfSearch].getHash2();

		nextMove *= (-1);

	}

	@Override
	protected void unmakeWhitePromotion(int prom, int to)

	{

		toBitMap = Bitboards.BITSET[to];

		setWhitePawns(getWhitePawns() ^ toBitMap);

		Material += BasicEngine.PAWN_VALUE;

		if (prom == BasicEngine.W_QUEEN)

		{

			setWhiteQueens(getWhiteQueens() ^ toBitMap);

			Material -= BasicEngine.QUEEN_VALUE;

		} else if (prom == BasicEngine.W_KNIGHT)

		{

			setWhiteKnights(getWhiteKnights() ^ toBitMap);

			Material -= BasicEngine.KNIGHT_VALUE;

		}

		else if (prom == BasicEngine.W_ROOK)

		{

			setWhiteRooks(getWhiteRooks() ^ toBitMap);

			Material -= BasicEngine.ROOK_VALUE;

		}

		else if (prom == BasicEngine.W_BISHOP)

		{

			setWhiteBishops(getWhiteBishops() ^ toBitMap);

			Material -= BasicEngine.BISHOP_VALUE;

		}

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
			if ("whitePawns".equals(piece)) {

				move.setPiec(W_PAWN);

				tempMove = ((getWhitePawns() & Bitboards.BITSET[comingFrom]) << 8) & freeSquares;

				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(to - 8);
					move.setTosq(to);
					if (Bitboards.RANKS[to] == 8) {
						move.setProm(W_QUEEN);
						list.add(new Move(move));
						move.setProm(W_ROOK);
						list.add(new Move(move));
						move.setProm(W_BISHOP);
						list.add(new Move(move));
						move.setProm(W_KNIGHT);
						list.add(new Move(move));
						move.setProm(EMPTY);
					} else {
						list.add(new Move(move));
					}
					tempMove ^= Bitboards.BITSET[to];

				}

				tempMove = (((((getWhitePawns() & Bitboards.BITSET[comingFrom]) & 65280L) << 8) & freeSquares) << 8)
						& freeSquares;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(to - 16);
					move.setTosq(to);
					list.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];

				}

				tempMove = (((getWhitePawns() & Bitboards.BITSET[comingFrom]) & 9187201950435737471L) << 9);
				if (epSquare != 0) { // do a quick check first
					if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
						move.setFrom(epSquare - 9);
						move.setTosq(epSquare);
						move.setCapt(B_PAWN);
						move.setProm(W_PAWN);
						list.add(new Move(move));
						move.setProm(EMPTY);
					}
				}
				tempMove &= blackPieces;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(to - 9);
					move.setTosq(to);
					move.setCapt(square[to]);
					if (Bitboards.RANKS[to] == 8) {
						move.setProm(W_QUEEN);
						list.add(new Move(move));
						move.setProm(W_ROOK);
						list.add(new Move(move));
						move.setProm(W_BISHOP);
						list.add(new Move(move));
						move.setProm(W_KNIGHT);
						list.add(new Move(move));
						move.setProm(EMPTY);
					} else {
						list.add(new Move(move));
					}
					tempMove ^= Bitboards.BITSET[to];
				}

				tempMove = (((getWhitePawns() & Bitboards.BITSET[comingFrom]) & (-72340172838076674L)) << 7);
				if (epSquare != 0) { // do a quick check first
					if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
						move.setFrom(epSquare - 7);
						move.setTosq(epSquare);
						move.setCapt(B_PAWN);
						move.setProm(W_PAWN);
						list.add(new Move(move));
						move.setProm(EMPTY);
					}
				}
				tempMove &= blackPieces;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(to - 7);
					move.setTosq(to);
					move.setCapt(square[to]);
					if (Bitboards.RANKS[to] == 8) {
						move.setProm(W_QUEEN);
						list.add(new Move(move));
						move.setProm(W_ROOK);
						list.add(new Move(move));
						move.setProm(W_BISHOP);
						list.add(new Move(move));
						move.setProm(W_KNIGHT);
						list.add(new Move(move));
						move.setProm(EMPTY);
					} else {
						list.add(new Move(move));
					}
					tempMove ^= Bitboards.BITSET[to];

				}

			} else if ("whiteKnights".equals(piece)) {

				move.setPiec(W_KNIGHT);

				move.setFrom(comingFrom);
				tempMove = Bitboards.KNIGHT_ATTACKS[comingFrom] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					list.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

			} else if ("whiteKing".equals(piece)) {

				move.setPiec(W_KING);

				move.setFrom(comingFrom);
				tempMove = Bitboards.KING_ATTACKS[comingFrom] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					list.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

				if ((castleWhite & CANCASTLEOO) != 0) {
					if ((Bitboards.maskFG[0] & occupiedSquares) == 0) {
						list.add(new Move(Bitboards.WHITE_OO_CASTL));
					}
				}

				if ((castleWhite & CANCASTLEOOO) != 0) {
					if ((Bitboards.maskBD[0] & occupiedSquares) == 0) {
						list.add(new Move(Bitboards.WHITE_OOO_CASTL));
					}
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
					move.setCapt(square[to]);
					list.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];

				}

			}

			else if ("whiteQueens".equals(piece)) {

				move.setPiec(W_QUEEN);

				move.setFrom(comingFrom);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[comingFrom])
						* Bitboards.FILEMAGIC[comingFrom]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares
						& Bitboards.RANKMASK[comingFrom]) >>> Bitboards.RANKSHIFT[comingFrom]);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[(int) comingFrom])
						* Bitboards.DIAGA8H1MAGIC[comingFrom] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[(int) comingFrom])
						* Bitboards.DIAGA1H8MAGIC[comingFrom] >>> 57);
				tempMove = (Bitboards.RANK_ATTACKS[comingFrom][rBitstate6]
						| Bitboards.FILE_ATTACKS[comingFrom][fBitstate6]
						| Bitboards.DIAGA1H8_ATTACKS[comingFrom][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[comingFrom][diaga8h1Bitstate6]) & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
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
					move.setCapt(square[to]);
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

		whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
		blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
		occupiedSquares = whitePieces | blackPieces;

		if ((castleWhite & CANCASTLEOO) != 0) {
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_WHITE];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_WHITE];

		}
		if ((castleWhite & CANCASTLEOOO) != 0) {
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_WHITE];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_WHITE];
		}
		if ((castleBlack & CANCASTLEOO) != 0) {
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_SHORT_CASTLE_BLACK];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_SHORT_CASTLE_BLACK];

		}
		if ((castleBlack & CANCASTLEOOO) != 0) {
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_LONG_CASTLE_BLACK];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_LONG_CASTLE_BLACK];
		}

		if (nextMove == -1) {
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_BLACK_TO_MOVE];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_BLACK_TO_MOVE];
		}

		if (epSquare != 0) {
			int add;
			if (epSquare >= 40) {
				add = epSquare - 40;
			} else {
				add = epSquare - 16;
			}

			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[Bitboards.HASH_EP_SQUARE + add];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[Bitboards.HASH_EP_SQUARE + add];
		}

		// Enpassent hash not beachtet so far

		int from;
		long tempPiece;
		for (int i = 0; i < 64; i++) {
			square[i] = 0;
		}

		tempPiece = whitePawns;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = W_PAWN;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_PAWN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_PAWN * 64 + from];
			Material += PAWN_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
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
		tempPiece = whiteQueens;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = W_QUEEN;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_QUEEN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_QUEEN * 64 + from];
			Material += QUEEN_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = whiteKing;

		from = Long.numberOfTrailingZeros(tempPiece);
		square[from] = W_KING;
		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[W_KING * 64 + from];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[W_KING * 64 + from];

		tempPiece = blackPawns;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = B_PAWN;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_PAWN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_PAWN * 64 + from];
			Material -= PAWN_VALUE;
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
		tempPiece = blackQueens;
		while (tempPiece != 0) {
			from = Long.numberOfTrailingZeros(tempPiece);
			square[from] = B_QUEEN;
			currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_QUEEN * 64 + from];
			currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_QUEEN * 64 + from];
			Material -= QUEEN_VALUE;
			tempPiece ^= Bitboards.BITSET[from];
		}
		tempPiece = blackKing;

		from = Long.numberOfTrailingZeros(tempPiece);
		square[from] = B_KING;
		currentHash ^= Bitboards.ZOBRIST_HASH_RANDOMS[B_KING * 64 + from];
		currentHash2 ^= Bitboards.ZOBRIST_HASH_RANDOMS2[B_KING * 64 + from];

	}

	public void setPosition(Position position) {

		whitePawns = position.whitePawns;
		whiteKnights = position.whiteKnights;
		whiteBishops = position.whiteBishops;
		whiteRooks = position.whiteRooks;
		whiteQueens = position.whiteQueens;
		whiteKing = position.whiteKing;
		blackPawns = position.blackPawns;
		blackKnights = position.blackKnights;
		blackBishops = position.blackBishops;
		blackRooks = position.blackRooks;
		blackQueens = position.blackQueens;
		blackKing = position.blackKing;

		castleWhite = position.castleWhite;
		castleBlack = position.castleBlack;
		nextMove = position.nextMove; // white or black Move
		epSquare = position.epSquare;
		fiftyMove = position.fiftyMove;

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
			if (nextMove == 1) {
				compare1 = (m1.getCapt() - 8) * 6 - m1.getPiec();
				compare2 = (m2.getCapt() - 8) * 6 - m2.getPiec();
			} else {
				compare1 = m1.getCapt() * 6 - m1.getPiec() + 8;
				compare2 = m2.getCapt() * 6 - m2.getPiec() + 8;
			}

			return compare2 - compare1;

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
			case W_PAWN:
				System.out.print("P ");
				break;
			case B_PAWN:
				System.out.print("p ");
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
			case W_QUEEN:
				System.out.print("Q ");
				break;
			case B_QUEEN:
				System.out.print("q ");
				break;
			case W_KING:
				System.out.print("K ");
				break;
			case B_KING:
				System.out.print("k ");
				break;
			default:
				;

			}

			j++;

		}

		System.out.println();

	}

	private boolean isPseudoLegalMove(Move move) {

		from = move.getFrom();
		piece = move.getPiec();
		to = move.getTosq();
		captured = move.getCapt();

		// quick check first!!
		if (square[from] != piece || ((!move.isEnpassant()) && square[to] != captured)) {
			return false;
		}

		if (nextMove == 1) {

			switch (piece) {
			case W_PAWN:
				if (move.isEnpassant()) {
					return (to == epSquare);
				} else if (move.isPawnDoublemove()) {
					return (((Bitboards.BITSET[from] << 8) & occupiedSquares) == 0);
				} else {
					return true;
				}

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

			case W_QUEEN:
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
						| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & Bitboards.BITSET[to];
				return (tempMove != 0);

			case W_KING:
				if (move.equals(Bitboards.WHITE_OO_CASTL)) {
					return ((castleWhite & CANCASTLEOO) != 0) && ((Bitboards.maskFG[0] & occupiedSquares) == 0)
							&& !isAttacked(Bitboards.maskEG[0]);

				} else if (move.equals(Bitboards.WHITE_OOO_CASTL)) {
					return ((castleWhite & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[0] & occupiedSquares) == 0)
							&& !isAttacked(Bitboards.maskCE[0]);

				}

				else {
					return true;
				}

			default:
				return false;

			}

		} else {
			switch (piece) {
			case B_PAWN:
				if (move.isEnpassant()) {
					return (to == epSquare);
				} else if (move.isPawnDoublemove()) {
					return (((Bitboards.BITSET[from] >>> 8) & occupiedSquares) == 0);
				} else {
					return true;
				}

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

			case B_QUEEN:
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);
				tempMove = (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6]
						| Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & Bitboards.BITSET[to];
				return (tempMove != 0);

			case B_KING:
				if (move.equals(Bitboards.BLACK_OO_CASTL)) {
					return ((castleBlack & CANCASTLEOO) != 0) && ((Bitboards.maskFG[1] & occupiedSquares) == 0)
							&& !isAttacked(Bitboards.maskEG[1]);

				} else if (move.equals(Bitboards.BLACK_OOO_CASTL)) {

					return ((castleBlack & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[1] & occupiedSquares) == 0)
							&& !isAttacked(Bitboards.maskCE[1]);

				} else {
					return true;
				}

			default:
				return false;

			}

		}

	}

	private int pseudoLegalCheckNotHeavyCaptureGenerator(int index) {

		move.clear();
		freeSquares = ~occupiedSquares;

		if (nextMove == 1) {
			targetBitboard = freeSquares | blackPawns;

			king = Long.numberOfTrailingZeros(blackKing);

			move.setPiec(W_KNIGHT);
			// generate all knight checks!!!
			tempPiece = Bitboards.KNIGHT_ATTACKS[king] & targetBitboard;

			while (tempPiece != 0) {
				to = Long.numberOfTrailingZeros(tempPiece);
				move.setTosq(to);
				move.setCapt(square[to]);
				tempMove = Bitboards.KNIGHT_ATTACKS[to] & whiteKnights;
				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				tempPiece ^= Bitboards.BITSET[to];
			}

			// now rook and queen checks!!!
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[king]) * Bitboards.FILEMAGIC[king]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[king]) >>> Bitboards.RANKSHIFT[king]);

			tempPiece = (Bitboards.RANK_ATTACKS[king][rBitstate6] | Bitboards.FILE_ATTACKS[king][fBitstate6])
					& targetBitboard;

			while (tempPiece != 0) {
				to = Long.numberOfTrailingZeros(tempPiece);
				move.setTosq(to);
				move.setCapt(square[to]);

				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);
				tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6])
						& (whiteRooks | whiteQueens);
				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					move.setPiec(square[from]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				// now rest of queen checks
				move.setPiec(W_QUEEN);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
						* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
						* Bitboards.DIAGA1H8MAGIC[to] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & whiteQueens;

				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				tempPiece ^= Bitboards.BITSET[to];
			}

			// now bishop and queen
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[king])
					* Bitboards.DIAGA8H1MAGIC[king] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[king])
					* Bitboards.DIAGA1H8MAGIC[king] >>> 57);
			tempPiece = (Bitboards.DIAGA1H8_ATTACKS[king][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[king][diaga8h1Bitstate6]) & whiteQueens;

			while (tempPiece != 0) {
				to = Long.numberOfTrailingZeros(tempPiece);
				move.setTosq(to);
				move.setCapt(square[to]);

				// now whiteQueen + whiteBishop
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
						* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
						* Bitboards.DIAGA1H8MAGIC[to] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & (whiteBishops | whiteQueens);

				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					move.setPiec(square[from]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				// now the rest for the queen!
				move.setPiec(W_QUEEN);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);
				tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6])
						& whiteQueens;
				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				tempPiece ^= Bitboards.BITSET[to];
			}

			// now white Pawn checks

		} else {

			targetBitboard = freeSquares | whitePawns;

			king = Long.numberOfTrailingZeros(whiteKing);

			move.setPiec(B_KNIGHT);
			// generate all knight checks!!!
			tempPiece = Bitboards.KNIGHT_ATTACKS[king] & targetBitboard;

			while (tempPiece != 0) {
				to = Long.numberOfTrailingZeros(tempPiece);
				move.setTosq(to);
				move.setCapt(square[to]);
				tempMove = Bitboards.KNIGHT_ATTACKS[to] & blackKnights;
				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				tempPiece ^= Bitboards.BITSET[to];
			}

			// now rook and queen checks!!!
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[king]) * Bitboards.FILEMAGIC[king]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[king]) >>> Bitboards.RANKSHIFT[king]);

			tempPiece = (Bitboards.RANK_ATTACKS[king][rBitstate6] | Bitboards.FILE_ATTACKS[king][fBitstate6])
					& targetBitboard;

			while (tempPiece != 0) {
				to = Long.numberOfTrailingZeros(tempPiece);
				move.setTosq(to);
				move.setCapt(square[to]);

				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);
				tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6])
						& (blackRooks | blackQueens);
				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					move.setPiec(square[from]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				// now rest of queen checks
				move.setPiec(B_QUEEN);
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
						* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
						* Bitboards.DIAGA1H8MAGIC[to] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & blackQueens;

				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				tempPiece ^= Bitboards.BITSET[to];
			}

			// now bishop and queen
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[king])
					* Bitboards.DIAGA8H1MAGIC[king] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[king])
					* Bitboards.DIAGA1H8MAGIC[king] >>> 57);
			tempPiece = (Bitboards.DIAGA1H8_ATTACKS[king][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[king][diaga8h1Bitstate6]) & whiteQueens;

			while (tempPiece != 0) {
				to = Long.numberOfTrailingZeros(tempPiece);
				move.setTosq(to);
				move.setCapt(square[to]);

				// now whiteQueen + whiteBishop
				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
						* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
						* Bitboards.DIAGA1H8MAGIC[to] >>> 57);
				tempMove = (Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & (blackBishops | blackQueens);

				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					move.setPiec(square[from]);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				// now the rest for the queen!
				move.setPiec(B_QUEEN);
				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);
				tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6])
						& blackQueens;
				while (tempMove != 0) {
					from = Long.numberOfTrailingZeros(tempMove);
					move.setFrom(from);
					currentSearch[index++].setMoveInt(move.getMoveInt());
					tempMove ^= Bitboards.BITSET[from];
				}

				tempPiece ^= Bitboards.BITSET[to];
			}

			// now white Pawn checks

		}

		return index;
	}

	private boolean positionRepeated() {

		int fiftyMoveCopy = fiftyMove;
		fiftyMoveCopy = fiftyMoveCopy - 2;

		// The check for i >=0 is necessary, because we might not have gotten
		// the initial position from
		// the starting position plus a few moves

		for (int i = endOfSearch - 2; fiftyMoveCopy >= 0 && i >= 0; i = i - 2, fiftyMoveCopy = fiftyMoveCopy - 2) {
			if (gameLine[i].getHash() == currentHash) {
				return true;
			}

		}

		return false;
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
