package Model;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

public class ShortAlgebraicMoveNotationParser {

	private Logger logger = Logger.getLogger(ShortAlgebraicMoveNotationParser.class);

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

		DetermineWhitePromotionPiece();
		DetermineWhetherItsAWhiteEnPassentCapture();

		DetermineUnambigousWhiteFromSquare();
		DetermineUnambigousFromSquare2();

		return theMove;
	}

	private Move ParseBlackMove() {
		DetermineBlackMovingPiece();
		DetermineUnambigiousFromFileOrRank();
		DetermineDestinationSquare();

		DetermineBlackPromotionPiece();
		DetermineWhetherItsABlackEnPassentCapture();
		
		DetermineUnambigousBlackFromSquare();
		DetermineUnambigousFromSquare2();

		return theMove;
	}

	private void DetermineUnambigousBlackFromSquare() {
		switch ((int) theMove.getPiec()) {
		case (int) BasicEngine.B_QUEEN: {
			// Tue so als ob weiße Dame auf destination square stehen würde
			// und schaue, wo sie hinziehen könnte
			targetBitboard = position.getBlackQueens();

			fBitstate6 = (byte) (((position.getOccupiedSquares() & Bitboards.FILEMASK[destination]) * Bitboards.FILEMAGIC[destination]) >>> 57);

			rBitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.RANKMASK[destination]) >>> Bitboards.RANKSHIFT[destination]);
			diaga8h1Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA8H1MASK[(int) destination])
					* Bitboards.DIAGA8H1MAGIC[destination] >>> 57);
			diaga1h8Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA1H8MASK[(int) destination])
					* Bitboards.DIAGA1H8MAGIC[destination] >>> 57);
			tempMove = (Bitboards.RANK_ATTACKS[destination][rBitstate6]
					| Bitboards.FILE_ATTACKS[destination][fBitstate6]
					| Bitboards.DIAGA1H8_ATTACKS[destination][diaga1h8Bitstate6] | Bitboards.DIAGA8H1_ATTACKS[destination][diaga8h1Bitstate6])
					& targetBitboard;

			break;

		}
		case (int) BasicEngine.B_ROOK: {

			targetBitboard = position.getBlackRooks();

			fBitstate6 = (byte) (((position.getOccupiedSquares() & Bitboards.FILEMASK[destination]) * Bitboards.FILEMAGIC[destination]) >>> 57);

			rBitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.RANKMASK[destination]) >>> Bitboards.RANKSHIFT[destination]);

			tempMove = (Bitboards.RANK_ATTACKS[destination][rBitstate6] | Bitboards.FILE_ATTACKS[destination][fBitstate6])
					& targetBitboard;

			break;
		}

		case (int) BasicEngine.B_BISHOP: {

			targetBitboard = position.getBlackBishops();

			diaga8h1Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA8H1MASK[(int) destination])
					* Bitboards.DIAGA8H1MAGIC[destination] >>> 57);
			diaga1h8Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA1H8MASK[(int) destination])
					* Bitboards.DIAGA1H8MAGIC[destination] >>> 57);
			tempMove = (Bitboards.DIAGA1H8_ATTACKS[destination][diaga1h8Bitstate6] | Bitboards.DIAGA8H1_ATTACKS[destination][diaga8h1Bitstate6])
					& targetBitboard;

			break;

		}

		case (int) BasicEngine.B_KNIGHT: {

			targetBitboard = position.getBlackKnights();

			tempMove = Bitboards.KNIGHT_ATTACKS[destination] & targetBitboard;
			break;

		}

		case (int) BasicEngine.B_KING: {

			targetBitboard = position.getBlackKing();

			tempMove = Bitboards.KING_ATTACKS[destination] & targetBitboard;
			break;

		}

		case (int) BasicEngine.B_PAWN: {

			if (!capture) {
				int i = 1;
				tempMove = 0L;
				while (tempMove == 0) {
					tempMove = position.getBlackPawns()
							& Bitboards.RANK[destination + i * 8]
							& Bitboards.FILE[destination];
					i++;
				}
			} else {
				int i = 1;
				tempMove = 0L;
				while (tempMove == 0) {
					tempMove = position.getBlackPawns() & Bitboards.FILE[fromColumn]
							& Bitboards.RANK[destination + i * 8];
					i++;
				}

			}

			break;

		}
		default:
			logger.debug("Unknown moving piece");
			throw new IllegalArgumentException("Unknown moving piece");

		}

		// Schneide tempMove mit rankmask, filemask, wenn nicht eindeutig
		if (fromColumn != -1) {
			tempMove &= Bitboards.FILE[fromColumn];
		}
		if (fromRow != -1) {
			tempMove &= Bitboards.RANK[fromRow * 8];
		}
		
	}

	private void DetermineWhetherItsABlackEnPassentCapture() {
		if (capture && theMove.getPiec() == BasicEngine.B_PAWN
				&& position.getSquare()[destination] == BasicEngine.EMPTY) {
			theMove.setCapt(BasicEngine.W_PAWN);
			theMove.setProm(BasicEngine.B_PAWN);
		} else {
			theMove.setCapt(position.getSquare()[destination]);

		}
		
	}

	private void DetermineBlackPromotionPiece() {
		if (index < charArray.length && charArray[index] == '=') {
			switch (charArray[index + 1]) {
			case 'N':
				theMove.setProm(BasicEngine.B_KNIGHT);
				break;
			case 'B':
				theMove.setProm(BasicEngine.B_BISHOP);
				break;
			case 'R':
				theMove.setProm(BasicEngine.B_ROOK);
				break;
			case 'Q':
				theMove.setProm(BasicEngine.B_QUEEN);
				break;

			default:
				logger.debug("Can't parse promotion piece");
				throw new IllegalArgumentException();

			}

		}

	}

	private void DetermineBlackMovingPiece() {
		if (Character.isUpperCase(charArray[0])) {

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
			case 'Q':
				theMove.setPiec(Position.B_QUEEN);
				break;
			case 'K':
				theMove.setPiec(Position.B_KING);
				break;
			default:
				logger.debug("Can't parse moving piece!");
				throw new IllegalArgumentException("Can't parse moving piece!");

			}
			index++;

		} else if (Character.isLowerCase(charArray[index]) && charArray[index + 1] == 'x') {

			theMove.setPiec(Position.B_PAWN);

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

			capture = true;

			index = index + 2;

		} else {
			theMove.setPiec(Position.B_PAWN);
		}

	}

	private void DetermineUnambigousFromSquare2() {
		if (Long.bitCount(tempMove) == 1) {
			theMove.setFrom(Long.numberOfTrailingZeros(tempMove));
		} else {
			while (tempMove != 0) {
				int from = Long.numberOfTrailingZeros(tempMove);
				theMove.setFrom(from);
				position.makeMove(theMove);
				if (!position.isOtherKingAttacked()) {
					position.unmakeMove(theMove);
					break;
				}
				position.unmakeMove(theMove);
				tempMove ^= Bitboards.BITSET[from];
			}
		}
	}

	private void DetermineUnambigousWhiteFromSquare() {

		switch ((int) theMove.getPiec()) {
		case (int) Position.W_QUEEN: {
			// Tue so als ob weiße Dame auf destination square stehen würde
			// und schaue, wo sie hinziehen könnte
			targetBitboard = position.getWhiteQueens();

			fBitstate6 = (byte) (((position.getOccupiedSquares() & Bitboards.FILEMASK[destination])
					* Bitboards.FILEMAGIC[destination]) >>> 57);

			rBitstate6 = (byte) ((position.getOccupiedSquares()
					& Bitboards.RANKMASK[destination]) >>> Bitboards.RANKSHIFT[destination]);
			diaga8h1Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA8H1MASK[(int) destination])
					* Bitboards.DIAGA8H1MAGIC[destination] >>> 57);
			diaga1h8Bitstate6 = (byte) ((position.getOccupiedSquares() & Bitboards.DIAGA1H8MASK[(int) destination])
					* Bitboards.DIAGA1H8MAGIC[destination] >>> 57);
			tempMove = (Bitboards.RANK_ATTACKS[destination][rBitstate6] | Bitboards.FILE_ATTACKS[destination][fBitstate6]
					| Bitboards.DIAGA1H8_ATTACKS[destination][diaga1h8Bitstate6]
					| Bitboards.DIAGA8H1_ATTACKS[destination][diaga8h1Bitstate6]) & targetBitboard;

			break;

		}
		case (int) Position.W_ROOK: {

			targetBitboard = position.getWhiteRooks();

			fBitstate6 = (byte) (((position.getOccupiedSquares() & Bitboards.FILEMASK[destination])
					* Bitboards.FILEMAGIC[destination]) >>> 57);

			rBitstate6 = (byte) ((position.getOccupiedSquares()
					& Bitboards.RANKMASK[destination]) >>> Bitboards.RANKSHIFT[destination]);

			tempMove = (Bitboards.RANK_ATTACKS[destination][rBitstate6] | Bitboards.FILE_ATTACKS[destination][fBitstate6])
					& targetBitboard;

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

		case (int) Position.W_KING: {

			targetBitboard = position.getWhiteKing();

			tempMove = Bitboards.KING_ATTACKS[destination] & targetBitboard;
			break;

		}

		case (int) Position.W_PAWN: {

			if (!capture) {
				int i = 1;
				tempMove = 0L;
				while (tempMove == 0) {

					tempMove = position.getWhitePawns() & Bitboards.RANK[destination - i * 8] & Bitboards.FILE[destination];
					i++;
				}
			} else {
				int i = 1;
				tempMove = 0L;
				while (tempMove == 0) {
					tempMove = position.getWhitePawns() & Bitboards.FILE[fromColumn] & Bitboards.RANK[destination - i * 8];
					i++;
				}

			}

			break;
		}
		default:
			logger.debug("Unknown moving piece!");
			throw new IllegalArgumentException("Unknown moving piece!");

		}

		// Schneide tempMove mit rankmask, filemask, wenn nicht eindeutig
		if (fromColumn != -1) {
			tempMove &= Bitboards.FILE[fromColumn];
		}
		if (fromRow != -1) {
			tempMove &= Bitboards.RANK[fromRow * 8];
		}
	}

	private void DetermineWhiteMovingPiece() {
		if (Character.isUpperCase(charArray[index])) {

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
			case 'Q':
				theMove.setPiec(Position.W_QUEEN);
				break;
			case 'K':
				theMove.setPiec(Position.W_KING);
				break;
			default:
				logger.debug("Can't parse promotion piece!");
				throw new IllegalArgumentException("Can't parse promotion piece!");
			}
			index++;

		} else if (Character.isLowerCase(charArray[index]) && charArray[index + 1] == 'x') {

			theMove.setPiec(Position.W_PAWN);

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

			capture = true;

			index = index + 2;

		} else {
			theMove.setPiec(Position.W_PAWN);
		}
	}

	private void DetermineWhitePromotionPiece() {
		if (index < charArray.length && charArray[index] == '=') {
			switch (charArray[index + 1]) {
			case 'N':
				theMove.setProm(Position.W_KNIGHT);
				break;
			case 'B':
				theMove.setProm(Position.W_BISHOP);
				break;
			case 'R':
				theMove.setProm(Position.W_ROOK);
				break;
			case 'Q':
				theMove.setProm(Position.W_QUEEN);
				break;

			default:
				System.out.println("Fehler!");

			}

		}
	}

	private void DetermineWhetherItsAWhiteEnPassentCapture() {
		if (capture && theMove.getPiec() == Position.W_PAWN && position.getSquare()[destination] == BasicEngine.EMPTY) {
			theMove.setCapt(Position.B_PAWN);
			theMove.setProm(Position.W_PAWN);
		} else {
			theMove.setCapt(position.getSquare()[destination]);
		}
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
