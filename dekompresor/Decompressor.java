package dekompresor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class Decompressor extends FileManager {
    public String cipherKey; // klucz szyfrowania
    public int cipherPos = 0;
    public int cipherLength; // długość szyfru

    //zapisanie ustawień w flagach - pierwsza zmienna odnosi się do wszystkich bajtów poza końcowym, druga tylko do końcowego
    public Flags defFlag = new Flags();
    public Flags allFlag = new Flags();
    public Buffer buf = new Buffer(), codeBuf = new Buffer(); // przechowywanie buforu
    public int anBitsVal; // zmienna na przechowywanie tymczasowego rezultatu funkcji analyzeBits
    public static DNode tree = new DNode(); // drzewo służące do odtworzenia słownika
    public HashMap<String, Integer> listCodes = new HashMap<>(); // tu przechowujemy znaki i odpowiadający mu kod Huffmana
    public Mode mode = new Mode(1); // obiekt przechowujący aktualny tryb odczytywania danych z pliku
    public int c; // zmienna pomocnicza do wczytywania po 8 bitów z pliku
    File input_1;

    public void prepareFile() {
        try {
            // w poniższym kostruktorze dajemy informację o zmianie sposobu kodowania
            // jest to konieczne aby ujemne wartości znaków były wczytywane poprawnie
            inputReader = new FileReader(input_1, StandardCharsets.ISO_8859_1);
            inputBufferedReader = new BufferedReader(inputReader);
            inputBufferedReader.skip(2);
            c = inputBufferedReader.read();
        } catch (IOException e) {
            System.out.println("Input file error!\n" + input.getName());
            System.exit(2);
        }
    }

    public int decompress(File input, File output, Settings settings) {
        input_1 = input;
        // dajemy poczatkowe dane do pliku tymczasowego
        SendDataToGUI.insertDataToFile("0 0 0");
        buf.buf = new char[64]; // tymczasowy bufor przechowujący po 8 wczytanych bitów
        codeBuf.buf = new char[64]; // tymczasowy bufor przechowujący aktualnie wczytany kod Huffmana
        Arrays.fill(codeBuf.buf, 'x');
        Arrays.fill(buf.buf, 'x');
        cipherKey = settings.cipherKey;
        cipherLength = cipherKey.length();
        buf.curSize = 8192; // aktualna wielkosc buforu na odczytane bity
        buf.pos = 0; // aktualna pozycja w buforze na odczytane bity
        codeBuf.curSize = 8192; // aktualna wielkosc buforu dla kodow przejsc po drzewie
        codeBuf.pos = 0; // aktualna pozycja w buforze dla kodow

        // odczytujemy flagi - ustawiamy kursor na trzeci znak zawierający flagi
        prepareFile();
        allFlag.compLevel = ((c & 192) >> 6 != 0) ? 4 * (((c & 192) >> 6) + 1) : 0; // odczytanie poziomu kompresji (192 == 0b11000000)
        allFlag.cipher = (c & 32) != 0; // odczytanie szyfrowania (32 == 0b00100000)
        allFlag.redundantZero = ((c & 16) > 0); // sprawdzenie, czy konieczne bedzie odlaczenie nadmiarowego koncowego znaku '\0' (16 == 0b00010000)
        allFlag.redundantBits = c & 7; // odczytanie ilosci nadmiarowych bitow konczacych (7 == 0b00000111)
        defFlag.compLevel = allFlag.compLevel; // ustawienie odpowiednich wartosci flagi domyslnej
        defFlag.cipher = allFlag.cipher; // uzywanej do kazdego innego symbolu oprocz ostatniego
        defFlag.redundantZero = false;
        defFlag.redundantBits = 0;
        try {
            FileWriter fileWriter = new FileWriter("tree");
            BufferedWriter treeWrite = new BufferedWriter(fileWriter);
            treeWrite.write((char)('0' + allFlag.compLevel / 4)); // 0 - 0-bit, 2 - 8-bit, 3 - 12-bit, 4 - 16-bit
            treeWrite.close();
        } catch (IOException e) {
            System.out.println("Output file error!\n");
            System.exit(3);
        }

        // Przypadek pliku nieskompresowanego, ale zaszyfrowanego
        if (allFlag.compLevel == 0 && allFlag.cipher) {
            Decrypt.decryptFile(input, output, cipherKey);
            return 0;
        }
        long actualIndex = 4; // iterujemy po każdym bajcie w pliku, zaczynamy od piątego bajtu(od słownika)
        // Analiza pliku
        try {
            inputBufferedReader.skip(1); // pomijamy bajt z sumą kontrolną
            while ((c = inputBufferedReader.read()) != -1) {
                if (actualIndex == input.length() - 1)
                    break; //przerywamy pętlę jeżeli znajdziemy się na ostatnim bajcie
                if (allFlag.cipher) {
                    c -= cipherKey.charAt(cipherPos % cipherLength); // odszyfrowanie
                    cipherPos++;
                    c = BitsAnalyze.signedToUnsigned8bitvalue(c); // aby uniknąć ujemnych kodów znaku
                }
                // analizowanie kazdego bitu przy pomocy funkcji
                anBitsVal = BitsAnalyze.analyzeBits(output, c, defFlag, mode, buf, codeBuf, listCodes, actualIndex, input);
                if (anBitsVal == -1) {
                    System.err.println("Decompression failure due to the incorrect cipher!\nCipher provided during the decompression has to be the exact same as the one provided during the compression to receive an accurate output!\n");
                    return -1;
                }
                actualIndex++;
            }
        } catch (IOException e) {
            System.out.println("Input file error!\n" + input.getName());
            System.exit(2);
        }
        // to polecenie poniżej wykona się tylko dla ostatniego bajtu
        anBitsVal = BitsAnalyze.analyzeBits(output, c, allFlag, mode, buf, codeBuf, listCodes, actualIndex, input);
        if (anBitsVal == -1) {
            System.err.println("Decompression failure due to the incorrect cipher!\nCipher provided during the decompression has to be the exact same as the one provided during the compression to receive an accurate output!\n");
            return -1;
        }
        SendDataToGUI.insertDataToFile("3 0 0"); // dekompresja zakończona
        System.err.println("File successfully decompressed!\n");

        return 0;
    }
}
