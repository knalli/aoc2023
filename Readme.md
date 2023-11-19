# Advent of Code 2023 Solutions

Welcome to my solutions of [Advent Of Code](http://adventofcode.com) 2023 (AOC 2023).

A huge thanks to @topaz and his team for providing this great service.

After multiple years using Go and last year's Rust, this time I will step back using
the good old Java. Needless to say, this means the latest Java 21 with enabled preview
flags. There may be usage of virtual threads, structured scopes and even
native experiments (although not part of Java itself, but an overall current theme).
We put all the s… together.

## Disclaimer

These are my personal solutions of the Advent Of Code (AOC). The code is
*not indented* to be perfect in any kind of area. 
These snippets are here for everyone learning more, too.

If you think, there is a piece of improvement: Go to the code,
fill a PR, and we are all happy. Share the knowledge.

## Structure

The AOC contains 25 days with at least one puzzle/question per day (mostly there are two parts).

* Base path is the root folder with a standard Maven project. The base
  package is `de.knallisworld.aoc2023`.
* Each day is a sub package named `day01`, `day02` until `day25` with
  a corresponding main file
* Each day may contain additional test files (in the test dir)
* Depending on content, a day could import (exported) symbols of a (previous) day.

## Usage

For running the day `day00`

* Use your IDE, it's simpler: Run the specific day.
* CLI: just enter `export DAY=01; ./mvnw compile exec:java -Dexec.mainClass=de.knallisworld.aoc2023.day$DAY.Day$DAY`

## License / Copyright

Everything is free for all.

Licensed under MIT. Copyright Jan Philipp.
