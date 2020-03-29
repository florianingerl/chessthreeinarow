package Model;


public class GameLineRecord {
	private Move move;
	private byte castleWhite;
	private byte castleBlack;
	private int fiftyMove;
	private int epSquare;
	private long hash;
	private long hash2;
	
	public GameLineRecord(){
		move = new Move();
		castleWhite = 0;
		castleBlack = 0;
		fiftyMove = 0;
		epSquare = 0;
		hash = 0L;
		hash2 = 0L;
		
	}

	public GameLineRecord(GameLineRecord gameLineRecord) {
		this.move = new Move(gameLineRecord.move);
		this.castleWhite = gameLineRecord.castleWhite;
		this.castleBlack = gameLineRecord.castleBlack;
		this.fiftyMove = gameLineRecord.fiftyMove;
		this.epSquare = gameLineRecord.epSquare;
	}

	public Move getMove() {
		return move;
	}

	public void setCastleWhite(byte castleWhite) {
		this.castleWhite = castleWhite;
	}

	public void setCastleBlack(byte castleBlack) {
		this.castleBlack = castleBlack;
		
	}

	public void setFiftyMove(int fiftyMove) {
		this.fiftyMove = fiftyMove;
		
	}

	public void setEpSquare(int epSquare) {
		this.epSquare = epSquare;
		
	}

	public byte getCastleWhite() {
		return castleWhite;
	}
	
	public byte getCastleBlack(){
		return castleBlack;
	}
	
	public int getEpSquare(){
		return epSquare;
	}
	
	public int getFiftyMove(){
		return fiftyMove;
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	public long getHash2() {
		return hash2;
	}

	public void setHash2(long hash2) {
		this.hash2 = hash2;
	}


	
	
	

}
