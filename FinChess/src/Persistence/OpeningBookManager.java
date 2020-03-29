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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import Model.MoveAndStatistik;
import Model.Position;
import Model.SquareRepresentationConverter;


public class OpeningBookManager {

	private static Logger logger = Logger.getLogger(OpeningBookManager.class);
	
	private static OpeningBookManager instance = null;
	
	private static File theOpeningBookExe;
	private static File grandmasterBook;
	
	
	public List<MoveAndStatistik> getMoveStatistiks(Position position) throws IOException{
		
		if( theOpeningBookExe == null || !theOpeningBookExe.exists() || grandmasterBook == null || !grandmasterBook.exists() )
			return new LinkedList<MoveAndStatistik>();
		
		try {

			Runtime runTime = Runtime.getRuntime();
			// C:\\Dev-Cpp\\Project1.exe
			String fen = position.getFenString();
			fen = "\"" + fen + " \"";
			logger.debug(theOpeningBookExe.getAbsolutePath()+" " + grandmasterBook.getAbsolutePath()+" " + fen);
			Process findMoveFromFen = runTime.exec(theOpeningBookExe.getAbsolutePath()+" " + grandmasterBook.getAbsolutePath()+" " + fen);
			
			InputStream findMove = findMoveFromFen.getInputStream();
			
			StringBuilder sb = new StringBuilder();
			for(int c= findMove.read(); c!=-1; c = findMove.read()){
				sb.append((char) c);
			}
			
			findMove.close();
			
			return getMoveStatistiks( sb.toString(), position );

			
		} catch (IOException e) {
			throw e;
		}
		
	}
	
	private List<MoveAndStatistik> getMoveStatistiks(String moveStatistics, Position position) {

		List<MoveAndStatistik> list = new LinkedList<MoveAndStatistik>();

		String[] entries = moveStatistics.split("\n");

		for (String entry : entries) {

			if (entry.length() == 0) {
				continue;
			}
			entry.trim();
			int from = SquareRepresentationConverter.getBitFromString(entry.substring(5, 7));
			int to = SquareRepresentationConverter.getBitFromString(entry.substring(7, 9));
			MoveAndStatistik moveAndStatistik = new MoveAndStatistik();
			moveAndStatistik.setMove(position.getMove(from, to));
			moveAndStatistik.setWeight(entry.substring(17));
			list.add(moveAndStatistik);
		}

		return list;

	}
	
	
	private OpeningBookManager(){
		
		theOpeningBookExe = SettingsManager.getPathFile("CG_QUERY.exe");
		grandmasterBook = SettingsManager.getPathFile("gm2001.bin");
		
	}
	
	
	public static OpeningBookManager getInstance(){
		if(instance==null){
			instance = new OpeningBookManager();
		}
		return instance;
		
	}

	
	
	
}
