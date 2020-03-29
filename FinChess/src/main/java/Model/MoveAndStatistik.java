package Model;

/**
 * Represents an entry in a chess opening book.
 * 
 * @author Florian Ingerl
 *
 */
public class MoveAndStatistik {

	private Move move;
	private String weight;

	public MoveAndStatistik() {

	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

}
