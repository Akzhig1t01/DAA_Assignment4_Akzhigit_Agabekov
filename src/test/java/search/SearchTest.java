package search;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchTest {
    private final StringSearch[] methods = {new NaiveSearch(), new KmpSearch()};

    @Test
    void lpsReferenceArrays() {
        assertTrue(java.util.Arrays.equals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, KmpSearch.lps("AAACAAAA")));
        assertTrue(java.util.Arrays.equals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, KmpSearch.lps("ABABCABAB")));
        assertEquals(0, KmpSearch.lps("").length);
    }

    @Test
    void referenceMatches() {
        for (StringSearch s : methods) {
            assertEquals(List.of(10), s.search("ABABDABACDABABCABAB", "ABABCABAB"), s.name());
            assertEquals(List.of(0, 1, 2, 3), s.search("AAAAA", "AA"), s.name());       // overlapping
            assertEquals(List.of(0, 2, 4), s.search("ABABABA", "ABA"), s.name());       // overlapping
        }
    }

    @Test
    void naiveAndKmpAgreeOn100RandomTexts() {
        Random rnd = new Random(77);
        for (int t = 0; t < 100; t++) {
            String text = randomString(rnd, rnd.nextInt(60), 2);
            String pattern = randomString(rnd, 1 + rnd.nextInt(5), 2);
            assertEquals(methods[0].search(text, pattern), methods[1].search(text, pattern),
                    "text='" + text + "' pattern='" + pattern + "'");
        }
    }

    @Test
    void emptyPatternThrows() {
        for (StringSearch s : methods)
            assertThrows(IllegalArgumentException.class, () -> s.search("abc", ""));
    }

    @Test
    void patternLongerThanTextGivesEmptyList() {
        for (StringSearch s : methods) {
            assertTrue(s.search("ab", "abc").isEmpty(), s.name());
            assertTrue(s.search("", "a").isEmpty(), s.name());
        }
    }

    @Test
    void noMatchGivesEmptyList() {
        for (StringSearch s : methods) assertTrue(s.search("aaaa", "b").isEmpty(), s.name());
    }

    @Test
    void kmpNeverMakesMoreThan2nComparisons() {
        KmpSearch kmp = new KmpSearch();
        Random rnd = new Random(3);
        for (int t = 0; t < 100; t++) {
            String text = randomString(rnd, 1 + rnd.nextInt(200), 2);
            kmp.search(text, randomString(rnd, 1 + rnd.nextInt(8), 2));
            assertTrue(kmp.comparisons() <= 2L * text.length());
        }
        String text = "A".repeat(1000);
        kmp.search(text, "A".repeat(99) + "B");
        assertTrue(kmp.comparisons() <= 2000);
    }

    @Test
    void naiveCountsMTimesNOnItsWorstCase() {
        NaiveSearch naive = new NaiveSearch();
        naive.search("A".repeat(10), "AAB");          // 8 starts, 3 comparisons each
        assertEquals(24, naive.comparisons());
        naive.search("A".repeat(10), "BAA");          // 8 starts, 1 comparison each
        assertEquals(8, naive.comparisons());
    }

    private static String randomString(Random rnd, int len, int alphabet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append((char) ('A' + rnd.nextInt(alphabet)));
        return sb.toString();
    }
}
