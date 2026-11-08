package search;

import java.util.List;

/** One interface for both search methods. */
public interface StringSearch {
    /**
     * Start indices (from 0) of all occurrences of pattern in text, increasing, overlapping ones included.
     * @throws IllegalArgumentException if the pattern is empty
     */
    List<Integer> search(String text, String pattern);

    /** Character comparisons text[..] vs pattern[..] made by the LAST search (search phase only). */
    long comparisons();

    /** Preparation work of the LAST search (KMP: comparisons while building LPS; Naive: 0). */
    long prepOps();

    String name();
}
