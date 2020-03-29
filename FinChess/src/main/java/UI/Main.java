package UI;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Model.Bitboards;

public class Main {

	private static Logger logger = LogManager.getLogger();
	
	public static void main(String[] args) {
		
		ChessboardDisplay f = new ChessboardDisplay();

		if(args.length!=0){
			f.openPgnFile(new File(args[0]));
		}
		else{
			f.resumePlay();
		}
		
	}
	
	
	
}
