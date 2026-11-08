package edit;

/**
 * Version C: same recurrence, but only two rows are kept. Time O(n*m), memory O(m).
 *
 * DP design:
 *   State:      prev[j] = dp[i-1][j] and cur[j] = dp[i][j] (only the previous and the current row exist).
 *   Transition: cur[j] = prev[j-1]                              if a[i-1] == b[j-1]
 *               cur[j] = 1 + min(cur[j-1], prev[j], prev[j-1])  otherwise
 *   Base case:  prev[j] = j for the row i = 0, and cur[0] = i at the start of row i.
 * After each row the two arrays are swapped. Answer: prev[m] after the last row.
 * Trade-off: the distance is the same, but the table is gone, so the edit operations cannot be traced back.
 */
public class TwoRowsEditDistance implements EditDistance {
    private long cells;
    private long mem;

    @Override
    public int distance(String a, String b) {
        int n = a.length(), m = b.length();
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        mem = 2L * (m + 1);
        for (int j = 0; j <= m; j++) prev[j] = j;
        long c = 0;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            for (int j = 1; j <= m; j++) {
                c++;
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    cur[j] = prev[j - 1];
                } else {
                    cur[j] = 1 + Math.min(cur[j - 1], Math.min(prev[j], prev[j - 1]));
                }
            }
            int[] t = prev; prev = cur; cur = t;
        }
        cells = c;
        return prev[m];
    }

    @Override public long ops() { return cells; }
    @Override public long memCells() { return mem; }
    @Override public String name() { return "two_rows"; }
}
