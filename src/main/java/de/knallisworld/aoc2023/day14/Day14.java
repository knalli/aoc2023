package de.knallisworld.aoc2023.day14;

import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day14 {

	public static void main(String[] args) {
		printHeader(14);
		printSolution(1, () -> part1(parseInput(readInputLines(14, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(14, "part1"))));
	}

	enum Tile {
		Rock,
		Dish,
		Empty
	}

	static FixGrid<Tile> parseInput(final List<String> lines) {
		final var grid = FixGrid.create(Tile.class, lines.size(), lines.getFirst().length());
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 IntStream.range(0, lines.get(y).length())
							  .forEach(x -> {
								  grid.setValue(x, y, switch (lines.get(y).charAt(x)) {
									  case '.' -> Tile.Empty;
									  case '#' -> Tile.Rock;
									  case 'O' -> Tile.Dish;
									  default -> throw new IllegalStateException("invalid input");
								  });
							  });
				 });
		return grid;
	}

	static void renderGrid(final FixGrid<Tile> grid) {
		System.out.println("=".repeat(100));
		System.out.println(grid.toString((_, v) -> switch (v) {
			case Rock -> "#";
			case Empty -> ".";
			case Dish -> "O";
		}));
		System.out.println("=".repeat(100));
	}

	static long calcLoad(final FixGrid<Tile> grid) {
		final var height = grid.getHeight();
		return IntStream.range(0, height)
						.mapToLong(i -> {
							final var power = height - i;
							return power * grid.fields()
											   .row(i)
											   .filter(f -> f.value() == Tile.Dish)
											   .count();
						})
						.sum();
	}

	static String part1(final FixGrid<Tile> grid) {
		doRoll(grid);
		return "sum = %d".formatted(calcLoad(grid));
	}

	static String part2(final FixGrid<Tile> grid) {

		final var cache = new HashMap<String, Long>();

		final Runnable cycle = () -> {
			// north
			doRoll(grid);
			// west
			grid.transform().rotateLeft();
			doRoll(grid);
			// south
			grid.transform().rotateLeft();
			doRoll(grid);
			// east
			grid.transform().rotateLeft();
			doRoll(grid);
			// back to north
			grid.transform().rotateLeft();
		};

		final Supplier<String> keyGen = () -> grid.toString((_, v) -> v.toString());

		final var max = 1_000_000_000;
		LongStream.range(0, max)
				  // run as long as not matching cache
				  .dropWhile(i -> {
					  cycle.run();
					  final var key = keyGen.get();
					  if (cache.containsKey(key)) {
						  return false;
					  }
					  cache.put(key, i);
					  return true;
				  })
				  .findFirst()
				  .stream()
				  .flatMap(i -> {
					  // skip with knowledge
					  final var cycleLen = i - cache.get(keyGen.get());
					  final var rounds = (max - 1 - i) / cycleLen;
					  return LongStream.range(i + 1 + (cycleLen * rounds), max);
				  })
				  .forEach(_ -> cycle.run());
		return "sum = %d".formatted(calcLoad(grid));
	}

	static void doRoll(final FixGrid<Tile> grid) {
		for (int x = 0; x < grid.getWidth(); x++) {
			var base = 0;
			while (base < grid.getHeight()) {
				if (grid.getValueRequired(x, base) == Tile.Empty) {
					// try to pull
					boolean pullStop = false;
					for (var y = base + 1; y < grid.getHeight(); y++) {
						switch (grid.getValueRequired(x, y)) {
							case Dish -> {
								grid.setValue(x, base, Tile.Dish);
								grid.setValue(x, y, Tile.Empty);
								base++;
							}
							case Rock -> {
								pullStop = true;
							}
						}
						if (pullStop) {
							break;
						}
					}
				}
				base++;
			}
		}
	}

}

