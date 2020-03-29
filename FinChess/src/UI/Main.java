package UI;

import java.io.File;

import org.apache.log4j.Logger;

import Model.Bitboards;

public class Main {

	private static Logger logger = Logger.getLogger(Main.class);
	
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
