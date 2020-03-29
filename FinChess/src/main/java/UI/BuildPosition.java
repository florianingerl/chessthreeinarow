package UI;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import Model.BasicEngine;
import Model.Bitboards;
import Model.Position;

public class BuildPosition extends JDialog implements ActionListener{

	private BuildPositionChessboard screen;
	
	private JButton b_wPawn;
	private JButton b_wKnight;
	private JButton b_wBishop;
	private JButton b_wRook;
	private JButton b_wQueen;
	private JButton b_wKing;

	private JButton b_bPawn;
	private JButton b_bKnight;
	private JButton b_bBishop;
	private JButton b_bRook;
	private JButton b_bQueen;
	private JButton b_bKing;

	private JButton ok;
	private JButton cancel;
	private JCheckBox blackToMove;
	private JCheckBox whiteCastleShort;
	private JCheckBox whiteCastleLong;
	private JCheckBox blackCastleShort;
	private JCheckBox blackCastleLong;
	private JCheckBox jcb_epSquare;

	private ChessboardDisplay display;
	
	
	public BuildPosition(ChessboardDisplay display) {
		this.display = display;
		
		JPanel contentPanel = (JPanel) getContentPane();

		contentPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		JPanel playingPanel = new JPanel(new GridBagLayout());

		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0;

		contentPanel.add(playingPanel, gbc);

		screen = new BuildPositionChessboard(this);
		screen.setBorder(new LineBorder(Color.BLACK));
		
		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		playingPanel.add(screen, gbc);

		JPanel controlPanel = new JPanel(new GridBagLayout());
		gbc = ChessboardDisplay.setGbcValues(1, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;

		contentPanel.add(controlPanel, gbc);

		JLabel labelWhitePieces = new JLabel("White");

		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		controlPanel.add(labelWhitePieces, gbc);

		JPanel whitePiecesPanel = new JPanel(new GridLayout(2, 3));
		gbc = ChessboardDisplay.setGbcValues(0, 1, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 30, 0);
		controlPanel.add(whitePiecesPanel, gbc);

		Image img = null;
		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/WhitePawnIcon.png"));
			b_wPawn = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_wPawn = new JButton("whitePawn");
		}
		whitePiecesPanel.add(b_wPawn);
		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/WhiteKnightIcon.png"));
			b_wKnight = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_wKnight = new JButton("whiteKnight");
		}

		whitePiecesPanel.add(b_wKnight);
		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/WhiteBishopIcon.png"));
			b_wBishop = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_wBishop = new JButton("whiteBishop");
		}

		whitePiecesPanel.add(b_wBishop);

		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/WhiteRookIcon.png"));
			b_wRook = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_wRook = new JButton("whiteRook");
		}

		whitePiecesPanel.add(b_wRook);

		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/WhiteQueenIcon.png"));
			b_wQueen = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_wQueen = new JButton("whiteQueen");
		}

		whitePiecesPanel.add(b_wQueen);
		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/WhiteKingIcon.png"));
			b_wKing = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_wKing = new JButton("whiteKing");
		}

		whitePiecesPanel.add(b_wKing);

		JLabel labelBlackPieces = new JLabel("Black");

		gbc = ChessboardDisplay.setGbcValues(0, 2, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;

		controlPanel.add(labelBlackPieces, gbc);

		JPanel blackPiecesPanel = new JPanel(new GridLayout(2, 3));
		gbc = ChessboardDisplay.setGbcValues(0, 3, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 30, 0);
		controlPanel.add(blackPiecesPanel, gbc);

		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/BlackPawnIcon.png"));
			b_bPawn = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_bPawn = new JButton("blackPawn");
		}
		blackPiecesPanel.add(b_bPawn);
		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/BlackKnightIcon.png"));
			b_bKnight = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_bKnight = new JButton("blackKnight");
		}

		blackPiecesPanel.add(b_bKnight);
		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/BlackBishopIcon.png"));
			b_bBishop = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_bBishop = new JButton("blackBishop");
		}

		blackPiecesPanel.add(b_bBishop);

		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/BlackRookIcon.png"));
			b_bRook = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_bRook = new JButton("blackRook");
		}

		blackPiecesPanel.add(b_bRook);

		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/BlackQueenIcon.png"));
			b_bQueen = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_bQueen = new JButton("blackQueen");
		}

		blackPiecesPanel.add(b_bQueen);
		try {
			img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"ImagesOfPieces/BlackKingIcon.png"));
			b_bKing = new JButton(new ImageIcon(img));
		} catch (IOException e) {
			b_bKing = new JButton("blackKing");
		}

		blackPiecesPanel.add(b_bKing);

		blackToMove = new JCheckBox("Black to move");

		gbc = ChessboardDisplay.setGbcValues(0, 4, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 30, 0);
		controlPanel.add(blackToMove, gbc);

		jcb_epSquare = new JCheckBox("En passent Square");
		gbc = ChessboardDisplay.setGbcValues(0, 5, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0,0,30,0);
		controlPanel.add(jcb_epSquare, gbc);
		
		JPanel castleOptionsPanel = new JPanel(new GridLayout(4, 1));

		whiteCastleShort = new JCheckBox("White O-O Castle");
		castleOptionsPanel.add(whiteCastleShort);
		whiteCastleLong = new JCheckBox("White O-O-O Castle");
		castleOptionsPanel.add(whiteCastleLong);
		blackCastleShort = new JCheckBox("Black O-O Castle");
		castleOptionsPanel.add(blackCastleShort);
		blackCastleLong = new JCheckBox("Black O-O-O Castle");
		castleOptionsPanel.add(blackCastleLong);

		gbc = ChessboardDisplay.setGbcValues(0, 6, 1, 1);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weighty = 1;
		gbc.weightx = 1;
		controlPanel.add(castleOptionsPanel, gbc);

		JPanel finishPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		finishPanel.add(ok);
		finishPanel.add(cancel);

		gbc = ChessboardDisplay.setGbcValues(0, 7, 1, 1);
		gbc.fill = GridBagConstraints.BOTH;
		controlPanel.add(finishPanel, gbc);

		b_wPawn.addActionListener(this);
		b_wKnight.addActionListener(this);
		b_wBishop.addActionListener(this);
		b_wRook.addActionListener(this);
		b_wQueen.addActionListener(this);
		b_wKing.addActionListener(this);
		
		b_bPawn.addActionListener(this);
		b_bKnight.addActionListener(this);
		b_bBishop.addActionListener(this);
		b_bRook.addActionListener(this);
		b_bQueen.addActionListener(this);
		b_bKing.addActionListener(this);
		
		jcb_epSquare.addActionListener(this);
		
		ok.addActionListener(this);
		cancel.addActionListener(this);

		pack();
		setLocationRelativeTo(display);
		setModal(true);
		setVisible(true);

	}

	public static void main(String[] args) {

		new BuildPosition(null);

	}
	
	public boolean isBlackToMoveSelected(){
		return blackToMove.isSelected();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()== ok){
			
			screen.okClicked();
			dispose();
			
			return;
		}
		else if(e.getSource()==cancel){
			dispose();
			return;
		}
		else if(e.getSource()==jcb_epSquare){
			if(jcb_epSquare.isSelected()){
			screen.setLookingForEnPassentSquare(true);
			
			}
			else{
			screen.setEpSquare(0);
			}
		}
		
		else if(e.getSource()==b_wKnight){
			screen.setPieceCurrentlyAtHand(Position.W_KNIGHT);
		}
		else if(e.getSource()==b_wBishop){
			screen.setPieceCurrentlyAtHand(Position.W_BISHOP);
		}
		else if(e.getSource()==b_wRook){
			screen.setPieceCurrentlyAtHand(Position.W_ROOK);
		}
		
		else if(e.getSource()==b_bKnight){
			screen.setPieceCurrentlyAtHand(Position.B_KNIGHT);
		}
		else if(e.getSource()==b_bBishop){
			screen.setPieceCurrentlyAtHand(Position.B_BISHOP);
		}
		else if(e.getSource()==b_bRook){
			screen.setPieceCurrentlyAtHand(Position.B_ROOK);
		}
		
		
		
	}
	
	public void repaint(){
		screen.repaint();
	}

	
	public static Position getBuildPosition(ChessboardDisplay display){
		BuildPosition bP = new BuildPosition(display);
		
		return bP.screen.getPosition();
	}

	public boolean isWhiteCastleShortSelected() {
		return whiteCastleShort.isSelected();
	}

	public boolean isWhiteCastleLongSelected() {
		return whiteCastleLong.isSelected();
	}

	public boolean isBlackCastleShortSelected() {
		return blackCastleShort.isSelected();
	}

	public boolean isBlackCastleLongSelected() {
		return blackCastleLong.isSelected();
	}
	

}



