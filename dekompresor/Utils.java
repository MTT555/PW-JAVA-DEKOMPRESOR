package dekompresor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Utils extends FileManager {
    //dodawanie kodu wraz ze znakiem do hashmapy
    File input_1; //zmienna pomocnicza w celu usunięcia błędu

    public void prepareFile() {
        try {
            inputReader = new FileReader(input_1, StandardCharsets.ISO_8859_1);
            inputBufferedReader = new BufferedReader(inputReader);
        } catch (IOException e) {
            System.out.println("Output file error!");
        }
    }

    public static void addToListCodes(HashMap<String, Integer> listCodes, int character, String code) {
        listCodes.put(code, character);
    }

    public static void showHelpMessage() {
        System.out.println("\n---------------------------------- HUFFMAN DECOMPRESSOR HELPBOX ----------------------------------\n\n");
        System.out.println("Decompressor made by Adrian Chmiel & Mateusz Tyl\n\n");
        System.out.println("Usage: [program_name] [input_file] [output_file] [arguments]\n\n");
        System.out.println("program_name - location of the program itself\n");
        System.out.println("input_file - location of the file that contains the string that is supposed to be compressed\n");
        System.out.println("output_file - location of the file that is supposed to save the result of the program\n");
        System.out.println("arguments - arguments that change the settings and behaviour of the program\n\n");
        System.out.println("[input_file] and [output_file] fields are mandatory!\n");
        System.out.println("[arguments] field is optional and lets you provide more than one argument\n\n");
        System.out.println("Possible arguments:\n");
        System.out.println("-h - displays this help message\n");
        System.out.println("-d - force decompression\n\n");
        System.out.println("-c \"cipher\" - encrypts/decrypts the entire output using the given cipher (max length: 4096)\n");
        System.out.println("If no custom cipher is provided, \"Politechnika_Warszawska\" is used by default)\n");
        System.out.println("Important! If the cipher used during compression doesn't match with the one used during decompression,\n");
        System.out.println("the output will be highly inaccurate!\n\n");
        System.out.println("Error codes:\n");
        System.out.println("0 - Program finished successfully\n");
        System.out.println("1 - Too few arguments have been provided\n");
        System.out.println("2 - Input file could not be opened\n");
        System.out.println("3 - Output file could not be opened\n");
        System.out.println("4 - Input file is empty\n");
        System.out.println("5 - Decompression has been forced but the input file could not be decompressed\n");
        System.out.println("6 - Memory allocation/reallocation failure\n");
        System.out.println("7 - Cipher provided during the decompression is not the same as the one given during the compression\n\n");
        System.out.println("------------------------------------------------------------------------------------------------\n\n");
    }

    // Sprawdzamy czy plik skompresowany jest poprawny
    public int fileIsGood(File input, char xorCorrectValue) throws IOException {
        input_1 = input;
        prepareFile();
        if (input.length() < 4)
            return 4; // czy plik jest pusty (długość w bajtach jest mniejsza niż nagłówek)
        // Sprawdzamy czy nagłówek jest prawidłowy
        // dwa pierwsze bajty to powinno być CT
        if (inputBufferedReader.read() != 'C')
            return 1;
        if (inputBufferedReader.read() != 'T')
            return 1;
        // Sprawdzamy bit w masce odpowiedzialny za informację o kompresji pliku
        int c = inputBufferedReader.read();
        if ((c & 8) == 0)
            return 2;
        // Sprawdzamy sumę kontrolną XOR
        int xor = inputBufferedReader.read();
        int actual_index = 4;
        while (actual_index != input.length()) {
            c = inputBufferedReader.read();
            xor ^= c;
            actual_index++;
        }
        if (xor == xorCorrectValue)
            return 0;
        else {
            System.err.println("Provided file cannot be decompressed since it is corrupted!\n");
            SendDataToGUI.insertDataToFile("8 0 0");
            return 3; // plik jest uszkodzony
        }
    }

}
