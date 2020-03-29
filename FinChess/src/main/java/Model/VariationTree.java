package Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


// The left Teilbaum means the main Variation!

public class VariationTree {

	private VariationNode root; // the root has move= null; earlierMove== null;
	
	// commentary = null; but it has options on the nextMove
	
	public VariationTree(){
		this.root = new VariationNode();
		root.setNumberOfMove(0);
	}
	

	public VariationNode getRoot() {
		return root;
	}
	
	
	public String toString(){
		
		return root.toString();
	}
	
	public String toStringSAN(){
		
		return root.toStringSAN(new Position());
	}
	
	public String toStringSAN(String fenString) {
		return root.toStringSAN(Position.fromFenString(fenString));
	}
	
	//From the list of integers is clear, which variations to choose from the root to get
	//to the desired position
	
	public List<Integer> getPositionFromCaret(int caretPosition){
		VariationNode lastNode = new VariationNode();
		// method must modify this object, so set attributes, but not assign any value
		root.getLastNodeFromCaret(caretPosition,0, lastNode);
		
		List<Integer> list = new LinkedList<Integer>();
		
		VariationNode nextMove = null;
		
		while(!lastNode.isRoot()){
			nextMove = lastNode;
			lastNode = lastNode.getEarlierMove();
			// now on lastNode.nextMove somewhere in the variations is the move I want
			int i = 0;
			
			for(VariationNode node: lastNode.getNextMove()){
			if(node.equals(nextMove)){
				break;
			}
			i++;
			}
			list.add(0,i);
			
			
			
		}
		
		return list;
	}
	
	public List<Integer> getPositionFromCaretSAN( int caretPosition )
	{
		VariationNode lastNode = new VariationNode();
		// method must modify this object, so set attributes, but not assign any value
		root.getLastNodeFromCaretSAN(caretPosition,0, new Position(), lastNode);
		
		List<Integer> list = new LinkedList<Integer>();
		
		VariationNode nextMove = null;
		
		while(!lastNode.isRoot()){
			nextMove = lastNode;
			lastNode = lastNode.getEarlierMove();
			// now on lastNode.nextMove somewhere in the variations is the move I want
			int i = 0;
			
			for(VariationNode node: lastNode.getNextMove()){
			if(node.equals(nextMove)){
				break;
			}
			i++;
			}
			list.add(0,i);
			
			
			
		}
		
		return list;
	}
	
	
	
}
