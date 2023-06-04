package dekompresor;

public class Flags {
    int compLevel; /* poziom kompresji */
    boolean cipher; /* koniecznosc odszyfrowania */
    boolean redundantZero; /* koniecznosc odlaczenia nadmiarowego koncowego znaku '\0' */
    int redundantBits; /* ilosc nadmiarowych bitow do odrzucenia */
}
