import java.util.LinkedList;
import java.util.ListIterator;

public class MyHashSet {

    @SuppressWarnings("unchecked")
    private final LinkedList<Integer>[] array = new LinkedList[8];

    public void add(int key){
        int index = ( key % 8 + 8 ) & 7;
        LinkedList<Integer> integers = array[index];
        if (!contains(key)) {
            integers.add(key);
        }
    }

    public boolean contains(int key){
        int index = ( key % 8 + 8 ) & 7;
        LinkedList<Integer> integers = array[index];
        return integers.contains(key);
    }



}
