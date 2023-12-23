package de.knallisworld.aoc2023.day23;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.*;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day23 {

	public static void main(String[] args) {
		printHeader(23);
		printSolution(1, () -> part1(parseInput(readInputLines(23, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(23, "part1"))));
	}

	enum Tile {
		Path,
		Forest,
		SlopeLeft,
		SlopeRight,
		SlopeDown,
		SlopeUp
	}

	static FixGrid<Tile> parseInput(final List<String> lines) {
		return FixGrid.parseBySymbols2D(
				Tile.class,
				lines,
				c -> switch (c) {
					case '.' -> Tile.Path;
					case '#' -> Tile.Forest;
					case '<' -> Tile.SlopeLeft;
					case '>' -> Tile.SlopeRight;
					case 'v' -> Tile.SlopeDown;
					case '^' -> Tile.SlopeUp;
					default -> throw new IllegalStateException("invalid input");
				}
		);
	}

	record SearchResult(long len) {
	}

	record Way(List<Point2D<Integer>> path) {
	}

	static SearchResult dfs(final Map<Point2D<Integer>, List<Way>> nextCache,
							final Point2D<Integer> start,
							final Point2D<Integer> goal) {
		record Item(Point2D<Integer> p, Collection<Point2D<Integer>> path) {

			public int len() {
				return path.size() - 1;
			}

		}

		final var q = new LinkedList<Item>(); // Comparator.comparing(Item::len).reversed()
		q.add(new Item(start, Set.of(start)));

		final var maxD = new HashMap<Point2D<Integer>, Integer>();

		var maxLen = Long.MIN_VALUE;
		while (!q.isEmpty()) {
			final var item = q.pollLast();

			/*
			if (maxD.getOrDefault(item.p(), 0) > item.len()) {
				// optimize: this branch won't be better, safely abort
				continue;
			}
			maxD.put(item.p(), item.len());
			 */

			if (item.p().equals(goal)) {
				if (item.len() > maxLen) {
					maxLen = item.len();
					log.info("FOUND new max = %d".formatted(maxLen));
				}
				continue;
			}

			log.debug("try %s".formatted(item.p()));
			nextCache.get(item.p())
					 .stream()
					 .filter(not(way -> item.path.contains(way.path().getLast())))
					 .forEach(way -> {
						 final var path = new HashSet<>(item.path);
						 path.addAll(way.path());
						 q.add(new Item(way.path().getLast(), path));
					 });
		}

		return new SearchResult(maxLen);
	}

	static String part1(final FixGrid<Tile> grid) {

		final var start = grid.fields().row(0)
							  .filter(f -> f.value() == Tile.Path)
							  .findFirst()
							  .map(FixGrid.FieldsView.Field::pos)
							  .orElseThrow();
		final var goal = grid.fields().row(-1)
							 .filter(f -> f.value() == Tile.Path)
							 .findFirst()
							 .map(FixGrid.FieldsView.Field::pos)
							 .orElseThrow();

		// pre-compute direction graph (using weighted=1, because part2)
		final var gridFields = grid.fields();
		final var data = grid
				.fields()
				.stream()
				.filter(f -> f.value() != Tile.Forest)
				.collect(toMap(FixGrid.FieldsView.Field::pos, f -> {
					return switch (f.value()) {
						case Path -> {
							final var p = f.pos();
							yield gridFields.getAdjacents4(p)
											.filter(grid::hasValue)
											// ignore forest
											.filter(not(p0 -> grid.getValueRequired(p0) == Tile.Forest))
											.map(p0 -> new Way(List.of(p0)))
											.toList();
						}
						case SlopeLeft -> {
							final var p0 = f.pos().left();
							yield List.of(new Way(List.of(p0)));
						}
						case SlopeRight -> {
							final var p0 = f.pos().right();
							yield List.of(new Way(List.of(p0)));
						}
						case SlopeDown -> {
							final var p0 = f.pos().down();
							yield List.of(new Way(List.of(p0)));
						}
						case SlopeUp -> {
							final var p0 = f.pos().up();
							yield List.of(new Way(List.of(p0)));
						}
						case Forest -> throw new IllegalStateException("invalid state");
					};
				}));
		log.debug(() -> "Searching for %s -> %s".formatted(start, goal));
		return "result = %s".formatted(dfs(data, start, goal));
	}

	static String part2(final FixGrid<Tile> grid) {

		final var start = grid.fields().row(0)
							  .filter(f -> f.value() == Tile.Path)
							  .findFirst()
							  .map(FixGrid.FieldsView.Field::pos)
							  .orElseThrow();
		final var goal = grid.fields().row(-1)
							 .filter(f -> f.value() == Tile.Path)
							 .findFirst()
							 .map(FixGrid.FieldsView.Field::pos)
							 .orElseThrow();

		// pre-compute direction graph (weighted)
		final var gridFields = grid.fields();
		final var data = grid
				.fields()
				.stream()
				.filter(f -> f.value() != Tile.Forest)
				.filter(f -> {
					if (start.equals(f.pos())) {
						return true;
					}
					// otherwise, determine only the junctions
					final var count = gridFields.getAdjacents4(f.pos())
												.filter(not(p -> grid.getValueRequired(p) == Tile.Forest))
												.count();
					return count > 2;
				})
				.peek(f -> log.debug(() -> "Found junction @ %s".formatted(f.pos())))
				.collect(toMap(FixGrid.FieldsView.Field::pos, f -> switch (f.value()) {
					case Path, SlopeLeft, SlopeRight, SlopeDown, SlopeUp -> {
						final var p = f.pos();
						yield gridFields.getAdjacents4(p)
										.filter(grid::hasValue)
										// ignore forest
										.filter(not(p0 -> grid.getValueRequired(p0) == Tile.Forest))
										//.map(p0 -> new Way(p0, List.of(p0)))
										.map(p0 -> {
											var offset = p0.add(Point2D.create(-p.getX(), -p.getY()));
											final var r = new ArrayList<Point2D<Integer>>();
											r.add(p0);

											var temp = p0;
											while (true) {
												var next = temp.add(offset);
												if (!grid.hasValue(next)) {
													break;
												}
												if (grid.getValueRequired(next) == Tile.Forest) {
													// maybe a direction change
													final var any = gridFields.getAdjacents4(temp)
																			  .filter(not(p1 -> grid.getValueRequired(p1) == Tile.Forest))
																			  .filter(not(r::contains))
																			  .filter(not(p::equals))
																			  .filter(not(next::equals))
																			  .toList();
													if (any.size() == 1) {
														next = any.getFirst();
													} else {
														break;
													}
												}
												final var countAdjacents = gridFields.getAdjacents4(next)
																					 .filter(not(p1 -> grid.getValueRequired(p1) == Tile.Forest))
																					 .filter(not(temp::equals))
																					 .count();
												if (next.equals(goal) || countAdjacents > 1) {
													// either the goal or a junction (>2).
													// point itself adding, but then stop here
													r.add(next);
													break;
												} else if (countAdjacents == 0) {
													// dead end, these paths won't be relevant
													return null;
												} else /* count==1 */ {
													// found the next point in a list
													// no junction, so proceed
													r.add(next);
													offset = next.add(Point2D.create(-temp.getX(), -temp.getY()));
													temp = next;
												}
											}
											return new Way(r);
										})
										.filter(Objects::nonNull)
										.toList();
					}
					case Forest -> throw new IllegalStateException("invalid state");
				}));
		log.debug(() -> "Searching for %s -> %s".formatted(start, goal));
		return "result = %s".formatted(dfs(data, start, goal));
	}

}

