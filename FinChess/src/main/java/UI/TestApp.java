package UI;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class TestApp extends Application {

	public static void main(String [] args){
		Application.launch(args);
	}
	
	@Override
	public void start(Stage stage){
		 Scene scene = new Scene(new ChessboardFX(), 800, 800);
         stage.setTitle("Hello World Example");        
         stage.setScene(scene);        
         stage.show();  
	}


}