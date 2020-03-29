package UI;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Model.VariationNode;

public class ChooseVariationDialog extends JDialog implements ListSelectionListener{

	private JList<String> listOfVariations;

	public ChooseVariationDialog(VariationNode node) {

		setTitle("Choose the variation!");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JPanel contentPanel = (JPanel) getContentPane();

		contentPanel.setLayout(new BorderLayout());

		String[] list = new String[node.getNextMove().size()];

		int i=0;
		for (VariationNode vn : node.getNextMove()) {

			list[i] = vn.getMove().toString();
			i++;

		}

		listOfVariations = new JList<String>(list);
		listOfVariations.addListSelectionListener(this);
		
		
		
		contentPanel.add(listOfVariations, BorderLayout.CENTER);

	}

	public static VariationNode showDialog(VariationNode node) {
		ChooseVariationDialog dialog = new ChooseVariationDialog(node);

		dialog.pack();
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		

		return node.getVariation(dialog.listOfVariations.getSelectedIndex());

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()){
			return;
		}
		if(listOfVariations.getSelectedIndex()!=-1)
		dispose();
		
	}

}
