package Model.UnitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import Model.PgnGame;
import Model.Position;
import Model.ReadPGN;
import Model.VariationNode;
import Model.VariationTree;

public class VariationTreeTest {

	private static Logger logger = LogManager.getLogger();
	
	@Test
	public void getPositionFromCaretTest() {
		

		getPositionFromCaretTest(false);
		
		
	}
	
	@Test
	public void getPositionFromCaretSANTest() {
		
		getPositionFromCaretTest(true);
		
		
	}
	
	private void getPositionFromCaretTest(boolean san) {
		try {
			ReadPGN rpn = new ReadPGN(getClass().getClassLoader().getResourceAsStream("portals_3_files_2014_kataropen_kramniksjugirov.pgn") );
			List<PgnGame> listOfGames = rpn.getListOfGames();
		
			VariationTree vt = listOfGames.get(0).getGame();
			int caret;
			if(san)
			{
				caret = vt.toStringSAN().indexOf("axb6");
			}
			else
			{
				caret = vt.toString().indexOf("Ba5-b6");
			}
			
			
			List<Integer> path = san ? vt.getPositionFromCaretSAN(caret + 1) : vt.getPositionFromCaret(caret + 1);
			VariationNode currentNode = vt.getRoot();
			
			Position position = new Position();
			while(!currentNode.isLastMove())
			{
				currentNode = currentNode.getVariation(path.remove(0));
				position.makeMove( currentNode.getMove() );
			}
			
			PositionTest.parseAndMakeMoves(position, new String[]{"Ne3", "Bb3", "Qxg5", "Kh2", "Qg2"});
			
			logger.debug(position.toString());
			assertTrue(position.isCheckmate());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fail("Should not happen!");
		}
	}

}
