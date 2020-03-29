package UI;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import Model.BasicEngine;
import Model.FutureTaskCancelWaits;
import Model.IEngine;
import Model.Move;
import Model.MoveAndStatistik;
import Model.MyThreadPool;
import Model.MyUCIEngine;
import Model.PgnGame;
import Model.Position;
import Model.ReadPGN;
import Model.ReadPGNHeaders;
import Model.VariationNode;
import Model.VariationTree;
import Persistence.OpeningBookManager;
import Persistence.SettingsManager;
import java.awt.image.BufferedImage;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

public class ChessboardDisplay extends JFrame
		implements ActionListener, ListSelectionListener, CaretListener, MouseWheelListener, WindowStateListener {

	private static Logger logger = Logger.getLogger(ChessboardDisplay.class);

	private PgnGame currentGame = new PgnGame() {
		{
			setWhite(System.getProperty("user.name"));
			setBlack("Computer");
			setEvent("Games for fun at home");
			setSite("San Diego");
			setDate(new Date());
			setRound(1);
			setGame(new VariationTree());

		}
	};
	private VariationTree actuallyPlayedMoves = currentGame.getGame();
	private VariationNode currentMove = actuallyPlayedMoves.getRoot();
	private int numberOfPlayedMoves = 1;

	private IEngine engine = null;
	private Position currentPosition;

	private HumanMoveGetter humanMoveGetter;

	private Future<?> playingThreadsFuture = null;

	private int startedThreads = 0;
	private int stoppedThreads = 0;

	private volatile boolean computersTurn = false;

	private JMenuBar menuBar = new JMenuBar();

	private JMenu file = new JMenu("File");

	private JMenuItem openPgn = new JMenuItem("Open .pgn") {
		{
			setToolTipText("Open a .pgn (Portable game notation) file");
		}
	};
	private JMenuItem savePgn = new JMenuItem("Save as .pgn") {
		{
			setToolTipText("Save current game as a .pgn (Portable game notation) file");
		}
	};

	private JMenuItem saveAsGif = new JMenuItem("Save as .gif") {
		{
			setToolTipText("Save current diagramm as a .gif image");
		}
	};

	private JMenuItem print = new JMenuItem("Print");

	private JMenu edit = new JMenu("Edit");

	private JMenuItem undo = new JMenuItem("Undo");
	private JMenuItem redo = new JMenuItem("Redo");

	private JMenu game = new JMenu("Game");

	private JMenuItem startEngine = new JMenuItem("Start Engine!") {
		{
			setToolTipText("Let the chess computer calculate a move in this position!");
		}
	};
	private JMenuItem stopEngine = new JMenuItem("Stop Engine!") {
		{
			setToolTipText("Turn the chess computer off!");
		}
	};
	private JMenuItem loadUCIEngine = new JMenuItem("Load UCI Engine!") {
		{
			setToolTipText("Load an UCI (Universal chess interface) engine!");
		}
	};
	private JMenuItem newGame = new JMenuItem("New Game!") {
		{
			setToolTipText("Finish this game and start a new game!");
		}
	};

	private JMenuItem buildPositionFromFen = new JMenuItem("from a fen string") {
		{
			setToolTipText("Build a position by providing a fen string");
		}
	};

	private JMenuItem buildPositionByHand = new JMenuItem("by hand") {
		{
			setToolTipText("Build a position by dragging pieces onto the chess board");
		}
	};

	private JMenu buildPosition = new JMenu("Build position") {
		{
			setToolTipText("Start a game from a position other than the starting position!");
			add(buildPositionFromFen);
			add(buildPositionByHand);
		}
	};

	private JMenuItem forceMove = new JMenuItem("Force Move") {
		{
			setToolTipText(
					"Force the computer to abandon its calculations and make the best move it has found so far.");
		}
	};

	private boolean engineOn = true;

	private JMenu preferences = new JMenu("Preferences");

	private JMenuItem setDepthOfEngine = new JMenuItem("Depth of engine!");
	private JMenuItem flipBoard = new JMenuItem("Flip Board");
	private JMenuItem changeColor = new JMenuItem("Change Colour");
	private JMenuItem loadPolyglot = new JMenuItem("Load polyglot opening book!") {
		{
			setToolTipText("Load a different opening book!");
		}
	};

	private DragHumanMoveGetter screen;

	private BlackToMove blackToMove;
	private WhiteToMove whiteToMove;

	private JLabel blackName;
	private JLabel whiteName;

	private JLabel eloWhite;
	private JLabel eloBlack;

	private JButton start;
	private JButton back;
	private JButton forward;
	private JButton end;

	private JTabbedPane tabbedPane = new JTabbedPane();
	private JTextArea sheet = new JTextArea();
	private JTable tableOpeningBook = new JTable(new OpeningBookTableModel());

	private JTextArea calculation = new JTextArea();
	private JLabel scoreLabel;

	private final Dimension sizeOfNavigationButtons = new Dimension(40, 20);
	public static final Font notation = new Font("Monospaced", Font.BOLD, 14);
	public static final Color blueBorder = new Color(51, 204, 255);
	private final Color blueEmptySpace = new Color(0, 204, 204);
	private final Color blueWhite = new Color(0, 184, 245);
	private final Color blueBlack = new Color(0, 102, 204);
	private final Color blueSheet = new Color(153, 204, 255);
	private final Color yellowScore = new Color(245, 245, 0);

	private boolean wantForcedMove = false;

	private JPanel scoreSheet;

	private JPanel blackPanel;

	private JPanel whitePanel;

	public ChessboardDisplay() {

		super("Flori's chessprogramm");

		logger.debug("Chessboard Display constructed!");

		currentPosition = new Position();
		DragHumanMoveGetter dhmg = new DragHumanMoveGetter(currentPosition);
		screen = dhmg;
		humanMoveGetter = dhmg;

		engine = new BasicEngine();

		file.add(openPgn);
		file.add(savePgn);
		file.add(saveAsGif);
		file.add(loadPolyglot);
		file.add(print);
		print.setEnabled(false);

		menuBar.add(file);

		edit.add(undo);
		undo.setEnabled(false);
		edit.add(redo);
		redo.setEnabled(false);

		menuBar.add(edit);

		game.add(newGame);
		game.add(buildPosition);
		game.addSeparator();
		game.add(loadUCIEngine);
		game.add(startEngine);
		game.add(stopEngine);
		game.add(forceMove);

		forceMove.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_SPACE));

		menuBar.add(game);

		preferences.add(setDepthOfEngine);
		preferences.add(flipBoard);
		preferences.add(changeColor);

		menuBar.add(preferences);

		setJMenuBar(menuBar);

		JPanel contentPanel = (JPanel) getContentPane();
		contentPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc;

		JSplitPane wholeSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		wholeSplitPane.setResizeWeight(1);

		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;

		contentPanel.add(wholeSplitPane, gbc);

		JSplitPane notationAndPlayingSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		notationAndPlayingSplitPane.setResizeWeight(0.4);

		wholeSplitPane.add(notationAndPlayingSplitPane);

		notationAndPlayingSplitPane.add(screen);

		scoreSheet = new JPanel(new GridBagLayout());

		notationAndPlayingSplitPane.add(scoreSheet);

		blackPanel = new JPanel(new GridBagLayout());
		blackPanel.setOpaque(true);
		blackPanel.setBackground(blueBlack);

		blackToMove = new BlackToMove();
		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0.05;
		blackPanel.add(blackToMove, gbc);

		blackName = new JLabel("Computer");
		blackName.setForeground(Color.BLACK);
		blackName.setFont(new Font("Monospaced", Font.BOLD, 12));

		gbc = ChessboardDisplay.setGbcValues(1, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;

		blackPanel.add(blackName, gbc);

		eloBlack = new JLabel("");
		eloBlack.setForeground(Color.BLACK);
		eloBlack.setFont(new Font("Monospaced", Font.BOLD, 12));

		gbc = ChessboardDisplay.setGbcValues(1, 1, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;

		blackPanel.add(eloBlack, gbc);

		gbc = ChessboardDisplay.setGbcValues(0, 0, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.05;
		gbc.weightx = 1;

		scoreSheet.add(blackPanel, gbc);

		JLabel emptySpaceTop = new JLabel();
		emptySpaceTop.setOpaque(true);
		emptySpaceTop.setBackground(blueEmptySpace);
		emptySpaceTop.setPreferredSize(new Dimension(10, 15));
		gbc = ChessboardDisplay.setGbcValues(0, 1, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 0.05;
		scoreSheet.add(emptySpaceTop, gbc);

		sheet.setBackground(blueSheet);
		sheet.setFont(new Font("Monospaced", Font.BOLD, 12));
		sheet.setLineWrap(true);
		sheet.setWrapStyleWord(true);
		sheet.addCaretListener(this);
		sheet.setEditable(false);
		sheet.getCaret().setVisible(true);
		sheet.addMouseWheelListener(this);
		sheet.getInputMap().setParent(null);

		tabbedPane.add("Score sheet", new JScrollPane(sheet));

		tabbedPane.add("book", new JScrollPane(tableOpeningBook));

		tableOpeningBook.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableOpeningBook.getSelectionModel().addListSelectionListener(this);

		tabbedPane.setPreferredSize(new Dimension(300, 10));

		gbc = ChessboardDisplay.setGbcValues(0, 2, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.8;
		gbc.weightx = 1;

		scoreSheet.add(tabbedPane, gbc);

		JPanel navigation = new JPanel(new FlowLayout());
		Font navigationButtons = new Font("Monospaced", Font.BOLD, 8);
		Insets navigationButtonsInsets = new Insets(0, 0, 0, 0);

		start = new JButton() {
			{
				setToolTipText("Go to the very start of this game!");
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				int h = this.getHeight();
				int w = this.getWidth();

				g.setColor(Color.BLACK);
				g.fillPolygon(new int[] { (int) (3 / 4.0 * w), (int) (1 / 4.0 * w), (int) (3 / 4.0 * w) },
						new int[] { (int) (3 / 4.0 * h), (int) (1 / 2.0 * h), (int) (1 / 4.0 * h) }, 3);
				g.drawLine((int) (1 / 4.0 * w), (int) (1 / 4.0 * h), (int) (1 / 4.0 * w), (int) (3 / 4.0 * h));
			}
		};
		start.setFont(navigationButtons);
		start.setPreferredSize(sizeOfNavigationButtons);
		start.setMargin(navigationButtonsInsets);
		start.getInputMap().setParent(null);
		navigation.add(start);
		back = new JButton() {
			{
				setToolTipText("Go one move back!");
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				int h = getHeight();
				int w = getWidth();

				g.setColor(Color.BLACK);
				g.fillPolygon(new int[] { (int) (3 / 4.0 * w), (int) (1 / 4.0 * w), (int) (3 / 4.0 * w) },
						new int[] { (int) (3 / 4.0 * h), (int) (1 / 2.0 * h), (int) (1 / 4.0 * h) }, 3);
			}
		};
		back.setFont(navigationButtons);
		back.setPreferredSize(sizeOfNavigationButtons);
		back.setMargin(navigationButtonsInsets);
		back.getInputMap().setParent(null);

		navigation.add(back);
		forward = new JButton() {
			{
				setToolTipText("Go one move forward!");
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				int h = this.getHeight();
				int w = this.getWidth();

				g.setColor(Color.BLACK);
				g.fillPolygon(new int[] { (int) (1 / 4.0 * w), (int) (3 / 4.0 * w), (int) (1 / 4.0 * w) },
						new int[] { (int) (3 / 4.0 * h), (int) (1 / 2.0 * h), (int) (1 / 4.0 * h) }, 3);

			}
		};
		forward.setFont(navigationButtons);
		forward.setPreferredSize(sizeOfNavigationButtons);
		forward.setMargin(navigationButtonsInsets);
		forward.getInputMap().setParent(null);
		navigation.add(forward);
		end = new JButton() {
			{
				setToolTipText("Go to the very end of this game!");
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				int h = this.getHeight();
				int w = this.getWidth();

				g.setColor(Color.BLACK);
				g.fillPolygon(new int[] { (int) (1 / 4.0 * w), (int) (3 / 4.0 * w), (int) (1 / 4.0 * w) },
						new int[] { (int) (3 / 4.0 * h), (int) (1 / 2.0 * h), (int) (1 / 4.0 * h) }, 3);
				g.drawLine((int) (3 / 4.0 * w), (int) (1 / 4.0 * h), (int) (3 / 4.0 * w), (int) (3 / 4.0 * h));
			}
		};
		end.setFont(navigationButtons);
		end.setPreferredSize(sizeOfNavigationButtons);
		end.setMargin(navigationButtonsInsets);
		end.getInputMap().setParent(null);
		navigation.add(end);

		navigation.setPreferredSize(new Dimension(50, 50));
		gbc = ChessboardDisplay.setGbcValues(0, 3, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 0;
		scoreSheet.add(navigation, gbc);

		JLabel emptySpaceBottom = new JLabel();
		emptySpaceBottom.setOpaque(true);
		emptySpaceBottom.setBackground(blueEmptySpace);
		emptySpaceBottom.setPreferredSize(new Dimension(50, 50));
		gbc = ChessboardDisplay.setGbcValues(0, 4, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		scoreSheet.add(emptySpaceBottom, gbc);

		whitePanel = new JPanel(new GridBagLayout());
		whitePanel.setOpaque(true);
		whitePanel.setBackground(blueWhite);

		whiteToMove = new WhiteToMove();
		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0.05;
		whitePanel.add(whiteToMove, gbc);

		whiteName = new JLabel(System.getProperty("user.name"));
		whiteName.setForeground(Color.WHITE);
		whiteName.setFont(new Font("Monospaced", Font.BOLD, 12));

		gbc = ChessboardDisplay.setGbcValues(1, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.weightx = 1;

		whitePanel.add(whiteName, gbc);

		eloWhite = new JLabel("");
		eloWhite.setForeground(Color.WHITE);
		eloWhite.setFont(new Font("Monospaced", Font.BOLD, 12));

		gbc = ChessboardDisplay.setGbcValues(1, 1, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.weightx = 1;

		whitePanel.add(eloWhite, gbc);

		gbc = ChessboardDisplay.setGbcValues(0, 5, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 0.05;

		scoreSheet.add(whitePanel, gbc);

		JPanel calculationPanel = new JPanel(new GridBagLayout());

		wholeSplitPane.add(calculationPanel);

		scoreLabel = new JLabel();
		scoreLabel.setVerticalAlignment(SwingConstants.TOP);
		scoreLabel.setOpaque(true);
		scoreLabel.setBackground(yellowScore);
		scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
		scoreLabel.setPreferredSize(new Dimension(20, 20));

		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.weightx = 0;
		calculationPanel.add(scoreLabel, gbc);

		getCalculation().setFont(new Font("Monospaced", Font.BOLD, 18));
		getCalculation().setLineWrap(true);
		getCalculation().setWrapStyleWord(true);
		getCalculation().setPreferredSize(new Dimension(25, 20));

		gbc = ChessboardDisplay.setGbcValues(1, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		calculationPanel.add(getCalculation(), gbc);

		openPgn.addActionListener(this);
		savePgn.addActionListener(this);
		saveAsGif.addActionListener(this);
		print.addActionListener(this);
		undo.addActionListener(this);
		redo.addActionListener(this);
		loadUCIEngine.addActionListener(this);
		startEngine.addActionListener(this);
		stopEngine.addActionListener(this);
		newGame.addActionListener(this);
		buildPositionFromFen.addActionListener(this);
		buildPositionByHand.addActionListener(this);
		start.addActionListener(this);
		back.addActionListener(this);
		forward.addActionListener(this);
		end.addActionListener(this);
		setDepthOfEngine.addActionListener(this);
		flipBoard.addActionListener(this);
		changeColor.addActionListener(this);
		loadPolyglot.addActionListener(this);
		forceMove.addActionListener(this);

		restoreSettingsFromLastTime();

		searchForOpeningMoves();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pack();
		setVisible(true);
		this.addWindowStateListener(this);
		repaintToMoves();

	}

	public void repaintToMoves() {

		blackToMove.repaint();
		whiteToMove.repaint();

	}

	private class BlackToMove extends JLabel {

		private double scale = 1;

		public BlackToMove() {
			setOpaque(true);
			setBackground(blueBlack);
		}

		public Dimension getPreferredSize() {
			return new Dimension(60, 60);
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Dimension size = getSize();
			double edgeLength = Math.min(size.getHeight(), size.getWidth());

			// 800 is the default size of the board
			scale = edgeLength / 120;

			g.setColor(Color.BLACK);

			g.fillOval((int) (20 * scale), (int) (20 * scale), (int) (80 * scale), (int) (80 * scale));
			if (currentPosition.getNextMove() == -1) {
				g.drawOval((int) (10 * scale), (int) (10 * scale), (int) (100 * scale), (int) (100 * scale));
			}

		}

	}

	private class WhiteToMove extends JLabel {

		private float scale = 0.5f;

		public WhiteToMove() {
			setOpaque(true);
			setBackground(blueWhite);
		}

		public Dimension getPreferredSize() {
			return new Dimension(60, 60);
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Dimension size = getSize();
			double edgeLength = Math.min(size.getHeight(), size.getWidth());

			// 800 is the default size of the board
			scale = (float) (edgeLength / 120);

			g.setColor(Color.WHITE);

			g.fillOval((int) (20 * scale), (int) (20 * scale), (int) (80 * scale), (int) (80 * scale));
			if (currentPosition.getNextMove() == 1) {
				g.drawOval((int) (10 * scale), (int) (10 * scale), (int) (100 * scale), (int) (100 * scale));
			}
		}

	}

	public List<Move> getActuallyPlayed() {
		return new LinkedList<Move>();
	}

	public static GridBagConstraints setGbcValues(int gridx, int gridy, int gridwidth, int gridheight) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		return gbc;
	}

	public void setEvaluation(final Integer evaluation) {
		if (SwingUtilities.isEventDispatchThread()) {
			scoreLabel.setText(Integer.toString(evaluation));
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					scoreLabel.setText(Integer.toString(evaluation));
				}

			});
		}

	}

	public JTextArea getCalculation() {
		return calculation;
	}

	public Position getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Position currentPosition) {
		this.currentPosition = currentPosition;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == undo) {

		} else if (e.getSource() == buildPositionByHand) {
			menuBuildPositionClicked();
		} else if (e.getSource() == buildPositionFromFen) {
			menuBuildPositionFromFenClicked();
		} else if (e.getSource() == start) {

			buttonStartClicked();

		} else if (e.getSource() == print) {

		} else if (e.getSource() == loadPolyglot) {
			menuLoadPolyglotClicked();
		} else if (e.getSource() == setDepthOfEngine) {
			menuSetDepthOfEngineClicked();
		} else if (e.getSource() == flipBoard) {
			menuFlipBoardClicked();
		} else if (e.getSource() == newGame) {
			menuNewGameClicked();
		} else if (e.getSource() == redo) {

		} else if (e.getSource() == forward) {
			buttonForwardClicked();
		} else if (e.getSource() == openPgn) {
			menuOpenPgnClicked();
		} else if (e.getSource() == loadUCIEngine) {
			menuLoadUCIEngineClicked();
		} else if (e.getSource() == startEngine) {
			menuStartEngineClicked();
		} else if (e.getSource() == back) {
			buttonBackClicked();
		} else if (e.getSource() == stopEngine) {
			menuStopEngineClicked();
		} else if (e.getSource() == forceMove) {
			menuForceMoveClicked();
		} else if (e.getSource() == end) {
			buttonEndClicked();

		} else if (e.getSource() == changeColor) {
			menuChangeColorClicked();
		} else if (e.getSource() == savePgn) {
			menuSavePgnClicked();
		} else if (e.getSource() == saveAsGif) {
			menuSaveAsGifClicked();
		}

	}

	private void menuBuildPositionFromFenClicked() {
		String fenString = JOptionPane.showInputDialog("Please enter a valid fen string!");
		if (fenString == null)
			return;

		interruptPlayingThread();
		currentGame.setFenString(fenString);
		currentGame.setGame(new VariationTree());
		actuallyPlayedMoves = currentGame.getGame();
		currentMove = actuallyPlayedMoves.getRoot();
		currentPosition = currentGame.getInitialPosition();
		screen.setPosition(currentPosition);
		screen.redrawPosition();
		screen.repaint();

		sheet.setText("");
		repaintToMoves();

		searchForOpeningMoves();

		computersTurn = false;

		resumePlay();
	}

	private void menuSaveAsGifClicked() {
		GraphicsConfiguration gfxConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage image = gfxConf.createCompatibleImage(screen.getWidth(), screen.getHeight());
		screen.paintComponent(image.createGraphics());

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				return f.getName().toLowerCase().endsWith(".png");
			}

			@Override
			public String getDescription() {
				return "Portable Network Graphic (*.png)";
			}

		});

		int result = fileChooser.showSaveDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {

			File f = fileChooser.getSelectedFile();
			try {
				ImageIO.write(image, "png", f);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Could not write .png file!");
			}

		}

	}

	private void menuSavePgnClicked() {
		SavePgnDialog spd = new SavePgnDialog();
		PgnGame pgnGame = spd.open();

		if (pgnGame == null) {
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				return f.getName().toLowerCase().endsWith(".pgn");
			}

			@Override
			public String getDescription() {
				return "Portable Game Notation (*.pgn)";
			}

		});

		int result = fileChooser.showSaveDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {

			File f = fileChooser.getSelectedFile();
			pgnGame.write(f, actuallyPlayedMoves);

		}

	}

	private void menuChangeColorClicked() {
		screen.changeColour();

	}

	private void buttonEndClicked() {
		interruptPlayingThread();

		while (!currentMove.isLastMove()) {
			currentMove = currentMove.getVariation(0);
			currentPosition.makeMove(currentMove.getMove());

		}

		screen.redrawPosition();
		screen.repaint();
		repaintToMoves();

		searchForOpeningMoves();

		computersTurn = false;
		resumePlay();

	}

	private void menuForceMoveClicked() {
		engineOn = true;

		if (computersTurn) {
			wantForcedMove = true;
			engine.stop();
		} else {
			interruptPlayingThread();
			computersTurn = true;
			resumePlay();
		}

	}

	private void menuStopEngineClicked() {
		interruptPlayingThread();

		engineOn = false;
		computersTurn = false;
		resumePlay();

	}

	private void buttonBackClicked() {
		interruptPlayingThread();

		Move toUnmake = currentMove.getMove();
		if (toUnmake == null) {
			Toolkit.getDefaultToolkit().beep();

		} else {
			currentMove = currentMove.getEarlierMove();
			unmakeMove(toUnmake);
		}

		computersTurn = false;
		resumePlay();

	}

	private void menuLoadUCIEngineClicked() {

		interruptPlayingThread();

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				return f.getName().toLowerCase().endsWith(".exe");
			}

			@Override
			public String getDescription() {
				return "UCIEngine (*.exe)";
			}

		});

		int result = fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {

			File exeFile = fileChooser.getSelectedFile();

			engine.quit();

			// the other engine will be garbage collected!!
			engine = new MyUCIEngine(exeFile, this);
			engineOn = true;

		}

		computersTurn = false;
		resumePlay();

	}

	private void menuStartEngineClicked() {
		interruptPlayingThread();

		engineOn = true;
		computersTurn = true;
		resumePlay();

	}

	private File selectPgnFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				return f.getName().toLowerCase().endsWith(".pgn");
			}

			@Override
			public String getDescription() {
				return "Portable Game Notation (*.pgn)";
			}

		});

		int result = fileChooser.showOpenDialog(this);

		File f = null;
		if (result == JFileChooser.APPROVE_OPTION) {

			f = fileChooser.getSelectedFile();

		}
		return f;

	}

	public void openPgnFile(File f) {

		if (f == null) {
			logger.error(".pgn file was null");
			computersTurn = false;
			resumePlay();
			return;
		}

		try {
			ReadPGNHeaders readPGN = new ReadPGNHeaders(new FileInputStream(f));
			readPGN.parseHeaders();
			List<PgnGame> listOfGames = readPGN.getListOfGames();
			logger.debug("There are " + listOfGames.size() + " games in the .pgn-file!");
			DefaultListModel<PgnGame> model = new DefaultListModel<PgnGame>();
			for (PgnGame game : listOfGames) {
				model.addElement(game);
			}

			final JList<PgnGame> jlistOfGames = new JList<PgnGame>(model);
			tabbedPane.addTab(listOfGames.get(0).getEvent(), new JScrollPane(jlistOfGames) {
				{
					setPreferredSize(new Dimension(50, 350));
				}
			});
			jlistOfGames.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {

					int selectedRow = jlistOfGames.getSelectionModel().getLeadSelectionIndex();

					if (selectedRow != -1) {
						interruptPlayingThread();

						DefaultListModel<PgnGame> model = (DefaultListModel<PgnGame>) jlistOfGames.getModel();
						currentGame = model.get(selectedRow);

						actuallyPlayedMoves = currentGame.getGame();
						currentMove = actuallyPlayedMoves.getRoot();
						setWhiteName(currentGame.getWhite());
						setBlackName(currentGame.getBlack());
						setWhiteElo(Integer.toString(currentGame.getEloWhite()));
						setBlackElo(Integer.toString(currentGame.getEloBlack()));

						currentPosition = currentGame.getInitialPosition();
						screen.setPosition(currentPosition);

						searchForOpeningMoves();

						repaintToMoves();

						sheet.setText(actuallyPlayedMoves.toString());
						tabbedPane.setSelectedIndex(0); // Score Sheet
						engineOn = false;
						computersTurn = false;
						resumePlay();

					}

				}

			});

			engineOn = false;
			computersTurn = false;
			resumePlay();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Could not open the pgn-File");
			computersTurn = false;
			resumePlay();

		}

	}

	private void menuOpenPgnClicked() {
		interruptPlayingThread();

		File f = selectPgnFile();

		if (f != null) {
			openPgnFile(f);
		} else {
			computersTurn = false;
			resumePlay();

		}
	}

	private void buttonForwardClicked() {
		interruptPlayingThread();

		if (currentMove.isLastMove()) {
			Toolkit.getDefaultToolkit().beep();
			computersTurn = false;
			resumePlay();
			return;
		}

		currentMove = currentMove.getVariation(0);

		Move toMake = currentMove.getMove();
		makeMove(toMake);

		computersTurn = false;
		resumePlay();

	}

	private void menuNewGameClicked() {
		interruptPlayingThread();

		currentPosition.reset();
		screen.redrawPosition();
		screen.repaint();

		searchForOpeningMoves();
		actuallyPlayedMoves = new VariationTree();
		currentMove = actuallyPlayedMoves.getRoot();

		sheet.setText("");

		repaintToMoves();

		computersTurn = false;
		resumePlay();

	}

	private void menuFlipBoardClicked() {
		screen.flipBoard();

		// Flip tow panels for white and black (i.e. change the layout)
		GridBagLayout layout = (GridBagLayout) scoreSheet.getLayout();
		GridBagConstraints constraint1 = layout.getConstraints(blackPanel);

		scoreSheet.remove(blackPanel);

		GridBagConstraints constraint2 = layout.getConstraints(whitePanel);
		scoreSheet.remove(whitePanel);

		scoreSheet.add(whitePanel, constraint1);
		scoreSheet.add(blackPanel, constraint2);

		scoreSheet.validate();
	}

	private void menuSetDepthOfEngineClicked() {
		Integer depth;

		depth = (Integer) JOptionPane.showInputDialog(this,
				"Choose the depth for the engine! \nYou can always press space to force the computer to move!", "Depth",
				JOptionPane.QUESTION_MESSAGE, null, new Integer[] { 3, 4, 5, 6, 7, 8, 9, 10 }, 3);

		if (depth != null) {
			engine.setDepth(depth);
			SettingsManager.getInstance().setLastDepth(depth);

		}

	}

	private void menuLoadPolyglotClicked() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return (file.getName().toLowerCase().endsWith(".bin"));
			}

			public String getDescription() {
				return "Polyglot opening book(*.bin)";
			}
		});

		int result = fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();

			SettingsManager.getInstance().setPathOpeningBook(f);

			searchForOpeningMoves();

		} else {

			result = JOptionPane.showConfirmDialog(this, "Would you like to download \n a book from the web?",
					"Search Web", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {

				Desktop desktop = Desktop.getDesktop();

				try {
					desktop.browse(new URI("http://www.chessengine.co.in/chess_opening_book_bin.html"));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Could not open Browser!");
					return;
				} catch (URISyntaxException e1) {
					JOptionPane.showMessageDialog(null, "Could not open the download Site");
					return;
				}
			}

		}

	}

	private void buttonStartClicked() {
		interruptPlayingThread();

		while (!currentMove.isRoot()) {
			currentPosition.unmakeMove(currentMove.getMove());
			currentMove = currentMove.getEarlierMove();

		}

		screen.redrawPosition();
		screen.repaint();

		repaintToMoves();

		searchForOpeningMoves();

		computersTurn = false;
		resumePlay();

	}

	private void menuBuildPositionClicked() {
		/*
		 * Position position = BuildPosition.getBuildPosition(this); if (position !=
		 * null) { interruptPlayingThread();
		 * 
		 * currentPosition = position; screen.setPosition(currentPosition);
		 * 
		 * searchForOpeningMoves();
		 * 
		 * repaintToMoves();
		 * 
		 * computersTurn = false; resumePlay(); }
		 */

	}

	public void resumePlay() {

		++startedThreads;

		playingThreadsFuture = MyThreadPool.getPool().submit(new FutureTaskCancelWaits(new Runnable() {

			public void run() {
				resumePlayMethod();
			}

		}));

	} // end method;

	private void resumePlayMethod() {

		logger.debug("A new thread is started!");

		logger.debug("Started Threads " + startedThreads + " Stopped Threads " + stoppedThreads);

		Move m;

		while (true) {

			if (computersTurn) {

				if (currentPosition.isStalemate()) {
					JOptionPane.showMessageDialog(null, "Stalemate!");
					return;
				} else if (currentPosition.isCheckmate()) {
					JOptionPane.showMessageDialog(null, "Congratulations! It's checkmate!");
					return;
				}

				m = tryToBlackFromBook();

				if (m == null) {

					if (currentGame.getFenString() == null) {
						engine.setPosition(getMovesFromStart());
					} else {
						engine.setPositionFromFen(currentPosition.getFenString());
					}

					wantForcedMove = true;
					calculation.setText("Calculating the best move...");
					engine.findBestMove();
					calculation.setText("");

					// This is bad, let the engine return a
					// move, if
					// it's a null move, then see what to do
					//

					if (!wantForcedMove) {
						return;
					}

					m = engine.getBestMove();

				}

				int result = addVariation(m);

				if (result == NewVariationDialog.CANCEL) {
					computersTurn = false;
					continue;
				}

				makeMove(m);
				logger.debug(currentPosition.getFenString());

				if (currentPosition.isCheckmate()) {
					JOptionPane.showMessageDialog(null, "You got checkmated!");
					return;
				} else if (currentPosition.isStalemate()) {
					JOptionPane.showMessageDialog(null, "It's stalemate");
					return;
				}

				toogleComputersTurn();

			} else {

				logger.debug("Humang move getter get Move!");
				try {
					m = humanMoveGetter.getMove();
				} catch (InterruptedException ie) {
					return;
				}
				logger.debug("We got the move!");

				int result = addVariation(m);

				if (result == NewVariationDialog.CANCEL) {
					computersTurn = false;
					continue;
				}

				makeMove(m);
				logger.debug(currentPosition.getFenString());
				if (engineOn) {
					toogleComputersTurn();
				}

			}

		}

	}

	private void toogleComputersTurn() {
		computersTurn ^= true;
	}

	public void setWhiteName(final String whiteName) {
		if (SwingUtilities.isEventDispatchThread()) {
			ChessboardDisplay.this.whiteName.setText(whiteName);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ChessboardDisplay.this.whiteName.setText(whiteName);
				}
			});
		}

	}

	public void setBlackName(final String blackName) {
		if (SwingUtilities.isEventDispatchThread()) {
			ChessboardDisplay.this.blackName.setText(blackName);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ChessboardDisplay.this.blackName.setText(blackName);
				}
			});
		}
	}

	public void setWhiteElo(final String eloWhite) {
		if (SwingUtilities.isEventDispatchThread()) {
			ChessboardDisplay.this.eloWhite.setText(eloWhite);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ChessboardDisplay.this.eloWhite.setText(eloWhite);
				}
			});
		}
	}

	public void setBlackElo(final String eloBlack) {
		if (SwingUtilities.isEventDispatchThread()) {
			ChessboardDisplay.this.eloBlack.setText(eloBlack);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ChessboardDisplay.this.eloBlack.setText(eloBlack);
				}
			});
		}
	}

	public void makeMove(Move m) {

		if (m.isNullMove()) {
			currentPosition.makeNullMove();
		} else {
			screen.makeMove(m);
		}

		searchForOpeningMoves();

		sheet.setText(actuallyPlayedMoves.toString());

	}

	public void unmakeMove(Move m) {

		if (m.isNullMove()) {
			currentPosition.makeNullMove();
		} else {
			screen.unmakeMove(m);

		}

		searchForOpeningMoves();

	}

	public void interruptPlayingThread() {
		++stoppedThreads;

		if (playingThreadsFuture != null) {
			if (!computersTurn) {
				playingThreadsFuture.cancel(true);
			} else {
				wantForcedMove = false;
				engine.stop();
				try {
					playingThreadsFuture.get();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				} catch (ExecutionException ee) {
					ee.printStackTrace();
				}

			}
		}
		playingThreadsFuture = null;

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if (e.getValueIsAdjusting()) {
			return;
		}

		if (e.getSource() == tableOpeningBook.getSelectionModel()) {

			int selectedRow = tableOpeningBook.getSelectionModel().getLeadSelectionIndex();

			if (selectedRow != -1) {

				interruptPlayingThread();

				Move m = ((OpeningBookTableModel) tableOpeningBook.getModel()).getMoveAt(selectedRow);

				// If the move is made from here, it doesn't come into the
				// variationTree
				int result = addVariation(m);

				if (result == NewVariationDialog.CANCEL) {
					computersTurn = false;
					resumePlay();
					return;
				}

				makeMove(m);

				computersTurn = true;

				resumePlay();

			}
		}

	}

	private List<Move> getMovesFromStart() {
		List<Move> movesFromStart = new LinkedList<Move>();

		VariationNode helperNode = currentMove;
		while (!helperNode.isRoot()) {
			movesFromStart.add(0, helperNode.getMove());
			helperNode = helperNode.getEarlierMove();
		}

		return movesFromStart;
	}

	@Override
	public void caretUpdate(CaretEvent ce) {

		if (ce.getSource() == sheet && sheet.isFocusOwner()) {

			interruptPlayingThread();

			List<Integer> variationList = actuallyPlayedMoves.getPositionFromCaret(ce.getDot());

			while (!currentMove.isRoot()) {
				currentPosition.unmakeMove(currentMove.getMove());
				currentMove = currentMove.getEarlierMove();

				numberOfPlayedMoves--;

			}

			Move toMake = null;
			Iterator<Integer> it = variationList.iterator();

			while (it.hasNext()) {
				Integer i = it.next();
				currentMove = currentMove.getVariation(i);
				toMake = currentMove.getMove();
				currentPosition.makeMove(toMake);
				numberOfPlayedMoves++;

			}

			screen.redrawPosition();
			screen.repaint();

			repaintToMoves();

			computersTurn = false;
			resumePlay();
		}

	}

	public int addVariation(Move m) {

		boolean anotherOption = currentMove.isVariation();

		int result = NewVariationDialog.OVERRIDE;

		if (anotherOption) {
			result = NewVariationDialog.showDialog();

			if (result == NewVariationDialog.NEW_VARIATION) {
				int variation = currentMove.addFurtherOption(m);
				currentMove = currentMove.getVariation(variation);
			} else if (result == NewVariationDialog.NEW_MAIN_VARIATION) {
				currentMove.addNewMain(m);
				currentMove = currentMove.getVariation(0);
			} else if (result == NewVariationDialog.OVERRIDE) {
				currentMove.override(m);
				currentMove = currentMove.getVariation(0);
			} else if (result == NewVariationDialog.CANCEL) {
				screen.redrawPosition();
				screen.repaint();
				computersTurn = false;

			}

		}

		else {
			currentMove.addFurtherOption(m);
			currentMove = currentMove.getVariation(0);
		}

		return result;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

	}

	public int getNumberOfPlayedMoves() {
		return numberOfPlayedMoves;
	}

	public void searchForOpeningMoves() {
		try {
			((OpeningBookTableModel) tableOpeningBook.getModel())
					.setOpeningMoves(OpeningBookManager.getInstance().getMoveStatistiks(currentPosition));
		} catch (Exception e) {
			((OpeningBookTableModel) tableOpeningBook.getModel()).setOpeningMoves(new LinkedList<MoveAndStatistik>());

		}

	}

	public Move tryToBlackFromBook() {

		List<MoveAndStatistik> listOfMoves = ((OpeningBookTableModel) tableOpeningBook.getModel())
				.getMoveAndStatistiks();

		if (listOfMoves.size() > 0) {
			int i = 0;
			int rangeRandomNumber = 0;
			int d = 0;
			String weight;
			int[] weights = new int[listOfMoves.size()];
			int indexOfPoint;
			while (i < listOfMoves.size()) {
				weight = listOfMoves.get(i).getWeight();
				indexOfPoint = weight.indexOf(".");
				try {
					d = Integer.valueOf(weight.substring(0, indexOfPoint)
							.concat(weight.substring(indexOfPoint + 1, indexOfPoint + 3)).trim());
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}

				rangeRandomNumber += d;
				weights[i] = rangeRandomNumber;
				i++;
			}

			Random random = new Random();
			int randomNumber = random.nextInt(rangeRandomNumber);

			i = 0;
			while (randomNumber > weights[i]) {
				i++;
			}

			Move m = listOfMoves.get(i).getMove();

			return m;
		} else {
			return null;
		}

	}

	public void restoreSettingsFromLastTime() {

		if (!SettingsManager.getInstance().canRestoreSettingsFromLastSession()) {
			return;
		}

		Integer depth = SettingsManager.getInstance().getLastDepth();
		if (depth != null)
			engine.setDepth(depth);

		Integer flip = SettingsManager.getInstance().getLastFlip();
		if (flip != null) {

		}

	}

	public void setEngine(IEngine engine) {
		this.engine = engine;
	}

	public IEngine getEngine() {
		return engine;
	}

	@Override
	public void windowStateChanged(WindowEvent e) {

	}

	public JLabel getScoreLabel() {
		return scoreLabel;
	}

}
