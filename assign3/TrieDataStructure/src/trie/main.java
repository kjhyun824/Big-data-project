package trie;

import java.util.*;

public class main {
    public static void main(String[] args) {
        /** OPTIONAL **/
        // Test freely whether your implementation is correct.

        ArrayList<ItemSet> matchedItemSet = new ArrayList<>();

        Trie trie = new Trie(4);

        ItemSet[] itemSets = new ItemSet[21];
        Random random = new Random();
        for(int i = 0; i < 20; i++) {
            itemSets[i] = new ItemSet(i);
            Set<Integer> arr = new HashSet<>();

            while(arr.size() < 4) {
                arr.add(random.nextInt(16));
            }
            Iterator<Integer> iter = arr.iterator();
            for(int j = 0; j < 4; j++) {
                itemSets[i].add(iter.next());
            }
            trie.add(itemSets[i]);
        }
        itemSets[20] = new ItemSet(20);
        itemSets[20].add(11);
        itemSets[20].add(12);
        itemSets[20].add(13);
        itemSets[20].add(14);
        trie.add(itemSets[20]);

        trie.printTrie(trie.getRootNode());
        ArrayList<Integer> transaction = new ArrayList<>();

        for(int i = 0; i < 15; i++) {
            transaction.add(i);
        }
        trie.findItemSets(matchedItemSet,transaction);

        System.out.println(matchedItemSet);
    }

}
