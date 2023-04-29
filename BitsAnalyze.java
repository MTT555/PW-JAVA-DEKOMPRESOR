package dekompresor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class BitsAnalyze {
    public static FileWriter fileWriter;
    public static int returnBit(int c, int x){
        int ch = c;
        ch >>= (7 - x);
        return ch % 2;
    }
    public static boolean compareBuffer(HashMap<String, Integer> listCodes, String buf, File output, int compLevel, boolean redundantZero, int currentBits, int tempCode) {
        try {
            fileWriter = new FileWriter(output, Charset.forName("ISO-8859-1"));
        }catch (IOException e){
            System.err.println("error");
        }
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        int temp, tempC;
        for (Map.Entry<String,Integer> set : listCodes.entrySet()){

            if(set.getKey().equals(buf)){
                if(compLevel == 8){
                    tempC = set.getValue()/(1<<8);
                    try {
                        bufferedWriter.write(tempC);
                    }catch (IOException e){
                        System.err.println("error");
                    }
                }
                else if(compLevel == 16) { /* dla kompresji 16-bit */
                    tempC = set.getValue()/(1<<8);
                    try {/* piszemy 2 symbole */
                        bufferedWriter.write(tempC);
                    }catch (IOException e){
                        System.err.println("error");
                    }
                    if(!redundantZero) { /* chyba ze oznaczenie o nadmiarowym znaku '\0' ustawione na true */
                        tempC = set.getValue();
                        try {
                            bufferedWriter.write(tempC);
                        }catch (IOException e){
                            System.err.println("error");
                        }
                    }
                }
                else if(compLevel == 12) { /* dla kompresji 12-bit */
                tempCode <<= 12;
                tempCode += set.getValue();
                currentBits += 12;
                    if(currentBits == 12) { /* jezeli liczba zajetych bitow wynosi dokladnie 12 */
                        temp = tempCode % 16; /* odcinamy 4 ostatnie bity i przechowujemy pod zmienna tymczasowa */
                    tempCode >>= 4;
                        tempC = tempCode;
                        try {
                            bufferedWriter.write(tempC);
                        }catch (IOException e){
                            System.err.println("error");
                        }
                    tempCode = temp; /* i przywracamy 4 odciete bity do tempCode */
                    currentBits = 4;
                    } else { /* w przeciwnym wypadku wynosi 16, wiec robimy to samo co dla kompresji 16-bit */
                        tempC = tempCode / (1 << 8);
                        try {
                            bufferedWriter.write(tempC);
                        }catch (IOException e){
                            System.err.println("error");
                        }
                        if(!redundantZero) {
                            tempC = tempCode;
                            try {
                                bufferedWriter.write(tempC);
                            }catch (IOException e){
                                System.err.println("error");
                            }
                        }
                    tempCode = 0;
                    currentBits = 0;
                    }
                }
                return true;
            }
        }


        return false;
    }
    public static int analyzeBits(File output, int c, flag_t f, dNode tree, mod_t mode, buffer_t buf, buffer_t codeBuf, int currentBits, int tempCode, HashMap<String, Integer> listCodes){
        int i, down;
        int bits = 0; /* ilosc przeanalizowanych bitow */
        int currentCode; /* obecny kod przejscia w sciezce */
        StringBuilder builder = new StringBuilder();
        StringBuilder builder1 = new StringBuilder();
        mode = mode.dictRoad;

        dNode iterator = tree;

        while (bits != 8 - f.redundantBits) { /* f.redundantBits bedzie != 0 jedynie przy ostatnim analizowanym znaku */
            if(buf.pos > 100000 || codeBuf.pos > 100000) /* zapobieganie przepelnianiu pamieci w momencie podania zlego szyfru do odszyfrowania */
                return 2;
            switch(mode) {
                case dictRoad: {
                    currentCode = 2 * returnBit(c, bits) + returnBit(c, bits + 1);
                    bits += 2;
                    if(currentCode == 3) {
                        buf.pos = 0;
                    mode = mode.bitsToWords;
                    } else if(currentCode == 2) {
                    iterator = iterator.prev;
                        (codeBuf.pos)--; /* wyjscie o jeden w gore */
                    } else if(currentCode == 1) {
                        down = dNode.goDown(iterator);
                        if(down == -1)
                            return 1;

                        //przejście o jeden w dół
                        //ten kod poniżej to umieszczanie w stringu podanego znaku na podanej pozycji
                        builder.append(codeBuf.buf);
                        builder.insert(codeBuf.pos,'0' + down);
                        codeBuf.buf = builder.toString();
                        builder.append(codeBuf.buf);
                        codeBuf.pos++;

                        builder.insert(codeBuf.pos,'\0');
                        codeBuf.buf = builder.toString();
                        builder.append(codeBuf.buf);
                    mode = mode.dictWord;
                    } else if(currentCode == 0) {
                        down = dNode.goDown(iterator);
                        if(down == -1)
                            return 1;
                        builder.insert(codeBuf.pos,'0' + down);
                        codeBuf.buf = builder.toString();
                        builder.append(codeBuf.buf);
                        codeBuf.pos++;
                    }
                    break;
                }
                case dictWord: {
                    builder1.insert((buf.pos)++,returnBit(c, bits++));
                    buf.buf = builder1.toString();
                    builder1.append(buf.buf);

                    builder1.insert((buf.pos)++,returnBit(c, bits++));
                    buf.buf = builder1.toString();
                    builder1.append(buf.buf);

                    if(buf.pos == f.compLevel) {
                        int result = 0;
                        for(i = 0; i < f.compLevel; i++) {
                            result *= 2;
                            result += buf.buf.charAt(i);
                        }
                        Utils.addToListCodes(listCodes, result, codeBuf.buf);
                        buf.pos = 0;
                    iterator = iterator.prev;
                        (codeBuf.pos)--;
                    mode = mode.dictRoad;
                    }
                    break;
                }
                case bitsToWords: {
                    builder1.insert((buf.pos)++,'0' + returnBit(c, bits));
                    buf.buf = builder1.toString();
                    builder1.append(buf.buf);

                    builder1.insert(buf.pos,'\0');
                    buf.buf = builder1.toString();
                    builder1.append(buf.buf);
                    bits++;
                    if(compareBuffer(listCodes, buf.buf, output, f.compLevel, ((((bits == 8 ? 1:0) - f.redundantBits)==1?true:false) ? f.redundantZero : false), currentBits, tempCode))
                        buf.pos = 0;
                    break;
                }
            }
        }
        return 0;
    }
}
