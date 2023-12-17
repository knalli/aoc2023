package de.knallisworld.aoc2023.day16;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.Direction;
import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.function.Function.identity;

@Log4j2
public class Day16 {

	public static void main(String[] args) {
		printHeader(16);
		printSolution(1, () -> part1(parseInput(readInputLines(16, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(16, "part1"))));
	}

	enum Tile {

		Emtpy("."),
		SplitterV("|"),
		SplitterH("-"),
		MirrorR("/"),
		MirrorL("\\");

		private final String v;

		Tile(String v) {
			this.v = v;
		}

		@Override
		public String toString() {
			return v;
		}

	}

	static FixGrid<Tile> parseInput(final List<String> lines) {
		final var grid = FixGrid.create(Tile.class, lines.size(), lines.getFirst().length());
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 IntStream.range(0, lines.get(y).length())
							  .forEach(x -> {
								  grid.setValue(x, y, switch (lines.get(y).charAt(x)) {
									  case '.' -> Tile.Emtpy;
									  case '-' -> Tile.SplitterH;
									  case '|' -> Tile.SplitterV;
									  case '/' -> Tile.MirrorR;
									  case '\\' -> Tile.MirrorL;
									  default -> throw new IllegalStateException("invalid input");
								  });
							  });
				 });
		return grid;
	}

	static int calcEnergized(final FixGrid<Tile> grid,
							 final Point2D<Integer> initialPoint,
							 final Direction initialDirection) {
		record Item(Point2D<Integer> pos, Direction dir) {
		}

		final var q = new ArrayDeque<Item>();
		q.add(new Item(initialPoint, initialDirection));

		final var used = new HashMap<Point2D<Integer>, Set<Direction>>();

		while (!q.isEmpty()) {
			final var item = q.pop();
			if (!grid.hasValue(item.pos)) {
				continue;
			}
			final var pointVisited = used.computeIfAbsent(item.pos, _ -> new HashSet<>());
			if (pointVisited.contains(item.dir)) {
				// already visited from this direction
				continue;
			}
			pointVisited.add(item.dir);
			switch (grid.getValueRequired(item.pos)) {
				case Emtpy -> {
					// .
					q.add(new Item(item.pos.add(item.dir.offset()), item.dir));
				}
				case MirrorR -> {
					// /
					final var nextDir = switch (item.dir) {
						case North, South -> item.dir.right();
						case East, West -> item.dir.left();
					};
					q.add(new Item(item.pos.add(nextDir.offset()), nextDir));
				}
				case MirrorL -> {
					// \
					final var nextDir = switch (item.dir) {
						case North, South -> item.dir.left();
						case East, West -> item.dir.right();
					};
					q.add(new Item(item.pos.add(nextDir.offset()), nextDir));
				}
				case SplitterH -> {
					// -
					switch (item.dir) {
						case East, West -> q.add(new Item(item.pos.add(item.dir.offset()), item.dir));
						case North, South -> {
							Set.of(item.dir.left(), item.dir.right())
							   .forEach(nextDir -> {
								   q.add(new Item(item.pos.add(nextDir.offset()), nextDir));
							   });
						}
					}
				}
				case SplitterV -> {
					// |
					switch (item.dir) {
						case North, South -> q.add(new Item(item.pos.add(item.dir.offset()), item.dir));
						case East, West -> {
							Set.of(item.dir.left(), item.dir.right())
							   .forEach(nextDir -> {
								   q.add(new Item(item.pos.add(nextDir.offset()), nextDir));
							   });
						}
					}
				}
			}

		}

		return used.size();
	}

	static String part1(final FixGrid<Tile> grid) {
		return "energized = %d".formatted(calcEnergized(grid, Point2D.create(0, 0), Direction.East));
	}

	static String part2(final FixGrid<Tile> grid) {
		final var max = Stream
				.of(
						grid.fields().topEdge()
							.mapToInt(p -> calcEnergized(grid, p, Direction.South))
							.boxed(),
						grid.fields().rightEdge()
							.mapToInt(p -> calcEnergized(grid, p, Direction.West))
							.boxed(),
						grid.fields().bottomEdge()
							.mapToInt(p -> calcEnergized(grid, p, Direction.North))
							.boxed(),
						grid.fields().leftEdge()
							.mapToInt(p -> calcEnergized(grid, p, Direction.East))
							.boxed()
				)
				.flatMap(identity())
				.mapToInt(Integer::valueOf)
				.max()
				.orElse(0);
		return "energized = %d".formatted(max);
	}

}

