package Model;

public class BinaryMoveTree {

	public BinaryMoveNode root = new BinaryMoveNode();
	public int depth;

	public BinaryMoveTree(int depth) {
		this.depth = depth;
		root.generateChilds(depth);
	}

	public String getPrincipalVariation() {
		StringBuilder sb = new StringBuilder();

		BinaryMoveNode currentNode = root.leftChild;
		while (currentNode != null) {
			sb.append(currentNode.value.toString() + " ");
			currentNode = currentNode.leftChild;
		}
		return sb.toString();

	}

	public Move getPrincipalMove() {
		BinaryMoveNode currentNode = root.leftChild;
		if (currentNode != null) {
			return new Move(root.leftChild.value);
		}
		// else return null move
		return new Move();
	}

}
