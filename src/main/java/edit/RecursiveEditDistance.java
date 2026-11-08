package edit;

/**
 * Version A: plain recursion, no memory (slow on purpose).
 *
 * DP design:
 *   State:      f(i, j) = edit distance between the first i characters of a and the first j characters of b.
 *   Transition: if a[i-1] == b[j-1]:  f(i, j) = f(i-1, j-1)
 *               else f(i, j) = 1 + min( f(i, j-1)     insert b[j-1],
 *                                       f(i-1, j)     delete a[i-1],
 *                                       f(i-1, j-1) ) replace a[i-1] by b[j-1]
 *   Base case:  f(0, j) = j (insert j characters),  f(i, 0) = i (delete i characters).
 * The same f(i, j) is computed again and again, so the number of calls grows exponentially.
 */
public class RecursiveEditDistance implements EditDistance {
    private long calls;

    @Override
    public int distance(String a, String b) {
        calls = 0;
        return f(a, b, a.length(), b.length());
    }

    private int f(String a, String b, int i, int j) {
        calls++;                                   // one unit of work = one call
        if (i == 0) return j;
        if (j == 0) return i;
        if (a.charAt(i - 1) == b.charAt(j - 1)) return f(a, b, i - 1, j - 1);
        int ins = f(a, b, i, j - 1);
        int del = f(a, b, i - 1, j);
        int rep = f(a, b, i - 1, j - 1);
        return 1 + Math.min(ins, Math.min(del, rep));
    }

    @Override public long ops() { return calls; }
    @Override public long memCells() { return 0; }
    @Override public String name() { return "recursive"; }
}
