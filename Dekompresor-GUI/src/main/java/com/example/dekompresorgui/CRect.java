package com.example.dekompresorgui;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CRect implements CObject {
    private final String code;
    private final String symbol;
    int level;
    public Rectangle rect;
    private final StackPane result;

    public CRect(String word, String symbol, int recursionLevel) {
        this.level = recursionLevel;
        double x = 2000 / (double) (3 * level);
        double y = 2000 / (double) (3 * level);
        this.code = word;
        this.symbol = symbol;
        this.rect = new Rectangle(x, y, Color.DARKBLUE);
        this.rect.setStroke(Color.WHITE);
        this.rect.toFront();
        this.result = new StackPane();
    }

    public StackPane get() {
        Text codeText = new Text('(' + code + ')');
        codeText.setTranslateY(- 0.6 * rect.getHeight());
        double fontSize = 250 / (1 + level / 1.5);
        codeText.setFont(Font.font("Arial", fontSize / 5));
        codeText.setFill(Color.DARKMAGENTA);
        codeText.toFront();

        Text symbolText = new Text(symbol);
        symbolText.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        symbolText.setFill(Color.WHITE);
        symbolText.toFront();

        result.getChildren().addAll(rect, codeText, symbolText);
        return result;
    }
}
