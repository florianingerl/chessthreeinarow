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
	public void parseMove_NormalPawnMovesInShortAlgebraicNotation() {

		Position position = new Position();

		Move move = position.parseMove("e4", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(12, move.getFrom());
		assertSame(28, move.getTosq());
		assertTrue(move.isPawnDoublemove());
		assertTrue(!move.isBlackmove());

		move = position.parseMove("b3", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(9, move.getFrom());
		assertSame(17, move.getTosq());
		assertFalse(move.isPawnDoublemove());
		assertTrue(move.isPawnmove());
		assertTrue(!move.isBlackmove());

	}

	@Test
	public void parseMove_NormalNightMovesInShortAlgebraicNotation() {
		Position position = new Position();

		Move move = position.parseMove("Nf3", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(6, move.getFrom());
		assertSame(21, move.getTosq());
		assertSame(Position.EMPTY, move.getCapt());
		assertTrue(move.isWhitemove());
		assertSame(Position.W_KNIGHT, move.getPiec());

	}

	@Test
	public void parseMove_CapturePawnMoveInShortAlgebraicNotation() {
		Position position = new Position();

		Move move = position.parseMove("e4", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);

		move = position.parseMove("d5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);

		move = position.parseMove("exd5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);

		assertSame(28, move.getFrom());
		assertSame(35, move.getTosq());
		assertSame(Position.B_PAWN, move.getCapt());
		assertFalse(move.isEnpassant());
		assertTrue(move.isWhitemove());
		assertSame(Position.W_PAWN, move.getPiec());
		assertTrue(move.isCapture());

	}

	@Test
	public void parseMove_CapturePawnMoveEnPassentInShortAlgebraicNotation() {
		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "e4", "e6", "e5", "f5" });

		Move move = position.parseMove("exf6e.p.", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(36, move.getFrom());
		assertSame(45, move.getTosq());
		assertTrue(move.isEnpassant());
		assertSame(Position.B_PAWN, move.getCapt());
		assertSame(Position.W_PAWN, move.getPiec());

	}

	@Test
	public void parseMove_NightMoveWithGivenFromRank() {
		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "d4", "d5", "Nf3", "Nf6" });

		Move move = position.parseMove("N1d2", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(1, move.getFrom());

		position.makeMove(move);

		move = position.parseMove("N6d7", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(45, move.getFrom());
		assertTrue(move.isBlackmove());

	}

	@Test
	public void parseMove_NightMoveWithGivenFromFile() {
		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "e3", "e6", "Ne2", "Ne7", "Na3", "Na6", "c3", "c6", "Nc2", "Nc7" });

		Move move = position.parseMove("Ncd4", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(10, move.getFrom());

		position.makeMove(move);

		move = position.parseMove("Ned5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertSame(52, move.getFrom());
		assertTrue(move.isBlackmove());

	}

	@Test
	public void parseMove_PromotionMoveWithCapture() {

		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "d4", "d5", "c4", "c6", "Nf3", "Nf6", "Nc3", "e6", "e3", "Nbd7",
				"Bd3", "dxc4", "Bxc4", "b5", "Bd3", "a6", "e4", "c5", "e5", "cxd4", "exf6", "dxc3", "fxg7", "cxb2" });
		Move move = position.parseMove("gxh8=Q", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertTrue(move.isPromotion());
		assertSame(Position.W_QUEEN, move.getProm());
		assertSame(Position.B_ROOK, move.getCapt());
		position.makeMove(move);

		move = position.parseMove("bxc1=N", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertTrue(move.isPromotion());
		assertSame(Position.B_KNIGHT, move.getProm());
		assertSame(Position.W_BISHOP, move.getCapt());

	}

	@Test
	public void getFenString_VariousPositionsFromTheStartingPosition() {
		Position position = new Position();

		LinkedList<Pair<Move, String>> movesAndFens = new LinkedList<Pair<Move, String>>();
		Pair<Move, String> pms = new Pair<Move, String>(null,
				"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		assertEquals(pms.second, position.getFenString());

		Move move = position.parseMove("e4", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		pms.first = move;
		movesAndFens.add(pms);
		position.makeMove(move);

		pms = new Pair<Move, String>(null, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("e5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("Nf3", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("Nc6", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("Bb5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3");
		assertEquals(pms.second, position.getFenString());

		move = position.parseMove("Nf6", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqkb1r/pppp1ppp/2n2n2/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("O-O", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqkb1r/pppp1ppp/2n2n2/1B2p3/4P3/5N2/PPPP1PPP/RNBQ1RK1 b kq - 5 4");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("Bc5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqk2r/pppp1ppp/2n2n2/1Bb1p3/4P3/5N2/PPPP1PPP/RNBQ1RK1 w kq - 6 5");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("Bxc6", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqk2r/pppp1ppp/2B2n2/2b1p3/4P3/5N2/PPPP1PPP/RNBQ1RK1 b kq - 0 5");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("bxc6", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqk2r/p1pp1ppp/2p2n2/2b1p3/4P3/5N2/PPPP1PPP/RNBQ1RK1 w kq - 0 6");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("Nxe5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		pms = new Pair<Move, String>(null, "r1bqk2r/p1pp1ppp/2p2n2/2b1N3/4P3/8/PPPP1PPP/RNBQ1RK1 b kq - 0 6");
		assertEquals(pms.second, position.getFenString());
		move = position.parseMove("O-O", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		position.makeMove(move);
		pms.first = move;
		movesAndFens.add(pms);

		assertEquals("r1bq1rk1/p1pp1ppp/2p2n2/2b1N3/4P3/8/PPPP1PPP/RNBQ1RK1 w - - 1 7", position.getFenString());

		while (!movesAndFens.isEmpty()) {
			Pair<Move, String> p = movesAndFens.removeLast();
			position.unmakeMove(p.first);
			assertEquals(p.second, position.getFenString());
		}

	}

	@Test
	public void isCheckmate_InFoolsMatePosition_ReturnsTrue() {
		Position position = new Position();
		assertFalse(position.isCheckmate());

		parseAndMakeMoves(position, new String[] { "f4", "e6", "g4", "Qh4" });
		assertTrue(position.isCheckmate());
		assertFalse(position.isStalemate());

	}

	@Test
	public void isCheckmate_InAPositionWhereItsJustCheck_ReturnsFalse() {
		Position position = new Position();
		assertFalse(position.isCheck());

		parseAndMakeMoves(position, new String[] { "f4", "e6", "e4", "Qh4" });
		assertTrue(position.isCheck());
		assertFalse(position.isStalemate());
		assertFalse(position.isCheckmate());

	}

	@Test
	public void getMove_NormalPawnMoves() {

		Position position = new Position();

		Move move = position.getMove(12, 28);

		assertTrue(move.isPawnDoublemove());
		assertTrue(move.isWhitemove());

		move = position.getMove(9, 17);

		assertFalse(move.isPawnDoublemove());
		assertTrue(move.isPawnmove());
		assertTrue(move.isWhitemove());

	}

	@Test
	public void getMove_CapturePawnMove() {
		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "e4", "d5" });

		Move move = position.getMove(28, 35);

		assertSame(Position.B_PAWN, move.getCapt());
		assertFalse(move.isEnpassant());
		assertTrue(move.isWhitemove());
		assertSame(Position.W_PAWN, move.getPiec());
		assertTrue(move.isCapture());

	}

	@Test
	public void getMove_CapturePawnMoveEnPassent() {
		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "e4", "e6", "e5", "f5" });

		Move move = position.getMove(36, 45);

		assertTrue(move.isEnpassant());
		assertSame(Position.B_PAWN, move.getCapt());
		assertSame(Position.W_PAWN, move.getPiec());

	}

	@Test
	public void getMove_PromotionMoveWithCapture() {

		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "d4", "d5", "c4", "c6", "Nf3", "Nf6", "Nc3", "e6", "e3", "Nbd7",
				"Bd3", "dxc4", "Bxc4", "b5", "Bd3", "a6", "e4", "c5", "e5", "cxd4", "exf6", "dxc3", "fxg7", "cxb2" });
		Move move = position.getMove(54, 63);
		assertTrue(move.isPromotion());
		assertSame(Position.W_QUEEN, move.getProm());
		assertSame(Position.B_ROOK, move.getCapt());
		position.makeMove(move);

		move = position.getMove(9, 2);
		assertTrue(move.isPromotion());
		assertSame(Position.B_QUEEN, move.getProm());
		assertSame(Position.W_BISHOP, move.getCapt());

	}

	@Test
	public void isMoveLegal_KingIsLeftInCheck_ReturnsFalse() {
		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "f4", "e6", "e4", "Qh4" });
		Move move = position.parseMove("e5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertFalse(position.isMoveLegal(move));

		move = position.parseMove("g3", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertTrue(position.isMoveLegal(move));

	}

	@Test
	public void getMove_ShortCastle() {

		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "e4", "e5", "Nf3", "Nf6", "Bc4", "Bc5" });
		Move move = position.getMove(4, 6);
		assertEquals(Bitboards.WHITE_OO_CASTL, move);
		position.makeMove(move);

		move = position.getMove(60, 62);
		assertEquals(Bitboards.BLACK_OO_CASTL, move);

	}

	@Test
	public void getMove_LongCastle() {

		Position position = new Position();

		parseAndMakeMoves(position, new String[] { "d4", "d5", "Nc3", "Nc6", "Bf4", "Bf5", "Qd2", "Qd7" });
		Move move = position.getMove(4, 2);
		assertEquals(Bitboards.WHITE_OOO_CASTL, move);
		position.makeMove(move);

		move = position.getMove(60, 58);
		assertEquals(Bitboards.BLACK_OOO_CASTL, move);

	}

	@Test
	public void isStalemate_InAStalematePosition_ReturnsTrue() {
		Position position = Position.fromFenString("1k6/1P6/1K6/8/8/8/8/8 b - - 30 100");
		assertTrue(position.isStalemate());
		assertFalse(position.isCheckmate());
		
		position = Position.fromPiecePlacements("bka8Qc7Kh1");
		assertTrue(position.isStalemate());
		assertFalse(position.isCheckmate() );
	}

	@Test
	public void getMovesOfPieceOn_InStartingPositionOfRightNight_ReturnsAllTheMoves() {
		Position position = new Position();
		List<Move> moves = position.getMovesOfPieceOn(6);

		assertSame(2, moves.size());
		assertTrue(moves.get(0).getTosq() == 23 || moves.get(0).getTosq() == 21);
		assertTrue(moves.get(1).getTosq() == 23 || moves.get(1).getTosq() == 21);
	}
	
	@Test
	public void toShortAlgebraicNotationTest()
	{
		String [] moves = {"d4", "d5", "c4", "c6", "Nf3", "Nf6", "Nc3", "e6", "e3", "Nbd7",
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
		
		toShortAlgebraicNotationTest(moves);
	}
	
	private void toShortAlgebraicNotationTest(String [] moves)
	{
		int i = 0;
		Position position = new Position();
		for(String m : moves)
		{
			Move move = position.parseMove(m, MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			assertEquals(moves[i++], position.toShortAlgebraicNotation(move));
			position.makeMove(move);
		}
	}

	public static void parseAndMakeMoves(Position position, String[] moves) {
		for (String m : moves) {
			Move move = position.parseMove(m, MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
			position.makeMove(move);
		}
	}

}
