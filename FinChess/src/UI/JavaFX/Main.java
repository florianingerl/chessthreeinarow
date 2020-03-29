package UI.JavaFX;

import fxmlexample.FXMLExample;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] arguments)
	   {
	      Application.launch(Main.class, arguments);
	   }


	   @Override
	   public void start(Stage stage) throws Exception {
		   Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
	   
	       Scene scene = new Scene(root, 800, 800);
	   
	       stage.setTitle("Flori's chess program");
	       stage.setScene(scene);
	       stage.show();
	   }

}
