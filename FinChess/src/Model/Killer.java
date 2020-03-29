package Model;

/**
 * A killer is a particularly good move. If its spotted to be good in a certain
 * chess position during the move search, it will be considered first in similar
 * positions.
 * 
 * @author Florian Ingerl
 *
 */
public class Killer {

	private Move move;
	private int radicalChange;

	public Killer() {
		move = new Move();
		radicalChange = 0;
	}

	public Move getMove() {
		return move;
	}

	public void setMoveInt(int moveInt) {
		move.setMoveInt(moveInt);
	}

	/**
	 * 
	 * @return The change in the assessment of the position, that this move
	 *         brought about.
	 */
	public int getRadicalChange() {
		return radicalChange;
	}

	public void setRadicalChange(int radicalChange) {
		this.radicalChange = radicalChange;
	}

}
