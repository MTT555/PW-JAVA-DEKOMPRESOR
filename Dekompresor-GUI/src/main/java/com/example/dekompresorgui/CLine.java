package com.example.dekompresorgui;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class CLine implements CObject {
    private final double x1, y1, x2, y2;

    public CLine(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public StackPane get() {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(10);
        line.setTranslateX((x1 + x2) / 2);
        line.setTranslateY((y1 + y2) / 2);
        line.toBack();

        StackPane result = new StackPane();
        result.getChildren().add(0, line);
        return result;
    }
}
