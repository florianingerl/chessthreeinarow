package Model.UnitTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import Model.Bitboards;
import Model.Move;
import Model.MoveEncoding;
import Model.Pair;
import Model.Position;

public class PositionTest {

	@Test
	public void getFile_VariousTests() {

		assertSame('h', Position.getFile(63));
		assertSame('d', Position.getFile(3));
		assertSame('a', Position.getFile(16));
		assertSame('e', Position.getFile(28));
	}

	@Test
	public void getRank_VariousTests() {

		assertSame('8', Position.getRank(63));
		assertSame('1', Position.getRank(3));
		assertSame('3', Position.getRank(16));
		assertSame('4', Position.getRank(28));
	}


	@Test
	public void parseMove_NormalNightMovesInShortAlgebraicNotation() {
		Position position = new Position();

		Move move = position.parseMove("Nd2", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertTrue(move.isWhitemove());
		assertSame(Position.W_KNIGHT, move.getPiec());
		assertSame(17, move.getFrom());
		assertSame(11, move.getTosq());
		

	}

	
	
	@Test
	public void isCheckmate_InFoolsMatePosition_ReturnsTrue() {
		Position position = new Position();
		assertFalse(position.isCheckmate());

		
		parseAndMakeMoves(position, new String[] { "Be4", "Rh7", "Bb1", "Rh8", "Na1", "Rh7", "Rc1" });
		assertTrue(position.getSquare()[0] == Position.W_KNIGHT);
		
		assertTrue(position.isCheckmate());

	}
	
	@Test
	public void isCheckmate_InAnotherPosition_ReturnsTrue() {
		Position position = new Position();
		assertFalse(position.isCheckmate());

		
		parseAndMakeMoves(position, new String[] { "Be4", "Rh7", "Nd4", "Rh8", "Rf1", "Rh7", "Rf4" });
		
		assertTrue(position.isCheckmate());
	}
	
	@Test
	public void getMovesOfPieceOn_InStartingPositionOfRightNight_ReturnsAllTheMoves() {
		Position position = new Position();
		List<Move> moves = position.getMovesOfPieceOn(17);

		assertSame(6, moves.size());
	}
	
	@Test
	public void toShortAlgebraicNotationTest()
	{
		/*String [] moves = {"d4", "d5", "c4", "c6", "Nf3", "Nf6", "Nc3", "e6", "e3", "Nbd7",
		"Bd3", "dxc4", "Bxc4", "b5", "Bd3", "a6", "e4", "c5", "e5", "cxd4", "exf6", "dxc3", "fxg7", "cxb2"};
	
		toShortAlgebraicNotationTest(moves);
		
		moves = new String[]{  "e4", "c5",  "Nf3", "Nc6",  "Bb5", "g6",  "Bxc6", "dxc6",  "d3", "Nf6",  "h3", "Bg7",  "Nc3", "Nd7", 
				"Be3", "e5",  "Qd2", "Qe7",  "Bh6", "f6",  "Bxg7", "Qxg7",  "Nh2", "Nf8",  "f4", "exf4",  "Qxf4", "Ne6",
				 "Qh4", "Nd4",  "O-O-O", "O-O",  "Rdf1", "Be6",  "Ng4", "f5",  "Nh6", "Kh8",  "exf5", "Bxf5",
				 "g4", "Be6",  "g5", "Bf5",  "Rf2", "Rae8",  "Rhf1", "b5",  "Ne4", "Bxe4",  "Qxe4", "Nf5", 
				"Nxf5", "gxf5",  "Rxf5", "Qxg5",  "Rxg5", "Rxf1",  "Kd2", "Rxe4",  "dxe4", "Rf2",  "Ke3", "Rxc2",
				 "e5", "Rc1",  "Ke4", "Re1",  "Kf5", "c4",  "e6", "b4",  "Kf6", "Rf1",  "Ke7", "c3",  "bxc3",
				"bxc3",  "Rc5", "Rf3",  "h4", "h6",  "a4", "Kg7",  "a5", "Rh3",  "h5", "Rd3",  "Rxc6" };
		toShortAlgebraicNotationTest(moves);
		
		moves = new String[] {  "e4", "c5",  "Nc3", "e6",  "g3", "d5",  "d3", "d4",  "Nce2", "Nc6",  "Bg2", "e5",  "f4", "Bd6",  "Nf3",
				"Nge7",  "O-O", "Bg4",  "h3", "Bxf3",  "Rxf3", "Qc7",  "c3", "f6",  "cxd4", "exd4",  "Bd2", "O-O-O",
				 "Qb3", "Kb8",  "Rc1", "Rhe8",  "f5", "Ne5",  "Rff1", "Qb6",  "Qxb6", "axb6",  "Nf4", "g5", 
				"fxg6 e.p.", "N7xg6",  "Rcd1", "c4",  "Nxg6", "hxg6",  "dxc4", "Nxc4",  "Rxf6", "Bxg3",  "Rxg6", "Be5",
				 "b3", "Nxd2",  "Rxd2", "Rc8",  "Kf1", "Rc3",  "Ke2", "Re3",  "Kd1", "d3",  "Bf1", "Rd8", 
				"Rg5", "Bg3",  "Rxd3", "Rexd3",  "Bxd3", "Rxd3",  "Ke2", "Rc3",  "Kd2", "Rf3",  "Ke2", "Rc3", 
				"Kd2", "Bf4",  "Kxc3", "Bxg5",  "Kc4", "Kc7",  "Kb5", "Bh4",  "a4", "Be1",  "b4", "Bg3",  "a5", "Be1",
				 "Ka4", "Kc6",  "axb6", "Kd6",  "Kb5", "Ke5",  "Kc5", "Bf2" };
		
		toShortAlgebraicNotationTest(moves);*/
	}
	
	private void toShortAlgebraicNotationTest(String [] moves)
	{
		/*int i = 0;
		Position position = new Position();
		for(String m : moves)
		{
			Move move = position.parseMove(m, MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			assertEquals(moves[i++], position.toShortAlgebraicNotation(move));
			position.makeMove(move);
		}*/
	}

	public static void parseAndMakeMoves(Position position, String[] moves) {
		for (String m : moves) {
			Move move = position.parseMove(m, MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			position.makeMove(move);
		}
	}

}
