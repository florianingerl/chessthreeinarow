package Model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.florianingerl.util.regex.CaptureTreeNode;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import Persistence.OpeningBookManager;

public class Position {

	// This class only describes the internal representation of a position
	// and methods for making and umaking moves
	// and a method for checking wheter a move is legal

	private static Logger logger = LogManager.getLogger();

	public static final int EMPTY = 0;
	//public static final int W_PAWN = 1;
	//public static final int W_KING = 2;
	public static final int W_KNIGHT = 3;
	public static final int W_BISHOP = 5;
	public static final int W_ROOK = 6;
	//public static final int W_QUEEN = 7;
	//public static final int B_PAWN = 9;
	//public static final int B_KING = 10;
	public static final int B_KNIGHT = 11;
	public static final int B_BISHOP = 13;
	public static final int B_ROOK = 14;
	//public static final int B_QUEEN = 15;

	//public static final byte CANCASTLEOO = 1;
	//public static final byte CANCASTLEOOO = 1 << 1;

	//protected long whitePawns;
	protected long whiteKnights;
	protected long whiteBishops;
	protected long whiteRooks;
	//protected long whiteQueens;
	//protected long whiteKing;
	//protected long blackPawns;
	protected long blackKnights;
	protected long blackBishops;
	protected long blackRooks;
	//protected long blackQueens;
	//protected long blackKing;

	protected long whitePieces;
	protected long blackPieces;
	protected long occupiedSquares;

	protected int[] square = new int[64];

	//protected byte castleWhite = CANCASTLEOO + CANCASTLEOOO;
	//protected byte castleBlack = CANCASTLEOO + CANCASTLEOOO;
	protected byte nextMove = 1; // white or black Move
	//protected int epSquare = 0;
	//protected int fiftyMove = 0;

	protected GameLineRecord[] gameLine = new GameLineRecord[800];
	protected int endOfSearch = 0;

	//int numberOfPlayedMoves = 1;

	public Position() {

		//whitePawns = 65280L;

		//blackPawns = (65280L) << (5 * 8);

		//whiteKing = 16L;
		//blackKing = (16L) << (7 * 8);

		whiteKnights = 1L << (1+2*8);
		blackKnights = 1L << (1+5*8);

		whiteBishops = 1L << (6+1*8);
		blackBishops = (1L) << (6+ 6* 8);

		whiteRooks = 1L << 7;
		blackRooks = 1L << (7 + 7*8);

		//whiteQueens = 8L;
		//blackQueens = (8L) << (7 * 8);

		setWhiteAndBlackPieces();
		setOccupiedSquares();

		square[1+2*8] = W_KNIGHT;
		square[6+1*8] = W_BISHOP;
		square[7] = W_ROOK;
		
		square[1+5*8] = B_KNIGHT;
		square[6+ 6* 8] = B_BISHOP;
		square[7 + 7*8] = B_ROOK;

		constructGameLine();

		//numberOfPlayedMoves = 1;

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

		//whitePawns = 0L;
		//blackPawns = 0L;
		whiteKnights = 0L;
		blackKnights = 0L;
		whiteBishops = 0L;
		blackBishops = 0L;
		whiteRooks = 0L;
		blackRooks = 0L;
		//whiteQueens = 0L;
		//blackQueens = 0L;
		//whiteKing = 0L;
		//blackKing = 0L;

		// Remove later dependency on data!!!
		for (int i = 0; i < 64; i++) {

			switch (square[i]) {

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
			default:
				;
			}

		}

		setWhiteAndBlackPieces();
		setOccupiedSquares();
	}
	
	private void setWhiteAndBlackPieces() {
		whitePieces =  whiteKnights | whiteBishops | whiteRooks;
		blackPieces =  blackKnights | blackBishops | blackRooks;
	}
	
	private void setOccupiedSquares() {
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
		initializeEmptyBoard();
		
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
	
					case 'r':
						piece = B_ROOK;
						break;
					case 'n':
						piece = B_KNIGHT;
						break;
					case 'b':
						piece = B_BISHOP;
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
		
		/*castleWhite = 0;
		castleBlack = 0;

		epSquare = 0;
		fiftyMove = 0;
		numberOfPlayedMoves = 0;*/
		
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
			case 'r':
				square[sq] = B_ROOK;
				break;
			case 'n':
				square[sq] = B_KNIGHT;
				break;
			case 'b':
				square[sq] = B_BISHOP;
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

		//castleWhite = 0;
		//castleBlack = 0;
		// Initialize all castle possibilities

		/*
		if (fenTokens[2].contains("K"))
			castleWhite = (byte) (castleWhite | CANCASTLEOO);
		if (fenTokens[2].contains("Q"))
			castleWhite = (byte) (castleWhite | CANCASTLEOOO);
		if (fenTokens[2].contains("k"))
			castleBlack = (byte) (castleBlack | CANCASTLEOO);
		if (fenTokens[2].contains("q"))
			castleBlack = (byte) (castleBlack | CANCASTLEOOO);
			*/

		/*
		epSquare = 0;
		if (fenTokens[3].length() >= 2) {
			if ((fenTokens[3].charAt(0) >= 'a') && (fenTokens[3].charAt(0) <= 'h')
					&& ((fenTokens[3].charAt(1) == '3') || (fenTokens[3].charAt(1) == '6'))) {
				aFile = fenTokens[3].charAt(0) - 96; // ASCII 'a' = 97
				aRank = fenTokens[3].charAt(1) - 48; // ASCII '1' = 49
				epSquare = (aRank - 1) * 8 + aFile - 1;
			}

		}*/

		
		/*
		try {
			fiftyMove = Integer.valueOf(fenTokens[4]);
		} catch (NumberFormatException nfe) {

		}

		try {
			numberOfPlayedMoves = Integer.valueOf(fenTokens[5]);
		} catch (NumberFormatException nfe) {

		}
		*/

		endOfSearch = 0;

	}

	/*
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

	}*/

	
	/*
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

	}*/

	public void makeMove(Move move)

	{
		/*if (nextMove == -1)
			numberOfPlayedMoves++;*/

		int from = (int) move.getFrom();
		int to = (int) move.getTosq();
		int piece = (int) move.getPiec();

		//int captured = (int) move.getCapt();

		long fromBitMap = Bitboards.BITSET[from];

		long fromToBitMap = fromBitMap | Bitboards.BITSET[to];

		gameLine[endOfSearch].getMove().setMoveInt(move.getMoveInt());

		/*gameLine[endOfSearch].setCastleWhite(castleWhite);

		gameLine[endOfSearch].setCastleBlack(castleBlack);

		gameLine[endOfSearch].setFiftyMove(fiftyMove);

		gameLine[endOfSearch].setEpSquare(epSquare);*/

		endOfSearch++;

		switch (piece)

		{

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_KNIGHT;

			/*epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_BISHOP;

			/*epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
			occupiedSquares ^= fromToBitMap;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = W_ROOK;

			/*epSquare = 0;

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

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_KNIGHT;

			/*epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_BISHOP;

			/*epSquare = 0;

			fiftyMove++;

			if (captured != 0)

			{

				makeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = EMPTY;

			square[to] = B_ROOK;

			/*epSquare = 0;

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

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		}

		nextMove *= (-1);

	}

	/*protected void makeWhitePromotion(int prom, int to)

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

	}*/

	
	/*protected void unmakeBlackPromotion(int prom, int to)

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

	}*/

	
	/*protected void unmakeCapture(int captured, int to)

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

	}*/

	public void unmakeMove(Move move)

	{
		/*if (nextMove == 1)
			numberOfPlayedMoves--;*/

		int piece = (int) move.getPiec();

		//int captured = (int) move.getCapt();

		int from = (int) move.getFrom();

		int to = (int) move.getTosq();

		long fromBitMap = Bitboards.BITSET[from];

		long fromToBitMap = fromBitMap | Bitboards.BITSET[to];

		switch (piece)

		{

		case W_KNIGHT: // white knight:

			setWhiteKnights(getWhiteKnights() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_KNIGHT;

			square[to] = EMPTY;

			/*if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case W_BISHOP: // white bishop:

			setWhiteBishops(getWhiteBishops() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_BISHOP;

			square[to] = EMPTY;

			/*if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case W_ROOK: // white rook:

			setWhiteRooks(getWhiteRooks() ^ fromToBitMap);

			whitePieces ^= fromToBitMap;

			square[from] = W_ROOK;

			square[to] = EMPTY;

			/*if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		

		case B_KNIGHT: // black knight:

			setBlackKnights(getBlackKnights() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_KNIGHT;

			square[to] = EMPTY;

			/*if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case B_BISHOP: // black bishop:

			setBlackBishops(getBlackBishops() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_BISHOP;

			square[to] = EMPTY;

			/*if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;

		case B_ROOK: // black rook:

			setBlackRooks(getBlackRooks() ^ fromToBitMap);

			blackPieces ^= fromToBitMap;

			square[from] = B_ROOK;

			square[to] = EMPTY;

			/*if (captured != 0)

			{

				unmakeCapture(captured, to);

				occupiedSquares ^= fromBitMap;

			}

			else*/
				occupiedSquares ^= fromToBitMap;

			break;


		}

		if (endOfSearch > 0) {
			endOfSearch--;

			/*castleWhite = gameLine[endOfSearch].getCastleWhite();

			castleBlack = gameLine[endOfSearch].getCastleBlack();

			epSquare = gameLine[endOfSearch].getEpSquare();

			fiftyMove = gameLine[endOfSearch].getFiftyMove();*/
		}

		nextMove *= (-1);

	}

	/*protected void unmakeWhitePromotion(int prom, int to)

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

	}*/


	public int getNextMove() {
		return nextMove;
	}

	public int[] getSquare() {
		return square;
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

	
	public long getOccupiedSquares() {
		return occupiedSquares;
	}

	public void makeNullMove() {
		nextMove *= (-1);
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

		targetBitboard = ~occupiedSquares;
		
		if (nextMove == 1) {

			move.setPiec(W_KNIGHT);

			tempPiece = getWhiteKnights();
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					//move.setCapt(square[to]);
					moves.add(new Move(move));
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
					//move.setCapt(square[to]);
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
					//move.setCapt(square[to]);
					moves.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		} else {

			
			move.setPiec(B_KNIGHT);

			tempPiece = getBlackKnights();
			while (tempPiece != 0) {
				from = Long.numberOfTrailingZeros(tempPiece);
				move.setFrom(from);
				tempMove = Bitboards.KNIGHT_ATTACKS[from] & targetBitboard;
				while (tempMove != 0) {
					to = Long.numberOfTrailingZeros(tempMove);
					move.setTosq(to);
					//move.setCapt(square[to]);
					moves.add(new Move(move));
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
					//move.setCapt(square[to]);
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
					//move.setCapt(square[to]);
					moves.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		}

		/*if (moves.contains(m)) {

			makeMove(m);
			if (isOtherKingAttacked()) {
				unmakeMove(m);
				return false;
			}
			unmakeMove(m);
			return true;
		}*/
		return moves.contains(m);
		//return false;

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
		//move.setCapt(square[toSquare]);

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
		//m.setCapt(square[tosq]);

		return m;
	}

	public String getFenString() {
		return CalculateFenString.getFenString(this);
	}

	private void initializeEmptyBoard() {
		whiteKnights = 0L;
		blackKnights = 0L;

		whiteBishops = 0L;
		blackBishops = 0L;

		whiteRooks = 0L;
		blackRooks = 0L;

		//whiteQueens = 8L;
		//blackQueens = (8L) << (7 * 8);

		setWhiteAndBlackPieces();
		setOccupiedSquares();

		for(int i=0; i < 64; ++i) {
			square[i] = EMPTY;
		}

		
		nextMove = 1; // white or black Move

		endOfSearch = 0;
	}
	
	public void reset() {

		whiteKnights = 1L << (1+2*8);
		blackKnights = 1L << (1+5*8);

		whiteBishops = 1L << (6+1*8);
		blackBishops = (1L) << (6+ 6* 8);

		whiteRooks = 1L << 7;
		blackRooks = 1L << (7 + 7*8);

		//whiteQueens = 8L;
		//blackQueens = (8L) << (7 * 8);

		setWhiteAndBlackPieces();
		setOccupiedSquares();

		for(int i=0; i < 64; ++i) {
			square[i] = EMPTY;
		}
		square[1+2*8] = W_KNIGHT;
		square[6+1*8] = W_BISHOP;
		square[7] = W_ROOK;
		
		square[1+5*8] = B_KNIGHT;
		square[6+ 6* 8] = B_BISHOP;
		square[7 + 7*8] = B_ROOK;

		
		nextMove = 1; // white or black Move

		endOfSearch = 0;

	}

	public String toShortAlgebraicNotation(Move move) {

		StringBuilder sb = new StringBuilder();

		long tempMove = 0;
		int piece = move.getPiec();
		int to = move.getTosq();
		int from = move.getFrom();

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

		
		}


		sb.append(getFile(to));
		sb.append(getRank(to));

		return sb.toString();
	}

	public static char getFile(int square) {

		return (char) (1 + (square % 8) + 96);

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
		//move.setCapt(square[toSquare]);

		return move;
	}

	private static long checkmate1 = 1L | (1L << 1) | (1L << 2);
	private static long checkmate2 = 1L | (1L << 8) | (1L << 16);
			
	public boolean isCheckmate() {
		long pieces;
		if (nextMove == -1) {
			pieces = whitePieces;
		}
		else {
			pieces = blackPieces;
		}
		long l = pieces >> Long.numberOfTrailingZeros(pieces);
		if(l == checkmate2)
			return true;
		return l == checkmate1 && (Long.numberOfTrailingZeros(pieces)%8)<6;
		
	}

	public List<Move> getMovesOfPieceOn(int from) {
		List<Move> moves = getLegalMoves();
		List<Move> result = new LinkedList<Move>();

		for (Move move : moves) {
			if (move.getFrom() == from && isMoveLegal(move))
				result.add(move);
		}

		return result;
	}

	/**
	 * 
	 * @return null, if it's checkmate
	 */
	public List<Move> getLegalMoves() {

		if(isCheckmate()) {
			return null;
		}
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
					//move.setCapt(square[to]);
					result.add(new Move(move));
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
					//move.setCapt(square[to]);
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
					//move.setCapt(square[to]);
					result.add(new Move(move));
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
					//move.setCapt(square[to]);
					result.add(new Move(move));
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
					//move.setCapt(square[to]);
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
					//move.setCapt(square[to]);
					result.add(new Move(move));
					tempMove ^= Bitboards.BITSET[to];
				}
				tempPiece ^= Bitboards.BITSET[from];

			}

		}

		return result;

	}

}
