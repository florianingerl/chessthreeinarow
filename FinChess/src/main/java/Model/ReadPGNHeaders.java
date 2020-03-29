package Model;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
//import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadPGNHeaders {

	private static Logger logger = LogManager.getLogger();
	
	private String pgnGames;
	private LinkedList<PgnGame> listOfGames = new LinkedList<PgnGame>();
	private PgnGame currentGame;
	
	public ReadPGNHeaders(InputStream in) throws IOException
	{
		pgnGames = IOUtils.toString(in, ReadPGN.PGN_ENCODING);
	}
	
	public void parseHeaders()
	{
		Pattern pattern = Pattern.compile("(\\[\\s*[A-Z][A-Za-z0-9_]*\\s*\"(\\\\.|[^\"\\\\])*\"\\s*\\]\\s*)+");
		Matcher matcher = pattern.matcher(pgnGames);
		
		while(matcher.find())
		{
			if( listOfGames.size() > 0)
			{
				listOfGames.getLast().setEndIndex(matcher.start());
			}
			
			currentGame = new PgnGame();
			currentGame.setPgnDatabase(pgnGames);
			currentGame.setStartIndex(matcher.start());
			
			parseHeaders(pgnGames.substring(matcher.start(), matcher.end()));
			
			listOfGames.add(currentGame);
		}
		if(listOfGames.size() > 0)
		{
			listOfGames.getLast().setEndIndex(pgnGames.length() );
		}
	}

	public LinkedList<PgnGame> getListOfGames() {
		return listOfGames;
	}
	
	private void parseHeaders(String headers)
	{
		
		Pattern pattern = Pattern.compile("\\[\\s*(?<tagname>[A-Z][A-Za-z0-9_]*)\\s*\"(?<tagvalue>(\\\\.|[^\"\\\\])*)\"\\s*\\]");
		Matcher matcher = pattern.matcher(headers);
		
		while(matcher.find())
		{
			String tag = matcher.group("tagname");
			String value = matcher.group("tagvalue");
			
			if ("White".equals(tag)) {
				currentGame.setWhite(value);
			} else if ("Black".equals(tag)) {
				currentGame.setBlack(value);
			} else if ("WhiteElo".equals(tag) && value.matches("^\\d+$") ) {
				currentGame.setEloWhite(Integer.valueOf(value));
			} else if ("BlackElo".equals(tag) && value.matches("^\\d+$")) {
				currentGame.setEloBlack(Integer.valueOf(value));
			} else if ("Event".equals(tag)) {
				currentGame.setEvent(value);
			} else if ("Result".equals(tag)) {
				currentGame.setResult(value);
			} else if( "FEN".equals(tag) ){
				currentGame.setFenString(value);
			}
			
		}
	}
	
	
}
