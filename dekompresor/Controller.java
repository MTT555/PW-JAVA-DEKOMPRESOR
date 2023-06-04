package dekompresor;

import java.io.*;

public class Controller extends FileManager {
    public String[] args;

    public Controller(String[] args) {
        this.args = args;
    }

    public Decompressor decompress = new Decompressor();
    public Settings settings = new Settings(); //paczka ustawień dotyczących dekompresji
    public Utils utils = new Utils();

    public void prepareFile() { //przygotowanie i sprawdzenie poprawności plików
        input = new File(args[0]);
        output = new File(args[1]);
        try {
            inputReader = new FileReader(input);
        } catch (FileNotFoundException ex) {
            System.err.println("Input file could not be opened!\n" + args[0]);
            System.exit(2);
        }
        inputBufferedReader = new BufferedReader(inputReader);

        //czy plik wejściowy da się odczytać/czy istnieje
        if (!input.canRead() && !input.exists()) {
            System.err.println("Input file could not be opened!\n" + args[0]);
            SendDataToGUI.insertDataToFile("5 0 0");
            System.exit(2);
        }

        //czy plik nie jest pusty
        if (input.length() == 0) {
            System.err.println("Input file is empty!\n" + args[1]);
            SendDataToGUI.insertDataToFile("6 0 0");
            System.exit(4);
        }
    }

    public void checkArgumentsAndRun() {
        //Analiza argumentów podanych przy wywołaniu programu
        //jeżeli nie podano żadnego argumentu lub podano tylko argument -h program wyświetli pomoc
        if (args.length == 0 || args[1].equals("-h")) Utils.showHelpMessage();
        prepareFile();
        //Sprawdzamy czy podano zarówno plik wejściowy jak i wyjściowy
        if (args.length < 2) {
            System.err.println("Too few arguments!");
            SendDataToGUI.insertDataToFile("4 0 0");
            System.exit(1);
        }
        //sprawdzamy pozostałe argumenty
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("-d")) { //ustawiono wymuszenie dekompresji
                settings.decomp = true;
            }
            if (args[i].equals("-c")) { //użytkownik podał klucz odszyfrowania
                try {
                    settings.cipherKey = args[i + 1];
                } catch (IndexOutOfBoundsException e) {
                    settings.cipherKey = "Politechnika_Warszawska";
                }
            }
        }
        if (!settings.decomp) { /* jezeli nie wymuszono zachowania programu, sprawdzamy plik */
            try {
                if (utils.fileIsGood(input, (char) 183) == 0) /* (183 = 0b10110111) */ settings.decomp = true;
            } catch (IOException ioe) {
                System.out.println("Input file error!\n" + args[0]);
                System.exit(2);
            }
        }
        if (settings.decomp) {
            try {
                fileCheck = utils.fileIsGood(input, (char) 183); /* (183 = 0b10110111) */
            } catch (IOException ioe) {
                System.out.println("Input file error!\n");
                System.exit(2);
            }
            if (fileCheck == 0) {
                decompVal = decompress.decompress(input, output, settings);
                if (decompVal == 1) {
                    System.err.println("Decompression memory failure!\n");
                    System.exit(6);
                } else if (decompVal == 2) {
                    System.err.println("Decompression encryption failure!\n");
                    SendDataToGUI.insertDataToFile("7 0 0");
                    System.exit(7);
                } else {
                    System.exit(5);
                }
            }
        }
    }
}
