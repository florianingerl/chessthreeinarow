package Model;

import java.util.List;



/**
 * Basic interface for a chess engine.
 * @author Hermann
 *
 */
public interface IEngine {

	/**
	 * Stops the move search as soon as possible, still
	 * finding a reasonable move.
	 */
	public void stop();
	
	/**
	 * When this method returns, {@link #getBestMove()} returns
	 * the best move found by this chess engine.
	 */
	public void findBestMove();
	
	/**
	 * 
	 * @return the best move found by {@link #findBestMove()}
	 */
	public Move getBestMove();
	
	public void setDepth(int depth);

	/**
	 * Sets the position to the given Forsyth�Edwards Notation string.
	 * @param fen Forsyth�Edwards Notation string
	 */
	public void setPositionFromFen(String fen);
	
	public void setPosition(List<Move> movesFromStartingPosition);
	
	public void quit();
	
	
}
