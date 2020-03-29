package Model;

import javax.management.RuntimeErrorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShortAlgebraicMoveNotationParser {

	private static Logger logger = LogManager.getLogger();

	private Position position;

	private String move;

	Move theMove = new Move();

	private int index = 0;
	private int fromColumn = -1;
	private int fromRow = -1;

	private int toColumn;
	private int toRow;

	private long tempMove = 0L;
	private byte fBitstate6;
	private byte rBitstate6;
	private byte diaga8h1Bitstate6;
	private byte diaga1h8Bitstate6;

	private long targetBitboard;

	private boolean capture = false;

	// We start by the moveing piece
	private char[] charArray;

	private int destination;

	public ShortAlgebraicMoveNotationParser(Position position) {
		this.position = position;
	}

	public Move parseMove(String move) {
		this.move = move;
		Move theMove = IfCastleReturnCastleMove();

		if (theMove != null)
			return theMove;

		charArray = move.toCharArray();
		theMove = new Move();

		if (position.getNextMove() == 1) {
			return ParseWhiteMove();
		} else {
			return ParseBlackMove();
		}

	}

	private Move IfCastleReturnCastleMove() {
		// It's important that castle long is treated first
		if (move.startsWith("O-O-O")) {
			if (position.getNextMove() == 1) {
				return new Move(Bitboards.WHITE_OOO_CASTL);
			} else {
				return new Move(Bitboards.BLACK_OOO_CASTL);
			}

		}

		if (move.startsWith("O-O")) {
			if (position.getNextMove() == 1) {
				return new Move(Bitboards.WHITE_OO_CASTL);
			} else {
				return new Move(Bitboards.BLACK_OO_CASTL);
			}

		}
		return null;

	}

	private Move ParseWhiteMove() {
		DetermineWhiteMovingPiece();

		DetermineUnambigiousFromFileOrRank();
		DetermineDestinationSquare();

		DetermineUnambigousWhiteFromSquare();
		DetermineUnambigousFromSquare2();

		return theMove;
	}

	private Move ParseBlackMove() {
		DetermineBlackMovingPiece();
		DetermineUnambigiousFromFileOrRank();
		DetermineDestinationSquare();


		DetermineUnambigousBlackFromSquare();
		DetermineUnambigousFromSquare2();

		return theMove;
	}

	private void DetermineUnambigousBlackFromSquare() {
		switch ((int) theMove.getPiec()) {

		case (int) BasicEngine.B_ROOK: {

			targetBitboard = position.getBlackRooks();

			fBitstate6 = (byte) (((position.getOccupiedSquares() & Bitboards.FILEMASK[destination])
					* Bitboards.FILEMAGIC[destination]) >>> 57);

			rBitstate6 = (byte) ((position.getOccupiedSquares()
					& Bitboards.RANKMASK[destination]) >>> Bitboards.RANKSHIFT[destination]);

			tempMove = (Bitboards.RANK_ATTACKS[destination][rBitstate6]
					| Bitboards.FILE_ATTACKS[destination][fBitstate6]) & targetBitboard;

			break;
		}

		case (int) BasicEngine.B_BISHOP: {

			targetBitboard = position.getBlackBishops();

			diaga8h1Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA8H1MASK[(int) destination])
					* Bitboards.DIAGA8H1MAGIC[destination] >>> 57);
			diaga1h8Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA1H8MASK[(int) destination])
					* Bitboards.DIAGA1H8MAGIC[destination] >>> 57);
			tempMove = (Bitboards.DIAGA1H8_ATTACKS[destination][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[destination][diaga8h1Bitstate6]) & targetBitboard;

			break;

		}

		case (int) BasicEngine.B_KNIGHT: {

			targetBitboard = position.getBlackKnights();

			tempMove = Bitboards.KNIGHT_ATTACKS[destination] & targetBitboard;
			break;

		}

		default:
			logger.debug("Unknown moving piece");
			throw new IllegalArgumentException("Unknown moving piece");

		}

	}

	private void DetermineBlackMovingPiece() {

		switch (charArray[index]) {
		case 'N':
			theMove.setPiec(Position.B_KNIGHT);
			break;
		case 'B':
			theMove.setPiec(Position.B_BISHOP);
			break;
		case 'R':
			theMove.setPiec(Position.B_ROOK);
			break;

		default:
			logger.debug("Can't parse moving piece!");
			throw new IllegalArgumentException("Can't parse moving piece!");

		}
		index++;

	}

	private void DetermineUnambigousFromSquare2() {
		theMove.setFrom(Long.numberOfTrailingZeros(tempMove));
	}

	private void DetermineUnambigousWhiteFromSquare() {

		switch ((int) theMove.getPiec()) {

		case (int) Position.W_ROOK: {

			targetBitboard = position.getWhiteRooks();

			fBitstate6 = (byte) (((position.getOccupiedSquares() & Bitboards.FILEMASK[destination])
					* Bitboards.FILEMAGIC[destination]) >>> 57);

			rBitstate6 = (byte) ((position.getOccupiedSquares()
					& Bitboards.RANKMASK[destination]) >>> Bitboards.RANKSHIFT[destination]);

			tempMove = (Bitboards.RANK_ATTACKS[destination][rBitstate6]
					| Bitboards.FILE_ATTACKS[destination][fBitstate6]) & targetBitboard;

			break;
		}

		case (int) Position.W_BISHOP: {

			targetBitboard = position.getWhiteBishops();

			diaga8h1Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA8H1MASK[(int) destination])
					* Bitboards.DIAGA8H1MAGIC[destination] >>> 57);
			diaga1h8Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA1H8MASK[(int) destination])
					* Bitboards.DIAGA1H8MAGIC[destination] >>> 57);
			tempMove = (Bitboards.DIAGA1H8_ATTACKS[destination][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[destination][diaga8h1Bitstate6]) & targetBitboard;

			break;

		}

		case (int) Position.W_KNIGHT: {

			targetBitboard = position.getWhiteKnights();

			tempMove = Bitboards.KNIGHT_ATTACKS[destination] & targetBitboard;
			break;

		}

		default:
			logger.debug("Unknown moving piece!");
			throw new IllegalArgumentException("Unknown moving piece!");

		}

	}

	private void DetermineWhiteMovingPiece() {

		switch (charArray[index]) {
		case 'N':

			theMove.setPiec(Position.W_KNIGHT);
			break;
		case 'B':
			theMove.setPiec(Position.W_BISHOP);
			break;
		case 'R':
			theMove.setPiec(Position.W_ROOK);
			break;
		default:
			logger.debug("Can't parse promotion piece!");
			throw new IllegalArgumentException("Can't parse promotion piece!");
		}
		index++;

	}


	private void DetermineUnambigiousFromFileOrRank() {
		boolean destinationSquare = true;

		if (!(Character.isLowerCase(charArray[index]) && Character.isDigit(charArray[index + 1]))) {
			destinationSquare = false;
		}

		if (!destinationSquare) {

			// gibt die Spalte an!
			if (Character.isLowerCase(charArray[index])) {
				switch (charArray[index]) {
				case 'a':
					fromColumn = 0;
					break;
				case 'b':
					fromColumn = 1;
					break;
				case 'c':
					fromColumn = 2;
					break;
				case 'd':
					fromColumn = 3;
					break;
				case 'e':
					fromColumn = 4;
					break;
				case 'f':
					fromColumn = 5;
					break;
				case 'g':
					fromColumn = 6;
					break;
				case 'h':
					fromColumn = 7;
					break;

				}

				index++;

			} else if (Character.isDigit(charArray[index])) {
				fromRow = Integer.valueOf(Character.toString(charArray[index])) - 1;
				// Row begins with 1, do not change
				index++;
			}

			if (charArray[index] == 'x') {
				capture = true;
				index++;
			}

		}
	}

	private void DetermineDestinationSquare() {
		DetermineDestinationFile();
		DetermineDestinationRank();
		destination = toRow * 8 + toColumn;

		if (!(destination >= 0 && destination < 64)) {
			logger.debug("Destination wrong " + destination);
			throw new IllegalArgumentException("Destination wrong " + destination);
		}

		theMove.setTosq(destination);
	}

	private void DetermineDestinationFile() {
		// now comes the destination square

		switch (charArray[index]) {
		case 'a':
			toColumn = 0;
			break;
		case 'b':
			toColumn = 1;
			break;
		case 'c':
			toColumn = 2;
			break;
		case 'd':
			toColumn = 3;
			break;
		case 'e':
			toColumn = 4;
			break;
		case 'f':
			toColumn = 5;
			break;
		case 'g':
			toColumn = 6;
			break;
		case 'h':
			toColumn = 7;
			break;
		default:
			toColumn = -1;
			logger.debug("Can't parse destination square");
			throw new IllegalArgumentException("Can't parse destination square");

		}

		index++;

	}

	private void DetermineDestinationRank() {
		toRow = Integer.valueOf(Character.toString(charArray[index])) - 1;
		index++;
	}

}
