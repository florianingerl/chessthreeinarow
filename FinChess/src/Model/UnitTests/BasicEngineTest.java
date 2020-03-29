package Model.UnitTests;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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

		engine.setPosition(getListOfMoves(new String[] { "f4", "e6", "g4" }));

		engine.setDepth(3);
		engine.findBestMove();
		Move move = engine.getBestMove();
		assertNotNull(move);
		assertSame(Position.B_QUEEN, move.getPiec());
		assertSame(31, move.getTosq());

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
	public void getBestMove_CanWinQueenForRookAndBishop_FindsTheBestMove() {
		engine.setPositionFromFen("4rrk1/pp1q2bn/3p2p1/2pP1p2/PnP4B/2NB1Q1P/1P4P1/R4RK1 b - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertNotNull(move);
		assertSame(60, move.getFrom());
		assertSame(20, move.getTosq());
	}

	/*
	 * @Test public void getBestMove_CanWinQueenForAKnight_FindsTheBestMove() {
	 * engine.setPosition(
	 * "r4rqk1/1b2bppp/pp2pn2/4N1N1/2Pp4/3B4/PP2QPPP/3RR1K1 w - - 0 1");
	 * 
	 * engine.setDepth(6); engine.findBestMove();
	 * 
	 * Move move = engine.getBestMove(); //assertSame(36, move.getFrom());
	 * assertSame(Position.W_KNIGHT, move.getPiec()); assertSame(51,
	 * move.getTosq());
	 * 
	 * }
	 */

	@Test
	public void getBestMove_CanWinTheQueenForARook_FindsTheBestMoves() {
		engine.setPositionFromFen("6k1/ppr1r1b1/4pBQ1/4P2p/7P/2P2R2/P4PPK/3q4 w - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertNotNull(move);
		assertSame(45, move.getFrom());
		assertSame(54, move.getTosq());

	}

	@Test
	public void getBestMove_CanWinTheQueenForABishop_FindsTheBestMove() {
		engine.setPositionFromFen("rnb1kb1r/ppp2ppp/2qp4/3N4/4PQn1/8/PPP2PPP/R1B1KBNR w KQkq - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertNotNull(move);
		assertSame(33, move.getTosq());
		assertSame(5, move.getFrom());
	}

	@Test
	public void getBestMove_VeryLongCombination_FindsTheBestMove() {
		engine.setPositionFromFen("4r1k1/1pb3pp/p1p2p2/6r1/1P1PP2n/P6q/2Q2RP1/2B1RNK1 b - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertNotNull(move);

		assertSame(38, move.getFrom());
		assertSame(14, move.getTosq());
	}

	@Test
	public void getBestMove_CanWinQueenForRookAndKnight_FindsTheBestMove() {
		engine.setPositionFromFen("2r5/q4kp1/3Rp1Np/3b1p1P/8/4P3/1Q3PP1/6K1 w - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertNotNull(move);

		assertSame(43, move.getFrom());
		assertSame(51, move.getTosq());
	}

	@Test
	public void getBestMove_CanWinARook_FindsTheBestMove() {
		engine.setPositionFromFen("4/r2k/pR5p/6p1/4b3/3B4/2P2K2/1P6/8 w - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertNotNull(move);

		assertSame(49, move.getFrom());
		assertSame(57, move.getTosq());
	}

	@Test
	public void getBestMove_CanWinARookWithAQueenCheck_FindsTheBestMove() {
		engine.setPositionFromFen("4r1k1/3R1ppp/5q2/8/3QP3/2p2P2/6PP/6K1 b - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		String principalVariation = engine.printPrincipalVariation();

		assertTrue(principalVariation.startsWith("Bc3-c2"));
	}

	@Test
	public void getBestMove_SpectacularQueenSacrificeWinsARook_FindsTheBestMove() {
		engine.setPositionFromFen("1rb2bk1/p5pp/1nn1p3/q2pP1B1/2pP2B1/1pP3P1/PP1Q3P/RN3NK1 b - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertSame(8, move.getTosq());
		assertSame(Position.B_QUEEN, move.getPiec());

	}

	@Test
	public void getBestMove_CanWinTheQueenForABishopAndARookWithASpeer_FindsTheBestMove() {
		engine.setPositionFromFen("3r2k1/pRp2p2/6pQ/5b2/2B5/2qP2P1/P6P6K1 w - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertSame(53, move.getTosq());
		assertSame(26, move.getFrom());
	}

	@Test
	public void getBestMove_CanWinTheQueenForARookWithASpear_FindsTheBestMove() {
		engine.setPositionFromFen("1r4k1/6p1/4O2p/2p2p2/P3q3/4P3/2R2P1P/2Q2K2 b - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertSame(1, move.getTosq());
		assertSame(Position.B_ROOK, move.getPiec());
	}

	@Test
	public void getBestMove_CanWinABishopWithASpectacularQueenSacrifice_FindsTheBestMove() {
		engine.setPositionFromFen("B1b5/2q5/p2kp3/4b1Q1/8/4B1P1/PPP4P/6K1 w - - 0 1");
		engine.setDepth(6);

		engine.findBestMove();
		Move move = engine.getBestMove();
		assertSame(36, move.getTosq());
		assertSame(38, move.getFrom());
	}

	@Test
	public void getBestMove_CanWinTheQueenForTwoKnights_FindsTheBestMove() {
		engine.setPositionFromFen("r2qr1k1/bpp2pp1/p5np/7N/4Np2/1bPP1Q1P/PP3PP1/R3R1K1 w - - 0 1");
		engine.setDepth(6);

		engine.findBestMove();
		Move move = engine.getBestMove();

		assertSame(28, move.getFrom());
		assertSame(45, move.getTosq());

	}

	@Test
	public void getBestMove_CanForkTwoRooks_FindsTheBestMove() {
		engine.setPositionFromFen("5rk1/r4qb1/3p2n1/2pN2P1/4P1QP/1P6/P2R1PK1/4R3 b - - 0 1");
		engine.setDepth(6);

		engine.findBestMove();
		Move move = engine.getBestMove();

		assertSame(21, move.getTosq());
		assertSame(53, move.getFrom());
	}

	@Test
	public void getBestMove_CanForkBishopAndRook_FindsTheBestMove() {
		engine.setPositionFromFen("1rr3k1/4Bpbp/3p2p1/8/q1p5/P5P1/1P1QPP1P/1RR3K1 b - - 0 1");
		engine.setDepth(6);

		engine.findBestMove();
		Move move = engine.getBestMove();

		assertSame(26, move.getFrom());
		assertSame(18, move.getTosq());
	}

	@Test
	public void getBestMove_CanWinQueenForRookAndKnightWithAFork_FindsTheBestMove() {
		engine.setPositionFromFen("4rrn1/3qp2p/p1p1R1pk/8/8/2Q2N1P/PP3PP1/4R1K1 w - - 0 1");

		engine.setDepth(6);
		engine.findBestMove();

		Move move = engine.getBestMove();
		assertSame(44, move.getFrom());
		assertSame(46, move.getTosq());
	}

	@Test
	public void getBestMove_NastyQueenForksAround_FindsTheBestMove() {
		engine.setPositionFromFen("r4rk1/pp2ppbp/5n2/2p4q/P4B1N/6QP/1PP3P1/3R1RK1 w - - 0 1");
		engine.setDepth(6);

		engine.findBestMove();
		Move move = engine.getBestMove();

		assertSame(29, move.getFrom());
		assertSame(47, move.getTosq());
	}

	@Test
	public void setPosition_WithAListOfMoves_ShouldLoadTheRightPosition() {
		List<Move> moves = getListOfMoves(new String[] { "e4", "c5", "Nc3", "Nc6", "f4", "g6", "Nf3", "Bg7", "Bb5",
				"Nd4", "O-O", "Nxb5", "Nxb5", "Qb6", "a4", "d6", "d3", "Bd7", "Na3", "Nh6", "Nc4", "Qc7", "f5", "O-O",
				"Qe1", "d5", "Nce5", "dxe4", "dxe4", "gxf5", "Qg3", "Ng4", "Bf4", "Qc8", "Nxd7", "Qxd7", "Rad1", "Qc8",
				"exf5", "Qxf5", "h3", "Nf6", "Nh4", "Qh5" });

		engine.setPosition(moves);

		engine.setDepth(6);

		engine.findBestMove();
		Move move = engine.getBestMove();

		assertSame(29, move.getFrom());
		assertSame(47, move.getTosq());

	}
	
	@Test(timeout = 20000)
	public void getBestMove_WhenThePositionIsSetWithAFenString_ReturnsSomeLegalMove()
	{
		engine.setPositionFromFen("8/1R5k/4bP1p/4N1p1/3pr3/1P6/3K4/8 b - - 1 54");
		engine.setDepth(6);
		engine.findBestMove();
		Move move = engine.getBestMove();
		assertSame(Position.B_KING, move.getPiec());
		
	}

	@Test
	public void getBestMove_CanDrawWithA3TimesRepetitionInAnOtherwiseHopelessPosition_FindsTheBestMove() {
		List<Move> moves = getListOfMoves(new String[] { "e4", "e5", "g3", "Qh4", "gxh4", "Nh6", "Nh3", "Ng8", "Ng1",
				"Nh6", "Nh3", "Ng8", "Ng1" });
		engine.setPosition(moves);
		engine.setDepth(3);
		
		engine.findBestMove();
		Move move = engine.getBestMove();
		
		/*
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
