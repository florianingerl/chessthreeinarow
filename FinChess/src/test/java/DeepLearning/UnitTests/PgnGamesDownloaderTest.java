package DeepLearning.UnitTests;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;

import DeepLearning.PgnGamesDownloader;

public class PgnGamesDownloaderTest {

	@Test
	public void PATTERN_DOWNLOAD_LINKS_test()
	{
		String toTest = "<a href=\"events/FideChamp1998.pgn\" class=\"view\">Download</a> Hello <a href=\"players/Adams.zip\" class=\"view\">Download</a>";
		
		Matcher matcher = PgnGamesDownloader.PATTERN_DOWNLOAD_LINKS.matcher(toTest);
		assertTrue( matcher.find());
		
		assertEquals( "events/FideChamp1998.pgn" , matcher.group(1) );
		
		assertTrue( matcher.find());
		
		assertEquals( "players/Adams.zip", matcher.group(1));
	}
	
	

}
