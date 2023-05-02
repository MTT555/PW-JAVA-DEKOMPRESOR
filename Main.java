package dekompresor;

import java.io.*;

/*
Dekompresor w Javie do kompresora napisanego w C
Autorzy:
Adrian Chmiel
Mateusz Tyl
 */
public class Main {
    public static File input; //plik wejściowy
    //poniżej dwie zmienne odpowiedzialne za wczytanie danych z pliku bajt po bajcie
    public static FileReader inputReader;
    public static BufferedReader inputBufferedReader;

    public static File output; //plik wyjściowy

    public static int fileCheck; //wartość zwracana po sprawdzeniu poprawności pliku
    public static int decompVal; //wartość zwracana po dekompresji

public static void main(String [] args){
    Decompressor decompress = new Decompressor();
    //Analiza argumentów podanych przy wywołaniu programu
    //jeżeli nie podano żadnego argumentu lub podano tylko argument -h program wyświetli pomoc
    if(args.length == 0 || args[1].equals("-h"))Utils.showHelpMessage();

    //Sprawdzamy czy podano zarówno plik wejściowy jak i wyjściowy
    if(args.length < 2) {
        System.err.println("Too few arguments!");
        System.exit(1);
    }
    input = new File(args[0]);
    output = new File(args[1]);
    Settings settings = new Settings();//paczka ustawień dotyczących dekompresji
    try {
        inputReader = new FileReader(input);
    }catch(FileNotFoundException ex){
        System.err.println("Input file could not be opened!\n" + args[0]);
        System.exit(2);
    }
    //zainicjowanie wczytywacza pliku
    inputBufferedReader = new BufferedReader(inputReader);

    //czy plik wejściowy da się odczytać/czy istnieje
    if (!input.canRead() && !input.exists()){
        System.err.println("Input file could not be opened!\n" + args[0]);
        System.exit(2);
    }

    //czy plik nie jest pusty
    if(input.length()==0){
        System.err.println("Input file is empty!\n" + args[1]);
        System.exit(4);
    }

    //sprawdzamy pozostałe argumenty
    for (int i = 2; i < args.length; i++){
        if(args[i].equals("-d")){ //ustawiono wymuszenie dekompresji
            settings.decomp = true;
        }
        if(args[i].equals("-c")){ //użytkownik podał klucz odszyfrowania
            settings.cipherKey = args[i+1];
        }
    }
    if(!settings.decomp) { /* jezeli nie wymuszono zachowania programu, sprawdzamy plik */
        try {
            if (Utils.fileIsGood(input, (char)183, false) == 0) /* (183 = 0b10110111) */
                settings.decomp = true;
        } catch (IOException ioe){
            System.out.println("Input file error!\n"+args[0]);
            System.exit(2);
        }
    }
    if(settings.decomp){
        try {
            fileCheck = Utils.fileIsGood(input, (char) 183, true); /* (183 = 0b10110111) */
        }catch(IOException ioe){
            System.out.println("Input file error!\n");
            System.exit(2);
        }
        if(fileCheck == 0){
                decompVal = decompress.decompress(input,output,settings);
            if (decompVal == 1){
                System.err.println("Decompression memory failure!\n");
                System.exit(6);
            } else if (decompVal == 2){
                System.err.println("Decompression encryption failure!\n");
                System.exit(7);
            } else {
                System.exit(5);
            }
        }

    }

}
}
