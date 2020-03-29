package Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VariationNode {

	private static Logger logger = LogManager.getLogger();
	
	private int numberOfMove;
	private VariationNode earlierMove = null;

	public int getNumberOfMove() {
		return numberOfMove;
	}

	public void setNumberOfMove(int numberOfMove) {
		this.numberOfMove = numberOfMove;
	}

	private Move move = null;
	private String commentary = null;
	private List<VariationNode> nextMove = new LinkedList<VariationNode>();

	public VariationNode() {
	}

	public boolean isVariation() {
		return !(nextMove.size() == 0);
	}

	public int addFurtherOption(Move move) {
		VariationNode variationNode = new VariationNode();
		variationNode.earlierMove = this;
		variationNode.numberOfMove = numberOfMove + 1;
		variationNode.move = move;
		nextMove.add(variationNode);
		return nextMove.size() - 1;
	}

	public void override(Move move) {
		VariationNode variationNode = new VariationNode();
		variationNode.earlierMove = this;
		variationNode.numberOfMove = numberOfMove + 1;
		variationNode.move = move;
		nextMove.remove(0);
		nextMove.add(0, variationNode);
	}

	public void addNewMain(Move move) {
		VariationNode variationNode = new VariationNode();
		variationNode.earlierMove = this;
		variationNode.numberOfMove = numberOfMove + 1;
		variationNode.move = move;
		nextMove.add(0, variationNode);

	}

	public VariationNode getEarlierMove() {
		return earlierMove;
	}

	public void removeAlternative(Move move) {
		for (VariationNode node : nextMove) {
			if (node.getMove().equals(move)) {
				nextMove.remove(node);
				return;
			}
		}
	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	public void setEarlierMove(VariationNode earlierMove) {
		this.earlierMove = earlierMove;
	}

	public void setNextMove(List<VariationNode> nextMove) {
		this.nextMove = nextMove;
	}

	public String getCommentary() {
		return commentary;
	}

	public void setCommentary(String commentary) {
		this.commentary = commentary;
	}

	public VariationNode getVariation(int currentVariation) {
		return nextMove.get(currentVariation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariationNode other = (VariationNode) obj;
		if (commentary == null) {
			if (other.commentary != null)
				return false;
		} else if (!commentary.equals(other.commentary))
			return false;
		if (earlierMove == null) {
			if (other.earlierMove != null)
				return false;
		} else if (!earlierMove.equals(other.earlierMove))
			return false;
		if (move == null) {
			if (other.move != null)
				return false;
		} else if (!move.equals(other.move))
			return false;
		if (nextMove == null) {
			if (other.nextMove != null)
				return false;
		} else if (!nextMove.equals(other.nextMove))
			return false;
		return true;
	}

	public String toStringSAN(Position position){
		
		StringBuilder sb = new StringBuilder();

		// you have to print the move of the main variation
		// then you need to print alternatives
		// then you need to print the mainVariation

		if (nextMove.size() > 0) {
			
			Iterator<VariationNode> iterator = nextMove.iterator();
			VariationNode mainVariation = iterator.next();
			
			if (mainVariation.numberOfMove % 2 != 0) {
				sb.append(mainVariation.numberOfMove / 2 + 1 + ". ");


			} else if (earlierMove.nextMove.size() >= 2 && mainVariation.earlierMove == earlierMove.nextMove.get(0)) {
				sb.append(mainVariation.numberOfMove / 2 + "... ");
			}

			sb.append(position.toShortAlgebraicNotation(mainVariation.getMove()) + " ");
			VariationNode currentVariation;

			while (iterator.hasNext()) {

				currentVariation = iterator.next();
				sb.append("( ");
				if (currentVariation.numberOfMove % 2 == 0) {
					sb.append(currentVariation.numberOfMove / 2 + "... ");
				} else {
					sb.append(currentVariation.numberOfMove / 2 + 1 + ". ");
				}
				sb.append(position.toShortAlgebraicNotation(currentVariation.getMove()) + " ");
				position.makeMove(currentVariation.getMove());
				sb.append(currentVariation.toStringSAN(position));
				position.unmakeMove(currentVariation.getMove());
				sb.append(")");
				
			}

			position.makeMove(mainVariation.getMove());
			sb.append(mainVariation.toStringSAN(position));
			position.unmakeMove(mainVariation.getMove());

		}

		return sb.toString();
		
		
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// you have to print the move of the main variation
		// then you need to print alternatives
		// then you need to print the mainVariation

		if (nextMove.size() > 0) {
			
			Iterator<VariationNode> iterator = nextMove.iterator();
			VariationNode mainVariation = iterator.next();
			
			if (mainVariation.numberOfMove % 2 != 0) {
				sb.append(mainVariation.numberOfMove / 2 + 1 + ". ");


			} else if (earlierMove.nextMove.size() >= 2 && mainVariation.earlierMove == earlierMove.nextMove.get(0)) {
				sb.append(mainVariation.numberOfMove / 2 + "... ");
			}

			sb.append(mainVariation.getMove().toString() + " ");
			VariationNode currentVariation;

			while (iterator.hasNext()) {

				currentVariation = iterator.next();
				sb.append("( ");
				if (currentVariation.numberOfMove % 2 == 0) {
					sb.append(currentVariation.numberOfMove / 2 + "... ");
				} else {
					sb.append(currentVariation.numberOfMove / 2 + 1 + ". ");
				}
				sb.append(currentVariation.getMove() + " " + currentVariation
						+ ")");
			}

			sb.append(mainVariation.toString());

		}

		return sb.toString();

	}
	
	public List<VariationNode> getNextMove() {
		return nextMove;
	}

	public boolean isLastMove(){
		return nextMove.size() == 0;
	}
	
	public boolean hasMoreThanOneContinuation(){
		return nextMove.size() > 1;
	}

	public int getLastNodeFromCaret(int caretPosition, int currentCaretPosition, VariationNode lastNode) {
		
		String toAppend = null;

		if (nextMove.size() > 0) {
			
			Iterator<VariationNode> iterator = nextMove.iterator();
			VariationNode mainVariation = iterator.next();
			
			if (mainVariation.numberOfMove % 2 != 0) {
				toAppend = mainVariation.numberOfMove / 2 + 1 + ". ";
				currentCaretPosition+= toAppend.length();


			} else if (earlierMove.nextMove.size() >= 2 && mainVariation.earlierMove == earlierMove.nextMove.get(0)) {
				toAppend = mainVariation.numberOfMove / 2 + "... ";
				currentCaretPosition+=toAppend.length();
			}

			toAppend = mainVariation.getMove().toString() + " ";
			currentCaretPosition+=toAppend.length();
			if(currentCaretPosition >= caretPosition){
				lastNode.setMove(mainVariation.getMove());
				lastNode.setCommentary(mainVariation.getCommentary());
				lastNode.setNumberOfMove(mainVariation.getNumberOfMove());
				lastNode.setEarlierMove(mainVariation.getEarlierMove());
				lastNode.setNextMove(mainVariation.getNextMove());
				
				return -1;
			}
			
			VariationNode currentVariation;

			while (iterator.hasNext()) {

				currentVariation = iterator.next();
				toAppend = "( ";
				currentCaretPosition+=toAppend.length();
				if (currentVariation.numberOfMove % 2 == 0) {
					toAppend = currentVariation.numberOfMove / 2 + "... ";
					currentCaretPosition+=toAppend.length();
					
				} else {
					toAppend = currentVariation.numberOfMove / 2 + 1 + ". ";
					currentCaretPosition+=toAppend.length();
				}
				toAppend = currentVariation.getMove() + " ";
				currentCaretPosition+=toAppend.length();
				if(currentCaretPosition >= caretPosition){
					lastNode.setCommentary(currentVariation.getCommentary()); 
					lastNode.setMove(currentVariation.getMove());
					lastNode.setEarlierMove(currentVariation.getEarlierMove());
					lastNode.setNextMove(currentVariation.getNextMove());
					lastNode.setNumberOfMove(currentVariation.getNumberOfMove());
					
					logger.debug(lastNode.getMove());
					return -1;
				}
				currentCaretPosition = currentVariation.getLastNodeFromCaret(caretPosition, currentCaretPosition, lastNode);
				if(currentCaretPosition==-1){
					return -1;
				}
				toAppend = ")";
				currentCaretPosition+=toAppend.length(); 
				
			}

			currentCaretPosition = mainVariation.getLastNodeFromCaret(caretPosition, currentCaretPosition, lastNode);
			if(currentCaretPosition==-1){
				return -1;
			}

		}
		
		
		return currentCaretPosition;
	}
	
	public boolean isRoot(){
		return move==null;
	}

	public int getLastNodeFromCaretSAN(int caretPosition, int currentCaretPosition, Position position, VariationNode lastNode) {
		
		String toAppend = null;

		if (nextMove.size() > 0) {
			
			Iterator<VariationNode> iterator = nextMove.iterator();
			VariationNode mainVariation = iterator.next();
			
			if (mainVariation.numberOfMove % 2 != 0) {
				toAppend = mainVariation.numberOfMove / 2 + 1 + ". ";
				currentCaretPosition+= toAppend.length();


			} else if (earlierMove.nextMove.size() >= 2 && mainVariation.earlierMove == earlierMove.nextMove.get(0)) {
				toAppend = mainVariation.numberOfMove / 2 + "... ";
				currentCaretPosition+=toAppend.length();
			}

			toAppend = position.toShortAlgebraicNotation(mainVariation.getMove() ) + " ";
			currentCaretPosition+=toAppend.length();
			if(currentCaretPosition >= caretPosition){
				lastNode.setMove(mainVariation.getMove());
				lastNode.setCommentary(mainVariation.getCommentary());
				lastNode.setNumberOfMove(mainVariation.getNumberOfMove());
				lastNode.setEarlierMove(mainVariation.getEarlierMove());
				lastNode.setNextMove(mainVariation.getNextMove());
				
				return -1;
			}
			
			VariationNode currentVariation;

			while (iterator.hasNext()) {

				currentVariation = iterator.next();
				toAppend = "( ";
				currentCaretPosition+=toAppend.length();
				if (currentVariation.numberOfMove % 2 == 0) {
					toAppend = currentVariation.numberOfMove / 2 + "... ";
					currentCaretPosition+=toAppend.length();
					
				} else {
					toAppend = currentVariation.numberOfMove / 2 + 1 + ". ";
					currentCaretPosition+=toAppend.length();
				}
				toAppend = position.toShortAlgebraicNotation(currentVariation.getMove()) + " ";
				currentCaretPosition+=toAppend.length();
				if(currentCaretPosition >= caretPosition){
					lastNode.setCommentary(currentVariation.getCommentary()); 
					lastNode.setMove(currentVariation.getMove());
					lastNode.setEarlierMove(currentVariation.getEarlierMove());
					lastNode.setNextMove(currentVariation.getNextMove());
					lastNode.setNumberOfMove(currentVariation.getNumberOfMove());
					
					logger.debug(lastNode.getMove());
					return -1;
				}
				position.makeMove(currentVariation.getMove());
				currentCaretPosition = currentVariation.getLastNodeFromCaretSAN(caretPosition, currentCaretPosition, position, lastNode);
				
				if(currentCaretPosition==-1){
					return -1;
				}
				position.unmakeMove(currentVariation.getMove());
				toAppend = ")";
				currentCaretPosition+=toAppend.length(); 
				
			}

			position.makeMove(mainVariation.getMove());
			currentCaretPosition = mainVariation.getLastNodeFromCaretSAN(caretPosition, currentCaretPosition, position, lastNode);
			if(currentCaretPosition==-1){
				return -1;
			}
			position.unmakeMove(mainVariation.getMove());

		}
		
		
		return currentCaretPosition;
		
	}

}
