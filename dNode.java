package dekompresor;

public class dNode {

        public dNode left;//lewy węzeł
        public dNode right;//prawy węzeł
        public dNode prev;//ojciec
        public dNode(){
            this.left = null;//lewy węzeł
            this.right = null;//prawy węzeł
            this.prev = null; //ojciec

    }
    public static dNode goDown(dNode iterator){
        if(iterator.left == null) { /* jezeli lewy wezel jest wolny */
            iterator.left = new dNode();
            iterator.left.prev = iterator; /* zapisanie poprzedniego wezla */
            iterator.left.left = null;
            iterator.left.right = null;
            BitsAnalyze.down = 0; //ustawiamy wartość na gałęzi drzewa
            return iterator.left;
        } else { /* w przeciwnym razie zajmujemy prawy wezel */
            iterator.right = new dNode();
            iterator.right.prev = iterator; /* zapisanie poprzedniego wezla */
            iterator.right.left = null;
            iterator.right.right = null;
            BitsAnalyze.down = 1; //ustawiamy wartość na gałęzi drzewa
            return iterator.right;
        }
    }

}
