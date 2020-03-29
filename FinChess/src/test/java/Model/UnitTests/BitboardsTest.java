package Model.UnitTests;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import Model.Bitboards;

public class BitboardsTest {

	private static Logger logger = LogManager.getLogger();
	
	@Test
	public void KNIGHT_ATTACKS_OnTheCornersOfTheBoard_ThereAreOnlyTwoKnightMoves() {
		
		int [] corners = new int[]{0,7,56,63};
		for(int corner : corners )
		{
			assertSame(2, Long.bitCount(Bitboards.KNIGHT_ATTACKS[corner]) );
		}
	}
	
	@Test
	public void KNIGHT_ATTACKS_OnTheSquareNextToTheCorner_ThereAreOnlyThreeMoves()
	{
		int [] nextToCorners = new int[]{1,8,6,15,48,57,62,55};
		for(int nextToCorner : nextToCorners)
		{
			assertSame(3, Long.bitCount(Bitboards.KNIGHT_ATTACKS[nextToCorner]));
		}
	}
	
	@Test
	public void KNIGHT_ATTACKS_WhenMovesArePossibleOnlyInForwardDirection_ThereAreOnlyFourMoves()
	{
		int [] knightPositions = new int[]{2,3,4,5,16,24,32,40,58,59,60,61,23,31,39,47};
		for(int knightPosition : knightPositions )
		{
			assertSame(4, Long.bitCount(Bitboards.KNIGHT_ATTACKS[knightPosition]));
		}
	}
	
	@Test
	public void KNIGHT_ATTACKS_WhenTheKnightHasMostOptions_ThereAreEightMoves()
	{
		for(int knightPosition = 0; knightPosition < 64; ++knightPosition)
		{
			int row = knightPosition / 8;
			int column = knightPosition % 8;
			if( row >= 2 && row <= 5 && column >=2 && column <= 5)
			{
				assertSame( 8, Long.bitCount(Bitboards.KNIGHT_ATTACKS[knightPosition]) );
			}
		}
	}

	@Test
	public void KNIGHT_ATTACKS_WhenTheKnightIsOnTheDiagonalNeighbourOfACorner_ThereAreOnlyFourMoves()
	{
		int [] knightPositions = new int[]{9, 14,54,49};
		for(int knightPosition : knightPositions)
		{
			assertSame( 4, Long.bitCount(Bitboards.KNIGHT_ATTACKS[knightPosition]));
		}
	}
	
	@Test
	public void KNIGHT_ATTACKS_WhenTheKnightIsBesideTheAdvancedCenter_ThereAreSixMoves()
	{
		int [] knightPositions = new int[]{10,11,12,13,17,25,33,41,22,30,38,46,50,51,52,53};
		for(int knightPosition : knightPositions)
		{
			assertSame( 6, Long.bitCount(Bitboards.KNIGHT_ATTACKS[knightPosition]));
		}
	}
	
	@Test
	public void KING_ATTACKS_WhenTheKingIsInOneOfTheCorners_ThereAreOnlyThreeMoves()
	{
		int [] kingPositions = new int[]{0,7,56,63};
		for(int kingPosition : kingPositions)
		{
			assertSame(3, Long.bitCount(Bitboards.KING_ATTACKS[kingPosition]));
		}
	}
	
	@Test
	public void KING_ATTACKS_WhenTheKingCanMoveInEveryDirection_ThereAreEightOptions()
	{
		for(int kingPosition = 0; kingPosition < 64; ++kingPosition )
		{
			int row = kingPosition / 8;
			int column = kingPosition % 8;
			if( row >= 1 && row <= 6 && column >= 1 && column <= 6)
			{
				assertSame(8, Long.bitCount(Bitboards.KING_ATTACKS[kingPosition]) );
			}
		}
	}
	
	@Test
	public void KING_ATTACKS_WhenTheKingIsOnTheEdgeOfTheBoard_ThereAreFiveMoves()
	{
		for(int kingPosition = 0; kingPosition < 64; ++kingPosition )
		{
			int row = kingPosition / 8;
			int column = kingPosition % 8;
			if( (row >= 1 && row <= 6 ) ^ (column >= 1 && column <= 6) )
			{
				assertSame(5, Long.bitCount(Bitboards.KING_ATTACKS[kingPosition]) );
			}
		}
	}
	
	
}
