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
            System.err.println("Input file not found!\n");
        }
        BufferedReader bufReader = new BufferedReader(decryptReader);
        try {
            bufReader.skip(4);
        } catch (IOException e){
            System.err.println("Error! The input file was not loaded correctly!");
            System.exit(2);
        }
        try {
            while ((c = bufReader.read()) != -1) {
                c -= cipherKey.charAt(cipherPos % cipherLength); /* odszyfrowanie */
                cipherPos++;
            }
        }catch (IOException e){
            System.err.println("Error! The input file was not loaded correctly!");
            System.exit(2);
        }
        System.err.println("File successfully decrypted!\n");
    }
}
