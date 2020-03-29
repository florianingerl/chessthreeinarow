package Model.UnitTests;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import Model.Move;
import Model.MoveEncoding;
import Model.BasicEngine;
import Model.Position;

public class BasicEngineTest {

	private BasicEngine engine;

	@Before
	public void initialize() {
		engine = new BasicEngine();
	}

	@Test
	public void getBestMove_InFoolsMatePosition_FindsTheCheckmate() {

		engine.setPosition(getListOfMoves(new String[] { "Be4", "Rh7", "Nd4", "Rh8", "Rf1", "Rh7" }));

		System.out.println(engine.getFenString());
		assertFalse(engine.isCheckmate());
		
		engine.setDepth(3);
		engine.findBestMove();
		Move move = engine.getBestMove();
		assertNotNull(move);
		assertSame(Position.W_ROOK, move.getPiec());
		assertSame(5+3*8, move.getTosq());
		

	}

	@Test
	public void stop_InStartingPosition_FindsSomeMove() {
		try {
			engine.setPosition(new LinkedList<Move>());

			engine.setDepth(500);

			new Thread() {
				@Override
				public void run() {
					engine.findBestMove();
				}
			}.start();
			Thread.sleep(100);
			engine.stop();

			Thread.sleep(1000);
			Move move = engine.getBestMove();
			assertTrue(move != null);

		} catch (InterruptedException ie) {
			fail("Should never happen!");
		}
	}

	

	
	
	


	@Test
	public void getBestMove_CanDrawWithA3TimesRepetitionInAnOtherwiseHopelessPosition_FindsTheBestMove() {
		/*List<Move> moves = getListOfMoves(new String[] { "e4", "e5", "g3", "Qh4", "gxh4", "Nh6", "Nh3", "Ng8", "Ng1",
				"Nh6", "Nh3", "Ng8", "Ng1" });
		engine.setPosition(moves);
		engine.setDepth(3);
		
		engine.findBestMove();
		Move move = engine.getBestMove();
		
		
		assertSame(62, move.getFrom());
		assertSame(55, move.getTosq());*/
	}

	private List<Move> getListOfMoves(String[] movesInShortAlgebraicNotation) {
		Position position = new Position();

		List<Move> moves = new LinkedList<Move>();

		for (String m : movesInShortAlgebraicNotation) {
			Move move = position.parseMove(m, MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			moves.add(move);
			position.makeMove(move);
		}

		return moves;
	}

}
