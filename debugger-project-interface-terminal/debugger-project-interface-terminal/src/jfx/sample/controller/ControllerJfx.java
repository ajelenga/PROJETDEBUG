package jfx.sample.controller;

import dbg.JDISimpleDebugger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ControllerJfx implements Initializable {

    @FXML
    private TextField command;
    @FXML
    private TextArea consoleUi;
    @FXML
    private Button continueC;
    @FXML
    private MenuItem exit;
    @FXML
    private TextField fileNameDebugg;
    @FXML
    private Button stack;
    @FXML
    private Button startDebugg;
    @FXML
    private Button executeCmd;
    private Button step;
    @FXML
    private Button step_over;

    public TextArea getConsoleUi() {
        return consoleUi;
    }

    @FXML
    void exitProgram(ActionEvent event) {

    }

    @FXML
    void executeCmd(ActionEvent event) {
        handleTextFieldAction();
    }

    @FXML
    void startDebugg(ActionEvent event) {

        String fileNameToDebugg = fileNameDebugg.getText();
        try {
            //Pour mettre le nom de la classe sur l'interface
            new JDISimpleDebugger(Class.forName("dbg." + fileNameToDebugg));


        } catch (ClassNotFoundException e) {
            //System.err.println("Le nom de cette claase n'en est pas une");
            fileNameDebugg.setStyle("-fx-text-box-border: #B22222; -fx-focus-color: #B22222;");
        }

    }

    @FXML
    private void handleTextFieldAction() {
        String input = command.getText();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            System.out.println(s);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new SDTest();
    }
}
