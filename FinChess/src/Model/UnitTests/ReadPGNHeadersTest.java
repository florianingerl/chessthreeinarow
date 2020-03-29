package Model.UnitTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import Model.Move;
import Model.MoveEncoding;
import Model.PgnGame;
import Model.Position;
import Model.ReadPGNHeaders;
import Model.VariationNode;
import Model.VariationTree;

public class ReadPGNHeadersTest {

	private Logger logger = Logger.getLogger(ReadPGNHeadersTest.class);
	
	@Test
	public void getListOfGames_OfKramnikVsSanan_ReturnsOnlyOneGame() {

		try {
			ReadPGNHeaders rpn = new ReadPGNHeaders(new FileInputStream("portals_3_files_2014_kataropen_kramniksjugirov.pgn"));
			rpn.parseHeaders();
			List<PgnGame> games = rpn.getListOfGames();
			assertSame(1, games.size());

			PgnGame game = games.get(0);

			assertEquals("Kramnik, Vladimir", game.getWhite());
			assertEquals("Sjugirov, Sanan", game.getBlack());
			assertEquals(2760, game.getEloWhite());
			assertEquals(2673, game.getEloBlack());

			assertEquals("Qatar Masters Open 2014", game.getEvent());
			assertEquals("1-0", game.getResult());

			VariationTree vt = game.getGame();
			VariationNode currentNode = vt.getRoot();

			Position position = new Position();
			while (!currentNode.isLastMove()) {
				currentNode = currentNode.getVariation(0);
				position.makeMove(currentNode.getMove());

			}

			Move move = position.parseMove("Nxf6", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			position.makeMove(move);

			move = position.parseMove("gxf6", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			position.makeMove(move);

			move = position.parseMove("Qxb3", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			position.makeMove(move);

			move = position.parseMove("Qxg7", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			position.makeMove(move);

			assertTrue(position.isCheckmate());

		} catch (IOException e) {
			fail("Could not parse pgn games!");
		}
	}
	
	@Test
	public void getListOfGames_OfAlkhines2Nc3d5Games_ReturnsALotOfGames() {
	{
		try {
			ReadPGNHeaders rpn = new ReadPGNHeaders(new FileInputStream("Alekhine2Nc3-d5.pgn"));
			rpn.parseHeaders();
			List<PgnGame> games = rpn.getListOfGames();
			
			PgnGame game = games.get(0);

			assertEquals("Hanham, James Moore", game.getWhite());
			assertEquals("Delmar, Eugene", game.getBlack());

			assertEquals("Manhattan", game.getEvent());
			assertEquals("1-0", game.getResult());

			game.loadGame();
			VariationTree vt = game.getGame();
			VariationNode currentNode = vt.getRoot();

			Position position = new Position();
			while (!currentNode.isLastMove()) {
				currentNode = currentNode.getVariation(0);
				position.makeMove(currentNode.getMove());

			}
			logger.debug( position.getFenString());
			assertTrue( position.getFenString().startsWith("8/1p5R/1kp5/8/1P6/7P/8/6K1") );


		} catch (IOException e) {
			fail("Could not parse pgn games!");
		}
		

	}
	
	

	}
	
	@Test
	public void getListOfGames_OfAPgnDatabaseThatHasAGameWithTheFenTag_TheGameIsParsedCorrectly()
	{
		
		try {
			ReadPGNHeaders rpn = new ReadPGNHeaders(new FileInputStream("inline.pgn"));
			rpn.parseHeaders();
			PgnGame game = rpn.getListOfGames().getFirst();
			
			Position position = game.getInitialPosition();
			assertEquals("1R6/7k/4bP1p/4N1p1/3pr3/1P6/3K4/8 w - - 0 54", position.getFenString() );
			
			VariationTree vTree = game.getGame();
			VariationNode currentNode = vTree.getRoot();
			
			while( !currentNode.isLastMove() )
			{
				currentNode = currentNode.getVariation(0);
				position.makeMove(currentNode.getMove());
			}
			
			assertEquals("1k6/1P6/2K5/6p1/8/8/7N/8 b - - 0 81", position.getFenString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} 
		
	}
	
}
