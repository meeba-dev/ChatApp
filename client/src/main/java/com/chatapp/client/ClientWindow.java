package com.chatapp.client;


import com.chatapp.network.TCPConnection;
import com.chatapp.network.TCPConnectionListener;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientWindow extends Application implements TCPConnectionListener {

    private static final String IP_ADDR = "127.0.0.1";
    private static final int PORT = 8189;
    private static final int WIDTH = 350;
    private static final int HEIGHT = 400;

    private TCPConnection connection;

    private final TextArea log = new TextArea();
    private final Label nickname = new Label("meeba");
    private final TextField fieldInput = new TextField();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Pane pane = new Pane();

        fieldInput.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        fieldInput.setFocusTraversable(false);
        fieldInput.setMinSize(250, 40);
        fieldInput.setLayoutX(50);
        fieldInput.setLayoutY(350);
        fieldInput.setPromptText("Write a message...");
        fieldInput.setOnAction(event -> {
            try {
                String msg = fieldInput.getText();
                if (msg.equals("")) return;
                fieldInput.setText(null);
                connection.sendString(nickname.getText() + ": " + msg);
                saveLogs(msg);
            } catch (NullPointerException e) {
                System.out.println("Empty message...");
            } catch (FileNotFoundException e) {
                System.out.println("File is not found...");
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        log.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        log.setPrefSize(345, 300);
        log.setEditable(false);
        log.setWrapText(true);
        log.setLayoutX(3);
        log.setLayoutY(3);

        pane.getChildren().addAll(log, fieldInput);
        primaryStage.setTitle("ChatApp");
        primaryStage.setScene(new Scene(pane, WIDTH, HEIGHT));
        primaryStage.show();

        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready ... ");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    private synchronized void printMsg(String msg) {
        log.appendText(msg + '\n');
        log.positionCaret(log.getText().length());
    }

    private synchronized void saveLogs(String msg) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String path = "./client/src/main/resources/logs.txt";
        PrintStream printStream = new PrintStream(new FileOutputStream(path, true));
        printStream.append(sdf.format(new Date()) + " " + nickname.getText() + ": " + msg + '\n');
        printStream.close();
    }

}
