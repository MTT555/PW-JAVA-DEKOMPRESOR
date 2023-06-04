package dekompresor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public abstract class FileManager {
    public File input; //plik wejściowy
    public File output; //plik wyjściowy
    //poniżej dwie zmienne odpowiedzialne za wczytanie danych z pliku bajt po bajcie
    public FileReader inputReader;
    public BufferedReader inputBufferedReader;
    public int fileCheck; //wartość zwracana po sprawdzeniu poprawności pliku
    public int decompVal; //wartość zwracana po dekompresji
    public File dataOutput; // plik z informacjami przekazywanymi do GUI

    public abstract void prepareFile();

}
