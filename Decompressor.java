package dekompresor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Decompressor {
int i;
char c;
String cipherKey;
int cipherPos = 0;
int cipherLength;
mod_t mode = new mod_t();//obiekt zawierający enuma do ustalania trybu odczytywania z pliku
int currentBits = 0;
int tempCode = 0;
flag_t defFlag = new flag_t();
flag_t allFlag = new flag_t();
    buffer_t buf = new buffer_t(), codeBuf = new buffer_t(); /* przechowywanie buforu */
    int anBitsVal; /* zmienna na przechowywanie tymczasowego rezultatu funkcji analyzeBits */
    int inputEOF; /* zmienne zawierajace pozycje koncowe pliku wejsciowego i wyjsciowego */
    dNode tree;

    public int decompress(File input, File output, Settings settings) throws IOException {
        cipherKey = settings.cipherKey;
        cipherLength = cipherKey.length();
        buf.curSize = 8192; /* aktualna wielkosc buforu na odczytane bity */
        buf.pos = 0; /* aktualna pozycja w buforze na odczytane bity */
        codeBuf.curSize = 8192; /* aktualna wielkosc buforu dla kodow przejsc po drzewie */
        codeBuf.pos = 0; /* aktualna pozycja w buforze dla kodow */
        tree = new dNode(); // inicjujemy drzewo do pozyskania kodów ze słownika

        //odczytuję flagi - ustawiamy kursor na trzeci znak zawierający flagi
        FileReader inputReader = new FileReader(input);
        BufferedReader read = new BufferedReader(inputReader);
        read.skip(2);
        int c = read.read();
        allFlag.compLevel = ((c & 192) >> 6 == 1) ? 4 * (((c & 192) >> 6) + 1) : 0; /* odczytanie poziomu kompresji (192 == 0b11000000) */
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
        int actualIndex = 4;
        /*Analiza pliku*/
        read.skip(1);
        while((c=read.read())!=-1){
            if(allFlag.cipher){
                c -= cipherKey.charAt(cipherPos % cipherLength); /* odszyfrowanie */
                cipherPos++;
            }
            if(actualIndex != input.length()-1){/* analizowanie kazdego bitu przy pomocy funkcji */
                anBitsVal = BitsAnalyze.analyzeBits();
                if(anBitsVal == 1)return 1;
                if(anBitsVal == 2){
                    System.err.println("Decompression failure due to the incorrect cipher!\nCipher provided during the decompression has to be the exact same as the one provided during the compression to receive an accurate output!\n");
                    return 2;
                }
            }
        }
        anBitsVal = BitsAnalyze.analyzeBits();
        if(anBitsVal == 1)return 1;
        if(anBitsVal == 2){
            System.err.println("Decompression failure due to the incorrect cipher!\nCipher provided during the decompression has to be the exact same as the one provided during the compression to receive an accurate output!\n");
            return 2;
        }
        System.err.println("File successfully decompressed!\n");



        return 0;
    }
}
