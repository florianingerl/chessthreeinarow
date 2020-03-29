package DeepLearning;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Model.BasicEngine;
import Model.PgnGame;
import Model.Position;
import Model.VariationNode;
import Model.VariationTree;

public class PgnGameAnalyzer implements Consumer<PgnGame> {

	private static Logger logger = LogManager.getLogger();

	private PgnGame currentGame;
	private Position position = new Position();

	private static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {
		{
			put(Position.EMPTY, 0);

			put(Position.W_KNIGHT, BasicEngine.KNIGHT_VALUE);
			put(Position.W_BISHOP, BasicEngine.BISHOP_VALUE);
			put(Position.W_ROOK, BasicEngine.ROOK_VALUE);

			put(Position.B_KNIGHT, -BasicEngine.KNIGHT_VALUE);
			put(Position.B_BISHOP, -BasicEngine.BISHOP_VALUE);
			put(Position.B_ROOK, -BasicEngine.ROOK_VALUE);
		}
	};

	private int gamesAnalyzed = 0;

	private NeuralNet net;
	private ILearningProgressPlotter learningProgressPlotter;

	private Random random = new Random();

	public PgnGameAnalyzer(NeuralNet net) {
		this.net = net;
	}

	public void setLearningProgressPlotter(ILearningProgressPlotter learningProgressPlotter) {
		this.learningProgressPlotter = learningProgressPlotter;
	}

	@Override
	public void accept(PgnGame t) {
		try {
			currentGame = t;
			analyseCurrentGame();

			++gamesAnalyzed;
			logger.debug("Analyzed the " + gamesAnalyzed + ".game " + currentGame);

			if (learningProgressPlotter != null && gamesAnalyzed % 10000 == 0) {
				testCurrentNeuralNet();

				FileOutputStream out = new FileOutputStream("NeuralNet");
				net.save(out);
				out.close();

			}

		} catch (Exception exception) {
			logger.error(ExceptionUtils.getStackTrace(exception));
		}

	}

	private void testCurrentNeuralNet() {

		NeuralNetTester tester = new NeuralNetTester(net);
		ConsumerProducerPattern<PgnGame> cpp = new ConsumerProducerPattern<PgnGame>(
				new PgnGameProducer(new File("PgnGamesTest"), 10000), tester);
		cpp.produceAndConsume();

		learningProgressPlotter.plotLearningProgress(gamesAnalyzed, tester.getRatioOfCorrectEvaluations());
	}

	private void analyseCurrentGame() {

		if (!currentGame.getResult().matches("1-0|0-1|1/2-1/2")) {
			--gamesAnalyzed;
			return;
		}

		position.reset();

		VariationTree vTree = currentGame.getGame();
		int numHalfMoves = countHalfMovesOfGame(vTree);

		VariationNode currentNode = vTree.getRoot();

		int currentHalfMove = 0;
		while (!currentNode.isLastMove()) {
			currentNode = currentNode.getVariation(0);
			position.makeMove(currentNode.getMove());
			++currentHalfMove;
			if (random.nextInt(5) != 0)
				continue;

			double desiredOutputVector[][] = new double[1][1];

			if (currentGame.getResult().equals("1-0")) {
				desiredOutputVector[0][0] = 1.0 / (numHalfMoves - currentHalfMove + 1);
			} else if (currentGame.getResult().equals("0-1")) {
				desiredOutputVector[0][0] = (-1) * 1.0 / (numHalfMoves - currentHalfMove + 1);
			} else {
				desiredOutputVector[0][0] = 0;
			}
			desiredOutputVector[0][0] = (desiredOutputVector[0][0] + 1) / 2.0f;

			net.learn(getInputVectorForNeuralNet(position), new BlockRealMatrix(desiredOutputVector));

		}

	}

	public static BlockRealMatrix getInputVectorForNeuralNet(Position position) {
		double inputVector[][] = new double[68][1];

		int[] square = position.getSquare();
		for (int i = 0; i < square.length; ++i) {
			inputVector[i][0] = map.get(square[i]) / ((double) BasicEngine.KING_VALUE);
		}

		inputVector[64][0] = position.getNextMove();
		
		return new BlockRealMatrix(inputVector);
	}

	private int countHalfMovesOfGame(VariationTree vTree) {
		int numHalfMoves = 0;
		VariationNode currentNode = vTree.getRoot();
		while (!currentNode.isLastMove()) {
			++numHalfMoves;
			currentNode = currentNode.getVariation(0);
		}
		return numHalfMoves;
	}

}
