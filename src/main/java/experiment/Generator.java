package experiment;

import java.util.Random;

/** Deterministic input generators. A fresh Random(seed) is created for every call, so the same call gives the same string. */
public final class Generator {
    private Generator() {}

    public static final long BASE_SEED = 42;

    public static String random(int n, String alphabet, long seed) {
        Random rnd = new Random(BASE_SEED + seed);
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        return sb.toString();
    }

    public static String repeat(char c, int n) {
        return String.valueOf(c).repeat(n);
    }
}
