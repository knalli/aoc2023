package de.knallisworld.aoc2023.day11;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.DynGrid;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.LongStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.geo.Utils.manhattenDistance;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.function.Predicate.not;

@Log4j2
public class Day11 {

	public static void main(String[] args) {
		printHeader(11);
		printSolution(1, () -> part1(parseInput(readInputLines(11, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(11, "part1"))));
	}

	enum Tile {
		Empty,
		Galaxy
	}

	static DynGrid<Long, Tile> parseInput(final List<String> lines) {
		final var grid = DynGrid.<Long, Tile>empty();
		for (var y = 0L; y < lines.size(); y++) {
			for (var x = 0L; x < lines.getFirst().length(); x++) {
				final var c = lines.get((int) y).charAt((int) x);
				if (c == '#') {
					final var p = Point2D.create(x, y);
					grid.setValue(p, Tile.Galaxy);
				}
				/*
				if (c == '.') {
					final var p = Point2D.create(x, y);
					grid.setValue(p, Tile.Empty);
				}
				 */
			}
		}
		return grid;
	}

	static void expandEmptyRowsAndColumns(final DynGrid<Long, Tile> grid, final int scale) {
		final Function<Point2D<Long>, Boolean> isGalaxy = p -> grid.getValue(p)
																   .map(v -> Tile.Galaxy == v) // obsolete
																   .orElse(false);

		final var offsets = new HashMap<Point2D<Long>, Point2D<Long>>();
		grid.fields()
			.stream()
			.map(DynGrid.FieldsView.Field::position)
			.forEach(p -> offsets.put(p, Point2D.create(0L, 0L)));

		for (var y = grid.minY(); y <= grid.maxY(); y++) {
			final var y2 = y;
			if (LongStream.rangeClosed(grid.minX(), grid.maxX()).anyMatch(x -> isGalaxy.apply(Point2D.create(x, y2)))) {
				continue;
			}
			log.trace(() -> STR."expand row @\{y2}");
			for (var dy = y + 1; dy <= grid.maxY(); dy++) {
				for (var dx = grid.minX(); dx <= grid.maxX(); dx++) {
					final var p = Point2D.create(dx, dy);
					if (offsets.containsKey(p)) {
						final var o = offsets.get(p)
											 .down(scale - 1);
						offsets.put(p, o);
					}
				}
			}
		}
		for (var x = grid.minX(); x <= grid.maxX(); x++) {
			final var x2 = x;
			if (LongStream.rangeClosed(grid.minY(), grid.maxY()).anyMatch(y -> isGalaxy.apply(Point2D.create(x2, y)))) {
				continue;
			}
			log.trace(() -> STR."expand column @\{x2}");
			for (var dx = x + 1; dx <= grid.maxX(); dx++) {
				for (var dy = grid.minY(); dy <= grid.maxY(); dy++) {
					final var p = Point2D.create(dx, dy);
					if (offsets.containsKey(p)) {
						final var o = offsets.get(p)
											 .right(scale - 1);
						offsets.put(p, o);
					}
				}
			}
		}

		record Tupel(Point2D<Long> from, Point2D<Long> to, Tile value) {
		}

		final var values =
				offsets.entrySet()
					   .stream()
					   .map(entry -> {
						   final var from = entry.getKey();
						   final var to = entry.getKey().add(entry.getValue());
						   return new Tupel(from, to, grid.getValueRequired(from));
					   })
					   .toList();
		values.forEach(t -> {
			grid.clearValue(t.from);
		});
		values.forEach(t -> {
			grid.setValue(t.to, t.value);
		});

	}

	static String part1(final DynGrid<Long, Tile> grid) {
		final var sum = computeGalaxyShortestDistancesSum(grid, 2);
		return "sum = %d".formatted(sum);
	}

	static String part2(final DynGrid<Long, Tile> grid) {
		final var sum = computeGalaxyShortestDistancesSum(grid, 1000000);
		return "sum = %d".formatted(sum);
	}

	static long computeGalaxyShortestDistancesSum(final DynGrid<Long, Tile> grid, final int scale) {
		expandEmptyRowsAndColumns(grid, scale);
		final var nodes = grid.fields()
							  .stream()
							  .filter(field -> field.value() == Tile.Galaxy) // obsolete
							  .map(DynGrid.FieldsView.Field::position)
							  .toList();
		final var pairs = new HashMap<Set<Point2D<Long>>, Long>();
		nodes.forEach(p1 -> {
			nodes.stream()
				 .filter(not(p1::equals))
				 .forEach(p2 -> {
					 pairs.computeIfAbsent(Set.of(p1, p2), _ -> {
						 final var d = manhattenDistance(p1, p2);
						 log.trace(() -> "%s -> %s = %d".formatted(p1, p2, d));
						 return d;
					 });
				 });
		});
		log.trace(() -> "pairs = %d".formatted(pairs.size()));

		return pairs.values()
					.stream()
					.mapToLong(s -> s)
					.sum();
	}

	static void renderGrid(final DynGrid<Long, Tile> grid) {
		System.out.println(grid.toString((_, tile) -> switch (tile) {
			case Empty -> ".";
			case Galaxy -> "#";
		}, () -> "."));
	}

}

