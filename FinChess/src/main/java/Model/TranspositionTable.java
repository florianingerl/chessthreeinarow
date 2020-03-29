package Model;

import java.math.BigInteger;


public class TranspositionTable {

	public static final int NOENTRY = 1000001;
	//999983
	//100000007 
	//10000019 
	private int k = 10000019;
	private TranspositionTableLookup[] entries = new TranspositionTableLookup[k]; 
	

	public TranspositionTable(){
		for(int i=0; i< k; i++){
			entries[i] = new TranspositionTableLookup();
		}
		
	}
	
	//forgets everything from previous searches
	public void clear(){
		for(int i=0; i < k; i++){
			entries[i].reset();
		}
	}
	
	// long can be negative!
	public void put(long hash, long hash2, int evaluation, int currentDepth, int nodeType){
		int entry = Math.abs((int)(hash%k));
		
		//if(ply >= entries[entry].getPly()){ Vergleiche sind teuer, allerdings sind die Einträge, die auf höherer Tiefe basieren wertvoller
		if(currentDepth >= entries[entry].getPly()){
		entries[entry].setPly(currentDepth);
		entries[entry].setEvaluation(evaluation);
		entries[entry].setHash(hash);
		entries[entry].setHash2(hash2);
		entries[entry].setNodeType(nodeType);
		}
		
	}
	
	public EvalNode getEvaluation(long hash, long hash2, int currentDepth){
		
		int entry = Math.abs((int)(hash%k));
		if(entries[entry].getHash()==hash && entries[entry].getHash2()==hash2 && entries[entry].getPly()>=currentDepth){
			//Here, we also need to return the type of node!!!
			return new EvalNode(entries[entry].getEvaluation(), entries[entry].getNodeType());
		}
		
		return new EvalNode(NOENTRY, -1);
		
		
	}
	
	
	
	
}
