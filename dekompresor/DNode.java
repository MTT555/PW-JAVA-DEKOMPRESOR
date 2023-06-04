package dekompresor;

public class DNode {

        public DNode left;//lewy węzeł
        public DNode right;//prawy węzeł
        public DNode prev;//ojciec
        public DNode(){
            this.left = null;//lewy węzeł
            this.right = null;//prawy węzeł
            this.prev = null; //ojciec

    }
    public static DNode goDown(DNode iterator){
        if(iterator.left == null) { /* jezeli lewy wezel jest wolny */
            iterator.left = new DNode();
            iterator.left.prev = iterator; /* zapisanie poprzedniego wezla */
            iterator.left.left = null;
            iterator.left.right = null;
            BitsAnalyze.down = 0; //ustawiamy wartość na gałęzi drzewa
            return iterator.left;
        } else { /* w przeciwnym razie zajmujemy prawy wezel */
            iterator.right = new DNode();
            iterator.right.prev = iterator; /* zapisanie poprzedniego wezla */
            iterator.right.left = null;
            iterator.right.right = null;
            BitsAnalyze.down = 1; //ustawiamy wartość na gałęzi drzewa
            return iterator.right;
        }
    }

}
