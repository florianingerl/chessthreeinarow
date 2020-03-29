package Model;


public class TranspositionTableLookup {

	private long hash = 0;
	private long hash2 = 0;
	private int evaluation = TranspositionTable.NOENTRY;
	private int ply = -1;
	private int nodeType = -1;
	
	public TranspositionTableLookup(){
		reset();
	}
	
	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public long getHash() {
		return hash;
	}
	public void setHash(long hash) {
		this.hash = hash;
	}
	public int getEvaluation() {
		return evaluation;
	}
	public void setEvaluation(int evaluation) {
		this.evaluation = evaluation;
	}
	public int getPly() {
		return ply;
	}
	public void setPly(int ply) {
		this.ply = ply;
	}
	public void reset() {
		hash = 0;
		hash2 = 0;
		evaluation = TranspositionTable.NOENTRY;
		ply = -1;
		nodeType = -1;
		
	}

	public long getHash2() {
		return hash2;
	}

	public void setHash2(long hash2) {
		this.hash2 = hash2;
	}

	
	
	
}
