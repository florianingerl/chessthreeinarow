package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import raptor.chess.Result;
import raptor.chess.pgn.AbstractPgnParser;
import raptor.chess.pgn.Nag;
import raptor.chess.pgn.PgnParser;
import raptor.chess.pgn.PgnParserListener;

public class ReadPGN implements PgnParserListener {

	private static Logger logger = LogManager.getLogger();

	private Position currentPosition;
	private VariationTree varationTree;
	private VariationNode currentMove;
	private Move toMake;

	private List<PgnGame> listOfGames = new LinkedList<PgnGame>();
	private PgnGame currentGame;

	private List<Integer> toComeBacks = new LinkedList<Integer>();

	int variation;
	boolean mustExecutePrevious;

	public static final String PGN_ENCODING = "ISO-8859-1";

	private String pgnGames;

	public ReadPGN(File f) throws IOException {
		if( f == null || !f.exists() ) throw new IOException("File doesn' exist!");
		pgnGames = FileUtils.readFileToString(f, PGN_ENCODING);

		readPGNGames();

	}
	
	public ReadPGN(String pgnGames)
	{
		this.pgnGames = pgnGames;
		readPGNGames();
	}

	private void readPGNGames() {
		toComeBacks.add(0);

		final AbstractPgnParser parser = new SimplePgnParser(pgnGames);
		parser.addPgnParserListener(this);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				parser.parse();
			}

		});

		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public ReadPGN(InputStream stream) throws IOException
	{
		pgnGames = IOUtils.toString(stream, PGN_ENCODING);
		readPGNGames();
	}

	@Override
	public void onAnnotation(PgnParser pgnParser, String annotation) {

		logger.info(annotation);
	}

	@Override
	public void onGameEnd(PgnParser pgnParser, Result result) {

		int toComeBack = toComeBacks.get(0);
		endVariation(toComeBack);

	}

	@Override
	public void onGameStart(PgnParser pgnParser) {

		mustExecutePrevious = false;
		variation = 0;

		currentGame = new PgnGame();
		varationTree = new VariationTree();
		currentMove = varationTree.getRoot();
		currentGame.setGame(varationTree);
		currentPosition = null;

		toComeBacks = new LinkedList<Integer>();
		toComeBacks.add(0);

		listOfGames.add(currentGame);

	}

	@Override
	public void onHeader(PgnParser pgnParser, String tag, String value) {
		if ("White".equals(tag)) {
			currentGame.setWhite(value);
		} else if ("Black".equals(tag)) {
			currentGame.setBlack(value);
		} else if ("WhiteElo".equals(tag) && value.matches("^\\d+$") ) {
			currentGame.setEloWhite(Integer.valueOf(value));
		} else if ("BlackElo".equals(tag) && value.matches("^\\d+$")) {
			currentGame.setEloBlack(Integer.valueOf(value));
		} else if ("Event".equals(tag)) {
			currentGame.setEvent(value);
		} else if ("Result".equals(tag)) {
			currentGame.setResult(value);
		} else if( "FEN".equals(tag)){
			currentGame.setFenString(value);
		}

	}

	@Override
	public void onMoveNag(PgnParser pgnParser, Nag nag) {

		logger.info(nag.getNagString());
	}

	@Override
	public void onMoveNumber(PgnParser pgnParser, int moveNumber) {
		if( currentPosition == null )
		{
			currentPosition = currentGame.getInitialPosition();
		}
	}

	@Override
	public void onMoveSublineEnd(PgnParser pgnParser) {
		int toComeBack = toComeBacks.remove(toComeBacks.size() - 1);

		endVariation(toComeBack);

		variation = 0;
		mustExecutePrevious = true;

	}

	private void endVariation(int toComeBack) {
		while (toComeBack != 0) {

			toMake = currentMove.getMove();
			if (toMake.isNullMove()) {
				currentPosition.makeNullMove();
			} else {
				currentPosition.unmakeMove(toMake);
			}

			currentMove = currentMove.getEarlierMove();

			toComeBack--;

		}

	}

	@Override
	public void onMoveSublineStart(PgnParser pgnParser) {

		toComeBacks.add(0);

		mustExecutePrevious = false;
	}

	@Override
	public void onMoveWord(PgnParser pgnParser, String moveWord) {

		if (mustExecutePrevious) {

			currentMove = currentMove.getVariation(variation);

			int toComeBack = toComeBacks.remove(toComeBacks.size() - 1);
			toComeBack++;
			toComeBacks.add(toComeBack);

			toMake = currentMove.getMove();
			if (toMake.isNullMove()) {
				currentPosition.makeNullMove();
			} else {
				currentPosition.makeMove(toMake);
			}

		} // end if must execute previous

		Move move = null;

		if ("--".equals(moveWord)) {
			move = new Move(); // Nullzug
		} else {

			move = currentPosition.parseMove(moveWord, MoveEncoding.SHORT_ALGEBRAIC_NOTATION);

		}

		variation = currentMove.addFurtherOption(move);

		mustExecutePrevious = true;

	}

	@Override
	public void onUnknown(PgnParser pgnParser, String string) {

		logger.info("Unknown " + string);
	}

	public List<PgnGame> getListOfGames() {
		return listOfGames;
	}

}
