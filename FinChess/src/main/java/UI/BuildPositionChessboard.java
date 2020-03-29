package UI;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import Model.BasicEngine;
import Model.Bitboards;
import Model.Position;

public class BuildPositionChessboard extends Chessboard implements
		MouseListener, MouseMotionListener {

	private boolean lookingForEnPassentSquare = false;
	private BuildPosition buildPosition;

	private Robot robot = null;

	private int pieceCurrentlyAtHand = 0;
	private int mouseX;
	private int mouseY;

	private int epSquare = 0;

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 800);
	}
	
	public void emptyBoard(){
		for(int i=0; i  < 8; i++){
			for(int j=0; j < 8; j++){
				squares[i][j] = Position.EMPTY;	
			}
		}
		
		epSquare = 0;
		
	}

	public BuildPositionChessboard(BuildPosition buildPosition) {

		this.buildPosition = buildPosition;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		try {
			robot = new Robot();
		} catch (AWTException awtException) {
			JOptionPane.showMessageDialog(this, "Got an awtException!");
		}
		
		emptyBoard();
		repaint();
		
		
		

	}

	public Point getMiddlePointOnScreen() {

		computeScale();
		Point p = this.getLocationOnScreen();
		p.x = (int) (p.x + scale*400);
		p.y = (int) (p.y + scale*400);

		return p;
	}

	public Point getEpCandidateSquareOnScreen() {
		Point p = this.getLocationOnScreen();
		p.y = (int) (p.y + 500*scale);
		return p;
	}

	public void setLookingForEnPassentSquare(boolean lookingForEnPassentSquare) {
		this.lookingForEnPassentSquare = lookingForEnPassentSquare;
		if (lookingForEnPassentSquare) {
			Point p = getEpCandidateSquareOnScreen();
			robot.mouseMove(p.x, p.y);
			repaint();

		}
		else{
			epSquare = 0;
			repaint();
			
		}

	}

	@Override
	public void paintComponent(Graphics graphics) {

		Graphics2D g = (Graphics2D) graphics;
		super.paintComponent(g);
		// draws the background and the pieces in int[][] squares;

		// don't forget scale
		if (pieceCurrentlyAtHand != 0) {
			BufferedImage image = piecesImages.get(pieceCurrentlyAtHand);
			double x = 1/scale;
			g.drawImage(image, (int) ((mouseX - image.getWidth() *scale / 2)*x),
					(int) ((mouseY - image.getHeight() * scale/ 2)*x), null);
			

		} else if (lookingForEnPassentSquare) {
			g.setColor(Color.RED);
			int i = (int) (mouseX / (100*scale));
			int j = (int) (7 - (mouseY / (100*scale)));
			g.fillRect((int) (i * 100*scale),(int) ((700 - j * 100)*scale), (int) (100*scale), (int) (100*scale));
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();

		repaint();

	}

	public void setPieceCurrentlyAtHand(int pieceCurrentlyAtHand) {
		this.pieceCurrentlyAtHand = pieceCurrentlyAtHand;
		Point p = this.getMiddlePointOnScreen();
		robot.mouseMove(p.x, p.y);
		
		repaint();
		
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (pieceCurrentlyAtHand != 0) {
			int i = (int) (mouseX / (100*scale));
			int j = (int) (7 - (mouseY / (100*scale)));

			squares[i][j] = pieceCurrentlyAtHand;

			pieceCurrentlyAtHand = 0;
			repaint();
		} else if (lookingForEnPassentSquare) {
			int i = (int) (mouseX / (100*scale));
			int j = (int) (7 - (mouseY / (100*scale)));

			epSquare = parseSquareToBit(i, j);
			lookingForEnPassentSquare = false;
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {

		mouseX = e.getX();
		mouseY = e.getY();
		repaint();

	}

	public void okClicked() {

		position = new Position();

		int[] square = new int[64];

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {

				square[parseSquareToBit(i, j)] = squares[i][j];

			}
		}

		position.setSquare(square);

		byte nextMove = 1;
		if (buildPosition.isBlackToMoveSelected()) {
			nextMove = -1;
		}
		position.setNextMove(nextMove);

	}

	public void setEpSquare(int epSquare) {
		this.epSquare = epSquare;
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
