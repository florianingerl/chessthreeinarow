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

	private class PositionAndPossibleMoves {
		String position;
		int knightMoves;
		int bishopMoves;
		int rookMoves;
		PositionAndPossibleMoves(String position, int knightMoves, int bishopMoves, int rookMoves){
			this.position = position;
			this.knightMoves = knightMoves;
			this.bishopMoves = bishopMoves;
			this.rookMoves = rookMoves;
		}
	}
	
	@Test
	public void getLegalMoves_InVariousPositions_ReturnsTheCorrectNumberOfMovesOfEachPiece() {
		List<PositionAndPossibleMoves> list = new LinkedList<PositionAndPossibleMoves>();
		
		list.add(new PositionAndPossibleMoves("wNc4Bd4Re2re5nd3bc3", 7, 6 , 10));
		list.add(new PositionAndPossibleMoves("wNc4Rd4Bd2rd3nd5be5", 6,9,4));
		list.add(new PositionAndPossibleMoves("wNd5Rd4Be4be5nf5rg7", 8,6,6));
		list.add(new PositionAndPossibleMoves("wBc6Rd6Nd4bb4nd5re5", 7,6,6));
		list.add(new PositionAndPossibleMoves("wRc4Nd4Bc3bd3rd2nf2", 8,4,6));
		list.add(new PositionAndPossibleMoves("wRb5Bd5Ne4rc4nd4bc3", 7,6,9));
		list.add(new PositionAndPossibleMoves("bRd4Bd5Nf5rc4bf4nf2", 6,11,9));
		list.add(new PositionAndPossibleMoves("brd6bc4nd3Rc5Bd5Nc6", 7,4,6));
		list.add(new PositionAndPossibleMoves("wBc3Ne4Re3bc4rd4nd2", 6,4,6));
		list.add(new PositionAndPossibleMoves("bng5rb5ba6Bb4Nb2Rh3", 5,2,8));
		list.add(new PositionAndPossibleMoves("brh4bg4nc6Rb4Nb2Bg8", 7,9,7));
		list.add(new PositionAndPossibleMoves("bnd3bc5rh5Nc4Rd4Bc6", 7,7,11));
		
		list.add(new PositionAndPossibleMoves("wNb3Bb1Rh1nc4bd4rh8",5,7,11));
		list.add(new PositionAndPossibleMoves("wNc2Bd4Rd5nc5bf4rh3",5,10,7));
		list.add(new PositionAndPossibleMoves("bre3be4nc5Nc4Be5Rd6",7,13,9));
		list.add(new PositionAndPossibleMoves("brd4bd3ne5Bd5Ne4Re3",7,7,3));
		list.add(new PositionAndPossibleMoves("wRc5Bd5Nd4bb5ne5re4",7,9,7));
		list.add(new PositionAndPossibleMoves("brd3nd5bf5Re4Ne1Bc3",7,7,7));
		list.add(new PositionAndPossibleMoves("wRd6Bd5Nb5bc5rd4ne4",4,9,9));
		list.add(new PositionAndPossibleMoves("brc5nc4bd4Rd5Bc3Ne2",8,7,5));
		list.add(new PositionAndPossibleMoves("bre5nc4bb4Bb3Re4Ne6",7,9,7));
		list.add(new PositionAndPossibleMoves("bre5nc4bb4Nb5Rd4Bb3",7,9,12));
		list.add(new PositionAndPossibleMoves("brf4be4nd5Bd4Ne5Rd6",7,9,9));
		list.add(new PositionAndPossibleMoves("wRc4Bd4Nd5bb4re4ne5",7,9,7));
		
		
		for(PositionAndPossibleMoves pm : list) {
			Position position = Position.fromPiecePlacements(pm.position);
			List<Move> moves = position.getLegalMoves();
			int knightMoves = 0;
			int bishopMoves = 0;
			int rookMoves = 0;
			for(Move move : moves) {
				if(move.getPiec() == Position.W_KNIGHT || move.getPiec() == Position.B_KNIGHT)
					++knightMoves;
				else if(move.getPiec() == Position.W_BISHOP || move.getPiec() == Position.B_BISHOP)
					++bishopMoves;
				else if(move.getPiec() == Position.W_ROOK || move.getPiec() == Position.B_ROOK)
					++rookMoves;
			}
			System.out.println("Knight moves " + knightMoves);
			
			assertSame(pm.knightMoves, knightMoves );
			assertSame(pm.bishopMoves, bishopMoves );
			assertSame(pm.rookMoves, rookMoves );
		}
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
	public void isCheckmate_InALotOfDifferentCheckmatePositions_ReturnsTrue() {
		Position position = Position.fromPiecePlacements("wrd4bd3nd2Bc3Re3Nf2");
		assertTrue(position.isCheckmate());
		position = Position.fromPiecePlacements("wBd4Nc4Rd6nd3re3bf3");
		assertTrue(position.isCheckmate());
		position = Position.fromPiecePlacements("bRb4Bb3Nb2na5bg4rh4");
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
