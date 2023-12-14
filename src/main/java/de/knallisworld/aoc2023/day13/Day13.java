package de.knallisworld.aoc2023.day13;

import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day13 {

	public static void main(String[] args) {
		printHeader(13);
		printSolution(1, () -> part1(parseInput(readInputLines(13, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(13, "part1"))));
	}

	enum Tile {
		Ash,
		Rock;

		@Override
		public String toString() {
			return switch (this) {
				case Ash -> ".";
				case Rock -> "#";
			};
		}
	}

	record Input(List<FixGrid<Tile>> patterns) {
	}

	static Input parseInput(final List<String> lines) {
		final var result = new ArrayList<FixGrid<Tile>>();

		final var backlog = new ArrayList<String>();
		for (int i = 0; i <= lines.size(); i++) {
			if (i < lines.size()) {
				final var line = lines.get(i);
				if (!line.isEmpty()) {
					backlog.add(line);
					continue;
				}
			}
			final var grid = FixGrid.create(Tile.class, backlog.size(), backlog.getFirst().length());
			IntStream.range(0, backlog.size())
					 .forEach(y -> {
						 IntStream.range(0, backlog.get(y).length())
								  .forEach(x -> {
									  grid.setValue(x, y, switch (backlog.get(y).charAt(x)) {
										  case '.' -> Tile.Ash;
										  case '#' -> Tile.Rock;
										  default -> throw new IllegalStateException("invalid input");
									  });
								  });
					 });
			result.add(grid);
			backlog.clear();
		}

		return new Input(List.copyOf(result));
	}

	static String part1(final Input input) {
		final var sum = input
				.patterns
				.stream()
				.mapToInt(pattern -> calc(pattern, false))
				.sum();
		return "sum = %d".formatted(sum);
	}

	static String part2(final Input input) {
		final var sum = input
				.patterns
				.stream()
				.mapToInt(pattern -> calc(pattern, true))
				.sum();
		return "sum = %d".formatted(sum);
	}

	static int calc(final FixGrid<Tile> pattern, final boolean useSmudge) {
		final int sumCols;
		{
			final var items = IntStream.range(0, pattern.getWidth() - 1)
									   .filter(x -> {
										   final var original = isMirrorAtColumn(pattern, x, x + 1);
										   if (useSmudge) {
											   final var found = isMirrorAtColumn(pattern, x, x + 1, new AtomicInteger(1));
											   return original != found;
										   } else {
											   return original;
										   }
									   })
									   .map(x -> x + 1)
									   .boxed()
									   .toList();
			if (items.size() > 1 && !useSmudge) {
				throw new IllegalStateException("only one column mirror");
			}
			sumCols = items.stream().findAny().orElse(0);
		}
		final int sumRows;
		{
			final var items = IntStream.range(0, pattern.getHeight() - 1)
									   .filter(y -> {
										   final var original = isMirrorAtRow(pattern, y, y + 1);
										   if (useSmudge) {
											   final var found = isMirrorAtRow(pattern, y, y + 1, new AtomicInteger(1));
											   return original != found;
										   } else {
											   return original;
										   }
									   })
									   .map(y -> 100 * (y + 1))
									   .boxed()
									   .toList();
			if (items.size() > 1 && !useSmudge) {
				throw new IllegalStateException("only one row mirror");
			}
			sumRows = items.stream().findAny().orElse(0);
		}
		if (sumCols == 0 && sumRows == 0) {
			throw new IllegalStateException("at least one mirror required");
		} else if (sumCols > 0 && sumRows > 0 && !useSmudge) {
			throw new IllegalStateException("at least one mirror required");
		}
		return sumCols + sumRows;
	}

	static Stream<FixGrid<Tile>> getPatternVariations(final FixGrid<Tile> pattern) {
		return IntStream
				.range(0, pattern.getHeight())
				.boxed()
				.flatMap(y -> IntStream
						.range(0, pattern.getWidth())
						.boxed()
						.map(x -> {
							final var copy = FixGrid.copy(pattern);
							copy.setValue(x, y, switch (copy.getValueRequired(x, y)) {
								case Ash -> Tile.Rock;
								case Rock -> Tile.Ash;
							});
							return copy;
						}));
	}

	static boolean areRowValuesIdentical(final FixGrid<Tile> grid,
										 final int y1,
										 final int y2,
										 final AtomicInteger smudgesLeft) {
		if (y1 == y2) {
			throw new IllegalStateException("invalid y1 or y2");
		}
		return IntStream.range(0, grid.getWidth())
						.allMatch(x -> {
							final var r = Objects.equals(grid.getValueRequired(x, y1), grid.getValueRequired(x, y2));
							if (!r && smudgesLeft.get() > 0) {
								smudgesLeft.decrementAndGet();
								return true;
							}
							return r;
						});
	}

	static boolean areColumnValuesIdentical(final FixGrid<Tile> grid,
											final int x1,
											final int x2,
											final AtomicInteger smudgesLeft) {
		if (x1 == x2) {
			throw new IllegalStateException("invalid x1 or x2");
		}
		return IntStream.range(0, grid.getHeight())
						.allMatch(y -> {
							final var r = Objects.equals(grid.getValueRequired(x1, y), grid.getValueRequired(x2, y));
							if (!r && smudgesLeft.get() > 0) {
								smudgesLeft.decrementAndGet();
								return true;
							}
							return r;
						});
	}

	static boolean isMirrorAtRow(final FixGrid<Tile> grid,
								 final int y1,
								 final int y2) {
		return isMirrorAtRow(grid, y1, y2, new AtomicInteger(0));
	}

	static boolean isMirrorAtRow(final FixGrid<Tile> grid,
								 final int y1,
								 final int y2,
								 final AtomicInteger smudgesLeft) {
		if (y2 < y1) {
			return isMirrorAtRow(grid, y2, y1, smudgesLeft);
		}
		if (y1 + 1 != y2) {
			throw new IllegalStateException("invalid row mirror");
		}
		final var l = Math.min(y1, grid.getHeight() - 1 - y2);
		if (l < 0) {
			return false;
		}
		return IntStream.rangeClosed(0, l)
						.allMatch(d -> areRowValuesIdentical(grid, y1 - d, y2 + d, smudgesLeft));
	}

	static boolean isMirrorAtColumn(final FixGrid<Tile> grid,
									final int x1,
									final int x2) {
		return isMirrorAtColumn(grid, x1, x2, new AtomicInteger(0));
	}

	static boolean isMirrorAtColumn(final FixGrid<Tile> grid,
									final int x1,
									final int x2,
									final AtomicInteger smudgesLeft) {
		if (x2 < x1) {
			return isMirrorAtColumn(grid, x2, x1, smudgesLeft);
		}
		if (x1 + 1 != x2) {
			throw new IllegalStateException("invalid column mirror");
		}
		final var l = Math.min(x1, grid.getWidth() - 1 - x2);
		if (l < 0) {
			return false;
		}
		return IntStream.rangeClosed(0, l)
						.allMatch(d -> areColumnValuesIdentical(grid, x1 - d, x2 + d, smudgesLeft));
	}

}

