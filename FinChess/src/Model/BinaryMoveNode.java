package Model;

class BinaryMoveNode {

	BinaryMoveNode father = null;
	BinaryMoveNode leftChild = null;
	BinaryMoveNode rightChild = null;

	Move value = new Move();

	void generateChilds(int depth) {
		if (depth > 0) {
			leftChild = new BinaryMoveNode();
			leftChild.father = this;
			leftChild.generateChilds(depth - 1);

			rightChild = new BinaryMoveNode();
			rightChild.father = this;
			rightChild.generateChilds(depth - 1);
		}

	}

	void setPrincipalVariation() {
		BinaryMoveNode currentNode = this;
		BinaryMoveNode leftBrother = currentNode.father.leftChild;

		while (currentNode != null) {
			leftBrother.value.setMoveInt(currentNode.value.getMoveInt());
			currentNode = currentNode.leftChild;
			leftBrother = leftBrother.leftChild;
		}

	}

	int level() {
		int i = 0;
		BinaryMoveNode currentNode = this;
		while (currentNode != null) {
			currentNode = currentNode.father;
			i++;
		}
		return i - 1;

	}

}
