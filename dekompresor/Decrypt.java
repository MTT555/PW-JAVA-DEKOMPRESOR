package dekompresor;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Decrypt {
    static FileReader decryptReader;
    static FileWriter fileWriter;
    static BufferedWriter bufferedWriter;
    public static void decryptFile(File input, File output, String cipherKey) {
        int c;
        int cipherPos = 0;
        int cipherLength = cipherKey.length();

        try {
            decryptReader = new FileReader(input, StandardCharsets.ISO_8859_1);
            fileWriter = new FileWriter(output, StandardCharsets.ISO_8859_1, true);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch(Exception e){
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
                try {
                    bufferedWriter.write(c);
                } catch (IOException e) {
                    System.err.println("Output file error!\n");
                    System.exit(3);
                }
            }
        } catch (IOException e){
            System.err.println("Error! The input file was not loaded correctly!");
            System.exit(2);
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Output file error!\n");
            System.exit(3);
        }
        System.err.println("File successfully decrypted!\n");
    }
}
