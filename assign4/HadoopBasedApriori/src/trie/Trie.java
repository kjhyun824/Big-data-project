package trie;

import list.ItemSet;
import list.Transaction;

import java.util.ArrayList;

/**
 * Trie are used for efficiently searching for a pattern of items in a transaction in frequent
 * itemset mining algorithms. This represents the structure of a Trie.
 */

public class Trie {
    private TrieNode rootNode;
    final int height;

    public Trie(int height) {
        rootNode = new TrieNode();
        this.height = height;
    }

    public boolean add(ItemSet itemSet) {
        /** COMPLETE **/
        /* KJH */
        if (itemSet.size() != height) return false; // If the size of itemSet is not same as height, return false.

        TrieNode currNode = rootNode;
        boolean alreadyFailed = false; // Check that if the containing has failed before.

        for (int i = 0; i < height - 1; i++) { // Iteration for Non-leaf nodes
            if (alreadyFailed || currNode.get(itemSet.get(i)) == null) { // If it's already failed or it failed to get the item add it.
                TrieNode inNode = new TrieNode();

                /* This is for change TrieNode with non-empty ItemSet
                ItemSet inItem = new ItemSet(height);
                for (int j = 0; j < i; j++) {
                    inItem.add(itemSet.get(j));
                }
                inNode.add(inItem);
                */

                inNode.setLeafNode(false);

                currNode.put(itemSet.get(i), inNode);

                if (!alreadyFailed)
                    alreadyFailed = true;
            }
            currNode = currNode.get(itemSet.get(i)); // After put a node or the node already existed, change the current node to it.
        }

        if (!alreadyFailed && currNode.get(itemSet.get(height - 1)) != null) return false; // If it has not failed and the leaf node exists, the node already exists -> return false.

        TrieNode newNode = new TrieNode();
        newNode.add(itemSet);
        newNode.setLeafNode(true);
        currNode.put(itemSet.get(height - 1), newNode); // Add the itemSet and return true.
        return true;
    }

    boolean contains(ItemSet itemSet) {
        /** COMPLETE **/

        /* KJH */
        TrieNode currNode = rootNode;
        for (int i = 0; i < height; i++) {
            currNode = currNode.get(itemSet.get(i)); // Go down the trie
            if (currNode == null) return false; // If the node doesn't exist, return false.
        }

        return true;
    }

    public TrieNode getRootNode() {
        return rootNode;
    }

    public void findItemSets(ArrayList<ItemSet> matchedItemSet, ArrayList<Integer> transaction) {
        /** COMPLETE **/

        /* KJH */
        if (transaction.size() < height) return; // If the transaction size is smaller than height, just return.

        int[] idxArr = new int[height]; // Stores the indices of transaction.
        TrieNode[] nodeArr = new TrieNode[height]; // Stores the nodes of each height that which node is running now.
        int nodeIdx = 0;

        nodeArr[0] = rootNode;
        idxArr[0] = 0;

        while (true) {
            if(idxArr[nodeIdx] >= transaction.size()) {
                nodeIdx = reorderIdx(idxArr, nodeIdx-1);
                if(height == 1) {
                    if (isDone(idxArr[0],transaction.size())) break;
                }
            }

            TrieNode temp = nodeArr[nodeIdx].get(transaction.get(idxArr[nodeIdx])); // Get the child node
            if (temp != null) { // If child node is not null -> matched
                if (temp.isLeafNode()) { // If the child node is leaf node, it's an answer.
                    matchedItemSet.add(temp.getItemSet());
                    idxArr[nodeIdx] += 1;
                    if(isDone(idxArr[0],transaction.size())) break;
                } else { // If the child node is not leaf node, find next one.
                    nodeIdx++;
                    nodeArr[nodeIdx] = temp;
                    idxArr[nodeIdx] = idxArr[nodeIdx - 1] + 1;
                }
            } else {
                idxArr[nodeIdx] += 1;
                if(isDone(idxArr[0],transaction.size())) break;
            }
        }
    }

    public int reorderIdx(int[] idxArr, int startIdx) { // reorder index array from starting index
        if(startIdx == -1) return 0;

        while (true) {
            if (startIdx > 0 && (idxArr[startIdx] + 1) == idxArr[startIdx + 1]) { // If the index can't grow, check the previous index
                startIdx--;
            } else {
                idxArr[startIdx] += 1;
                break;
            }
        }

        for (int i = startIdx; i < height - 1; i++) { // Reorder the index array
            idxArr[i+ 1] = idxArr[i] + 1;
        }

        return startIdx;
    }

    public boolean isDone(int firstIdx, int transSize) {
        if (firstIdx >= transSize - height) { // If the index of the first element is (transaction.size - height) -> Checked everything -> break.
            return true;
        }

        return false;
    }

    public void printTrie(TrieNode node) { // Just for Debug
        if(node.isLeafNode()) {
            System.out.println("[DEBUG]" + node.toString());
            return;
        }

        for(Integer key : node.keySet()) {
            printTrie(node.get(key));
        }
    }
}
