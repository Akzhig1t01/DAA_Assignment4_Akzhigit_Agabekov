#!/usr/bin/env python3
"""Reads results/results.csv and writes 6 PNG plots to results/plots/ (plus results/slopes.txt)."""
import csv, os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

root = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
rows = list(csv.DictReader(open(os.path.join(root, "results", "results.csv"))))
for r in rows:
    for k in ("n", "m", "ops", "prep_ops", "mem_cells", "result"):
        r[k] = int(r[k])
    r["time_ms"] = float(r["time_ms"])
out = os.path.join(root, "results", "plots")
os.makedirs(out, exist_ok=True)

def get(exp, algo, inp):
    return sorted((r for r in rows if r["experiment"] == exp and r["algorithm"] == algo and r["input"] == inp),
                  key=lambda r: (r["n"], r["m"]))

def finish(fig, ax, name, takeaway):
    ax.grid(True, which="both", alpha=0.3)
    fig.text(0.5, 0.01, takeaway, ha="center", fontsize=9, style="italic", wrap=True)
    fig.tight_layout(rect=(0, 0.05, 1, 1))
    fig.savefig(os.path.join(out, name), dpi=150)
    plt.close(fig)

slopes = []
def slope(label, xs, ys):
    s = float(np.polyfit(np.log10(xs), np.log10(ys), 1)[0])
    slopes.append(f"{label}: slope {s:.2f}")
    return s

# 1. recursion vs DP
fig, ax = plt.subplots(figsize=(8, 5.5))
colors = {"disjoint": "tab:red", "random": "tab:orange", "identical": "tab:green"}
for inp in ("disjoint", "random", "identical"):
    s = get("edit_small", "recursive", inp)
    ax.plot([r["n"] for r in s], [r["ops"] for r in s], "o-", color=colors[inp], label=f"recursive calls, input '{inp}'")
s = get("edit_small", "table", "random")
ax.plot([r["n"] for r in s], [r["ops"] for r in s], "k--s", label="table / two rows: cells filled (= n*n)")
ax.set_yscale("log"); ax.set_xlabel("string length n (characters, both strings)")
ax.set_ylabel("operations (calls or cells), log scale"); ax.set_title("Edit distance: recursion explodes, DP does not")
ax.legend(fontsize=8)
d = get("edit_small", "recursive", "disjoint")
finish(fig, ax, "1_edit_recursive_vs_dp.png",
       f"Recursive calls on 'disjoint' grow ~{(d[-1]['ops']/d[-2]['ops']):.1f}x per extra character (straight line on a log axis); DP stays at n*n.")
slopes.append("recursive disjoint growth per char (n=12->13): %.2f" % (d[-1]["ops"] / d[-2]["ops"]))

# 2. memory
fig, ax = plt.subplots(figsize=(8, 5.5))
for algo, st in (("table", "o-"), ("two_rows", "s-")):
    s = get("edit_large", algo, "random")
    ax.plot([r["n"] for r in s], [r["mem_cells"] for r in s], st, label=algo)
ax.set_xscale("log"); ax.set_yscale("log")
ax.set_xlabel("string length n (characters)"); ax.set_ylabel("memory: int cells allocated (1 cell = 4 bytes), log scale")
ax.set_title("Edit distance memory: full table vs two rows"); ax.legend()
t = get("edit_large", "table", "random")[-1]; w = get("edit_large", "two_rows", "random")[-1]
finish(fig, ax, "2_edit_memory.png",
       f"At n = {t['n']} the table holds {t['mem_cells']:,} cells, two rows only {w['mem_cells']:,} ({t['mem_cells']//w['mem_cells']}x less).")

# 3. time of DP versions
fig, ax = plt.subplots(figsize=(8, 5.5))
for algo, c in (("table", "tab:blue"), ("two_rows", "tab:orange")):
    for inp, ls in (("random", "-"), ("identical", ":"), ("disjoint", "--")):
        s = get("edit_large", algo, inp)
        ax.plot([r["n"] for r in s], [r["time_ms"] for r in s], ls, marker="o", ms=4, color=c, label=f"{algo}, '{inp}'")
        if inp == "random":
            big = [r for r in s if r["n"] >= 1000]          # n = 250 is dominated by JIT warm-up
            slope(f"edit time {algo} random (n >= 1000)", [r["n"] for r in big], [r["time_ms"] for r in big])
ns = np.array([250, 4000])
ax.plot(ns, 148 * (ns / 4000.0) ** 2, "k-.", lw=1, label="reference: time ~ n^2")
ax.set_xscale("log"); ax.set_yscale("log"); ax.set_xlabel("string length n (characters)")
ax.set_ylabel("time (ms, median of 3 runs), log scale"); ax.set_title("Edit distance time: table vs two rows")
ax.legend(fontsize=7, ncol=2)
sav = []
for n_ in (2000, 4000):
    for inp_ in ("random", "identical", "disjoint"):
        tt = [r for r in get("edit_large", "table", inp_) if r["n"] == n_][0]["time_ms"]
        tw = [r for r in get("edit_large", "two_rows", inp_) if r["n"] == n_][0]["time_ms"]
        sav.append(100 * (1 - tw / tt))
finish(fig, ax, "3_edit_time.png",
       f"From n = 1000 up the time grows about like n^2 and does not depend on the input; the two rows need {min(sav):.0f}-{max(sav):.0f}% less time at n >= 2000. n = 250 is JIT warm-up.")

# 4. comparisons per character (ratio, log y)
fig, ax = plt.subplots(figsize=(8, 5.5))
ic = {"random": "tab:blue", "best": "tab:green", "worst": "tab:red", "allmatch": "tab:purple"}
for inp in ("random", "best", "worst", "allmatch"):
    for algo, ls in (("naive", "-"), ("kmp", "--")):
        s = get("search_n", algo, inp)
        ax.plot([r["n"] for r in s], [r["ops"] / r["n"] for r in s], ls, marker="o" if algo == "naive" else "s", ms=4,
                color=ic[inp], label=f"{algo}, '{inp}'")
ax.axhline(2, color="gray", lw=1, ls=":"); ax.text(110000, 2.2, "KMP bound: 2 per character", fontsize=8, color="gray")
ax.set_xscale("log"); ax.set_yscale("log"); ax.set_xlabel("text length n (characters), pattern length m = 100")
ax.set_ylabel("character comparisons per text character, log scale")
ax.set_title("String search: comparisons per text character"); ax.legend(fontsize=7, ncol=2, loc="center right")
finish(fig, ax, "4_search_comparisons_per_char.png",
       "Naive needs ~m = 100 comparisons per character on 'worst' and 'allmatch'; KMP never exceeds 2, whatever the input.")

# 5. comparisons vs m
fig, ax = plt.subplots(figsize=(8, 5.5))
for inp, c in (("worst", "tab:red"), ("random", "tab:blue")):
    for algo, ls in (("naive", "-"), ("kmp", "--")):
        s = get("search_m", algo, inp)
        ax.plot([r["m"] for r in s], [r["ops"] for r in s], ls, marker="o", color=c, label=f"{algo}, '{inp}'")
        if algo == "naive" and inp == "worst": m_slope = slope("naive worst comparisons vs m", [r["m"] for r in s], [r["ops"] for r in s])
ax.set_xscale("log"); ax.set_yscale("log"); ax.set_xlabel("pattern length m (characters), text n = 1 000 000")
ax.set_ylabel("character comparisons, log scale"); ax.set_title("String search: effect of the pattern length"); ax.legend(fontsize=8)
finish(fig, ax, "5_search_vs_pattern_length.png",
       f"Naive on 'worst' grows in direct proportion to m (slope {m_slope:.2f} on log-log axes); KMP stays flat at 2n.")

# 6. time on worst/allmatch
fig, ax = plt.subplots(figsize=(8, 5.5))
for inp, c in (("worst", "tab:red"), ("allmatch", "tab:purple")):
    for algo, ls in (("naive", "-"), ("kmp", "--")):
        s = get("search_n", algo, inp)
        ax.plot([r["n"] for r in s], [r["time_ms"] for r in s], ls, marker="o", color=c, label=f"{algo}, '{inp}'")
        slope(f"search time {algo} {inp}", [r["n"] for r in s], [r["time_ms"] for r in s])
ax.set_xscale("log"); ax.set_yscale("log"); ax.set_xlabel("text length n (characters), pattern length m = 100")
ax.set_ylabel("time (ms, median of 3 runs), log scale"); ax.set_title("String search time on the hardest inputs"); ax.legend(fontsize=8)
ratios = [a["time_ms"] / b["time_ms"] for a, b in zip(get("search_n", "naive", "worst"), get("search_n", "kmp", "worst"))]
finish(fig, ax, "6_search_time.png",
       f"Naive is {min(ratios):.0f}-{max(ratios):.0f}x slower than KMP on 'worst'; on 'allmatch' KMP is slower than its comparison count suggests because it builds a huge list of matches.")

open(os.path.join(root, "results", "slopes.txt"), "w").write("\n".join(slopes) + "\n")
print("\n".join(slopes))
