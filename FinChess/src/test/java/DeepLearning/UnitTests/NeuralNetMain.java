package DeepLearning.UnitTests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import DeepLearning.ConsumerProducerPattern;
import DeepLearning.ExceptionUtils;
import DeepLearning.NeuralNet;
import DeepLearning.PgnGameAnalyzer;
import DeepLearning.PgnGameProducer;
import Model.PgnGame;
import Model.Position;
import Model.VariationNode;
import Model.VariationTree;

public class NeuralNetMain implements Consumer<PgnGame> {

private static Logger logger = LogManager.getLogger();
	
	private PgnGame currentGame;
	private Position position = new Position();
	
	private int gamesAnalyzed = 0;
	private int numCorrectEvaluations = 0;
	private int numWrongEvaluations = 0;

	private NeuralNet net;
	
	public NeuralNetMain(NeuralNet net)
	{
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

	@Override
	public void accept(PgnGame game) {
		
		try
		{
			currentGame = game;
			
			testCurrentGame();
			++gamesAnalyzed;
			
			
			logger.debug("Tested " + gamesAnalyzed + " games ");
			if(gamesAnalyzed > 10000)
			{
				System.out.println("Number of games analyzed: " + getGamesAnalyzed() );
				System.out.println("Number of correct evaluations: " + getNumCorrectEvaluations());
				
				assert getGamesAnalyzed() - getNumCorrectEvaluations() == getNumWrongEvaluations();
				
				System.out.println("Percentage of correct evaluations " + 100.0 * getNumCorrectEvaluations()/ getGamesAnalyzed() + "%" );
				System.exit(0);
			}
			 
		}
		catch(Exception exp)
		{
			logger.debug(ExceptionUtils.getStackTrace(exp));
		}
		
	}	
	
	private void testCurrentGame() 
	{
		
		if(!currentGame.getResult().matches("1-0|0-1")) 
		{
			--gamesAnalyzed;
			return;
		}
		position.reset();
		
		VariationTree vTree = currentGame.getGame();
		VariationNode currentNode = vTree.getRoot();
	
		while(!currentNode.isLastMove())
		{
			currentNode = currentNode.getVariation(0);
			position.makeMove( currentNode.getMove() );
		}
		
		double evaluation = net.computeOutputVector(PgnGameAnalyzer.getInputVectorForNeuralNet(position)).getEntry(0, 0);
		evaluation = evaluation * 2 - 1;
		System.out.println("Evaluation = " + evaluation );
		if(( currentGame.getResult().equals("1-0") && evaluation > 0 ) || (currentGame.getResult().equals("0-1") && evaluation < 0) )
		{
			++numCorrectEvaluations;
		}
		else
		{
			++numWrongEvaluations;
		}
	}

	public static void main(String[] args) {

		final NeuralNet net = new NeuralNet(68, 60, 1);
		try {
			InputStream in = new FileInputStream("NeuralNet");
			net.load(in);
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
		NeuralNetMain tester = new NeuralNetMain(net);
		ConsumerProducerPattern<PgnGame> cpp = new ConsumerProducerPattern<PgnGame>(new PgnGameProducer(new File("PgnGamesTest") ),
				tester);
		cpp.produceAndConsume();

		System.out.println("Number of games analyzed: " + tester.getGamesAnalyzed() );
		System.out.println("Number of correct evaluations: " + tester.getNumCorrectEvaluations());
		
		assert tester.getGamesAnalyzed() - tester.getNumCorrectEvaluations() == tester.getNumWrongEvaluations();
		
		System.out.println("Percentage of correct evaluations " + 100.0 * tester.getNumCorrectEvaluations()/ tester.getGamesAnalyzed() + "%" );
		
	}

}
