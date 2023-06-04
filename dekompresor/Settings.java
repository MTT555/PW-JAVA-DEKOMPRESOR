package dekompresor;

public class Settings {
/////////////////////////////////////////////////////////////////////////////////
    //Ustawienia dekompresora
/////////////////////////////////////////////////////////////////////////////////
    //wymuszanie dekompresji
    public boolean decomp = false;
    //true - wymuś dekompresję, false - nie wymuszaj

    //czy szyfrowanie zostało włączone czy nie(domyślnie wyłączone)
    public boolean cipher = false;

    //klucz szyfrowania(w użyciu, jeżeli plik został zaszyfrowany)
    public String cipherKey = "Politechnika_Warszawska";
}
