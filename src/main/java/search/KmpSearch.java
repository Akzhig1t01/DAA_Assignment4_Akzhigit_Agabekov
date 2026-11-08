package search;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt. Build LPS (longest proper prefix that is also a suffix) first,
 * then scan the text once: the text index i never moves backwards, only the pattern index j falls back via LPS.
 * Time O(n + m), at most 2n comparisons in the search phase.
 */
public class KmpSearch implements StringSearch {
    private long comparisons;
    private long prep;

    /** lps[i] = length of the longest proper prefix of p[0..i] that is also a suffix of p[0..i]. */
    public static int[] lps(String p) {
        return new KmpSearch().buildLps(p);
    }

    private int[] buildLps(String p) {
        prep = 0;
        int m = p.length();
        int[] lps = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            prep++;
            if (p.charAt(i) == p.charAt(len)) {
                lps[i++] = ++len;
            } else if (len > 0) {
                len = lps[len - 1];                  // fall back, i stays
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        if (pattern.isEmpty()) throw new IllegalArgumentException("pattern must not be empty");
        comparisons = 0;
        int n = text.length(), m = pattern.length();
        int[] lps = buildLps(pattern);
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < n) {
            comparisons++;                           // one unit of work = one character comparison
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    result.add(i - m);
                    j = lps[j - 1];                  // allow overlapping matches
                }
            } else if (j > 0) {
                j = lps[j - 1];                      // i does NOT move back
            } else {
                i++;
            }
        }
        return result;
    }

    @Override public long comparisons() { return comparisons; }
    @Override public long prepOps() { return prep; }
    @Override public String name() { return "kmp"; }
}
