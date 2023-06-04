package com.example.dekompresorgui;

import java.util.ArrayList;

public class CTree {
    public CTree left; //lewy węzeł
    public CTree right; //prawy węzeł
    public CTree prev; //ojciec
    public CRect rect;

    public CTree(String word, String symbol, int recursionLevel) {
        this.left = null; //lewy węzeł
        this.right = null; //prawy węzeł
        this.prev = null; //ojciec
        this.rect = new CRect(word, symbol, recursionLevel);
    }

    public CTree goDown(ArrayList<Character> buf, String word, String symbol, int recursionLevel){
        CTree iterator = this;
        if(iterator.left == null) { // jezeli lewy wezel jest wolny
            iterator.left = new CTree(word + '0', symbol, recursionLevel);
            iterator.left.prev = iterator; // zapisanie poprzedniego wezla
            iterator.left.left = null;
            iterator.left.right = null;
            buf.add('0');
            return iterator.left;
        } else { // w przeciwnym razie zajmujemy prawy wezel
            iterator.right = new CTree(word + '1', symbol, recursionLevel);
            iterator.right.prev = iterator;
            iterator.right.left = null;
            iterator.right.right = null;
            buf.add('1');
            return iterator.right;
        }
    }
}


