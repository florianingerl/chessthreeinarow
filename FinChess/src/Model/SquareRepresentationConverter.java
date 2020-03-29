package Model;

public class SquareRepresentationConverter {
	
	public static String getStringFromBit(int bit) {
	
		char[] square = new char[2];
	
		square[0] = getFileFromBit(bit);
		square[1] = getRankFromBit(bit);
		
		return new String(square);
	}
	
	public static int getBitFromString(String square) {
		int toColumn = getFileFromString(square);
		int toRow = getRankFromString(square);
	
		return (toRow * 8 + toColumn);
	}
	
	private static char getFileFromBit(int bit)
	{
		char result = 'a';
		result += (char)( bit % 8);
		return result;
	}
	
	private static char getRankFromBit(int bit)
	{
		char result = '1';
		result += (char)( bit/8 );
		return result;
	}
	
	private static int getFileFromString(String square)
	{
		return square.toCharArray()[0] - 'a';
	}
	private static int getRankFromString(String square)
	{
		return Integer.valueOf(square.substring(1)) - 1;
	}

}
