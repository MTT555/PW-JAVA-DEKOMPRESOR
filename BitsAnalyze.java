package dekompresor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class BitsAnalyze {
    public static int currentBits = 0;
    public static int tempCode = 0;
    public static byte cast;//zmienna pomocnicza do rzutowania na typ byte
    //ucinamy stringa tam gdzie napotkamy na znak '\0'
    public static String cutString(String string){
        for (int i = 0; i < string.length(); i++){
            if (string.charAt(i) == '\0'){
                return string.substring(0,i);
            }
        }
        return string;
    }
    public static FileWriter fileWriter;
    public static int down = 0; //wartość znajdująca się na gałęzi drzewa
    //przejście w lewo reprezentuje 0, w prawo 1

    //zwracamy x-ty bit zmiennej c
    public static int returnBit(int c, int x){
        int ch = c;
        ch >>= (7 - x);
        return ch % 2;
    }
    public static boolean compareBuffer(HashMap<String, Integer> listCodes, String buf, File output, int compLevel, boolean redundantZero) {
        try {
            fileWriter = new FileWriter(output, Charset.forName("ISO-8859-1"),true);
        }catch (IOException e){
            System.err.println("Output file error!\n");
            System.exit(3);
        }
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        int temp, tempC;
        for (Map.Entry<String,Integer> set : listCodes.entrySet()){

            if(set.getKey().equals(buf)){
                if(compLevel == 8){
                    tempC = set.getValue();
                    try {
                        bufferedWriter.write(tempC);
                    }catch (IOException e){
                        System.err.println("Output file error!\n");
                        System.exit(3);
                    }
                }
                else if(compLevel == 16) { /* dla kompresji 16-bit */
                    tempC = set.getValue()/(1<<8);
                    try {/* piszemy 2 symbole */
                        bufferedWriter.write(tempC);
                    }catch (IOException e){
                        System.err.println("Output file error!\n");
                        System.exit(3);
                    }
                    if(!redundantZero) { /* chyba ze oznaczenie o nadmiarowym znaku '\0' ustawione na true */
                        tempC = set.getValue();
                        byte cast = (byte)tempC;
                        tempC = (int)cast;
                        try {
                            bufferedWriter.write(tempC);
                        }catch (IOException e){
                            System.err.println("Output file error!\n");
                            System.exit(3);
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
                    byte cast = (byte)tempCode;
                        tempC = (int)cast;
                        try {
                            bufferedWriter.write(tempC);
                        }catch (IOException e){
                            System.err.println("Output file error!\n");
                            System.exit(3);
                        }
                    tempCode = temp; /* i przywracamy 4 odciete bity do tempCode */
                    currentBits = 4;
                    } else { /* w przeciwnym wypadku wynosi 16, wiec robimy to samo co dla kompresji 16-bit */
                        tempC = tempCode / (1 << 8);
                        cast = (byte)tempC;
                        tempC = (int)cast;
                        try {
                            bufferedWriter.write(tempC);
                        }catch (IOException e){
                            System.err.println("Output file error!\n");
                            System.exit(3);
                        }
                        if(!redundantZero) {
                            tempC = tempCode;
                            cast = (byte)tempCode;
                            tempC = (int)cast;
                            try {
                                bufferedWriter.write(tempC);
                            }catch (IOException e){
                                System.err.println("Output file error!\n");
                                System.exit(3);
                            }
                        }
                    tempCode = 0;
                    currentBits = 0;
                    }
                }
                try {
                    bufferedWriter.close();
                } catch (IOException e){
                    System.err.println("Output file error!\n");
                    System.exit(3);
                }
                return true;
            }
        }

        try {
            bufferedWriter.close();
        } catch (IOException e){
            System.err.println("Output file error!\n");
            System.exit(3);
        }
        return false;
    }
    public static int analyzeBits(File output, int c, flag_t f, mod_t mode, buffer_t buf, buffer_t codeBuf, HashMap<String, Integer> listCodes){
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
                    Decompressor.tree =  Decompressor.tree.prev;
                        (codeBuf.pos)--; /* wyjscie o jeden w gore */
                    } else if(currentCode == 1) {
                        Decompressor.tree = dNode.goDown(Decompressor.tree);
                       if(down == -1)
                          return 1;

                        //przejście o jeden w dół
                        //ten kod poniżej to umieszczanie w stringu podanego znaku na podanej pozycji
                        codeBuf.buf[codeBuf.pos] = (char)('0' + down);
                        codeBuf.pos++;
                        codeBuf.buf[codeBuf.pos] = (char)('\0');
                    mode.value = 2;
                    } else if(currentCode == 0) {
                        Decompressor.tree = dNode.goDown(Decompressor.tree);
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
                        Utils.addToListCodes(listCodes, result, cutString(String.valueOf(codeBuf.buf)));
                        buf.pos = 0;
                        Decompressor.tree =  Decompressor.tree.prev;
                        (codeBuf.pos)--;
                    mode.value = 1;
                    }
                    break;
                }
                case 3: {
                    buf.buf[(buf.pos)++] = (char)(returnBit(c, bits)+'0');
                    buf.buf[buf.pos] = (char)('\0');
                    bits++;
                    if(compareBuffer(listCodes, cutString(String.valueOf(buf.buf)), output, f.compLevel, (((bits == 8 ? 1:0) - f.redundantBits)==1?true:false) ? f.redundantZero : false))
                        buf.pos = 0;
                    break;
                }
            }
        }
        return 0;
    }
}
