package dekompresor;

import java.io.*;

/*
Dekompresor w Javie do kompresora napisanego w C
Autorzy:
Adrian Chmiel
Mateusz Tyl
 */
public class Main {

public static void main(String [] args) {
    Controller controller = new Controller(args);
    controller.checkArgumentsAndRun();
}
}
