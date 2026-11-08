package search;

import java.util.ArrayList;
import java.util.List;

/** Try every start position and compare character by character. Worst case O(n*m). */
public class NaiveSearch implements StringSearch {
    private long comparisons;

    @Override
    public List<Integer> search(String text, String pattern) {
        if (pattern.isEmpty()) throw new IllegalArgumentException("pattern must not be empty");
        comparisons = 0;
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        for (int s = 0; s + m <= n; s++) {
            int j = 0;
            while (j < m) {
                comparisons++;                       // one unit of work = one character comparison
                if (text.charAt(s + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == m) result.add(s);
        }
        return result;
    }

    @Override public long comparisons() { return comparisons; }
    @Override public long prepOps() { return 0; }
    @Override public String name() { return "naive"; }
}
