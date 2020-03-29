package DeepLearning;

import java.io.File;

import org.apache.log4j.Logger;

import Model.PgnGame;

public class Main {

	private static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		NeuralNet net = new NeuralNet(68, 60, 1);
		PgnGameAnalyzer pgnGameAnalyzer = new PgnGameAnalyzer(net);
		pgnGameAnalyzer.setLearningProgressPlotter(null);
		ConsumerProducerPattern<PgnGame> cpp = new ConsumerProducerPattern<PgnGame>(
				new PgnGameProducer(new File("PgnGames")), pgnGameAnalyzer);
		cpp.produceAndConsume();
	}

}
