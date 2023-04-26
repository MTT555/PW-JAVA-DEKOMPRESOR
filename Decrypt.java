package dekompresor;

import java.io.*;

public class Decrypt {
    static FileReader decryptReader;
    public static void decryptFile(File input, File output, String cipherKey){
        int c;
        int cipherPos = 0;
        int cipherLength = cipherKey.length();

        try {
            decryptReader = new FileReader(input);
        }catch(FileNotFoundException e){
            System.err.println("File not found");
        }
        BufferedReader bufReader = new BufferedReader(decryptReader);
        try {
            bufReader.skip(4);
        } catch (IOException e){
            System.err.println("Error");
        }
        try {
            while ((c = bufReader.read()) != -1) {
                c -= cipherKey.charAt(cipherPos % cipherLength); /* odszyfrowanie */
                cipherPos++;
            }
        }catch (IOException e){
            System.err.println("Error");
        }
        System.err.println("File successfully decrypted!\n");
    }
}
