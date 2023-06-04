package com.example.dekompresorgui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DictTree extends Application {
    private final ArrayList<Integer> list = new ArrayList<>();
    private static final double ZOOM_FACTOR = 1.2; // wspolczynnik skalowania
    private static final double MIN_SCALE = 0.1; // minimalna skala
    private static final double MAX_SCALE = 10.0; // maksymalna skala
    private double xOffset = 0, yOffset = 0; // pomocnicze do przeciagania myszka
    private final Scale scaleTransform = new Scale(1, 1);
    private StackPane root;
    private boolean dontGenerate = false;

    DictTree(File tree) {
        Scanner treeScan;
        try {
            treeScan = new Scanner(tree, StandardCharsets.ISO_8859_1);
            treeScan.useDelimiter("");
            while (treeScan.hasNext())
                list.add((int) (treeScan.next().charAt(0)));
            treeScan.close();
        } catch (IOException e) {
            dontGenerate = true;
            e.printStackTrace();
        }
        if (list.get(0) != 50)
            dontGenerate = true; // generujemy tylko dla 8-bit
    }

    @Override
    public void start(Stage stage) throws Exception {
        if (dontGenerate)
            return;

        root = new StackPane();
        root.setStyle("-fx-background-color: lightblue;");
        root.getTransforms().add(scaleTransform);

        Scene scene = new Scene(root, 960, 540);
        scene.setFill(Color.LIGHTBLUE);
        stage.setScene(scene);
        stage.setTitle("Decompression Tree Showcase");
        stage.setResizable(true);
        stage.show();

        scene.setOnMousePressed(this::handleMousePressed);
        scene.setOnMouseDragged(this::handleMouseDragged);
        scene.setOnScroll(this::handleScrollEvent);

        generateTree();
    }

    private void handleScrollEvent(ScrollEvent event) {
        double delta = event.getDeltaY();
        double scaleFactor = (delta > 0) ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;

        double currentScaleX = scaleTransform.getX(), currentScaleY = scaleTransform.getY();
        double newScaleX = currentScaleX * scaleFactor, newScaleY = currentScaleY * scaleFactor;
        if (newScaleX > MAX_SCALE) {
            newScaleX = MAX_SCALE;
        } else if (newScaleX < MIN_SCALE) {
            newScaleX = MIN_SCALE;
        }
        if (newScaleY > MAX_SCALE) {
            newScaleY = MAX_SCALE;
        } else if (newScaleY < MIN_SCALE) {
            newScaleY = MIN_SCALE;
        }

        double pivotX = event.getX(), pivotY = event.getY();
        double pivotScaleX = (pivotX - root.getTranslateX()), pivotScaleY = (pivotY - root.getTranslateY()) / currentScaleY;
        double translateX = root.getTranslateX() - (pivotScaleX * (newScaleX - currentScaleX)), translateY = root.getTranslateY() - (pivotScaleY * (newScaleY - currentScaleY));

        scaleTransform.setX(newScaleX);
        scaleTransform.setY(newScaleY);
        root.setTranslateX(translateX);
        root.setTranslateY(translateY);
        event.consume();
    }

    private void handleMousePressed(MouseEvent event) {
        double x = event.getSceneX();
        double y = event.getSceneY();
        xOffset = x - root.getTranslateX();
        yOffset = y - root.getTranslateY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double x = event.getSceneX();
        double y = event.getSceneY();
        root.setTranslateX(x - xOffset);
        root.setTranslateY(y - yOffset);
    }

    private void generateTree() {
        int direction, recursionLevel = 1;
        double length = 25550 / Math.pow(2, recursionLevel);
        double offsetRectX = 0, offsetRectY = 0;
        boolean skip = false;
        ArrayList<Character> buffer = new ArrayList<>();
        CTree ctree = new CTree("root", "", recursionLevel);
        StackPane rootNode = ctree.rect.get();
        root.getChildren().add(rootNode);

        for (int i = 1; i < list.size(); i++) {
            if(skip) {
                skip = false;
                continue;
            }
            StringBuilder word = new StringBuilder();
            if (list.get(i) - 48 == 0 || list.get(i) - 48 == 1) {
                for (char j : buffer)
                    word.append(j);
                ctree = ctree.goDown(buffer, word.toString(),
                        list.get(i) - 48 == 1 ? (char) ((int) list.get(i + 1)) + "" : "", recursionLevel);
                recursionLevel++;
                length = max(25550 / Math.pow(2, recursionLevel), 25550 / Math.pow(2, 9));
                direction = buffer.get(buffer.size() - 1) - 48;
                StackPane newNode = ctree.rect.get();
                if (direction == 0)
                    offsetRectX -= length;
                else
                    offsetRectX += length;
                offsetRectY += min(Math.pow(2, 24 - recursionLevel), 1000);
                newNode.setTranslateX(offsetRectX);
                newNode.setTranslateY(offsetRectY);
                newNode.toFront();
                root.getChildren().add(newNode);
            }
            if (list.get(i) - 48 == 1 || list.get(i) - 48 == 2) {
                if (ctree.prev.left == ctree)
                    offsetRectX += length;
                else
                    offsetRectX -= length;
                ctree = ctree.prev;
                offsetRectY -= min(Math.pow(2, 24 - recursionLevel), 1000);
                recursionLevel--;
                length = 25550 / Math.pow(2, recursionLevel);
                buffer.remove(buffer.size() - 1);
            }
            if(list.get(i) - 48 == 1)
                skip = true;
            if (list.get(i) - 48 == 3) {
                System.err.println("Tree generation successful!");
                break;
            }
        }

        root.toBack();
        while (ctree.prev != null)
            ctree = ctree.prev;
        generateConnections(ctree, 0);
    }

    private void generateConnections(CTree ctree, int recursionLevel) {
        Point2D sceneCoords1 = ctree.rect.rect.localToScene(0, 0);
        double x1 = sceneCoords1.getX();
        double y1 = sceneCoords1.getY();

        if (ctree.left != null) {
            Point2D sceneCoords2 = ctree.left.rect.rect.localToScene(0, 0);
            double x2 = sceneCoords2.getX();
            double y2 = sceneCoords2.getY();
            CLine line = new CLine(x1, y1, x2, y2);
            root.getChildren().add(0, line.get());
            generateConnections(ctree.left, recursionLevel + 1);
        }
        if (ctree.right != null) {
            Point2D sceneCoords2 = ctree.right.rect.rect.localToScene(0, 0);
            double x2 = sceneCoords2.getX();
            double y2 = sceneCoords2.getY();
            CLine line = new CLine(x1, y1, x2, y2);
            root.getChildren().add(0, line.get());
            generateConnections(ctree.right, recursionLevel + 1);
        }
    }
}

