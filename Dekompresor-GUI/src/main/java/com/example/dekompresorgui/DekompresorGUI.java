package com.example.dekompresorgui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

public class DekompresorGUI extends Application {

    static boolean forceDecompression = false;
    static boolean isCipherActivated = false;
    static String filePath;
    static Process process;
    static Scanner scanner;


    @Override
    public void start(Stage stage) throws IOException {
        //czyścimy plik data po zamknięciu okna programu i zabijamy proces dekompresora
        //ma to na celu zapobiegnięcie niewłaściwym statusom dekompresora
        stage.setOnCloseRequest((WindowEvent we) -> {
            try {
                process.destroy();
            } catch (NullPointerException e) {
                System.exit(0);
            }
            try {
                FileWriter writer = new FileWriter("data");
                writer.write("");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Group root = new Group(); //główny korzeń nodeów
        Scene scene = new Scene(root);
        scene.setFill(Color.AZURE);

        Label title = new Label("Dekompresor wykorzystujący algorytm Huffmana");
        title.setFont(Font.font(32));
        title.setMinWidth(800);
        title.setAlignment(Pos.CENTER);


        Label authors = new Label("Autorzy: Adrian Chmiel, Mateusz Tyl");
        authors.setFont(Font.font(16));
        authors.setMinWidth(800);
        authors.setAlignment(Pos.CENTER);
        authors.setLayoutY(50);


        Label info = new Label("Proszę podać plik do dekompresji. Plik powinien pochodzić z kompresora stworzonego \n przez osoby wymienione powyżej. Proszę także wybrać pożądane ustawienia programu.");
        info.setMinWidth(800);
        info.setAlignment(Pos.CENTER);
        info.setLayoutY(80);

        //napis pokazujący ścieżkę do wybranego pliku
        Label showPath = new Label();
        showPath.setMinWidth(800);
        showPath.setAlignment(Pos.CENTER);
        showPath.setLayoutY(150);

        Button chooseFile = new Button("Kliknij tu i wybierz plik, który chcesz zdekompresować");
        chooseFile.setLayoutY(120);
        chooseFile.setLayoutX(250);

        VBox container1 = new VBox();
        container1.setSpacing(10);
        container1.setAlignment(Pos.BASELINE_CENTER);
        container1.setLayoutY(160);
        container1.setLayoutX(160);

        Label outputLabel = new Label("Wpisz nazwę pliku wyjściowego:");
        TextField input = new TextField();
        RadioButton rb1 = new RadioButton("Zaznacz, jeżeli chcesz żeby program wymusił dekompresję");
        RadioButton rb2 = new RadioButton("Zaznacz, jeżeli plik jest zaszyfrowany. Po zaznaczeniu wpisz klucz deszyfrujący poniżej");
        TextField cipher = new TextField();
        cipher.setEditable(false);
        cipher.setStyle("-fx-background-color: #f2f2f2;");
        Button decompress = new Button("Dekompresuj");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMinWidth(200);

        container1.getChildren().add(outputLabel);
        container1.getChildren().add(input);
        container1.getChildren().add(rb1);
        container1.getChildren().add(rb2);
        container1.getChildren().add(cipher);
        container1.getChildren().add(decompress);
        container1.getChildren().add(progressBar);

        root.getChildren().add(container1);

        Label info1 = new Label("");
        Label info2 = new Label("");
        root.getChildren().add(info1);
        root.getChildren().add(info2);
        info1.setLayoutY(380);
        info1.setLayoutX(100);

        root.getChildren().add(title);
        root.getChildren().add(authors);
        root.getChildren().add(info);
        root.getChildren().add(showPath);
        root.getChildren().add(chooseFile);

        //Co się ma zdarzyć kiedy użytkownik wybierze plik wejściowy
        chooseFile.setOnAction(value -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Wybierz plik do dekompresji");
            File selectedFile = fileChooser.showOpenDialog(stage);
            // Sprawdzanie, czy użytkownik wybrał plik
            if (selectedFile != null) {
                // Pobieranie ścieżki do wybranego pliku
                filePath = selectedFile.getAbsolutePath();
                showPath.setText("Ścieżka do pliku: " + filePath);
            }
        });
        //wybrane wymuszanie dekompresji
        rb1.setOnAction(value -> forceDecompression = true);
        //wybrane szyfrowanie
        rb2.setOnAction(value -> {
            isCipherActivated = true;
            cipher.setEditable(true);
            cipher.setStyle("");
        });
        //klknięty przycisk dekompresji
        decompress.setOnAction(value -> {
            String arguments;
            progressBar.setProgress(0);
            String guiPath = "\"" + Paths.get("").toAbsolutePath().toString() + "\\Dekompresor-GUI\\Dekompresor.jar\"";
            // Tworzymy i wywołujemy polecenie uruchamiające kompresor zapisany w pliku Dekompresor.jar
            if (!forceDecompression && !isCipherActivated) {
                arguments = "java -jar " + guiPath + " \"" + filePath + "\" " + input.getText();
            } else if (isCipherActivated) {
                arguments = "java -jar " + guiPath + " \"" + filePath + "\" " + input.getText() + " -c " + cipher.getText();
            } else {
                arguments = "java -jar " + guiPath + " \"" + filePath + "\" " + input.getText() + " -d";
            }
            try {
                System.err.println(arguments);
                process = Runtime.getRuntime().exec(arguments);

            } catch (IOException e) {
                System.err.println("error");
            }
            File data = new File("data");
            File tree = new File("tree");
            try {
                scanner = new Scanner(data);
            } catch (IOException e) {
                System.err.println("File data error");
            }

            //sprawdzanie danych od dekompresora dajemy w innym wątku
            //dane od dekompresora zapisywane są w pliku data
            if (process.isAlive()) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        AnalyzeDataFromDecompressor analyzeData = new AnalyzeDataFromDecompressor();
                        // tutaj umieszczamy kod wykonywany w innym wątku niż wątek JavaFX
                        File check = new File(DekompresorGUI.filePath);
                        while (analyzeData.val1 != 3 && analyzeData.val1 != -1) {
                            updateLabel(analyzeData.run(check.length()));
                            setProgressBar(analyzeData.val1, analyzeData.val2, analyzeData.val3, check.length());
                        }
                        return null;
                    }

                    private void updateLabel(String text) {
                        //Zmieniamy komunikat tekstowy w zależności od zachowania dekompresora
                        Platform.runLater(() -> {
                            info1.setText(text);
                            // jeżeli dekompresja się powiodła, to wizualizujemy drzewo
                            if (text.equals("Dekompresja ukończona.")) {
                                try {
                                    DictTree dictTree = new DictTree(tree);
                                    dictTree.start(new Stage());
                                } catch (Exception e) {
                                    System.err.println("Tree generation error!");
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    private void setProgressBar(long val1, long val2, long val3, long check) {
                        //Ustawiamy pasek postępu w zależności od postępu dekompresora
                        Platform.runLater(() -> {
                            if (val1 == 2) {
                                if (check == val3)
                                    progressBar.setProgress((double) val2 / val3);
                            }
                            if (val1 == 3) {
                                progressBar.setProgress(1);
                            }
                        });
                    }
                };

                Thread thread = new Thread(task);
                thread.start();

            }

        });
        stage.setTitle("Dekompresor");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}