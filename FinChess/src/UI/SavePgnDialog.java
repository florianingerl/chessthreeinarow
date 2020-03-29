package UI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.*;

import org.hamcrest.core.IsNull;

import Model.PgnGame;

public class SavePgnDialog extends JDialog implements ActionListener{
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == ok){
			
			if(isNullOrWhiteSpace(textWhite.getText())){
				JOptionPane.showMessageDialog(this, "Please enter the white player!");
				return;
			}
			
			
			if( isNullOrWhiteSpace(textBlack.getText())){
				JOptionPane.showMessageDialog(this, "Please enter the black player!");
				return;
			}
			
			
			if(isNullOrWhiteSpace(textTournament.getText())){
				JOptionPane.showMessageDialog(this, "Please enter the tournament name!");
				return;
			}
			
			
			if(isNullOrWhiteSpace(textSite.getText())){
				JOptionPane.showMessageDialog(this, "Please enter the site!");
				return;
			}
			
			pgnGame = new PgnGame();
			pgnGame.setWhite(textWhite.getText());
			pgnGame.setBlack(textBlack.getText());
			pgnGame.setEvent(textTournament.getText());
			pgnGame.setSite(textSite.getText());
			
			pgnGame.setEloWhite((Integer) spinnerEloWhite.getValue());
			pgnGame.setEloBlack((Integer) spinnerEloBlack.getValue());
			pgnGame.setRound((int) spinnerRound.getValue());
			pgnGame.setDate((Date) spinnerDate.getValue());
			
			if(winWhite.isSelected()){
				pgnGame.setResult("1-0");
			}
			else if(draw.isSelected()){
				pgnGame.setResult("1/2-1/2");
			}
			else if(lossWhite.isSelected()){
				pgnGame.setResult("0-1");
			}
			
			
			this.dispose();
		}
		else if(e.getSource()==cancel){
			this.dispose();
		}
		
		
	}
	
	private PgnGame pgnGame;
	private JTextField textWhite = new JTextField(40);
	private JTextField textBlack = new JTextField(40);
	private JTextField textTournament = new JTextField(40);
	private JTextField textSite = new JTextField(40);
	
	private JSpinner spinnerEloWhite;
	private JSpinner spinnerEloBlack;
	private JSpinner spinnerRound;
	private JSpinner spinnerDate;
	
	private ButtonGroup buttonGroupResult = new ButtonGroup();
	private JRadioButton winWhite = new JRadioButton("1-0");
	private JRadioButton draw = new JRadioButton("1/2-1/2");
	private JRadioButton lossWhite = new JRadioButton("0-1");
	
	private JButton ok = new JButton("OK");
	private JButton cancel = new JButton("Cancel");
	
	public SavePgnDialog(){
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel contentPanel = (JPanel) getContentPane();
		
		contentPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc;
		
		JLabel white = new JLabel("White:");
		white.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0, 0, 1, 1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(white, gbc);
		gbc = ChessboardDisplay.setGbcValues(1, 0, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		contentPanel.add(textWhite,gbc);
	
		JLabel black = new JLabel("Black:");
		black.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0,1,1,1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(black, gbc);
		gbc = ChessboardDisplay.setGbcValues(1,1,2,1);
		gbc.fill = GridBagConstraints.BOTH;
		contentPanel.add(textBlack, gbc);
		
		JLabel tournament = new JLabel("Tournament:");
		tournament.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0, 2, 1, 1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(tournament, gbc);
		gbc = ChessboardDisplay.setGbcValues(1,2,2,1);
		gbc.fill = GridBagConstraints.BOTH;
		contentPanel.add(textTournament, gbc);
		
		JLabel site = new JLabel("Site:");
		site.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0, 3, 1, 1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(site, gbc);
		gbc = ChessboardDisplay.setGbcValues(1, 3, 2, 1);
		gbc.fill = GridBagConstraints.BOTH;
		contentPanel.add(textSite, gbc);
		
		JLabel eloWhite = new JLabel("Elo White:");
		eloWhite.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0, 4, 1, 1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(eloWhite, gbc);
		spinnerEloWhite = new JSpinner(new SpinnerNumberModel(2000, 0, 4000, 1));
		gbc = ChessboardDisplay.setGbcValues(1,4,1,1);
		gbc.anchor = GridBagConstraints.WEST;
		contentPanel.add(spinnerEloWhite, gbc);
		
		JLabel eloBlack = new JLabel("Elo Black:");
		eloBlack.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0, 5, 1, 1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(eloBlack, gbc);
		spinnerEloBlack = new JSpinner(new SpinnerNumberModel(2000, 0, 4000, 1));
		gbc = ChessboardDisplay.setGbcValues(1,5,1,1);
		gbc.anchor = GridBagConstraints.WEST;
		contentPanel.add(spinnerEloBlack, gbc);
		
		JLabel round = new JLabel("Round:");
		round.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0,6,1,1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(round, gbc);
		spinnerRound = new JSpinner(new SpinnerNumberModel(1,1,50, 1));
		gbc = ChessboardDisplay.setGbcValues(1,6,1,1);
		gbc.anchor = GridBagConstraints.WEST;
		contentPanel.add(spinnerRound, gbc);
		
		JLabel date = new JLabel("Date:");
		date.setHorizontalAlignment(SwingConstants.TRAILING);
		gbc = ChessboardDisplay.setGbcValues(0, 7, 1, 1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0,0, 0, 5);
		contentPanel.add(date, gbc);
		
		spinnerDate = new JSpinner(new SpinnerDateModel());
		gbc = ChessboardDisplay.setGbcValues(1,7,1,1);
		gbc.anchor = GridBagConstraints.WEST;
		contentPanel.add(spinnerDate, gbc);
		
		JPanel buttonPanelResult = new JPanel(new GridLayout(3,1));
		buttonPanelResult.setBorder(BorderFactory.createTitledBorder("Result"));
		buttonPanelResult.add(winWhite);
		buttonPanelResult.add(draw);
		buttonPanelResult.add(lossWhite);
		buttonGroupResult.add(winWhite);
		buttonGroupResult.add(draw);
		buttonGroupResult.add(lossWhite);
		
		gbc = ChessboardDisplay.setGbcValues(2,4,1,4);
		contentPanel.add(buttonPanelResult,gbc);
		
		winWhite.setSelected(true);
		
		ok.addActionListener(this);
		cancel.addActionListener(this);
		
		JPanel okCancelPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		okCancelPanel.add(ok);
		okCancelPanel.add(cancel);
		gbc = ChessboardDisplay.setGbcValues(2, 8, 3, 1);
		contentPanel.add(okCancelPanel, gbc);
		
		
		
	}
	
	public PgnGame open(){
		pack();
		setResizable(false);
		setModal(true);
		setVisible(true);
		
		return pgnGame;
		
		
	}
	
	private static boolean isNullOrWhiteSpace(String s)
	{
		if( s == null) return true;
		else return s.matches("\\s*");
	}

	
	
	
	
	
}
