package dekompresor;

import java.io.File;

import static dekompresor.BitsAnalyze.mod_t.dictRoad;

public class Decompressor {
int i;
char c;
String cipherKey;
int cipherPos = 0;
int cipherLength;
BitsAnalyze.mod_t currentMode = dictRoad;
int currentBits = 0;
int tempCode = 0;
flag_t defFlag = new flag_t();
flag_t allFlag = new flag_t();
    buffer_t buf = new buffer_t(), codeBuf = new buffer_t(); /* przechowywanie buforu */
    int anBitsVal; /* zmienna na przechowywanie tymczasowego rezultatu funkcji analyzeBits */
    int inputEOF; /* zmienne zawierajace pozycje koncowe pliku wejsciowego i wyjsciowego */
    dNode tree;

    public int decompress(File input, File output, Settings settings){
        cipherKey = settings.cipherKey;
        cipherLength = cipherKey.length();
        buf.curSize = 8192; /* aktualna wielkosc buforu na odczytane bity */
        buf.pos = 0; /* aktualna pozycja w buforze na odczytane bity */
        codeBuf.curSize = 8192; /* aktualna wielkosc buforu dla kodow przejsc po drzewie */
        codeBuf.pos = 0; /* aktualna pozycja w buforze dla kodow */
        return 0;
    }
}
