package de.knallisworld.aoc2023.day21;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import de.knallisworld.aoc2023.support.geo.grid2.InfiniteGrid;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.lang.Math.floorMod;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

@Log4j2
public class Day21 {

	public static void main(String[] args) {
		printHeader(21);
		printSolution(1, () -> part1(parseInput(readInputLines(21, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(21, "part0"))));
		printSolution(2, () -> part2(parseInput(readInputLines(21, "part1"))));
	}

	enum Tile {
		Empty,
		Rock,
		Gardener
	}

	static FixGrid<Tile> parseInput(final List<String> lines) {
		final var grid = FixGrid.create(Tile.class, lines.size(), lines.getFirst().length());
		for (var y = 0; y < lines.size(); y++) {
			final var line = lines.get(y);
			for (var x = 0; x < line.length(); x++) {
				grid.setValue(x, y, switch (line.charAt(x)) {
					case '.' -> Tile.Empty;
					case '#' -> Tile.Rock;
					case 'S' -> Tile.Gardener;
					default -> throw new IllegalStateException("invalid input");
				});
			}
		}
		return grid;
	}

	static String part1(final FixGrid<Tile> grid) {
		final var start = grid.fields().stream()
							  .filter(f -> f.value() == Tile.Gardener)
							  .findFirst()
							  .map(FixGrid.FieldsView.Field::pos)
							  .orElseThrow();
		grid.setValue(start, Tile.Empty);

		final var last = new HashSet<Point2D<Integer>>();
		last.add(start);
		for (int i = 0; i < 64; i++) {
			final var next = last
					.stream()
					.flatMap(p -> grid.fields().getAdjacents4(p))
					.distinct()
					.filter(p -> grid.getValueRequired(p) == Tile.Empty)
					.toList();
			last.clear();
			last.addAll(next);
		}
		return "sum = %d".formatted(last.size());
	}

	static String part2(final FixGrid<Tile> grid) {
		final var start = grid.fields().stream()
							  .filter(f -> f.value() == Tile.Gardener)
							  .findFirst()
							  .map(FixGrid.FieldsView.Field::pos)
							  .orElseThrow();
		grid.setValue(start, Tile.Empty);

		final Map<Point2D<Integer>, Set<Point2D<Integer>>> offsets;
		{
			final var infiniteGrid = InfiniteGrid.of(grid);
			offsets = new HashMap<>();
			final var zero = Point2D.createInt(0, 0);
			grid.fields()
				.stream()
				.filter(f -> grid.getValueRequired(f.pos()) == Tile.Empty)
				.forEach(f -> {
					offsets.put(
							f.pos(),
							Stream.of(
										  zero.up(),
										  zero.right(),
										  zero.down(),
										  zero.left()
								  )
								  .filter(offset -> infiniteGrid.getValueRequired(f.pos().add(offset)) == Tile.Empty)
								  .collect(toSet())
					);
				});
		}

		final var N = 26_501_365;

		final var visited = new HashSet<Point2D<Integer>>();
		final var next = new HashSet<Point2D<Integer>>();
		next.add(start);
		final var cache = new HashMap<Integer, Long>();
		cache.put(0, 1L);

		final var n = grid.getWidth();
		final var k = N / n;
		final var r = N % n;

		for (var i = 1; i < r + 2 * n + 1; i++) {
			final var visitedTemp = new HashSet<>(next);
			next.clear();
			visitedTemp.forEach(p -> p.getAdjacents4()
									  .filter(not(visited::contains))
									  .filter(a -> {
										  final var real = Point2D.create(floorMod(a.getX(), n), floorMod(a.getY(), n));
										  return grid.getValueRequired(real) == Tile.Empty;
									  })
									  .forEach(next::add));
			visited.clear();
			visited.addAll(visitedTemp);
			cache.put(i, next.size() + (i > 1 ? cache.get(i - 2) : 0));
		}

		final var d2 = cache.get(r + 2 * n) + cache.get(r) - 2 * cache.get(r + n);
		final var d1 = cache.get(r + 2 * n) - cache.get(r + n);

		return "sum = %d".formatted(
				cache.get(r + 2 * n) + (k - 2) * (2 * d1 + (k - 1) * d2) / 2
		);
	}

}

