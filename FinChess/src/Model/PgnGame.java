package Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class PgnGame {

	private VariationTree game;
	private String event;
	private String site;
	private Date date;
	private int round;
	private String white;
	private String black;
	private int eloWhite;
	private int eloBlack;
	private String result;
	
	private String pgnDatabase;
	private int startIndex = -1;
	private int endIndex;
	
	private String fenString;
	
	public String getFenString() {
		return fenString;
	}

	public void setFenString(String fenString) {
		this.fenString = fenString;
	}

	public String getPgnDatabase() {
		return pgnDatabase;
	}

	public Position getInitialPosition()
	{
		if(fenString != null)
		{
			return Position.fromFenString(fenString);
		}
		return new Position();
	}

	public void setPgnDatabase(String pgnDatabase) {
		this.pgnDatabase = pgnDatabase;
	}


	public int getStartIndex() {
		return startIndex;
	}


	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}


	public int getEndIndex() {
		return endIndex;
	}


	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}


	public PgnGame(){
		
	}

	public void loadGame()
	{
		if(game == null && pgnDatabase != null && startIndex != -1)
		{
			ReadPGN rpgn = new ReadPGN(pgnDatabase.substring(startIndex, endIndex) );
			PgnGame onlyGame = rpgn.getListOfGames().get(0);
			game = onlyGame.getGame();
		}
	}
	
	public VariationTree getGame() {
		
		if(game == null && pgnDatabase != null && startIndex != -1)
		{
			ReadPGN rpgn = new ReadPGN(pgnDatabase.substring(startIndex, endIndex) );
			PgnGame onlyGame = rpgn.getListOfGames().get(0);
			game = onlyGame.getGame();
		}
		
		return game;
	}


	public void setGame(VariationTree game) {
		this.game = game;
	}


	public String getEvent() {
		return event;
	}


	public void setEvent(String event) {
		this.event = event;
	}


	public String getWhite() {
		return white;
	}


	public void setWhite(String white) {
		this.white = white;
	}


	public String getBlack() {
		return black;
	}


	public void setBlack(String black) {
		this.black = black;
	}


	public int getEloWhite() {
		return eloWhite;
	}


	public void setEloWhite(int eloWhite) {
		this.eloWhite = eloWhite;
	}


	public int getEloBlack() {
		return eloBlack;
	}


	public void setEloBlack(int eloBlack) {
		this.eloBlack = eloBlack;
	}
	
	public String toString(){
		return white +" "+eloWhite +" vs "+black+" "+eloBlack+"  "+result;
	}


	public void setResult(String result) {
		this.result = result;
		
	}
	
	public String getResult()
	{
		return result;
	}
	
	
	public void write(File file, VariationTree game){
		OutputStreamWriter out = null;
		try{
			out = new OutputStreamWriter(new FileOutputStream(file), ReadPGN.PGN_ENCODING);
			out.write("[Event \""+event+"\"]\n");
			out.write("[Site \""+site+"\"]\n");
			out.write("[Date \""+date+"\"]\n");
			out.write("[Round \""+round+"\"]\n");
			out.write("[White \""+white+"\"]\n");
			out.write("[Black \""+black+"\"]\n");
			out.write("[Result \""+result+"\"]\n");
			out.write("[WhiteElo \""+eloWhite+"\"]\n");
			out.write("[BlackElo \""+eloBlack+"\"]\n");
			out.write("\n");
			
			out.write(game.toStringSAN());
			out.write(" "+result);	
			
			
		}
		catch(IOException ioe){
			
		}
		finally{
			try {
				out.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		
		
		
	}


	public void setSite(String site) {
		this.site = site;
		
	}


	public void setRound(int round) {
		this.round = round;
		
	}
	
	public void setDate(Date date){
		this.date = date;
	}
	
	
	
}
