package dekompresor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

public class Decompressor {
    public String cipherKey; //klucz szyfrowania
    public int cipherPos = 0;
    public int cipherLength; //długość szyfru

    //zapisanie ustawień w flagach - pierwsza zmienna odnosi się do wszystkich bajtów poza końcowym, druga tylko do końcowego
    public flag_t defFlag = new flag_t();
    public flag_t allFlag = new flag_t();
    public buffer_t buf = new buffer_t(), codeBuf = new buffer_t(); /* przechowywanie buforu */
    public int anBitsVal; /* zmienna na przechowywanie tymczasowego rezultatu funkcji analyzeBits */
    public static dNode tree = new dNode(); //drzewo służące do odtworzenia słownika
    public HashMap<String,Integer>listCodes = new HashMap<String, Integer>(); // tu przechowujemy znaki i odpowiadający mu kod Huffmana
    public mod_t mode = new mod_t(1);//obiekt przechowujący aktualny tryb odczytywania danych z pliku
    public int c;//zmienna pomocnicza do wczytywania po 8 bitów z pliku
    public FileReader inputReader;
    public BufferedReader read;
    public int decompress(File input, File output, Settings settings)  {
        buf.buf = new char[64]; //tymczasowy bufor przechowujący po 8 wczytanych bitów
        codeBuf.buf = new char[64]; //tymczasowy bufor przechowujący aktualnie wczytany kod Huffmana
        Arrays.fill(codeBuf.buf,'x');
        Arrays.fill(buf.buf,'x');
        cipherKey = settings.cipherKey;
        cipherLength = cipherKey.length();
        buf.curSize = 8192; /* aktualna wielkosc buforu na odczytane bity */
        buf.pos = 0; /* aktualna pozycja w buforze na odczytane bity */
        codeBuf.curSize = 8192; /* aktualna wielkosc buforu dla kodow przejsc po drzewie */
        codeBuf.pos = 0; /* aktualna pozycja w buforze dla kodow */

        //odczytujemy flagi - ustawiamy kursor na trzeci znak zawierający flagi
        try {
            //w poniższym kostruktorze dajemy informację o zmianie sposobu kodowania
            //jest to konieczne aby ujemne wartości znaków były wczytywane poprawnie
            inputReader = new FileReader(input, Charset.forName("ISO-8859-1"));
            read = new BufferedReader(inputReader);
            read.skip(2);
            c = read.read();
        }catch(IOException e){
            System.out.println("Input file error!\n"+input.getName());
            System.exit(2);
        }
        allFlag.compLevel = ((c & 192) >> 6 != 0) ? 4 * (((c & 192) >> 6) + 1) : 0; /* odczytanie poziomu kompresji (192 == 0b11000000) */
        allFlag.cipher = ((c & 32) == 1) ? true : false; /* odczytanie szyfrowania (32 == 0b00100000) */
        allFlag.redundantZero = ((c & 16) == 1) ? true : false; /* sprawdzenie, czy konieczne bedzie odlaczenie nadmiarowego koncowego znaku '\0' (16 == 0b00010000) */
        allFlag.redundantBits = c & 7; /* odczytanie ilosci nadmiarowych bitow konczacych (7 == 0b00000111) */
        defFlag.compLevel = allFlag.compLevel; /* ustawienie odpowiednich wartosci flagi domyslnej */
        defFlag.cipher = allFlag.cipher; /* uzywanej do kazdego innego symbolu oprocz ostatniego */
        defFlag.redundantZero = false;
        defFlag.redundantBits = 0;

        /* Przypadek pliku nieskompresowanego, ale zaszyfrowanego */
        if(allFlag.compLevel == 0 && allFlag.cipher) {
            Decrypt.decryptFile(input, output, cipherKey);
            return 0;
        }
        int actualIndex = 4; //iterujemy po każdym bajcie w pliku, zaczynamy od piątego bajtu(od słownika)
        /*Analiza pliku*/
        try {
            read.skip(1); //pomijamy bajt z sumą kontrolną
            while ((c = read.read()) != -1) {
                if (actualIndex == input.length() - 1)
                    break; //przerywamy pętlę jeżeli znajdziemy się na ostatnim bajcie
                if (allFlag.cipher) {
                    c -= cipherKey.charAt(cipherPos % cipherLength); /* odszyfrowanie */
                    cipherPos++;
                }
                /* analizowanie kazdego bitu przy pomocy funkcji */
                anBitsVal = BitsAnalyze.analyzeBits(output, c, defFlag, mode, buf, codeBuf, listCodes);
                if (anBitsVal == 1) return 1;
                if (anBitsVal == 2) {
                    System.err.println("Decompression failure due to the incorrect cipher!\nCipher provided during the decompression has to be the exact same as the one provided during the compression to receive an accurate output!\n");
                    return 2;
                }

                actualIndex++;
            }
        }catch (IOException e){
            System.out.println("Input file error!\n"+input.getName());
            System.exit(2);
        }
        //to polecenie poniżej wykona się tylko dla ostatniego bajtu
        anBitsVal = BitsAnalyze.analyzeBits(output,c,allFlag,mode,buf,codeBuf,listCodes);
        if(anBitsVal == 1)return 1;
        if(anBitsVal == 2){
            System.err.println("Decompression failure due to the incorrect cipher!\nCipher provided during the decompression has to be the exact same as the one provided during the compression to receive an accurate output!\n");
            return 2;
        }
        System.err.println("File successfully decompressed!\n");

        return 0;
    }
}
