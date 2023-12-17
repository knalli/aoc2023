package de.knallisworld.aoc2023.day17;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.Direction;
import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day17 {

	public static void main(String[] args) {
		printHeader(17);
		printSolution(1, () -> part1(parseInput(readInputLines(17, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(17, "part1"))));
	}

	static FixGrid<Integer> parseInput(final List<String> lines) {
		final var grid = FixGrid.create(Integer.class, lines.size(), lines.getFirst().length());
		for (int y = 0; y < lines.size(); y++) {
			final var line = lines.get(y);
			for (int x = 0; x < line.length(); x++) {
				grid.setValue(x, y, line.charAt(x) - 48);
			}
		}
		return grid;
	}

	static String part1(final FixGrid<Integer> input) {
		final var result = bfs(input, new Range(1, 3));
		log.debug(() -> renderGrid(input, result));
		return "min heat loss = %d".formatted(result.minHeatLoss);
	}

	static String part2(final FixGrid<Integer> input) {
		final var result = bfs(input, new Range(4, 10));
		log.debug(() -> renderGrid(input, result));
		return "min heat loss = %d".formatted(result.minHeatLoss);
	}

	static String renderGrid(final FixGrid<Integer> input, BfsResult result) {
		final var wpMap = result
				.waypoints
				.stream()
				.collect(toMap(Waypoint::pos, Waypoint::dir));
		return input.toString((p, v) -> {
			if (wpMap.containsKey(p)) {
				final var wp = wpMap.get(p);
				return switch (wp) {
					case North -> "^";
					case East -> ">";
					case South -> "v";
					case West -> "<";
				};
			}
			return "" + v;
		});
	}

	record BfsResult(
			int len,
			int minHeatLoss,
			List<Waypoint> waypoints
	) {
	}

	record Waypoint(Point2D<Integer> pos, Direction dir) {
	}

	record Range(int begin, int end) {
		public boolean contains(final int n) {
			return begin <= n && n <= end;
		}
	}

	static BfsResult bfs(final FixGrid<Integer> grid, final Range validBlocks) {

		final var start = Point2D.create(0, 0);
		final var goal = Point2D.create(grid.getWidth() - 1, grid.getHeight() - 1);

		record Item(Point2D<Integer> pos, int blocks, Direction dir, int heatLoss, List<Waypoint> parentPath) {
		}

		final var q = new PriorityQueue<Item>(comparing(Item::heatLoss));
		Arrays.stream(Direction.values())
			  .filter(dir -> grid.hasValue(start.add(dir.offset())))
			  .forEach(dir -> q.add(new Item(start, 1, dir, 0, List.of())));

		final var minHeatLoss = new AtomicInteger(Integer.MAX_VALUE);
		final var minWaypoints = new ArrayList<Waypoint>();

		record VisitedItem(Point2D<Integer> p, Direction dir, int blocks) {
		}
		final var visited = new HashSet<VisitedItem>();

		while (!q.isEmpty()) {
			final var current = q.poll();

			// optimize: cancel branches entering a visited point with a higher loss
			{
				final var key = new VisitedItem(current.pos, current.dir, current.blocks);
				if (visited.contains(key)) {
					continue;
				}
				visited.add(key);
			}

			// optimize: abort branches which already exceeding the global minimum
			if (current.heatLoss > minHeatLoss.get()) {
				log.trace(() -> "cancel (global min cap)");
				continue;
			}

			final var currentPath = new ArrayList<>(current.parentPath);
			currentPath.add(new Waypoint(current.pos, current.dir));

			if (current.pos.equals(goal)) {
				if (validBlocks.contains(current.blocks)) {
					if (current.heatLoss < minHeatLoss.get()) {
						minHeatLoss.set(current.heatLoss);
						minWaypoints.clear();
						minWaypoints.addAll(currentPath);
						log.trace(() -> "goal reached with %d".formatted(current.heatLoss));
						break;
					} else {
						log.error(() -> "goal reached with %d, but heat loss not lower".formatted(current.heatLoss));
					}
				} else {
					log.trace(() -> "goal reached with %d, but block requirement not fulfilled".formatted(current.heatLoss));
				}
				continue;
			}

			if (current.blocks < validBlocks.end()) {
				final var nextPos = current.pos.add(current.dir.offset());
				if (grid.hasValue(nextPos)) {
					final var nextHeatLoss = current.heatLoss + grid.getValueRequired(nextPos);
					// optimize: ignore branches which already exceeding the global min
					if (nextHeatLoss < minHeatLoss.get()) {
						q.add(new Item(
								nextPos,
								current.blocks + 1,
								current.dir,
								nextHeatLoss,
								currentPath
						));
					}
				}
			}
			if (validBlocks.contains(current.blocks)) {
				final var nextDir = current.dir.left();
				final var nextPos = current.pos.add(nextDir.offset());
				if (grid.hasValue(nextPos)) {
					final var nextHeatLoss = current.heatLoss + grid.getValueRequired(nextPos);
					// optimize: ignore branches which already exceeding the global min
					if (nextHeatLoss < minHeatLoss.get()) {
						q.add(new Item(
								nextPos,
								1,
								nextDir,
								nextHeatLoss,
								currentPath
						));
					}
				}
			}
			if (validBlocks.contains(current.blocks)) {
				final var nextDir = current.dir.right();
				final var nextPos = current.pos.add(nextDir.offset());
				if (grid.hasValue(nextPos)) {
					final var nextHeatLoss = current.heatLoss + grid.getValueRequired(nextPos);
					// optimize: ignore branches which already exceeding the global min
					if (nextHeatLoss < minHeatLoss.get()) {
						q.add(new Item(
								nextPos,
								1,
								nextDir,
								nextHeatLoss,
								currentPath
						));
					}
				}
			}
		}

		return new BfsResult(
				minWaypoints.size(),
				minHeatLoss.get(),
				List.copyOf(minWaypoints)
		);
	}

}

