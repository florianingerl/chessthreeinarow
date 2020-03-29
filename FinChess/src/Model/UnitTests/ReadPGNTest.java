package Model.UnitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import Model.Move;
import Model.MoveEncoding;
import Model.PgnGame;
import Model.Position;
import Model.ReadPGN;
import Model.VariationNode;
import Model.VariationTree;

public class ReadPGNTest {

	@Test
	public void getListOfGames_OfKramnikVsSanan_ReturnsOnlyOneGame() {

		try {
			ReadPGN rpn = new ReadPGN(new File("portals_3_files_2014_kataropen_kramniksjugirov.pgn"));
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
	public void getListOfGames_WithAPgnFileContainingThreeGames() {
		try {
			ReadPGN rpn = new ReadPGN(new File("12Neckar-Open2009(2).pgn"));
			List<PgnGame> listOfGames = rpn.getListOfGames();

			assertSame(3, listOfGames.size());
			Iterator<PgnGame> it = listOfGames.iterator();

			PgnGame game = it.next();
			assertEquals("Fedorchuk, Sergey", game.getWhite());
			assertEquals("Farmani Anosheh, Armin", game.getBlack());

			game = it.next();
			assertEquals("Valner, Uku", game.getWhite());
			assertEquals("Korneev, Oleg", game.getBlack());

			game = it.next();
			assertEquals("Graf, Alexander", game.getWhite());
			assertEquals("Schuler, Marc", game.getBlack());

		} catch (IOException e) {
			fail("File doesn't exist!");
		}

	}

}
