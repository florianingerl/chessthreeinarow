package Model;

import javax.swing.JOptionPane;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class OptionPaneLogger extends AppenderSkeleton {

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requiresLayout() {
		
		return true;
	}

	@Override
	protected void append(LoggingEvent le) {
		JOptionPane.showMessageDialog(null, getLayout().format(le));
	}

}
