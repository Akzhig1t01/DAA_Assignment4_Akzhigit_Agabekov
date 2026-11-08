package experiment;

import edit.EditDistance;
import edit.RecursiveEditDistance;
import edit.TableEditDistance;
import edit.TwoRowsEditDistance;
import search.KmpSearch;
import search.NaiveSearch;
import search.StringSearch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Self-checking experiment (see the Experiment Design Card in REPORT.md).
 * Usage: Experiment [outputCsv]      (default results/results.csv)
 * Fails loudly if two versions of the same family disagree or if a known property is violated.
 *
 * CSV columns: experiment,algorithm,input,n,m,time_ms,ops,prep_ops,mem_cells,result
 *   ops       = recursive calls (edit/recursive), table cells filled (edit/table, edit/two_rows),
 *               character comparisons in the search phase (naive, kmp)
 *   prep_ops  = KMP comparisons while building LPS (0 otherwise)
 *   mem_cells = int cells allocated (edit distance), LPS length (kmp), 0 otherwise
 *   result    = the distance (edit distance) or the number of matches (search)
 */
public class Experiment {
    static final int RUNS = 3;                                   // median of 3 runs

    // E1: all three versions, small sizes (the recursive one cannot go much further)
    static final int[] EDIT_SMALL = {4, 6, 8, 10, 12, 13};
    // E2: the two DP versions, large sizes
    static final int[] EDIT_LARGE = {250, 500, 1000, 2000, 4000};
    // E3: string search, growing text, fixed pattern length
    static final int[] SEARCH_N = {100_000, 200_000, 400_000, 800_000, 1_600_000};
    static final int SEARCH_M = 100;
    // E4: string search, fixed text, growing pattern length
    static final int FIXED_N = 1_000_000;
    static final int[] SEARCH_M_LIST = {10, 20, 40, 80, 160, 320};

    static final StringBuilder csv = new StringBuilder(
            "experiment,algorithm,input,n,m,time_ms,ops,prep_ops,mem_cells,result\n");

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        if (out.getParent() != null) Files.createDirectories(out.getParent());

        editSmall();
        editLarge();
        searchGrowingText();
        searchGrowingPattern();

        Files.writeString(out, csv.toString());
        System.out.println("\nALL CHECKS PASSED. Written: " + out);
    }

    // ---------------------------------------------------------------- edit distance
    /** Input types for edit distance, a = first string, b = second string. */
    static String[] editInput(String type, int n) {
        switch (type) {
            case "random":    // both random over {a,b,c,d}: a typical case, many matches
                return new String[]{Generator.random(n, "abcd", n), Generator.random(n, "abcd", 7_000_000L + n)};
            case "identical": // b == a: best case for the recursion (it follows the diagonal only)
                String s = Generator.random(n, "abcd", n);
                return new String[]{s, s};
            case "disjoint":  // no common character: worst case for the recursion (every call branches 3 ways)
                return new String[]{Generator.random(n, "ab", n), Generator.random(n, "cd", 7_000_000L + n)};
            default: throw new IllegalArgumentException(type);
        }
    }

    static final String[] EDIT_INPUTS = {"random", "identical", "disjoint"};

    static void editSmall() {
        System.out.println("== E1 edit_small: recursive vs table vs two_rows");
        for (int n : EDIT_SMALL) {
            for (String type : EDIT_INPUTS) {
                String[] ab = editInput(type, n);
                EditDistance[] versions = {new RecursiveEditDistance(), new TableEditDistance(), new TwoRowsEditDistance()};
                int[] answers = new int[3];
                for (int v = 0; v < 3; v++) {
                    answers[v] = measureEdit("edit_small", versions[v], type, ab[0], ab[1]);
                }
                check(answers[0] == answers[1] && answers[1] == answers[2],
                        "edit versions disagree for " + type + " n=" + n + ": " + Arrays.toString(answers));
                checkEditProperty(type, n, answers[0]);
            }
        }
    }

    static void editLarge() {
        System.out.println("== E2 edit_large: table vs two_rows");
        for (int n : EDIT_LARGE) {
            for (String type : EDIT_INPUTS) {
                String[] ab = editInput(type, n);
                int a1 = measureEdit("edit_large", new TableEditDistance(), type, ab[0], ab[1]);
                int a2 = measureEdit("edit_large", new TwoRowsEditDistance(), type, ab[0], ab[1]);
                check(a1 == a2, "table and two_rows disagree for " + type + " n=" + n);
                checkEditProperty(type, n, a1);
            }
        }
    }

    static void checkEditProperty(String type, int n, int d) {
        if (type.equals("identical")) check(d == 0, "identical strings must have distance 0");
        if (type.equals("disjoint")) check(d == n, "disjoint strings of length n must have distance n, got " + d);
    }

    static int measureEdit(String exp, EditDistance ed, String type, String a, String b) {
        double[] times = new double[RUNS];
        int result = 0;
        for (int r = 0; r < RUNS; r++) {
            long t0 = System.nanoTime();
            result = ed.distance(a, b);
            times[r] = (System.nanoTime() - t0) / 1e6;
        }
        double t = median(times);
        row(exp, ed.name(), type, a.length(), b.length(), t, ed.ops(), 0, ed.memCells(), result);
        return result;
    }

    // ---------------------------------------------------------------- string search
    /** Returns {text, pattern}. */
    static String[] searchInput(String type, int n, int m) {
        switch (type) {
            case "random": {     // random DNA-like text, pattern copied from the middle (at least one match)
                String t = Generator.random(n, "ACGT", n);
                return new String[]{t, t.substring(n / 2, n / 2 + m)};
            }
            case "best":         // first pattern character never occurs in the text: Naive needs 1 comparison per start
                return new String[]{Generator.repeat('A', n), "B" + Generator.repeat('A', m - 1)};
            case "worst":        // almost-match everywhere: Naive compares m-1 characters, then fails on the last one
                return new String[]{Generator.repeat('A', n), Generator.repeat('A', m - 1) + "B"};
            case "allmatch":     // a match at every position: maximum output, overlapping matches
                return new String[]{Generator.repeat('A', n), Generator.repeat('A', m)};
            default: throw new IllegalArgumentException(type);
        }
    }

    static final String[] SEARCH_INPUTS = {"random", "best", "worst", "allmatch"};

    static void searchGrowingText() {
        System.out.println("== E3 search_n: naive vs kmp, growing text, m = " + SEARCH_M);
        for (int n : SEARCH_N) {
            for (String type : SEARCH_INPUTS) {
                String[] tp = searchInput(type, n, SEARCH_M);
                runSearch("search_n", type, tp[0], tp[1]);
            }
        }
    }

    static void searchGrowingPattern() {
        System.out.println("== E4 search_m: naive vs kmp, text n = " + FIXED_N + ", growing pattern");
        for (int m : SEARCH_M_LIST) {
            for (String type : new String[]{"random", "worst"}) {
                String[] tp = searchInput(type, FIXED_N, m);
                runSearch("search_m", type, tp[0], tp[1]);
            }
        }
    }

    static void runSearch(String exp, String type, String text, String pattern) {
        NaiveSearch naive = new NaiveSearch();
        KmpSearch kmp = new KmpSearch();
        List<Integer> r1 = measureSearch(exp, naive, type, text, pattern);
        List<Integer> r2 = measureSearch(exp, kmp, type, text, pattern);
        int n = text.length(), m = pattern.length();
        check(r1.equals(r2), "naive and kmp return different matches for " + type + " n=" + n + " m=" + m);
        check(kmp.comparisons() <= 2L * n, "KMP made more than 2n comparisons");
        if (type.equals("best") || type.equals("worst")) check(r1.isEmpty(), type + " input must have no match");
        if (type.equals("allmatch")) check(r1.size() == n - m + 1, "allmatch must match at every start");
        if (type.equals("random")) check(!r1.isEmpty(), "random input has a planted match");
    }

    static List<Integer> measureSearch(String exp, StringSearch s, String type, String text, String pattern) {
        double[] times = new double[RUNS];
        List<Integer> result = null;
        for (int r = 0; r < RUNS; r++) {
            long t0 = System.nanoTime();
            result = s.search(text, pattern);
            times[r] = (System.nanoTime() - t0) / 1e6;
        }
        long mem = s.name().equals("kmp") ? pattern.length() : 0;
        row(exp, s.name(), type, text.length(), pattern.length(), median(times), s.comparisons(), s.prepOps(), mem, result.size());
        return result;
    }

    // ---------------------------------------------------------------- helpers
    static void row(String exp, String algo, String type, int n, int m, double timeMs,
                    long ops, long prep, long mem, long result) {
        csv.append(String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%.3f,%d,%d,%d,%d%n",
                exp, algo, type, n, m, timeMs, ops, prep, mem, result));
        System.out.printf(Locale.ROOT, "%-11s %-10s %-10s n=%-8d m=%-8d %10.2f ms  ops=%-12d result=%d%n",
                exp, algo, type, n, m, timeMs, ops, result);
    }

    static double median(double[] t) {
        double[] c = t.clone();
        Arrays.sort(c);
        return c[c.length / 2];
    }

    static void check(boolean ok, String msg) {
        if (!ok) throw new IllegalStateException("EXPERIMENT CHECK FAILED: " + msg);
    }
}
