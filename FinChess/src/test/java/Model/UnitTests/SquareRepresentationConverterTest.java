package Model.UnitTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import Model.SquareRepresentationConverter;

public class SquareRepresentationConverterTest {

	@Test
	public void getStringFromBit_VariousTests() {
		assertEquals("a1", SquareRepresentationConverter.getStringFromBit(0));
		assertEquals("b2", SquareRepresentationConverter.getStringFromBit(9));
		assertEquals("c3", SquareRepresentationConverter.getStringFromBit(18));
		assertEquals("d4", SquareRepresentationConverter.getStringFromBit(27));
		assertEquals("e5", SquareRepresentationConverter.getStringFromBit(36));
		assertEquals("f6", SquareRepresentationConverter.getStringFromBit(45));
		assertEquals("g7", SquareRepresentationConverter.getStringFromBit(54));
		assertEquals("h8", SquareRepresentationConverter.getStringFromBit(63));
	}

	
	@Test
	public void getBitFromString_VariousTests()
	{
		assertSame(0, SquareRepresentationConverter.getBitFromString("a1"));
		assertSame(9, SquareRepresentationConverter.getBitFromString("b2"));
		assertSame(18, SquareRepresentationConverter.getBitFromString("c3"));
		assertSame(27, SquareRepresentationConverter.getBitFromString("d4"));
		assertSame(36, SquareRepresentationConverter.getBitFromString("e5"));
		assertSame(45, SquareRepresentationConverter.getBitFromString("f6"));
		assertSame(54, SquareRepresentationConverter.getBitFromString("g7"));
		assertSame(63, SquareRepresentationConverter.getBitFromString("h8"));
	
	}
}
