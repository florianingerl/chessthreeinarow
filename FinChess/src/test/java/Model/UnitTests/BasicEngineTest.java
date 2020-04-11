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

	private class PositionAndBestMove {
		String move;
		String position;
		PositionAndBestMove(String position, String move) {
			this.position = position;
			this.move = move;
		}
	}
	
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
	public void getBestMove_CheckmatesInOneMove_FindsTheCheckmate() {
		
		List<PositionAndBestMove> list = new LinkedList<PositionAndBestMove>();
		list.add(new PositionAndBestMove("wNc4Bd4Re2bc3nd3re5", "Te2-e4"));
		list.add(new PositionAndBestMove("wBd2Rd4Nc4rd3nd5be5", "Ld2-b4"));
		list.add(new PositionAndBestMove("wBc6Rd6Nd4bb4nd5re5", "Sd4-e6"));
		list.add(new PositionAndBestMove("wRb5Bd5Ne4rc4nd4bc3","Se4-c5"));
		
		checkTheseBestMovesAreFound(list);
						
	}
	
	@Test
	public void getBestMove_HasToPreventCheckmateInOne_FindsTheDefence() {
		List<PositionAndBestMove> list = new LinkedList<PositionAndBestMove>();
		list.add(new PositionAndBestMove("brc4nf2bf4Rd4Bd5Nf5", "Lf4-d6"));
		list.add(new PositionAndBestMove("bNc6Rc5Bd5rd6bc4nd3", "Sd3-e5"));
		list.add(new PositionAndBestMove("bBc6bc5Nc4Rd4nd3rh5", "Th5-d5"));
		list.add(new PositionAndBestMove("bnc6bg4rh4Bg8Rb4Nb2", "Lg4-e6"));
		list.add(new PositionAndBestMove("bng5ba6rb5Bb4Nb2Rh3", "Sg5-f3"));
		
		//Only this test still fails
		list.add(new PositionAndBestMove("wBc3Re3Ne4bc4rd4nd2", "Te3-d3"));
		
		checkTheseBestMovesAreFound(list);
	}
	
	@Test
	public void getBestMove_CheckmateInTwoIsPossible_FindsTheFirstMove() {
		List<PositionAndBestMove> list = new LinkedList<PositionAndBestMove>();
		list.add(new PositionAndBestMove("bnd5be4rf4Rd6Ne5Bd4", "Sd5-f6"));
		list.add(new PositionAndBestMove("wNb3Bb1Rh1nc4bd4rh8", "Th1-d1"));
		list.add(new PositionAndBestMove("bnc5be4re3Nc4Rd6Be5", "Sc5-d3"));
		list.add(new PositionAndBestMove("bne5rd4bd3Bd5Ne4Re3", "Se5-c4"));
		list.add(new PositionAndBestMove("wRc5Bd5Nd4ne5re4bb5", "Tc5-c6"));
		list.add(new PositionAndBestMove("wNb5Bd5Rd6bc5rd4ne4", "Td6-b6"));
		list.add(new PositionAndBestMove("bbb4nc4re5Bb3Re4Ne6", "Te5-c5"));
		
		
		checkTheseBestMovesAreFound(list);
	}

	private void checkTheseBestMovesAreFound(List<PositionAndBestMove> list) {
		for(PositionAndBestMove pm : list) {
			engine.setPositionFromPiecePlacements(pm.position);
			
			System.out.println(engine.getFenString());
			assertFalse(engine.isCheckmate());
			
			engine.setDepth(3);
			engine.findBestMove();
			Move move = engine.getBestMove();
			assertNotNull(move);
			assertEquals( pm.move ,move.toString() );
		}
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
