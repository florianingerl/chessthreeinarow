package UI;

import Model.Move;

public interface HumanMoveGetter {

	// should be a legal move!
	public Move getMove() throws InterruptedException;
	
	
}
