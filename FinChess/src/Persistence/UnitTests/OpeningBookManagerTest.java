package Persistence.UnitTests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import Persistence.OpeningBookManager;

import Model.Move;
import Model.MoveAndStatistik;
import Model.Position;

public class OpeningBookManagerTest {

	@Test
	public void getMoveStatistiks_InStartingPosition_ReturnsAllCommonMoves() {
		
		
		try {
			List<MoveAndStatistik> mas = OpeningBookManager.getInstance().getMoveStatistiks(new Position());
		
			List<String> moves = new LinkedList<String>();
			for(MoveAndStatistik m : mas )
			{
				moves.add( m.getMove().toString());
			}
			
			assertTrue( moves.contains("Be2-e4") );
			assertTrue( moves.contains("Bd2-d4"));
			assertTrue( moves.contains("Sg1-f3"));
			assertTrue( moves.contains("Bc2-c4") );
			assertTrue( moves.contains("Bg2-g3"));
			assertTrue( moves.contains("Bb2-b3"));
			assertTrue( moves.contains("Sb1-c3"));
			assertTrue( moves.contains("Bf2-f4"));
			
		} catch (IOException e) {
			fail("IOException occured!");
		}
		
	}

}
