package UI.JavaFX;

import javafx.event.ActionEvent;

public class MainWindowController {

	public void handleAboutAction(ActionEvent event)
	{
		System.out.println("About was clicked!");
	}
	
	public void handleExitAction(ActionEvent event)
	{
		System.exit(0);
	}
	
}
