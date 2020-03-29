package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class IconEditor extends JLabel implements ChangeListener {

	private JSpinner spinner;
	private Integer piece;

	public IconEditor(JSpinner spinner) {
		setOpaque(true);
		setBackground(Color.BLACK);
		piece = (Integer) spinner.getValue();
		repaint();
		this.spinner = spinner;
		spinner.addChangeListener(this);

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		piece = (Integer) spinner.getValue();
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		BufferedImage image = Chessboard.piecesImages.get(piece);
		g.drawImage(image, (getSize().width - image.getWidth()) / 2,
				(getSize().height - image.getHeight()) / 2, null);

	}

}
