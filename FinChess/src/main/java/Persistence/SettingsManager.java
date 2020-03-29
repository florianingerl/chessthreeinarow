package Persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsManager {

	private static Logger logger = LogManager.getLogger();
	
	private static File settingsFromLastSession;
	private static Properties properties = new Properties(); 
	
	private static SettingsManager instance = null;
	
	private SettingsManager()
	{
		canRestoreSettingsFromLastSession();
	}
	
	public static SettingsManager getInstance()
	{
		if(instance == null)
		{
			instance = new SettingsManager();
		}
		return instance;
	}
	
	
	public boolean canRestoreSettingsFromLastSession(){
		
		settingsFromLastSession = SettingsManager.getPathFile("SettingsFromLastSession.properties");
		
		if(settingsFromLastSession != null && settingsFromLastSession.exists()){
			InputStream in = null;
			try {
				in = new FileInputStream(settingsFromLastSession);
				properties.load(in);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			finally{
				if(in!=null){
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return true;
		}
		return false;
			
	}
	
	
	public Integer getLastDepth(){
		
		String stringDepth = properties.getProperty("depth");
		if(stringDepth==null){
			return null;
		}
		int depth = Integer.valueOf(stringDepth);
		
		return depth;
		
	}
	
	public Integer getLastFlip(){
		
		String stringFlip = properties.getProperty("flip");
		if(stringFlip==null){
			return null;
		}
		int flip = Integer.valueOf(stringFlip);
		
		return flip;
		
	}
	
	public File getLastOpeningBook(){
		
		String stringOpeningBook = properties.getProperty("openingBook");
		if(stringOpeningBook==null){
			return null;
		}
		File openingBook = new File(stringOpeningBook);
		if(openingBook.exists()){
			return openingBook;
		}
		return null;
		
	}
	
	public void setLastDepth(int depth){
		properties.setProperty("depth", Integer.toString(depth));
		if(settingsFromLastSession == null) return;
		Date date = new Date();
		OutputStream out = null;
		try {
			out = new FileOutputStream(settingsFromLastSession);
			properties.store(out, date.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void setPathOpeningBook(File f) {
		setLastOpeningBook(f);	
	}
	
	public void setLastFlip(int flip){
		properties.setProperty("flip", Integer.toString(flip));
		if(settingsFromLastSession == null) return;
		Date date = new Date();
		OutputStream out = null;
		try {
			out = new FileOutputStream(settingsFromLastSession);
			properties.store(out, date.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	public void setLastOpeningBook(File file){
		if(file == null || !file.exists() )
		{
			return;
		}
		properties.setProperty("openingBook", file.getAbsolutePath());
		if(settingsFromLastSession == null) return;
		Date date = new Date();
		OutputStream out = null;
		try {
			out = new FileOutputStream(settingsFromLastSession);
			properties.store(new FileOutputStream(settingsFromLastSession), date.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	
	public static File getPathFile(String datei){
		
	    File resource =null;
	
	      try
	      {
	        CodeSource codeSource = OpeningBookManager.class.getProtectionDomain().getCodeSource();
	        File jarFile = new File(codeSource.getLocation().toURI().getPath());
	        if(Files.isSymbolicLink(jarFile.toPath())){
	        	Path path = Files.readSymbolicLink(jarFile.toPath());
	        	jarFile = path.toFile();
	        	
	        }
	        String jarDir = jarFile.getParentFile().getPath();
	        resource = new File(jarDir + System.getProperty("file.separator") + datei);
	        
	        
	      }
	      catch (Exception ex)
	      {
	    	  ex.printStackTrace();
	    	  logger.error("Settings from last Session is null");
	    	  return null;
	      }
	    
	    return resource;
	}
	
}
