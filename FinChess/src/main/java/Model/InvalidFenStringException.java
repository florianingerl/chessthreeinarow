package Model;

public class InvalidFenStringException extends Exception{

	private String fenString;
	
	public InvalidFenStringException(String fenString){
		this.fenString = fenString;
	}
	
	@Override
	public String getMessage(){
		return "Invalid fen String : "+fenString;
		
	}
	
}
