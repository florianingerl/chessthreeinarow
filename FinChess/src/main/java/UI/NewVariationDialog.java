package UI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NewVariationDialog extends JDialog implements ActionListener {


	public static final int NEW_VARIATION = 1;
	public static final int OVERRIDE = 2;
	public static final int NEW_MAIN_VARIATION = 3;
	public static final int CANCEL = 4;
	private static final int NO_OPTION = -1;

	private int result = NewVariationDialog.NO_OPTION;

	private JButton newVariation = new JButton("New Variation");
	private JButton override = new JButton("Override");
	private JButton newMainVariation = new JButton("New main variation");
	private JButton cancel = new JButton("Cancel");

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newVariation) {
			result = NewVariationDialog.NEW_VARIATION;
		} else if (e.getSource() == override) {
			result = NewVariationDialog.OVERRIDE;
		} else if (e.getSource() == newMainVariation) {
			result = NewVariationDialog.NEW_MAIN_VARIATION;
		} else if (e.getSource() == cancel) {
			result = NewVariationDialog.CANCEL;
		}
		dispose();

	}

	private NewVariationDialog() {

		JPanel contentPanel = (JPanel) this.getContentPane();
		contentPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc;

		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		contentPanel.add(newVariation, gbc);

		gbc = ChessboardDisplay.setGbcValues(0, 1, 1, 1);
		contentPanel.add(override, gbc);

		gbc = ChessboardDisplay.setGbcValues(0, 2, 1, 1);
		contentPanel.add(newMainVariation, gbc);

		gbc = ChessboardDisplay.setGbcValues(0, 3, 1, 1);
		contentPanel.add(cancel, gbc);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				Toolkit.getDefaultToolkit().beep();
			}
		});
		
		
		newVariation.addActionListener(this);
		override.addActionListener(this);
		newMainVariation.addActionListener(this);
		cancel.addActionListener(this);
		
		

	}

	public static int showDialog() {
		NewVariationDialog dialog = new NewVariationDialog();
		dialog.pack();
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		

		return dialog.result;
	}

	

}
