package Model;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UI.ChessboardDisplay;

import raptor.engine.uci.UCIBestMove;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIInfoListener;
import raptor.engine.uci.UCIMove;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.ScoreInfo;

public class MyUCIEngine implements IEngine, UCIInfoListener {

	private static Logger logger = LogManager.getLogger();
	
	private Lock lock = new ReentrantLock();
	private Condition searchFinished = lock.newCondition();

	private Position position = new Position();
	private Move bestMove = null;

	private RaptorUCIEngine raptorEngine = null;
	private ChessboardDisplay display = null;

	public MyUCIEngine(File exeFile, ChessboardDisplay display) {
		this.display = display;
		raptorEngine = new RaptorUCIEngine();
		raptorEngine.setProcessPath(exeFile.getAbsolutePath());

		// This waits for readyok
		raptorEngine.connect();

	}

	@Override
	public void stop() {
		raptorEngine.stop();
		// then soon engineSentBestMove will be called

	}

	@Override
	public void findBestMove() {

		// First parameters is for options
		raptorEngine.go("btime 200000 wtime 200000 winc 0 binc 0", this);
		lock.lock();
		try {
			searchFinished.await();
			
		} catch (InterruptedException ioe) {
			ioe.printStackTrace();
		} finally {
			lock.unlock();
		}

	}

	@Override
	public Move getBestMove() {
		return bestMove;
	}

	

	@Override
	public void setDepth(int depth) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPositionFromFen(String fen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPosition(List<Move> moves) {
		logger.debug("We are setting the position of UCIEngine!");

		position.reset();
		UCIMove ucimoves[] = new UCIMove[moves.size()];

		int i = 0;
		for (Move m : moves) {
			position.makeMove(m);
			ucimoves[i] = new UCIMove(m.toUCIMoveNotation());
			i++;

		}

		raptorEngine.setStartingPosition(ucimoves);
		logger.debug("RaptorChess set succesffully the starting postion!");

	}

	@Override
	public void engineSentBestMove(UCIBestMove uciBestMove) {
		logger.debug("Engine sent best move!");
		lock.lock();
		logger.debug("Got best move "
				+ uciBestMove.getBestMove().getValue());

		bestMove = position.readMove(uciBestMove.getBestMove().getValue());

		logger.debug(bestMove);
		logger.debug(bestMove.toUCIMoveNotation());
		// That is the Move in UCIFormat
		searchFinished.signalAll();
		lock.unlock();

	}

	@Override
	public void engineSentInfo(UCIInfo[] infos) {
		for (UCIInfo info : infos) {
			if (info instanceof ScoreInfo) {
				ScoreInfo si = (ScoreInfo) info;
				int mateInMoves = si.getMateInMoves();
				
				if (mateInMoves == 0) {
					logger.debug("Score: " + si.getValueInCentipawns()/100.0f+" ");
				} else {
					logger.debug("Score: #" + mateInMoves+" ");
				}
			
				
			} else if (info instanceof BestLineFoundInfo) {
				BestLineFoundInfo blfi = (BestLineFoundInfo) info;
				UCIMove[] moves = blfi.getMoves();

				for (UCIMove move : moves) {
					logger.debug(move.toString() + " ");
				}
				
				
			}

		}
	}

	@Override
	public void quit() {
		raptorEngine.quit();
	}

}
