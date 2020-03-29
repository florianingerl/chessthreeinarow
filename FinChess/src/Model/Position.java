package Model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.florianingerl.util.regex.CaptureTreeNode;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import Persistence.OpeningBookManager;

public class Position {

	// This class only describes the internal representation of a position
	// and methods for making and umaking moves
	// and a method for checking wheter a move is legal

	private static Logger logger = Logger.getLogger(Position.class);

	public static final int EMPTY = 0;
	public static final int W_PAWN = 1;
	public static final int W_KING = 2;
	public static final int W_KNIGHT = 3;
	public static final int W_BISHOP = 5;
	public static final int W_ROOK = 6;
	public static final int W_QUEEN = 7;
	public static final int B_PAWN = 9;
	public static final int B_KING = 10;
	public static final int B_KNIGHT = 11;
	public static final int B_BISHOP = 13;
	public static final int B_ROOK = 14;
	public static final int B_QUEEN = 15;

	public static final byte CANCASTLEOO = 1;
	public static final byte CANCASTLEOOO = 1 << 1;

	protected long whitePawns;
	protected long whiteKnights;
	protected long whiteBishops;
	protected long whiteRooks;
	protected long whiteQueens;
	protected long whiteKing;
	protected long blackPawns;
	protected long blackKnights;
	protected long blackBishops;
	protected long blackRooks;
	protected long blackQueens;
	protected long blackKing;

	protected long whitePieces;
	protected long blackPieces;
	protected long occupiedSquares;

	protected int[] square = new int[64];

	protected byte castleWhite = CANCASTLEOO + CANCASTLEOOO;
	protected byte castleBlack = CANCASTLEOO + CANCASTLEOOO;
	protected byte nextMove = 1; // white or black Move
	protected int epSquare = 0;
	protected int fiftyMove = 0;

	protected GameLineRecord[] gameLine = new GameLineRecord[800];
	protected int endOfSearch = 0;

	int numberOfPlayedMoves = 1;

	public Position() {

		whitePawns = 65280L;

		blackPawns = (65280L) << (5 * 8);

		whiteKing = 16L;
		blackKing = (16L) << (7 * 8);

		whiteKnights = 66L;
		blackKnights = (66L) << (7 * 8);

		whiteBishops = 36L;
		blackBishops = (36L) << (7 * 8);

		whiteRooks = 129L;
		blackRooks = (129L) << (7 * 8);

		whiteQueens = 8L;
		blackQueens = (8L) << (7 * 8);

		whitePieces = 65280L | 8L | 66L | 129L | 36L | 16L;
		blackPieces = ((65280L) << (5 * 8)) | ((8L) << (7 * 8)) | ((66L) << (7 * 8)) | ((129L) << (7 * 8))
				| ((36L) << (7 * 8)) | ((16L) << (7 * 8));

		occupiedSquares = whitePieces | blackPieces;

		square[0] = W_ROOK;
		square[1] = W_KNIGHT;
		square[2] = W_BISHOP;
		square[3] = W_QUEEN;
		square[4] = W_KING;
		square[5] = W_BISHOP;
		square[6] = W_KNIGHT;
		square[7] = W_ROOK;
		int i;
		for (i = 8; i < 16; i++) {
			square[i] = W_PAWN;
		}
		for (i = 16; i < 48; i++) {
			square[i] = EMPTY;
		}
		for (i = 48; i < 56; i++) {
			square[i] = B_PAWN;
		}
		square[56] = B_ROOK;
		square[57] = B_KNIGHT;
		square[58] = B_BISHOP;
		square[59] = B_QUEEN;
		square[60] = B_KING;
		square[61] = B_BISHOP;
		square[62] = B_KNIGHT;
		square[63] = B_ROOK;

		constructGameLine();

		numberOfPlayedMoves = 1;

	}

	private void constructGameLine() {
		for (int i = 0; i < 800; i++) {
			gameLine[i] = new GameLineRecord();
		}
	}

	public void setSquare(int[] square) {
		this.square = square;
		initializeBittboardsFromSquare();
	}

	private void initializeBittboardsFromSquare() {

		whitePawns = 0L;
		blackPawns = 0L;
		whiteKnights = 0L;
		blackKnights = 0L;
		whiteBishops = 0L;
		blackBishops = 0L;
		whiteRooks = 0L;
		blackRooks = 0L;
		whiteQueens = 0L;
		blackQueens = 0L;
		whiteKing = 0L;
		blackKing = 0L;

		// Remove later dependency on data!!!
		for (int i = 0; i < 64; i++) {

			switch (square[i]) {
			case W_PAWN:
				whitePawns ^= Bitboards.BITSET[i];
				break;
			case B_PAWN:
				blackPawns ^= Bitboards.BITSET[i];
				break;
			case W_KNIGHT:
				whiteKnights ^= Bitboards.BITSET[i];
				break;
			case B_KNIGHT:
				blackKnights ^= Bitboards.BITSET[i];
				break;
			case W_BISHOP:
				whiteBishops ^= Bitboards.BITSET[i];
				break;
			case B_BISHOP:
				blackBishops ^= Bitboards.BITSET[i];
				break;
			case W_ROOK:
				whiteRooks ^= Bitboards.BITSET[i];
				break;
			case B_ROOK:
				blackRooks ^= Bitboards.BITSET[i];
				break;
			case W_QUEEN:
				whiteQueens ^= Bitboards.BITSET[i];
				break;
			case B_QUEEN:
				blackQueens ^= Bitboards.BITSET[i];
				break;
			case W_KING:
				whiteKing ^= Bitboards.BITSET[i];
				break;
			case B_KING:
				blackKing ^= Bitboards.BITSET[i];
				break;
			default:
				;
			}

		}

		whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
		blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;

		occupiedSquares = whitePieces | blackPieces;

	}

	public static Position fromFenString(String fenString) {
		return new Position(fenString);
	}

	public static Position fromPiecePlacements(String piecePlacements) {
		return new Position(piecePlacements);
	}

	private Position(String description) {
		if(description.startsWith("w") || description.startsWith("b")) {
			this.setPositionFromPiecePlacements(description);
		}
		else {
			this.setPositionFromFen(description);
		}
		this.constructGameLine();
	}

	private static Pattern pPiecePlacements = Pattern
			.compile("(?<nextMove>w|b)((?<piece>[pPnNbBrRqQkK])(?<square>[a-h][1-8])+)+");

	public void setPositionFromPiecePlacements(String piecePlacements) {
		Matcher m = pPiecePlacements.matcher(piecePlacements);
		m.setMode(Matcher.CAPTURE_TREE);
		if (!m.matches()) {
			throw new IllegalArgumentException(piecePlacements);
		}

		m.captureTree().getRoot().getChildren().stream().filter(ctn -> ctn.getGroupNumber() == 2)
				.forEach(child -> {
					char letter = child.getChildren().get(0).getCapture().getValue().charAt(0);
					int piece = EMPTY;
					switch (letter) {
					case 'p':
						piece = B_PAWN;
						break;
					case 'r':
						piece = B_ROOK;
						break;
					case 'n':
						piece = B_KNIGHT;
						break;
					case 'b':
						piece = B_BISHOP;
						break;
					case 'q':
						piece = B_QUEEN;
						break;
					case 'k':
						piece = B_KING;
						break;
					case 'P':
						piece = W_PAWN;
						break;
					case 'R':
						piece = W_ROOK;
						break;
					case 'N':
						piece = W_KNIGHT;
						break;
					case 'B':
						piece = W_BISHOP;
						break;
					case 'Q':
						piece = W_QUEEN;
						break;
					case 'K':
						piece = W_KING;
						break;
					default:
					}
					
					Iterator<CaptureTreeNode> it = child.getChildren().iterator();
					it.next();
					
					while(it.hasNext() ) {
						int sq = SquareRepresentationConverter.getBitFromString( it.next().getCapture().getValue() );
						square[sq] = piece;

					}

				});
		
		initializeBittboardsFromSquare();

		nextMove = 1;

		if (m.group("nextMove").equals("w")) {
			nextMove = 1;
		} else {
			nextMove = -1;
		}
		
		castleWhite = 0;
		castleBlack = 0;

		epSquare = 0;
		fiftyMove = 0;
		numberOfPlayedMoves = 0;
		
		endOfSearch = 0;
	}

	public void setPositionFromFen(String fenString) {

		String[] fenTokens = fenString.split(" ");

		
		
		int i, j, state;
		int sq;
		char letter;
		// aFile ranges from 1 to 8
		// aRank ranges from 1 to 8
		int aRank, aFile;
		// empty board
		for (i = 0; i < 64; i++) {
			square[i] = EMPTY;
		}

		// read the board - translate each loop idx into a square
		j = 1;
		i = 0;

		while ((j < 65) && (i < fenTokens[0].length())) {

			letter = fenTokens[0].charAt(i);
			i++;
			aFile = 1 + ((j - 1) % 8);
			aRank = 8 - ((j - 1) / 8);
			sq = (aRank - 1) * 8 + aFile - 1;

			switch (letter) {
			case 'p':
				square[sq] = B_PAWN;
				break;
			case 'r':
				square[sq] = B_ROOK;
				break;
			case 'n':
				square[sq] = B_KNIGHT;
				break;
			case 'b':
				square[sq] = B_BISHOP;
				break;
			case 'q':
				square[sq] = B_QUEEN;
				break;
			case 'k':
				square[sq] = B_KING;
				break;
			case 'P':
				square[sq] = W_PAWN;
				break;
			case 'R':
				square[sq] = W_ROOK;
				break;
			case 'N':
				square[sq] = W_KNIGHT;
				break;
			case 'B':
				square[sq] = W_BISHOP;
				break;
			case 'Q':
				square[sq] = W_QUEEN;
				break;
			case 'K':
				square[sq] = W_KING;
				break;
			case '/':
				j--;
				break;
			case '1':
				break;
			case '2':
				j++;
				break;
			case '3':
				j += 2;
				break;
			case '4':
				j += 3;
				break;
			case '5':
				j += 4;
				break;
			case '6':
				j += 5;
				break;
			case '7':
				j += 6;
				break;
			case '8':
				j += 7;
				break;
			default:

			}
			j++;
		}

		initializeBittboardsFromSquare();

		nextMove = 1;

		if (fenTokens[1].equals("w")) {
			nextMove = 1;
		} else {
			nextMove = -1;
		}

		castleWhite = 0;
		castleBlack = 0;
		// Initialize all castle possibilities

		if (fenTokens[2].contains("K"))
			castleWhite = (byte) (castleWhite | CANCASTLEOO);
		if (fenTokens[2].contains("Q"))
			castleWhite = (byte) (castleWhite | CANCASTLEOOO);
		if (fenTokens[2].contains("k"))
			castleBlack = (byte) (castleBlack | CANCASTLEOO);
		if (fenTokens[2].contains("q"))
			castleBlack = (byte) (castleBlack | CANCASTLEOOO);

		epSquare = 0;
		if (fenTokens[3].length() >= 2) {
			if ((fenTokens[3].charAt(0) >= 'a') && (fenTokens[3].charAt(0) <= 'h')
					&& ((fenTokens[3].charAt(1) == '3') || (fenTokens[3].charAt(1) == '6'))) {
				aFile = fenTokens[3].charAt(0) - 96; // ASCII 'a' = 97
				aRank = fenTokens[3].charAt(1) - 48; // ASCII '1' = 49
				epSquare = (aRank - 1) * 8 + aFile - 1;
			}

		}

		try {
			fiftyMove = Integer.valueOf(fenTokens[4]);
		} catch (NumberFormatException nfe) {

		}

		try {
			numberOfPlayedMoves = Integer.valueOf(fenTokens[5]);
		} catch (NumberFormatException nfe) {

		}

		endOfSearch = 0;

	}

	protected void makeBlackPromotion(int prom, int to)

	{

		long toBitMap;

		toBitMap = Bitboards.BITSET[to];

		setBlackPawns(getBlackPawns() ^ toBitMap);

		switch (prom) {
		case B_QUEEN:
			setBlackQueens(getBlackQueens() ^ toBitMap);

			break;
		case B_ROOK:
			setBlackRooks(getBlackRooks() ^ toBitMap);

			break;
		case B_BISHOP:
			setBlackBishops(getBlackBishops() ^ toBitMap);

			break;
		case B_KNIGHT:
			setBlackKnights(getBlackKnights() ^ toBitMap);

			break;
		default:
		}

	}

	protected void makeCapture(int captured, int to)

	{

		// deals with all captures, except en-passant

		long toBitMap;

		toBitMap = Bitboards.BITSET[to];

		switch (captured)

		{

		case W_PAWN: // white pawn:

			setWhitePawns(getWhitePawns() ^ toBitMap);

			whitePieces ^= toBitMap;

			break;

		case W_KING: // white king:

			setWhiteKing(getWhiteKing() ^ toBitMap);

			whitePieces ^= toBitMap;

			break;

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ toBitMap);

			whitePieces ^= toBitMap;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ toBitMap);

			whitePieces ^= toBitMap;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ toBitMap);

			whitePieces ^= toBitMap;

			if (to == 0)
				castleWhite &= ~CANCASTLEOOO;

			if (to == 7)
				castleWhite &= ~CANCASTLEOO;

			break;

		case W_QUEEN: // white queen:

			setWhiteQueens(getWhiteQueens() ^ toBitMap);

			whitePieces ^= toBitMap;

			break;

		case B_PAWN: // black pawn:

			setBlackPawns(getBlackPawns() ^ toBitMap);

			blackPieces ^= toBitMap;

			break;

		case B_KING: // black king:

			setBlackKing(getBlackKing() ^ toBitMap);

			blackPieces ^= toBitMap;

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ toBitMap);

			blackPieces ^= toBitMap;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ toBitMap);

			blackPieces ^= toBitMap;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ toBitMap);

			blackPieces ^= toBitMap;

			if (to == 56)
				castleBlack &= ~CANCASTLEOOO;

			if (to == 63)
				castleBlack &= ~CANCASTLEOO;

			break;

		case B_QUEEN: // black queen:

			setBlackQueens(getBlackQueens() ^ toBitMap);

			blackPieces ^= toBitMap;

			break;

		}

		fiftyMove = 0;

	}

	public void makeMove(Move move)

	{
		if (nextMove == -1)
			numberOfPlayedMoves++;

		int from = (int) move.getFrom();
		int to = (int) move.getTosq();
		int piece = (int) move.getPiec();

		int captured = (int) move.getCapt();

		long fromBitMap = Bitboards.BITSET[from];

		long fromToBitMap = fromBitMap | Bitboards.BITSET[to];

		gameLine[endOfSearch].getMove().setMoveInt(move.getMoveInt());

		gameLine[endOfSearch].setCastleWhite(castleWhite);

		gameLine[endOfSearch].setCastleBlack(castleBlack);

		gameLine[endOfSearch].setFiftyMove(fiftyMove);

		gameLine[endOfSearch].setEpSquare(epSquare);

		endOfSearch++;

		switch (piece)

		{

		case W_PAWN: // white pawn:

			setWhitePawns(getWhitePawns() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_PAWN;

			epSquare = 0;

			fiftyMove = 0;

			if (Bitboards.RANKS[from] == 2)

				if (Bitboards.RANKS[to] == 4)
					epSquare = from + 8;

			if (captured != 0)

			{

				if (move.isEnpassant())

				{

					setBlackPawns(getBlackPawns() ^ Bitboards.BITSET[to - 8]);

					blackPieces ^= Bitboards.BITSET[to - 8];

					occupiedSquares ^= fromToBitMap | Bitboards.BITSET[to - 8];

					square[to - 8] = EMPTY;

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

				makeWhitePromotion((int) move.getProm(), to);

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

			if (from == 0)
				castleWhite &= ~CANCASTLEOOO;

			if (from == 7)
				castleWhite &= ~CANCASTLEOO;

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

			if (Bitboards.RANKS[from] == 7)

				if (Bitboards.RANKS[to] == 5)
					epSquare = from - 8;

			if (captured != 0)

			{

				if (move.isEnpassant())

				{

					setWhitePawns(getWhitePawns() ^ Bitboards.BITSET[to + 8]);

					whitePieces ^= Bitboards.BITSET[to + 8];

					occupiedSquares ^= fromToBitMap | Bitboards.BITSET[to + 8];

					square[to + 8] = EMPTY;

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

			if (from == 56)
				castleBlack &= ~CANCASTLEOOO;

			if (from == 63)
				castleBlack &= ~CANCASTLEOO;

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

	protected void makeWhitePromotion(int prom, int to)

	{

		long toBitMap;

		toBitMap = Bitboards.BITSET[to];

		setWhitePawns(getWhitePawns() ^ toBitMap);

		switch (prom) {
		case W_QUEEN:
			setWhiteQueens(getWhiteQueens() ^ toBitMap);

			break;
		case W_ROOK:
			setWhiteRooks(getWhiteRooks() ^ toBitMap);

			break;
		case W_BISHOP:
			setWhiteBishops(getWhiteBishops() ^ toBitMap);

			break;
		case W_KNIGHT:
			setWhiteKnights(getWhiteKnights() ^ toBitMap);

			break;
		default:
		}

	}

	protected void unmakeBlackPromotion(int prom, int to)

	{

		long toBitMap;

		toBitMap = Bitboards.BITSET[to];

		setBlackPawns(getBlackPawns() ^ toBitMap);

		if (prom == B_QUEEN)

		{

			setBlackQueens(getBlackQueens() ^ toBitMap);

		} else if (prom == B_KNIGHT)

		{

			setBlackKnights(getBlackKnights() ^ toBitMap);

		}

		else if (prom == B_ROOK)

		{

			setBlackRooks(getBlackRooks() ^ toBitMap);

		}

		else if (prom == B_BISHOP)

		{

			setBlackBishops(getBlackBishops() ^ toBitMap);

		}

	}

	protected void unmakeCapture(int captured, int to)

	{

		long toBitMap;

		toBitMap = Bitboards.BITSET[to];

		switch (captured)

		{

		case W_PAWN: // white pawn:

			setWhitePawns(getWhitePawns() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_PAWN;

			break;

		case W_KING: // white king:

			setWhiteKing(getWhiteKing() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_KING;

			break;

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_KNIGHT;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_BISHOP;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_ROOK;

			break;

		case W_QUEEN: // white queen:

			setWhiteQueens(getWhiteQueens() ^ toBitMap);

			whitePieces ^= toBitMap;

			square[to] = W_QUEEN;

			break;

		case B_PAWN: // black pawn:

			setBlackPawns(getBlackPawns() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_PAWN;

			break;

		case B_KING: // black king:

			setBlackKing(getBlackKing() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_KING;

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_KNIGHT;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_BISHOP;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_ROOK;

			break;

		case B_QUEEN: // black queen:

			setBlackQueens(getBlackQueens() ^ toBitMap);

			blackPieces ^= toBitMap;

			square[to] = B_QUEEN;

			break;

		}

	}

	public void unmakeMove(Move move)

	{
		if (nextMove == 1)
			numberOfPlayedMoves--;

		int piece = (int) move.getPiec();

		int captured = (int) move.getCapt();

		int from = (int) move.getFrom();

		int to = (int) move.getTosq();

		long fromBitMap = Bitboards.BITSET[from];

		long fromToBitMap = fromBitMap | Bitboards.BITSET[to];

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

				unmakeWhitePromotion((int) move.getProm(), to);

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

		if (endOfSearch > 0) {
			endOfSearch--;

			castleWhite = gameLine[endOfSearch].getCastleWhite();

			castleBlack = gameLine[endOfSearch].getCastleBlack();

			epSquare = gameLine[endOfSearch].getEpSquare();

			fiftyMove = gameLine[endOfSearch].getFiftyMove();
		}

		nextMove *= (-1);

	}

	protected void unmakeWhitePromotion(int prom, int to)

	{

		long toBitMap;

		toBitMap = Bitboards.BITSET[to];

		setWhitePawns(getWhitePawns() ^ toBitMap);

		if (prom == BasicEngine.W_QUEEN)

		{

			setWhiteQueens(getWhiteQueens() ^ toBitMap);

		} else if (prom == BasicEngine.W_KNIGHT)

		{

			setWhiteKnights(getWhiteKnights() ^ toBitMap);

		}

		else if (prom == BasicEngine.W_ROOK)

		{

			setWhiteRooks(getWhiteRooks() ^ toBitMap);

		}

		else if (prom == BasicEngine.W_BISHOP)

		{

			setWhiteBishops(getWhiteBishops() ^ toBitMap);

		}

	}

	public int getFiftyMove() {
		return fiftyMove;
	}

	public void setFiftyMove(int fiftyMove) {
		this.fiftyMove = fiftyMove;
	}

	public int getNextMove() {
		return nextMove;
	}

	public int[] getSquare() {
		return square;
	}

	public long getWhitePawns() {
		return whitePawns;
	}

	public void setWhitePawns(long whitePawns) {
		this.whitePawns = whitePawns;
	}

	public long getBlackPawns() {
		return blackPawns;
	}

	public void setBlackPawns(long blackPawns) {
		this.blackPawns = blackPawns;
	}

	public long getWhiteKnights() {
		return whiteKnights;
	}

	public void setWhiteKnights(long whiteKnights) {
		this.whiteKnights = whiteKnights;
	}

	public long getBlackKnights() {
		return blackKnights;
	}

	public void setBlackKnights(long blackKnights) {
		this.blackKnights = blackKnights;
	}

	public long getWhiteBishops() {
		return whiteBishops;
	}

	public void setWhiteBishops(long whiteBishops) {
		this.whiteBishops = whiteBishops;
	}

	public long getBlackBishops() {
		return blackBishops;
	}

	public void setBlackBishops(long blackBishops) {
		this.blackBishops = blackBishops;
	}

	public long getWhiteRooks() {
		return whiteRooks;
	}

	public void setWhiteRooks(long whiteRooks) {
		this.whiteRooks = whiteRooks;
	}

	public long getBlackRooks() {
		return blackRooks;
	}

	public void setBlackRooks(long blackRooks) {
		this.blackRooks = blackRooks;
	}

	public long getWhiteQueens() {
		return whiteQueens;
	}

	public void setWhiteQueens(long whiteQueens) {
		this.whiteQueens = whiteQueens;
	}

	public long getBlackQueens() {
		return blackQueens;
	}

	public void setBlackQueens(long blackQueens) {
		this.blackQueens = blackQueens;
	}

	public long getWhiteKing() {
		return whiteKing;
	}

	public void setWhiteKing(long whiteKing) {
		this.whiteKing = whiteKing;
	}

	public long getBlackKing() {
		return blackKing;
	}

	public void setBlackKing(long blackKing) {
		this.blackKing = blackKing;
	}

	public long getOccupiedSquares() {
		return occupiedSquares;
	}

	public void makeNullMove() {
		nextMove *= (-1);
	}

	protected boolean isAttacked(long interRochade) {

		int from;
		long tempMove = 0L;
		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;
		byte fBitstate6;
		byte rBitstate6;

		if (nextMove == -1) {
			while (interRochade != 0) {
				from = Long.numberOfTrailingZeros(interRochade);

				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);

				tempMove |= (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & (getWhiteQueens() | getWhiteBishops());

				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove |= (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
						& (getWhiteQueens() | getWhiteRooks());

				tempMove |= Bitboards.KNIGHT_ATTACKS[from] & getWhiteKnights();

				tempMove |= Bitboards.KING_ATTACKS[from] & getWhiteKing();

				tempMove |= ((getBlackKing() & 9187201950435737471L) >>> 7) & getWhitePawns();

				tempMove |= ((getBlackKing() & (-72340172838076674L)) >>> 9) & getWhitePawns();

				interRochade ^= Bitboards.BITSET[from];

			}
		}

		else {

			while (interRochade != 0) {
				from = Long.numberOfTrailingZeros(interRochade);

				diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
						* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

				diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
						* Bitboards.DIAGA1H8MAGIC[from] >>> 57);

				tempMove |= (Bitboards.DIAGA1H8_ATTACKS[from][diaga1h8Bitstate6]
						| Bitboards.DIAGA8H1_ATTACKS[from][diaga8h1Bitstate6]) & (getBlackQueens() | getBlackBishops());

				fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[from]) * Bitboards.FILEMAGIC[from]) >>> 57);

				rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[from]) >>> Bitboards.RANKSHIFT[from]);

				tempMove |= (Bitboards.RANK_ATTACKS[from][rBitstate6] | Bitboards.FILE_ATTACKS[from][fBitstate6])
						& (getBlackQueens() | getBlackRooks());

				tempMove |= Bitboards.KNIGHT_ATTACKS[from] & getBlackKnights();

				tempMove |= Bitboards.KING_ATTACKS[from] & getBlackKing();

				tempMove |= ((getWhiteKing() & 9187201950435737471L) << 9) & getBlackPawns();

				tempMove |= ((getWhiteKing() & (-72340172838076674L)) << 7) & getBlackPawns();

				interRochade ^= Bitboards.BITSET[from];
			}
		}

		if (tempMove != 0) {
			return true;
		} else {
			return false;
		}

	}

	public boolean isMoveLegal(Move m) {

		long targetBitboard, freeSquares;
		long tempPiece, tempMove;
		freeSquares = ~occupiedSquares;
		Move move = new Move();
		int from, to;
		byte fBitstate6;
		byte rBitstate6;
		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;

		List<Move> moves = new LinkedList<Move>();

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
					moves.add(new Move(move));
					move.setProm(W_ROOK);
					moves.add(new Move(move));
					move.setProm(W_BISHOP);
					moves.add(new Move(move));
					move.setProm(W_KNIGHT);
					moves.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					moves.add(new Move(move));
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((((getWhitePawns() & 65280L) << 8) & freeSquares) << 8) & freeSquares;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 16);
				move.setTosq(to);
				moves.add(new Move(move));
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((getWhitePawns() & 9187201950435737471L) << 9);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare - 9);
					move.setTosq(epSquare);
					move.setCapt(B_PAWN);
					move.setProm(W_PAWN);
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					move.setProm(W_ROOK);
					moves.add(new Move(move));
					move.setProm(W_BISHOP);
					moves.add(new Move(move));
					move.setProm(W_KNIGHT);
					moves.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					moves.add(new Move(move));
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
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					move.setProm(W_ROOK);
					moves.add(new Move(move));
					move.setProm(W_BISHOP);
					moves.add(new Move(move));
					move.setProm(W_KNIGHT);
					moves.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					moves.add(new Move(move));
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			move.setPiec(W_KNIGHT);

			tempPiece = getWhiteKnights();
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

				if (((castleWhite & CANCASTLEOO) != 0) && ((Bitboards.maskFG[0] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskEG[0])) {
					moves.add(new Move(Bitboards.WHITE_OO_CASTL)); // predefined
				}

				// White 0-0-0 Castling:

				if (((castleWhite & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[0] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskCE[0])) {
					moves.add(new Move(Bitboards.WHITE_OOO_CASTL)); // predefined

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
					moves.add(new Move(move));
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
					moves.add(new Move(move));
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
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					move.setProm(B_ROOK);
					moves.add(new Move(move));
					move.setProm(B_BISHOP);
					moves.add(new Move(move));
					move.setProm(B_KNIGHT);
					moves.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					moves.add(new Move(move));
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((((getBlackPawns() & ((65280L) << (5 * 8))) >>> 8) & freeSquares) >>> 8) & freeSquares;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 16);
				move.setTosq(to);
				moves.add(new Move(move));
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((getBlackPawns() & 9187201950435737471L) >>> 7);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare + 7);
					move.setTosq(epSquare);
					move.setCapt(W_PAWN);
					move.setProm(B_PAWN);
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					move.setProm(B_ROOK);
					moves.add(new Move(move));
					move.setProm(B_BISHOP);
					moves.add(new Move(move));
					move.setProm(B_KNIGHT);
					moves.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					moves.add(new Move(move));
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
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					move.setProm(B_ROOK);
					moves.add(new Move(move));
					move.setProm(B_BISHOP);
					moves.add(new Move(move));
					move.setProm(B_KNIGHT);
					moves.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					moves.add(new Move(move));
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			move.setPiec(B_KNIGHT);

			tempPiece = getBlackKnights();
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					move.setCapt(square[to]);
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

				if (((castleBlack & CANCASTLEOO) != 0) && ((Bitboards.maskFG[1] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskEG[1])) {
					moves.add(new Move(Bitboards.BLACK_OO_CASTL)); // predefined
				}

				if (((castleBlack & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[1] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskCE[1])) {
					moves.add(new Move(Bitboards.BLACK_OOO_CASTL)); // predefined
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
					moves.add(new Move(move));
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
					moves.add(new Move(move));
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
					moves.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		}

		if (moves.contains(m)) {

			makeMove(m);
			if (isOtherKingAttacked()) {
				unmakeMove(m);
				return false;
			}
			unmakeMove(m);
			return true;
		}

		return false;

	}

	protected boolean isOtherKingAttacked() {

		int from;
		long tempMove = 0L;
		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;
		byte fBitstate6;
		byte rBitstate6;

		if (nextMove == 1) {
			from = Long.numberOfTrailingZeros(blackKing);

			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from] >>> 57);

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

			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[from])
					* Bitboards.DIAGA8H1MAGIC[from] >>> 57);

			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[from])
					* Bitboards.DIAGA1H8MAGIC[from] >>> 57);

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

	public Move parseMove(String move, MoveEncoding encoding) {
		if (encoding == MoveEncoding.SHORT_ALGEBRAIC_NOTATION) {
			return valueOfShortAlgebraicNotation(move);
		} else if (encoding == MoveEncoding.LONG_ALGEBRAIC_NOTATION) {
			return valueOfLongAlgebraicNotation(move);
		} else {
			return null;
		}

	}

	private Move valueOfLongAlgebraicNotation(String stringMove) {

		Move move = new Move();

		int fromSquare;
		int toSquare;

		int index = 0;

		// 97 is kleines 'a'

		int fromColumn = stringMove.charAt(index) - 97;
		logger.debug("From column " + stringMove.charAt(index) + " " + fromColumn);
		index++;
		// fromRow begins with 0
		int fromRow = stringMove.charAt(index) - 49;
		logger.debug("From row " + stringMove.charAt(index) + " " + fromRow);

		fromSquare = fromRow * 8 + fromColumn;

		index++;
		int toColumn = stringMove.charAt(index) - 97;
		index++;
		int toRow = stringMove.charAt(index) - 49;
		toSquare = toRow * 8 + toColumn;

		move.setFrom(fromSquare);
		move.setTosq(toSquare);
		move.setPiec(square[fromSquare]);
		move.setCapt(square[toSquare]);

		if (nextMove == 1) {

			if (move.getPiec() == W_KING && Math.abs(move.getFrom() - move.getTosq()) == 2) {
				if (move.getFrom() - move.getTosq() > 0) {
					move = new Move(Bitboards.WHITE_OOO_CASTL);
				} else {
					move = new Move(Bitboards.WHITE_OO_CASTL);
				}
			}

			if (move.getPiec() == W_PAWN) {
				if (move.getCapt() == 0 && ((move.getFrom() - move.getTosq()) % 2) != 0) {
					move.setCapt(B_PAWN);
					move.setProm(W_PAWN);
				}

				if (Bitboards.RANKS[move.getTosq()] == 8) {
					index++;
					switch (stringMove.charAt(index)) {
					case 'q':
						move.setProm(W_QUEEN);
						break;
					case 'k':
						move.setProm(W_KNIGHT);
						break;
					case 'r':
						move.setProm(W_ROOK);
						break;
					case 'b':
						move.setProm(W_BISHOP);
						break;
					default:
						;
					}
				}

			}

		} else {

			if (move.getPiec() == B_KING && Math.abs(move.getFrom() - move.getTosq()) == 2) {
				if (move.getFrom() - move.getTosq() > 0) {
					move = new Move(Bitboards.BLACK_OOO_CASTL);
				} else {
					move = new Move(Bitboards.BLACK_OO_CASTL);
				}
			}

			if (move.getPiec() == B_PAWN) {
				if (move.getCapt() == 0 && ((move.getTosq() - move.getFrom()) % 2) != 0) {
					move.setCapt(W_PAWN);
					move.setProm(B_PAWN);
				}

				if (Bitboards.RANKS[move.getTosq()] == 1) {
					index++;
					switch (stringMove.charAt(index)) {
					case 'q':
						move.setProm(B_QUEEN);
						break;
					case 'k':
						move.setProm(B_KNIGHT);
						break;
					case 'r':
						move.setProm(B_ROOK);
						break;
					case 'b':
						move.setProm(B_BISHOP);
						break;
					default:
						;
					}

				}

			}

		}

		return move;

	}

	private Move valueOfShortAlgebraicNotation(String move) {
		ShortAlgebraicMoveNotationParser samnp = new ShortAlgebraicMoveNotationParser(this);
		return samnp.parseMove(move);
	}

	public Move getMove(int from, int tosq) {

		Move m = new Move();

		m.setFrom(from);
		m.setTosq(tosq);
		m.setPiec(square[from]);
		m.setCapt(square[tosq]);

		if (nextMove == 1) {

			if (m.getPiec() == BasicEngine.W_KING && Math.abs(m.getFrom() - m.getTosq()) == 2) {
				if (m.getFrom() - m.getTosq() > 0) {
					m = Bitboards.WHITE_OOO_CASTL;
				} else {
					m = Bitboards.WHITE_OO_CASTL;
				}
			}

			if (m.getPiec() == BasicEngine.W_PAWN) {
				if (m.getCapt() == 0 && ((m.getFrom() - m.getTosq()) % 2) != 0) {
					m.setCapt(BasicEngine.B_PAWN);
					m.setProm(BasicEngine.W_PAWN);
				}

				if (Bitboards.RANKS[(int) m.getTosq()] == 8) {
					m.setProm(BasicEngine.W_QUEEN);
				}

			}

		} else {

			if (m.getPiec() == BasicEngine.B_KING && Math.abs(m.getFrom() - m.getTosq()) == 2) {
				if (m.getFrom() - m.getTosq() > 0) {
					m = Bitboards.BLACK_OOO_CASTL;
				} else {
					m = Bitboards.BLACK_OO_CASTL;
				}
			}

			if (m.getPiec() == BasicEngine.B_PAWN) {
				if (m.getCapt() == 0 && ((m.getTosq() - m.getFrom()) % 2) != 0) {
					m.setCapt(BasicEngine.W_PAWN);
					m.setProm(BasicEngine.B_PAWN);
				}

				if (Bitboards.RANKS[(int) m.getTosq()] == 1) {
					m.setProm(BasicEngine.B_QUEEN);
				}

			}

		}

		return m;
	}

	public byte getCastleWhite() {
		return castleWhite;
	}

	public byte getCastleBlack() {
		return castleBlack;
	}

	public int getEpSquare() {
		return epSquare;
	}

	public int getNumberOfPlayedMoves() {
		return numberOfPlayedMoves;
	}

	public String getFenString() {
		return CalculateFenString.getFenString(this);
	}

	public void reset() {
		whitePawns = 65280L;

		blackPawns = (65280L) << (5 * 8);

		whiteKing = 16L;
		blackKing = (16L) << (7 * 8);

		whiteKnights = 66L;
		blackKnights = (66L) << (7 * 8);

		whiteBishops = 36L;
		blackBishops = (36L) << (7 * 8);

		whiteRooks = 129L;
		blackRooks = (129L) << (7 * 8);

		whiteQueens = 8L;
		blackQueens = (8L) << (7 * 8);

		whitePieces = 65280L | 8L | 66L | 129L | 36L | 16L;
		blackPieces = ((65280L) << (5 * 8)) | ((8L) << (7 * 8)) | ((66L) << (7 * 8)) | ((129L) << (7 * 8))
				| ((36L) << (7 * 8)) | ((16L) << (7 * 8));

		occupiedSquares = whitePieces | blackPieces;

		square[0] = W_ROOK;
		square[1] = W_KNIGHT;
		square[2] = W_BISHOP;
		square[3] = W_QUEEN;
		square[4] = W_KING;
		square[5] = W_BISHOP;
		square[6] = W_KNIGHT;
		square[7] = W_ROOK;
		int i;
		for (i = 8; i < 16; i++) {
			square[i] = W_PAWN;
		}
		for (i = 16; i < 48; i++) {
			square[i] = EMPTY;
		}
		for (i = 48; i < 56; i++) {
			square[i] = B_PAWN;
		}
		square[56] = B_ROOK;
		square[57] = B_KNIGHT;
		square[58] = B_BISHOP;
		square[59] = B_QUEEN;
		square[60] = B_KING;
		square[61] = B_BISHOP;
		square[62] = B_KNIGHT;
		square[63] = B_ROOK;

		numberOfPlayedMoves = 1;

		castleWhite = CANCASTLEOO + CANCASTLEOOO;
		castleBlack = CANCASTLEOO + CANCASTLEOOO;
		nextMove = 1; // white or black Move
		epSquare = 0;
		fiftyMove = 0;

		endOfSearch = 0;

	}

	public String toShortAlgebraicNotation(Move move) {
		if (move.isCastleOO()) {
			return "O-O";
		}
		if (move.isCastleOOO()) {
			return "O-O-O";
		}

		StringBuilder sb = new StringBuilder();

		long tempMove = 0;
		int piece = move.getPiec();
		int to = move.getTosq();
		int from = move.getFrom();
		int captured = move.getCapt();

		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;

		byte fBitstate6;
		byte rBitstate6;

		switch (piece) {
		case W_KNIGHT:
			sb.append('N');
			tempMove = Bitboards.KNIGHT_ATTACKS[to] & whiteKnights;

			break;
		case B_KNIGHT:
			sb.append('N');
			tempMove = Bitboards.KNIGHT_ATTACKS[to] & blackKnights;

			break;
		case W_BISHOP:

			sb.append('B');
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
					* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
					* Bitboards.DIAGA1H8MAGIC[to] >>> 57);
			tempMove = (Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & whiteBishops;

			break;
		case B_BISHOP:

			sb.append('B');
			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
					* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
					* Bitboards.DIAGA1H8MAGIC[to] >>> 57);
			tempMove = (Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & blackBishops;

			break;

		case W_ROOK:

			sb.append('R');
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);

			tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6]) & whiteRooks;

			break;

		case B_ROOK:
			sb.append('R');
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);

			tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6]) & blackRooks;

			break;

		case W_QUEEN:

			sb.append('Q');
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);

			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
					* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
					* Bitboards.DIAGA1H8MAGIC[to] >>> 57);

			tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6]
					| Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & whiteQueens;

			break;

		case B_QUEEN:

			sb.append('Q');
			fBitstate6 = (byte) (((occupiedSquares & Bitboards.FILEMASK[to]) * Bitboards.FILEMAGIC[to]) >>> 57);

			rBitstate6 = (byte) ((occupiedSquares & Bitboards.RANKMASK[to]) >>> Bitboards.RANKSHIFT[to]);

			diaga8h1Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA8H1MASK[to])
					* Bitboards.DIAGA8H1MAGIC[to] >>> 57);
			diaga1h8Bitstate6 = (byte) ((occupiedSquares & Bitboards.DIAGA1H8MASK[to])
					* Bitboards.DIAGA1H8MAGIC[to] >>> 57);

			tempMove = (Bitboards.RANK_ATTACKS[to][rBitstate6] | Bitboards.FILE_ATTACKS[to][fBitstate6]
					| Bitboards.DIAGA1H8_ATTACKS[to][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[to][diaga8h1Bitstate6]) & blackQueens;

			break;

		case W_KING:
			sb.append('K');
			if (move.isCapture())
				sb.append('x');
			sb.append(getFile(to));
			sb.append(getRank(to));
			return sb.toString();

		case B_KING:
			sb.append('K');
			if (move.isCapture())
				sb.append('x');
			sb.append(getFile(to));
			sb.append(getRank(to));
			return sb.toString();

		case W_PAWN:
			if (captured != Position.EMPTY) {
				sb.append(getFile(from));
			}

			break;
		case B_PAWN:
			if (captured != Position.EMPTY) {
				sb.append(getFile(from));
			}

			break;
		}

		if (Long.bitCount(tempMove) > 1) {
			logger.debug("bitCount(tempMove) > 1");
			if (Long.bitCount(tempMove & Bitboards.FILE[from]) == 1) {
				logger.debug("bitCount(tempmove & file) == 1");
				sb.append(getFile(from));
			} else if (Long.bitCount(tempMove & Bitboards.RANK[from]) == 1) {
				logger.debug("bitCount(tempmove & rank) == 1");
				sb.append(getRank(from));
			} else {
				sb.append(getFile(from));
				sb.append(getRank(from));
			}

		}

		if (captured != Position.EMPTY) {
			sb.append('x');

		}

		sb.append(getFile(to));
		sb.append(getRank(to));

		if (move.isEnpassant()) {
			sb.append(" e.p.");
		} else if (move.isPromotion()) {
			int prom = move.getProm();
			if (prom == W_QUEEN || prom == B_QUEEN) {
				sb.append("=Q");
			} else if (prom == W_KNIGHT || prom == B_KNIGHT) {
				sb.append("=N");
			} else if (prom == W_ROOK || prom == B_ROOK) {
				sb.append("=R");
			} else if (prom == W_BISHOP || prom == B_BISHOP) {
				sb.append("=B");
			}

		}

		return sb.toString();
	}

	public static char getFile(int square) {

		return (char) (1 + (square % 8) + 96);

	}

	public void setCastleWhite(byte castleWhite) {
		this.castleWhite = castleWhite;
	}

	public void setEpSquare(int epSquare) {
		this.epSquare = epSquare;
	}

	public void setCastleBlack(byte castleBlack) {
		this.castleBlack = castleBlack;
	}

	public void setNextMove(byte nextMove) {
		this.nextMove = nextMove;
	}

	public static char getRank(int square) {
		return (char) (1 + (square / 8) + 48);

	}

	// Read a move in UCIMoveFormat
	public Move readMove(String stringMove) {
		// null move
		if (stringMove.equals("0000")) {
			return null;
		}

		Move move = new Move();

		int fromSquare;
		int toSquare;

		int c = 0;

		// 97 is kleines 'a'
		int fromColumn = (int) stringMove.charAt(c) - 97;
		c++;
		// fromRow begins with 0
		int fromRow = (int) stringMove.charAt(c) - 49;

		fromSquare = fromRow * 8 + fromColumn;

		c++;
		int toColumn = (int) stringMove.charAt(c) - 97;
		c++;
		int toRow = (int) stringMove.charAt(c) - 49;
		toSquare = toRow * 8 + toColumn;

		move.setFrom(fromSquare);
		move.setTosq(toSquare);
		move.setPiec(square[fromSquare]);
		move.setCapt(square[toSquare]);

		if (nextMove == 1) {

			if (move.getPiec() == W_KING && Math.abs(move.getFrom() - move.getTosq()) == 2) {
				if (move.getFrom() - move.getTosq() > 0) {
					move.setMoveInt(Bitboards.WHITE_OOO_CASTL.getMoveInt());
				} else {
					move.setMoveInt(Bitboards.WHITE_OO_CASTL.getMoveInt());
				}
			}

			if (move.getPiec() == W_PAWN) {
				if (move.getCapt() == 0 && ((move.getFrom() - move.getTosq()) % 2) != 0) {
					move.setCapt(B_PAWN);
					move.setProm(W_PAWN);
				}

				if (Bitboards.RANKS[move.getTosq()] == 8) {
					c++;
					switch (stringMove.charAt(c)) {
					case 'q':
						move.setProm(W_QUEEN);
						break;
					case 'k':
						move.setProm(W_KNIGHT);
						break;
					case 'r':
						move.setProm(W_ROOK);
						break;
					case 'b':
						move.setProm(W_BISHOP);
						break;
					default:
						;
					}
				}

			}

		} else {

			if (move.getPiec() == B_KING && Math.abs(move.getFrom() - move.getTosq()) == 2) {
				if (move.getFrom() - move.getTosq() > 0) {
					move.setMoveInt(Bitboards.BLACK_OOO_CASTL.getMoveInt());
				} else {
					move.setMoveInt(Bitboards.BLACK_OO_CASTL.getMoveInt());
				}
			}

			if (move.getPiec() == B_PAWN) {
				if (move.getCapt() == 0 && ((move.getTosq() - move.getFrom()) % 2) != 0) {
					move.setCapt(W_PAWN);
					move.setProm(B_PAWN);
				}

				if (Bitboards.RANKS[move.getTosq()] == 1) {
					c++;
					switch (stringMove.charAt(c)) {
					case 'q':
						move.setProm(B_QUEEN);
						break;
					case 'k':
						move.setProm(B_KNIGHT);
						break;
					case 'r':
						move.setProm(B_ROOK);
						break;
					case 'b':
						move.setProm(B_BISHOP);
						break;
					default:
						;
					}

				}

			}

		}

		return move;
	}

	public boolean isCheck() {

		long tempMove = 0L;
		int from;
		byte fBitstate6;
		byte rBitstate6;
		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;

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

	public boolean isCheckmate() {
		if (!isCheck()) {
			return false;
		}

		List<Move> pseudoLegalMoves = pseudoLegalMoveGenerator();
		for (Move m : pseudoLegalMoves) {
			makeMove(m);
			if (!isOtherKingAttacked()) {
				unmakeMove(m);
				return false;
			}
			unmakeMove(m);
		}

		return true;
	}

	public boolean isStalemate() {
		if (isCheck()) {
			return false;
		}

		List<Move> pseudoLegalMoves = pseudoLegalMoveGenerator();
		for (Move m : pseudoLegalMoves) {
			makeMove(m);
			if (!isOtherKingAttacked()) {
				unmakeMove(m);
				return false;
			}
			unmakeMove(m);
		}

		return true;
	}

	public List<Move> getMovesOfPieceOn(int from) {
		List<Move> moves = pseudoLegalMoveGenerator();
		List<Move> result = new LinkedList<Move>();

		for (Move move : moves) {
			if (move.getFrom() == from && isMoveLegal(move))
				result.add(move);
		}

		return result;
	}

	public List<Move> pseudoLegalMoveGenerator() {

		Move move = new Move();
		long freeSquares = ~occupiedSquares;
		long targetBitboard;
		long tempMove, tempPiece;
		int to, from;
		byte fBitstate6;
		byte rBitstate6;
		byte diaga8h1Bitstate6;
		byte diaga1h8Bitstate6;

		List<Move> result = new LinkedList<Move>();

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
					result.add(new Move(move));
					move.setProm(W_ROOK);
					result.add(new Move(move));
					move.setProm(W_BISHOP);
					result.add(new Move(move));
					move.setProm(W_KNIGHT);
					result.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					result.add(new Move(move));
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((((getWhitePawns() & 65280L) << 8) & freeSquares) << 8) & freeSquares;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to - 16);
				move.setTosq(to);
				result.add(new Move(move));
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((getWhitePawns() & 9187201950435737471L) << 9);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare - 9);
					move.setTosq(epSquare);
					move.setCapt(B_PAWN);
					move.setProm(W_PAWN);
					result.add(new Move(move));
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
					result.add(new Move(move));
					move.setProm(W_ROOK);
					result.add(new Move(move));
					move.setProm(W_BISHOP);
					result.add(new Move(move));
					move.setProm(W_KNIGHT);
					result.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					result.add(new Move(move));
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
					result.add(new Move(move));
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
					result.add(new Move(move));
					move.setProm(W_ROOK);
					result.add(new Move(move));
					move.setProm(W_BISHOP);
					result.add(new Move(move));
					move.setProm(W_KNIGHT);
					result.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					result.add(new Move(move));
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
					result.add(new Move(move));
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
					result.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

				if (((castleWhite & CANCASTLEOO) != 0) && ((Bitboards.maskFG[0] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskEG[0])) {
					result.add(new Move(Bitboards.WHITE_OO_CASTL)); // predefined
				}

				// White 0-0-0 Castling:

				if (((castleWhite & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[0] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskCE[0])) {
					result.add(new Move(Bitboards.WHITE_OOO_CASTL)); // predefined

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
					result.add(new Move(move));
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
					result.add(new Move(move));
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
					result.add(new Move(move));
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
					result.add(new Move(move));
					move.setProm(B_ROOK);
					result.add(new Move(move));
					move.setProm(B_BISHOP);
					result.add(new Move(move));
					move.setProm(B_KNIGHT);
					result.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					result.add(new Move(move));
				}
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((((getBlackPawns() & ((65280L) << (5 * 8))) >>> 8) & freeSquares) >>> 8) & freeSquares;
			while (tempMove != 0) {
				to = Long.numberOfTrailingZeros(tempMove);
				move.setFrom(to + 16);
				move.setTosq(to);
				result.add(new Move(move));
				tempMove ^= Bitboards.BITSET[to];

			}

			tempMove = ((getBlackPawns() & 9187201950435737471L) >>> 7);
			if (epSquare != 0) { // do a quick check first
				if ((tempMove & Bitboards.BITSET[epSquare]) != 0) {
					move.setFrom(epSquare + 7);
					move.setTosq(epSquare);
					move.setCapt(W_PAWN);
					move.setProm(B_PAWN);
					result.add(new Move(move));
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
					result.add(new Move(move));
					move.setProm(B_ROOK);
					result.add(new Move(move));
					move.setProm(B_BISHOP);
					result.add(new Move(move));
					move.setProm(B_KNIGHT);
					result.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					result.add(new Move(move));
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
					result.add(new Move(move));
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
					result.add(new Move(move));
					move.setProm(B_ROOK);
					result.add(new Move(move));
					move.setProm(B_BISHOP);
					result.add(new Move(move));
					move.setProm(B_KNIGHT);
					result.add(new Move(move));
					move.setProm(EMPTY);
				} else {
					result.add(new Move(move));
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
					result.add(new Move(move));
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
					result.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}

				if (((castleBlack & CANCASTLEOO) != 0) && ((Bitboards.maskFG[1] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskEG[1])) {
					result.add(new Move(Bitboards.BLACK_OO_CASTL)); // predefined
				}

				if (((castleBlack & CANCASTLEOOO) != 0) && ((Bitboards.maskBD[1] & occupiedSquares) == 0)
						&& !isAttacked(Bitboards.maskCE[1])) {
					result.add(new Move(Bitboards.BLACK_OOO_CASTL)); // predefined
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
					result.add(new Move(move));
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
					result.add(new Move(move));
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
					result.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		}

		return result;

	}

}
