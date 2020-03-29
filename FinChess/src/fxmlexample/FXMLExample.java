package fxmlexample;

import java.io.File;
import java.io.FileInputStream;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;



public class FXMLExample extends Application
{

   public static void main(String[] arguments)
   {
      Application.launch(FXMLExample.class, arguments);
   }


   @Override
   public void start(Stage stage) throws Exception {
	   Parent root = FXMLLoader.load(getClass().getResource("fxml_example.fxml"));
   
       Scene scene = new Scene(root, 300, 275);
   
       stage.setTitle("FXML Welcome");
       stage.setScene(scene);
       stage.show();
   }
}