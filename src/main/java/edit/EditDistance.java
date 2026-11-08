package edit;

/** One interface for all three Edit Distance versions, so the experiment is a single loop. */
public interface EditDistance {
    /** Minimum number of insertions, deletions and replacements that turn a into b. */
    int distance(String a, String b);

    /** Main counter of the LAST call: recursive calls (version A) or table cells filled (B, C). */
    long ops();

    /** Number of int cells allocated by the LAST call (0 for the recursive version). */
    long memCells();

    String name();
}
