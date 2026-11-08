package edit;

/**
 * Version B: bottom-up with a full (n+1) x (m+1) table. Time O(n*m), memory O(n*m).
 *
 * DP design:
 *   State:      dp[i][j] = edit distance of the prefixes a[0..i) and b[0..j).
 *   Transition: dp[i][j] = dp[i-1][j-1]                       if a[i-1] == b[j-1]
 *               dp[i][j] = 1 + min(dp[i][j-1], dp[i-1][j], dp[i-1][j-1])   otherwise
 *   Base case:  dp[0][j] = j and dp[i][0] = i.
 * Answer: dp[n][m]. Every cell is computed exactly once.
 */
public class TableEditDistance implements EditDistance {
    private long cells;
    private long mem;

    @Override
    public int distance(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        mem = (long) (n + 1) * (m + 1);
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        long c = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                c++;                               // one unit of work = one cell filled by the transition
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i][j - 1], Math.min(dp[i - 1][j], dp[i - 1][j - 1]));
                }
            }
        }
        cells = c;
        return dp[n][m];
    }

    @Override public long ops() { return cells; }
    @Override public long memCells() { return mem; }
    @Override public String name() { return "table"; }
}
