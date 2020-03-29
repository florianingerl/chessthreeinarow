package UI;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import Model.BasicEngine;
import Model.InvalidFenStringException;
import Model.Move;
import Model.Position;

public class DragHumanMoveGetter extends Chessboard implements HumanMoveGetter,
		MouseListener, MouseMotionListener {

	private Logger logger = Logger.getLogger(DragHumanMoveGetter.class);
	
	private Lock lock = new ReentrantLock();

	private Condition moveDragAttempt = lock.newCondition();

	private boolean mouseExited = false;

	private boolean pieceCurrentlyDragged = false;

	private volatile int mouseX;
	private volatile int mouseY;

	private double relativeToBoundingX = -1000;
	private double relativeToBoundingY = -1000;

	private int pieceMovedX = -1;
	private int pieceMovedY = -1;
	
	List<int [] > circles = null;

	public DragHumanMoveGetter(Position currentPosition) {
		super(currentPosition);

	}

	public DragHumanMoveGetter(String fenString)
			throws InvalidFenStringException {
		super(fenString);
	}

	public DragHumanMoveGetter() {

	}

	private void attachListeners() {
		addMouseListener(this);
		addMouseMotionListener(this);

	}

	@Override
	public void paintComponent(Graphics graphics) {

		paintBackground(graphics);

		if( circles != null)
		{
			for( int [] circle : circles)
			{
				graphics.setColor(Color.BLUE);
				graphics.fillOval((int)(((circle[0] * 100)*scale)),(int)( (700 - circle[1] * 100) * scale),(int)( 100 * scale),(int)( 100 * scale));
			}
		}
		
		for (int i = 0; i <= 7; i++) {
			for (int j = 0; j <= 7; j++) {

				if (squares[i][j] != Position.EMPTY
						&& !(i == pieceMovedX && j == pieceMovedY))
					graphics.drawImage(
							scaledPiecesImages.get(squares[i][j]),
							(int) ((i * 100 + 50) * scale - this.scaledPiecesImages
									.get(squares[i][j]).getWidth() / 2.0f),
							(int) ((700 - j * 100 + 50) * scale - scaledPiecesImages
									.get(squares[i][j]).getHeight() / 2.0f),
							null);
			}
		}

		if (pieceMovedX != -1) {

			graphics.drawImage(scaledPiecesImages.get(squares[pieceMovedX][pieceMovedY]),
					(int) (mouseX - relativeToBoundingX),
					(int) (mouseY - relativeToBoundingY), null);
		}

	}

	@Override
	public Move getMove() throws InterruptedException {

		try {
			attachListeners();

			setMakingMoveHelpersToNone();

			Move m = null;

			while (m == null) {
				
				logger.debug("Wait friendly piece dragged! " + Thread.currentThread().getId());
				waitFriendlyPieceDragged();

				logger.debug("Friendly piece was dragged!" + Thread.currentThread().getId());
				waitFriendlyPieceReleasedFromDrag();
				logger.debug("Fiendly piece was released from drag!" + Thread.currentThread().getId());
				
				if (mouseExited) {
					setMakingMoveHelpersToNone();
					repaint();
					continue;
				}

				m = anchorPiece();
			}

			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR) );
			return m;
		} finally {
			detachListeners();
		}
	}

	private void detachListeners() {
		removeMouseListener(this);
		removeMouseMotionListener(this);

	}

	public Move anchorPiece() {

		int i = (int) Math.round((mouseX / (100 * scale)));
		int j = (int) Math.round((7 - (mouseY / (100 * scale))));

		int from = parseSquareToBit(pieceMovedX, pieceMovedY);
		int tosq = parseSquareToBit(i, j);
		squares[i][j] = squares[pieceMovedX][pieceMovedY];
		squares[pieceMovedX][pieceMovedY] = Position.EMPTY;

		Move m = position.getMove(from, tosq);

		if (position.isMoveLegal(m)) {

			if (m.getProm() == BasicEngine.W_QUEEN) {
				ChoosePieceWhite choose = new ChoosePieceWhite(m);

				choose.setModal(true);
				choose.setSize(300, 300);
				choose.setLocationRelativeTo(this);
				choose.setVisible(true);
			}
			if (m.getProm() == BasicEngine.B_QUEEN) {
				ChoosePieceBlack choose = new ChoosePieceBlack(m);

				choose.setModal(true);
				choose.setSize(300, 300);
				choose.setLocationRelativeTo(this);
				choose.setVisible(true);

			}

			setMakingMoveHelpersToNone();
			return m;

		} else {

			redrawPosition();
			setMakingMoveHelpersToNone();
			repaint();
			return null;
		}

	}

	public void setMakingMoveHelpersToNone() {

		mouseX = -1;
		mouseY = -1;
		relativeToBoundingX = -1000;
		relativeToBoundingY = -1000;
		pieceMovedX = -1;
		pieceMovedY = -1;
		pieceCurrentlyDragged = false;
		mouseExited = false;

	}

	public void waitFriendlyPieceReleasedFromDrag() throws InterruptedException {

		lock.lock();
		try {
			moveDragAttempt.await();
			circles = null;
		} finally {
			lock.unlock();
		}

	}

	public void waitFriendlyPieceDragged() throws InterruptedException {
		lock.lock();
		try {
			moveDragAttempt.await();
		}
		finally {
			lock.unlock();
		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (pieceCurrentlyDragged) {

			mouseX = e.getX();
			mouseY = e.getY();

			repaint();

		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		if(pieceCurrentlyDragged)
		{
			return;
		}
		
		final int i = (int) Math.round((e.getX() / (100 * scale)));
		final int j = (int) Math.round((7 - (e.getY() / (100 * scale))));

		if (!((0 <= i) && (i <= 7) && (0 <= j) && (j <= 7))
				|| squares[i][j] == Position.EMPTY) {

			setCursor(new Cursor(Cursor.DEFAULT_CURSOR) );
		}
		List<Move> moves = position.getMovesOfPieceOn( parseSquareToBit(i,j));
		if( moves.size() > 0 )
		{
			setCursor(new Cursor(Cursor.HAND_CURSOR) );
		}
		else
		{
			setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
		}

	}

	private boolean moveStarted(MouseEvent e) {

		final int i = (int) Math.round((e.getX() / (100 * scale)));
		final int j = (int) Math.round((7 - (e.getY() / (100 * scale))));

		if (!((0 <= i) && (i <= 7) && (0 <= j) && (j <= 7))
				|| squares[i][j] == Position.EMPTY) {

			return false;
		}

		mouseX = e.getX();
		mouseY = e.getY();

		pieceMovedX = i;
		pieceMovedY = j;
		
		List<Move> moves = position.getMovesOfPieceOn( parseSquareToBit(i, j) );
		logger.debug("Setting circles to new values!");
		circles = new LinkedList<int []>();
		
		for(Move move : moves)
		{	
			circles.add( parseBitToSquare(move.getTosq()) );
		}

		relativeToBoundingX = (e.getX() % (100 * scale));
		relativeToBoundingY = (e.getY() % (100 * scale));

		return true;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (moveStarted(e)) {
			lock.lock();
			pieceCurrentlyDragged = true;
			moveDragAttempt.signal();
			lock.unlock();

		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (pieceCurrentlyDragged) {
			lock.lock();

			pieceCurrentlyDragged = false;

			relativeToBoundingX = -1000;
			relativeToBoundingY = -1000;

			mouseX = e.getX();
			mouseY = e.getY();

			moveDragAttempt.signal();
			lock.unlock();
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseExited = false;

	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseExited = true;

	}

}
