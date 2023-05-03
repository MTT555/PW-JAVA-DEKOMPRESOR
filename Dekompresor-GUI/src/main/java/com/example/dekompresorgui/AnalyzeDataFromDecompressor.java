package com.example.dekompresorgui;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class AnalyzeDataFromDecompressor {
    public File data;//zmienna do obsługi pliku pomocniczego pobierającego dane od dekompresora
    Scanner scanner;//do wczytywania pliku liczba po liczbie
    long val1 = 0,val2 = 0,val3 = 0; //tu zapisujemy rezultat wczytywania

    AnalyzeDataFromDecompressor(){
        data = new File("data");
    }
    public String run(long valueCheck){
            try {
                //wczytujemy dane od dekompresora
                scanner = new Scanner(data);
                if (scanner.hasNext()) val1 = scanner.nextLong();
                if (scanner.hasNext()) val2 = scanner.nextLong();
                if (scanner.hasNext()) val3 = scanner.nextLong();
                scanner.close();
                //w zaleznosci od otrzymanej pierwszej wartosci zmieniamy napis w polu tekstowym pod przyciskiem dekompresji
                if(val3 != valueCheck && val3 != 0 && val1 != 1) return "Trwa dekompresja. Zapis danych do pliku wyjściowego. Proszę czekać.";
                //System.out.println(val1 + " " + val2 + " " + val3 + "\n");
                if (val1 == 1)
                    return "Trwa dekompresja. Analiza słownika. Liczba słów w słowniku: " + val2;
                else if (val1 == 2)
                    return "Trwa dekompresja. Zapis danych do pliku wyjściowego. Proszę czekać. Przeanalizowano " + val2 + "/" + val3 + " bajtów.";
                else if (val1 == 3)
                    return "Dekompresja ukończona.";
                else if (val1 == 4)
                    return "Nie podano nazwy pliku wejściowego.";
                else if (val1 == 5)
                    return "Nie można otworzyć pliku wejściowego.";
                else if (val1 == 6)
                    return "Plik wejściowy jest pusty.";
                else if (val1 == 7)
                    return "Niepoprawny klucz odszyfrowania.";
                else if (val1 == 8)
                    return "Plik jest uszkodzony lub niepoprawny.";
                else return "Program rozpoczyna pracę.";
            } catch (IOException e) {
                System.err.println("Data file error!");
                System.exit(6);
            }
        return "";
    }
}
