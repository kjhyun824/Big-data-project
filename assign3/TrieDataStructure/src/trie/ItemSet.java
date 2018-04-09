package trie;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemSet extends ArrayList<Integer> {
    int supportCount;   // Ignore this value. This is used in the next assignment

    public ItemSet(int supportCount) {
        this.supportCount = supportCount;
    }

    @Override
    public String toString() {
        return "ItemSet [items=" + Arrays.toString(toArray()) + ", supportCount=" + supportCount + "]";
    }

    public Integer get(int entry) {
        return super.get(entry);
    }

    public boolean add(int value) {
        if (super.contains(value)) {
            return false;
        }
        else {
            super.add(value);
            return true;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this == null) ? 0 : this.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ItemSet other = (ItemSet) obj;
        if (this == null) {
            if (other != null) {
                return false;
            }
        }
        else if (!super.equals(other)) {
            return false;
        }
        return true;
    }
}
