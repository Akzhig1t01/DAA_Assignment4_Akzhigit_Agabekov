package edit;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditDistanceTest {
    private final EditDistance[] versions = {
            new RecursiveEditDistance(), new TableEditDistance(), new TwoRowsEditDistance()};

    @Test
    void referenceValues() {
        for (EditDistance d : versions) {
            assertEquals(3, d.distance("kitten", "sitting"), d.name());
            assertEquals(2, d.distance("flaw", "lawn"), d.name());
            assertEquals(5, d.distance("intention", "execution"), d.name());
            assertEquals(3, d.distance("", "abc"), d.name());
            assertEquals(0, d.distance("abc", "abc"), d.name());
        }
    }

    @Test
    void emptyStrings() {
        for (EditDistance d : versions) {
            assertEquals(0, d.distance("", ""), d.name());
            assertEquals(4, d.distance("abcd", ""), d.name());
            assertEquals(4, d.distance("", "abcd"), d.name());
        }
    }

    @Test
    void threeVersionsAgreeOn100RandomShortStrings() {
        Random rnd = new Random(2024);
        for (int t = 0; t < 100; t++) {
            String a = randomString(rnd, rnd.nextInt(9));
            String b = randomString(rnd, rnd.nextInt(9));
            int expected = versions[1].distance(a, b);
            for (EditDistance d : versions)
                assertEquals(expected, d.distance(a, b), d.name() + " on '" + a + "','" + b + "'");
        }
    }

    @Test
    void distanceIsSymmetric() {
        Random rnd = new Random(5);
        for (int t = 0; t < 50; t++) {
            String a = randomString(rnd, rnd.nextInt(30)), b = randomString(rnd, rnd.nextInt(30));
            assertEquals(versions[2].distance(a, b), versions[2].distance(b, a));
        }
    }

    @Test
    void countersAreMeasuredInsideTheAlgorithms() {
        TableEditDistance table = new TableEditDistance();
        table.distance("abcd", "xyz");
        assertEquals(12, table.ops());                 // 4 * 3 cells
        assertEquals(20, table.memCells());            // 5 * 4 cells
        TwoRowsEditDistance two = new TwoRowsEditDistance();
        two.distance("abcd", "xyz");
        assertEquals(12, two.ops());
        assertEquals(8, two.memCells());               // 2 * (3 + 1)
        RecursiveEditDistance rec = new RecursiveEditDistance();
        rec.distance("same", "same");
        assertEquals(5, rec.ops());                    // follows the diagonal: n + 1 calls
    }

    private static String randomString(Random rnd, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append((char) ('a' + rnd.nextInt(3)));
        return sb.toString();
    }
}
