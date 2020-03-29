package Model;

import java.util.HashMap;

/**
 * Calculates the Forsyth�Edwards Notation for a chess position
 * 
 * @author Florian Ingerl
 *
 */
public class CalculateFenString {

	private Position position;

	private StringBuilder fenString = new StringBuilder();

	public static HashMap<Integer, String> pieces;

	private CalculateFenString(Position position) {

		this.position = position;

		if (pieces == null) {
			pieces = new HashMap<Integer, String>(12);

			pieces.put(BasicEngine.W_KNIGHT, "N");
			pieces.put(BasicEngine.W_BISHOP, "B");
			pieces.put(BasicEngine.W_ROOK, "R");
			
			pieces.put(BasicEngine.B_KNIGHT, "n");
			pieces.put(BasicEngine.B_BISHOP, "b");
			pieces.put(BasicEngine.B_ROOK, "r");
		}

	}

	/**
	 * Calculate the Forsyth�Edwards Notation for the specified chess position
	 */
	public static String getFenString(Position position) {
		CalculateFenString cFS = new CalculateFenString(position);

		cFS.figurenStellung();
		cFS.amZug();
		cFS.rochade();
		cFS.enpassent();
		cFS.halbzuege();
		cFS.zugnummer();

		return cFS.getFenString();

	}

	private void figurenStellung() {

		final int[] square = position.getSquare();
		int numberOfEmptySquares = 0;

		for (int row = 8; row >= 1; row--) {

			for (int i = 8 * row - 8; i < 8 * row; i++) {
				if (square[i] == BasicEngine.EMPTY) {
					numberOfEmptySquares++;
					continue;
				} else if (numberOfEmptySquares > 0) {
					fenString.append(Integer.toString(numberOfEmptySquares));
					numberOfEmptySquares = 0;
				}

				fenString.append(pieces.get(square[i]));

			}
			if (numberOfEmptySquares != 0) {
				fenString.append(Integer.toString(numberOfEmptySquares));
				numberOfEmptySquares = 0;
			}
			fenString.append("/");

		}

		fenString.setCharAt(fenString.length() - 1, ' ');

	}

	private void amZug() {

		if (position.getNextMove() == 1) {
			fenString.append("w ");
		} else {
			fenString.append("b ");
		}

	}

	private void rochade() {
		
		fenString.append("-");

		fenString.append(" ");

	}

	private void enpassent() {

		fenString.append("- ");

	}

	/**
	 * This is the number of halfmoves since the last capture or pawn advance.
	 * This is used to determine if a draw can be claimed under the fifty-move
	 * rule
	 * 
	 * @return the number of halfmoves since the last capture of pawn advance
	 *         starting from 0.
	 */
	private String halbzuege() {

		fenString.append("1 ");
		return fenString.toString();

	}

	private void zugnummer() {

		fenString.append(Integer.toString(1));

	}

	private String getFenString() {
		return fenString.toString();
	}

}
