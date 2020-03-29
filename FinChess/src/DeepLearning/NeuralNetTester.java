package DeepLearning;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import Model.PgnGame;
import Model.Position;
import Model.VariationNode;
import Model.VariationTree;

public class NeuralNetTester implements Consumer<PgnGame> {

	private static Logger logger = Logger.getLogger(NeuralNetTester.class);

	private PgnGame currentGame;
	private Position position = new Position();

	private int gamesAnalyzed = 0;
	private int numCorrectEvaluations = 0;
	private int numWrongEvaluations = 0;

	private NeuralNet net;

	public NeuralNetTester(NeuralNet net) {
		this.net = net;
	}

	public int getGamesAnalyzed() {
		return gamesAnalyzed;
	}

	public int getNumCorrectEvaluations() {
		return numCorrectEvaluations;
	}

	public int getNumWrongEvaluations() {
		return numWrongEvaluations;
	}

	public double getRatioOfCorrectEvaluations() {
		return ((double) getNumCorrectEvaluations()) / getGamesAnalyzed();
	}

	@Override
	public void accept(PgnGame game) {

		try {
			currentGame = game;

			testCurrentGame();
			++gamesAnalyzed;
			logger.debug("Tested " + gamesAnalyzed + " games ");
		} catch (Exception exp) {
			logger.debug(ExceptionUtils.getStackTrace(exp));
		}

	}

	private void testCurrentGame() {

		if (!currentGame.getResult().matches("1-0|0-1")) {
			--gamesAnalyzed;
			return;
		}
		position.reset();

		VariationTree vTree = currentGame.getGame();
		VariationNode currentNode = vTree.getRoot();

		while (!currentNode.isLastMove()) {
			currentNode = currentNode.getVariation(0);
			position.makeMove(currentNode.getMove());
		}

		double evaluation = net.computeOutputVector(PgnGameAnalyzer.getInputVectorForNeuralNet(position)).getEntry(0,
				0);
		evaluation = evaluation * 2 - 1;
		System.out.println("Evaluation = " + evaluation);
		if ((currentGame.getResult().equals("1-0") && evaluation > 0)
				|| (currentGame.getResult().equals("0-1") && evaluation < 0)) {
			++numCorrectEvaluations;
		} else {
			++numWrongEvaluations;
		}
	}

}
