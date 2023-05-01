package dekompresor;

public class mod_t {
    int value;
    // 1 - dictRoad, - przemieszczanie się w drzewie;
    // 2 - dictWord, - po dotarciu do liścia odczytanie zakodowanego znaku
    // 3 - bitsToWords - po odczytaniu całego słownika zamiana każdego kodu na odpowiadający mu znak
    mod_t(int value){
        this.value = value;
    }
}
