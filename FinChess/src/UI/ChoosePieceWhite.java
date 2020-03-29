package UI;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;

import Model.BasicEngine;
import Model.Move;


public class ChoosePieceWhite extends JDialog{
	
	
	public ChoosePieceWhite(final Move m){
		
		JPanel contentpanel = (JPanel) getContentPane();
		
		contentpanel.setLayout(new BorderLayout());
		
		final JSpinner spinner = new JSpinner(new SpinnerListModel(Arrays.<Integer>asList(BasicEngine.W_QUEEN,
				BasicEngine.W_KNIGHT,BasicEngine.W_ROOK, BasicEngine.W_BISHOP)));
		
		spinner.setEditor(new IconEditor(spinner));
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				
				Integer prom = (Integer) spinner.getValue();
				
				m.setProm(prom);
				
				ChoosePieceWhite.this.dispose();
			}
			
			
			
		});
		
		addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					
					Integer prom = (Integer) spinner.getValue();
					
					m.setProm(prom);
					
					ChoosePieceWhite.this.dispose();
				}
				}
			
			
		});
		
		
		contentpanel.add(spinner,BorderLayout.CENTER);
		
		
	}
	
}
