/**
 * 
 */
package UI.UnitTests;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import UI.Chessboard;

/**
 * @author Hermann
 *
 */
public class ChessboardTest{

	private static Logger logger = Logger.getLogger(ChessboardTest.class);
	/**
	 * @throws java.lang.Exception
	 */
	private Chessboard chessboard = null;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger.debug("setUp()");
		chessboard = new Chessboard();
	}

	/**
	 * Test method for {@link UI.Chessboard#parseSquareToBit(int, int)}.
	 */
	@Test
	public void testParseSquareToBit() {
		
		//fail("Not implemented!");
		int[] square = new int[]{5,1};
		assertEquals(chessboard.parseSquareToBit(square[0], square[1]),13);
		
		chessboard.flipBoard();
		
		square = new int[]{2,6};
		assertEquals(chessboard.parseSquareToBit(square[0], square[1]),13);
		
		
	}

	/**
	 * Test method for {@link UI.Chessboard#parseBitToSquare(int)}.
	 */
	@Test
	public void testParseBitToSquare() {
		
		
		int[] square = chessboard.parseBitToSquare(13);
		int[] shouldbe = new int[]{5,1};
		
		assertArrayEquals(shouldbe, square);
		
		chessboard.flipBoard();
		
		square = chessboard.parseBitToSquare(13);
		shouldbe = new int[]{2,6};
		
		assertArrayEquals(shouldbe, square);
		

	}
	
	


}
