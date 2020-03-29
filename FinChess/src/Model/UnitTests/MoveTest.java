package Model.UnitTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import Model.Move;
import Model.MoveEncoding;
import Model.Position;

public class MoveTest {

	@Test
	public void toUCIMoveNotation_VariousTests()
	{
		Move move = new Move();
		assertEquals("0000", move.toUCIMoveNotation());
		
		Position position = new Position();
		move = position.parseMove("e4", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		
		assertEquals("e2e4", move.toUCIMoveNotation());
		
		position.makeMove(move);
		move = position.parseMove("e5", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		
		assertEquals("e7e5", move.toUCIMoveNotation());
	}
	
	@Test
	public void toUCIMoveNotation_AllCastleMoves_ShouldReturnTheRightThing()
	{
		Position position = Position.fromFenString("r3k2r/pppq1ppp/2np1n2/2b1p1B1/2B1P1b1/2NP1N2/PPPQ1PPP/R3K2R w KQkq - 0 1");
		
		Move move = position.parseMove("O-O", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("e1g1", move.toUCIMoveNotation());
		
		move = position.parseMove("O-O-O", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("e1c1", move.toUCIMoveNotation());
		
		position.makeMove(move);
		
		move = position.parseMove("O-O", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("e8g8", move.toUCIMoveNotation());
		
		move = position.parseMove("O-O-O", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("e8c8", move.toUCIMoveNotation());
		
		
	}
	
	@Test
	public void toUCIMoveNotation_PromotionMoves_ShouldReturnTheRightThing()
	{
		Position position = Position.fromFenString("r1bqkb1r/3n1pPp/p3p3/1p6/8/3B1N2/Pp3PPP/R1BQK2R w KQkq - 0 1");
		
		Move move = position.parseMove("gxh8=Q", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("g7h8q", move.toUCIMoveNotation());
		move = position.parseMove("gxf8=R", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("g7f8r", move.toUCIMoveNotation());
		move = position.parseMove("g8=B", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("g7g8b", move.toUCIMoveNotation());
		move = position.parseMove("gxh8=N", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("g7h8k", move.toUCIMoveNotation());
		
		
		position.makeMove(move);
		
		move = position.parseMove("bxa1=Q", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("b2a1q", move.toUCIMoveNotation());
		move = position.parseMove("bxc1=R", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("b2c1r", move.toUCIMoveNotation());
		move = position.parseMove("b1=B", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("b2b1b", move.toUCIMoveNotation());
		move = position.parseMove("bxa1=N", MoveEncoding.SHORT_ALGEBRAIC_NOTATION);
		assertEquals("b2a1k", move.toUCIMoveNotation());
	}

}
