package dekompresor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SendDataToGUI {
    // Klasa ta zawiera metody odpowiedzialne za wymianę informacji pomiędzy dekompresorem a programem pomocniczym z interfejsem  graficznym
    // dane będą zapisywane w pliku tekstowym
    // schemat zapisu [aktualny stan programu] [ilość danych przeanalizowanych] [całkowita ilość danych]
    // aktualny stan: 1 - odtworzenie słownika, 2 - zapis danych do pliku, 3 - dekompresja zakończona
    // 4 - nie podano nazwy pliku wyjściowego, 5 - nie można otworzyć pliku wejściowego, 6 - plik wejściowy jest pusty
    // 7 - niepoprawny klucz odszyfrowywania, 8 - plik jest uszkodzony
    public static File output = new File("data");
    public static FileWriter fileWriter;
    public static void insertDataToFile(String data){
        try {
            fileWriter = new FileWriter(output);
            fileWriter.write(data);
            fileWriter.close();
        } catch (IOException e){
            System.err.println("Data file error!");
        }
    }
}
