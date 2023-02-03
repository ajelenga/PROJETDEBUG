package jfx.sample;

import dbg.JDISimpleDebuggee;
import dbg.JDISimpleDebugger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class DebuggerApp extends Application {

    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        //Pour mettre le nom de la classe directement sans passer par l'interface
        new JDISimpleDebugger(JDISimpleDebuggee.class);
        //launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource("view/debugger.fxml"));
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setTitle("DebuggerJfx");
        primaryStage.show();
    }
}
