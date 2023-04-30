package dekompresor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BitsAnalyze {
    public static String removeX(String string){
        for (int i = 0; i < string.length(); i++){
            if (string.charAt(i) == 'x'){
                return string.substring(0,i);
            }
        }
        return string;
    }
    public static FileWriter fileWriter;
    public static int down = 0;
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
        int i;
        int bits = 0; /* ilosc przeanalizowanych bitow */
        int currentCode; /* obecny kod przejscia w sciezce */


        while (bits != 8 - f.redundantBits) { /* f.redundantBits bedzie != 0 jedynie przy ostatnim analizowanym znaku */
            if(buf.pos > 100000 || codeBuf.pos > 100000) /* zapobieganie przepelnianiu pamieci w momencie podania zlego szyfru do odszyfrowania */
                return 2;
            switch(mode.value) {
                case 1: {
                    currentCode = 2 * returnBit(c, bits) + returnBit(c, bits + 1);
                    bits += 2;
                    if(currentCode == 3) {
                        buf.pos = 0;
                    mode.value = 3;
                    } else if(currentCode == 2) {
                    tree = tree.prev;
                        (codeBuf.pos)--; /* wyjscie o jeden w gore */
                    } else if(currentCode == 1) {
                        tree = dNode.goDown(tree);
                       if(down == -1)
                          return 1;

                        //przejście o jeden w dół
                        //ten kod poniżej to umieszczanie w stringu podanego znaku na podanej pozycji
                        codeBuf.buf[codeBuf.pos] = (char)('0' + down);
                        codeBuf.pos++;
                        codeBuf.buf[codeBuf.pos] = (char)('\0');
                    mode.value = 2;
                    } else if(currentCode == 0) {
                        tree = dNode.goDown(tree);
                        if(down == -1)
                            return 1;
                        codeBuf.buf[codeBuf.pos] = (char)('0' + down);
                        codeBuf.pos++;
                    }
                    break;
                }
                case 2: {
                    buf.buf[(buf.pos)++] = (char)(returnBit(c, bits++)+'0');
                    buf.buf[(buf.pos)++] = (char)(returnBit(c, bits++)+'0');

                    if(buf.pos == f.compLevel) {
                        int result = 0;
                        for(i = 0; i < f.compLevel; i++) {
                            result *= 2;
                            result += (buf.buf[i]-'0');
                        }
                        Utils.addToListCodes(listCodes, result, removeX(String.valueOf(codeBuf.buf)));
                        buf.pos = 0;
                    tree = tree.prev;
                        (codeBuf.pos)--;
                    mode.value = 1;
                    }
                    break;
                }
                case 3: {
                    buf.buf[(buf.pos)++] = (char)(returnBit(c, bits)+'0');
                    buf.buf[buf.pos] = (char)('\0');
                    bits++;
                    if(compareBuffer(listCodes, buf.buf.toString(), output, f.compLevel, ((((bits == 8 ? 1:0) - f.redundantBits)==1?true:false) ? f.redundantZero : false), currentBits, tempCode))
                        buf.pos = 0;
                    break;
                }
            }
        }
        return 0;
    }
}
