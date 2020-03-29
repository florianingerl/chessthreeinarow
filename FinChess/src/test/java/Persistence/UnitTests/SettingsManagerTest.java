package Persistence.UnitTests;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import Persistence.SettingsManager;

public class SettingsManagerTest {

	private static Logger logger = LogManager.getLogger();
	
	
	@Test
	@Ignore
	public void  canRestoreSettingsFromLastSession_PropertiesFileIsPresent_ReturnsTrue() {
		boolean can = SettingsManager.getInstance().canRestoreSettingsFromLastSession();
		assertTrue(can);
	}
	
	@Test
	@Ignore
	public void canRestoreSettingsFromLastSession_PropertiesFileIsntPresent_ReturnsFalse()
	{
		File propertiesFile = new File("SettingsFromLastSession.properties");
		File saveCopy = new File("SettingsFromLastSession(2).properties");
		
		
		if(! propertiesFile.renameTo(saveCopy) )
			return;
		
		try{
		boolean can = SettingsManager.getInstance().canRestoreSettingsFromLastSession();
		assertFalse(can);
		}
		finally{
			saveCopy.renameTo(propertiesFile);
		}
	}
	
	@Test
	@Ignore
	public void setLastDepth_SetItTo7_GetLastDepthShouldReturn7()
	{
		
		SettingsManager sm = SettingsManager.getInstance();
		Integer orignialDepth = sm.getLastDepth();
		
		sm.setLastDepth(7);
		assertSame(7, sm.getLastDepth());
		
		if(orignialDepth != null) sm.setLastDepth(orignialDepth);
	}
	
	@Test
	@Ignore
	public void setLastFlip_SetItToMinus1_GetLastFlipShouldReturnMinus1()
	{
		SettingsManager sm = SettingsManager.getInstance();
		Integer originalFlip = sm.getLastFlip();
		
		sm.setLastFlip(-1);
		assertSame(-1, sm.getLastFlip());
		
		if(originalFlip != null) sm.setLastFlip(originalFlip);
	}
	
	
	@Test
	@Ignore
	public void setLastOpeningBook_SetItTogm2016_GetLastOpeningBookShouldReturngm2016()
	{
		SettingsManager sm = SettingsManager.getInstance();
		
		File originalBook = sm.getLastOpeningBook();
		
		File crazyBook = SettingsManager.getPathFile("gm2016.bin");
		
		sm.setLastOpeningBook(crazyBook);
		assertEquals(crazyBook, sm.getLastOpeningBook());
		
		if(originalBook != null) sm.setLastOpeningBook(originalBook);
	}

}
