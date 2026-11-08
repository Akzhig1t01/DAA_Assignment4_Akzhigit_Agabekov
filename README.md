# DAA Assignment 4 - Dynamic Programming and String Algorithms

City Search & Spell-Check: Edit Distance in three versions (recursive, table, two rows) and string search (Naive, KMP),
with counters inside the algorithms and a self-designed experiment (see the Experiment Design Card at the start of `REPORT.md`).

## Layout
```
src/main/java/edit         EditDistance (interface), Recursive / Table / TwoRows versions (DP design in comments)
src/main/java/search       StringSearch (interface), NaiveSearch, KmpSearch (public static int[] lps(String))
src/main/java/experiment   Generator (fixed seeds), Experiment (self-checking, writes results.csv)
src/test/java              JUnit 5 tests (reference values, random cross-checks, edge cases)
results/results.csv        all runs: experiment,algorithm,input,n,m,time_ms,ops,prep_ops,mem_cells,result
results/plots/             6 PNG plots
scripts/plots.py           draws the plots from results.csv
REPORT.md                  report: design card, plots, prediction vs result, short answers
```

`ops` = recursive calls (edit/recursive), table cells filled (edit/table, edit/two_rows) or character comparisons (naive, kmp);
`prep_ops` = KMP comparisons while building LPS; `mem_cells` = int cells allocated; `result` = the distance or the number of matches.

## Requirements
JDK 17+ and Maven 3.8+ (or only IntelliJ IDEA: File > Open > select the folder that contains `pom.xml`). Python 3 with matplotlib and numpy only for the plots.

## Build and test
```
mvn test
```
(IntelliJ: right-click `src/test/java` > Run 'All Tests'.)

## Run the experiment (reproducible: fixed seeds, same numbers every time)
```
mvn -q compile exec:java
```
or run the class `experiment.Experiment` in IntelliJ (VM options `-Xmx2g`, working directory = project root).
It takes about 1-2 minutes (the recursive version at n = 13 needs a few seconds), writes `results/results.csv`, and **fails loudly** if two versions of a family disagree
(three edit distance versions, Naive vs KMP), if KMP makes more than 2n comparisons, or if a known property is violated. The last line is `ALL CHECKS PASSED`.

## Plots
```
python3 scripts/plots.py
```

## AI use acknowledgement
Prepared with editorial and structural support from Claude (Anthropic). All algorithms were implemented, executed, and verified manually against reference standards.
