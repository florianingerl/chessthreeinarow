package DeepLearning;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Model.PgnGame;
import Model.ReadPGNHeaders;

public class PgnGameProducer implements Producer<PgnGame> {

	private static Logger logger = LogManager.getLogger();
	private int numberOfGamesProduced = 0;
	private int numberOfGamesToBeProduced = Integer.MAX_VALUE;

	private LinkedBlockingQueue<PgnGame> pgnGames;

	private File pgnGamesDir;

	public PgnGameProducer(File pgnGamesDir) {
		this(pgnGamesDir, Integer.MAX_VALUE);
	}

	public PgnGameProducer(File pgnGamesDir, int numberOfGamesToBeProduced) {
		this.pgnGamesDir = pgnGamesDir;
		this.numberOfGamesToBeProduced = numberOfGamesToBeProduced;
	}

	@Override
	public void setQueue(LinkedBlockingQueue<PgnGame> queue) {
		this.pgnGames = queue;
	}

	@Override
	public void produce() {
		if (!pgnGamesDir.exists()) {
			logger.error("You have to run the PgnGamesDownloader first!");
			System.exit(1);
		}

		dealWithDirectory(pgnGamesDir);
	}

	public void dealWithDirectory(File directory) {
		if (numberOfGamesProduced >= numberOfGamesToBeProduced)
			return;
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				dealWithDirectory(file);
			} else {
				dealWithPgnFile(file);
			}

		}

	}

	public void dealWithPgnFile(File file) {
		if (numberOfGamesProduced >= numberOfGamesToBeProduced)
			return;
		try {
			logger.debug("Loading games of file " + file.getAbsolutePath());
			ReadPGNHeaders readPgn = new ReadPGNHeaders(new FileInputStream(file));
			readPgn.parseHeaders();

			LinkedList<PgnGame> games = readPgn.getListOfGames();
			for (PgnGame game : games) {
				logger.debug("Loading game " + game);
				game.loadGame();
				try {
					pgnGames.put(game);
					++numberOfGamesProduced;
					if (numberOfGamesProduced >= numberOfGamesToBeProduced) {
						return;
					}
				} catch (InterruptedException e) {
					logger.error(ExceptionUtils.getStackTrace(e));
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(ExceptionUtils.getStackTrace(e));

		}

	}

}
