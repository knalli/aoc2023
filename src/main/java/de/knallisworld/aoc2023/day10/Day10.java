package de.knallisworld.aoc2023.day10;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

@Log4j2
public class Day10 {

	public static void main(String[] args) {
		printHeader(10);
		printSolution(1, () -> part1(parseInput(readInputLines(10, "part1"))));
		// too high 733
		printSolution(2, () -> part2(parseInput(readInputLines(10, "part1"))));
	}

	enum Tile {
		Unknown,
		Ground,
		Vertical,
		Horizontal,
		NorthEast,
		NorthWest,
		SouthEast,
		SouthWest;

		boolean northConnected() {
			return this == Vertical || this == NorthEast || this == NorthWest;
		}

		boolean southConnected() {
			return this == Vertical || this == SouthEast || this == SouthWest;
		}

		boolean eastConnected() {
			return this == Horizontal || this == NorthEast || this == SouthEast;
		}

		boolean westConnected() {
			return this == Horizontal || this == NorthWest || this == SouthWest;
		}

	}

	record Input(FixGrid<Tile> grid, Point2D<Integer> start) {
	}

	record BfsResult(List<Point2D<Integer>> path, int farthestSteps) {
	}

	static Input parseInput(final List<String> lines) {
		final var grid = FixGrid.create(Tile.class, lines.size(), lines.getFirst().length());
		final var start = new AtomicReference<Point2D<Integer>>();
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> grid.setValue(
									  x,
									  y,
									  switch (line.charAt(x)) {
										  case '|' -> Tile.Vertical;
										  case '-' -> Tile.Horizontal;
										  case 'L' -> Tile.NorthEast;
										  case 'J' -> Tile.NorthWest;
										  case '7' -> Tile.SouthWest;
										  case 'F' -> Tile.SouthEast;
										  case '.' -> Tile.Ground;
										  case 'S' -> {
											  start.set(Point2D.create(x, y));
											  yield Tile.Unknown;
										  }
										  default -> throw new IllegalStateException("invalid input");
									  }
							  ));
				 });

		// start
		{
			final var s = start.get();
			final var upSourceConnected = grid.getValue(s.up()).map(Tile::southConnected).orElse(false);
			final var downNorthConnected = grid.getValue(s.down()).map(Tile::northConnected).orElse(false);
			final var leftEastConnected = grid.getValue(s.left()).map(Tile::eastConnected).orElse(false);
			final var rightWestConnected = grid.getValue(s.right()).map(Tile::westConnected).orElse(false);
			if (upSourceConnected && downNorthConnected) {
				grid.setValue(s, Tile.Vertical);
			} else if (leftEastConnected && rightWestConnected) {
				grid.setValue(s, Tile.Horizontal);
			} else if (upSourceConnected && leftEastConnected) {
				grid.setValue(s, Tile.NorthWest);
			} else if (upSourceConnected && rightWestConnected) {
				grid.setValue(s, Tile.NorthEast);
			} else if (downNorthConnected && leftEastConnected) {
				grid.setValue(s, Tile.SouthWest);
			} else if (downNorthConnected && rightWestConnected) {
				grid.setValue(s, Tile.SouthEast);
			} else {
				throw new IllegalStateException("invalid grid state");
			}
		}

		return new Input(grid, start.get());
	}

	static String part1(final Input input) {
		final var result = bfsForLoop(input);
		return "farthest_away_steps = %d".formatted(result.farthestSteps);
	}

	@SneakyThrows
	static String part2(final Input input) {
		final var result = bfsForLoop(input);
		final var grid = input.grid;

		grid.fields()
			.stream()
			.filter(not(field -> result.path.contains(field.pos())))
			.forEach(field -> {
				// clear junk (tiles not being part of the main loop)
				grid.setValue(field.pos(), Tile.Ground);
			});

		// tricky part here: in order to the get "squeezing" to work, we extrapolate the search grid
		final var searchGrid = FixGrid.extrapolated(grid, 3, (field, tempGrid) -> {
			tempGrid.fill(Tile.Ground);
			switch (field.value()) {
				case Vertical -> {
					tempGrid.setValue(1, 0, Tile.Vertical);
					tempGrid.setValue(1, 1, Tile.Vertical);
					tempGrid.setValue(1, 2, Tile.Vertical);
				}
				case Horizontal -> {
					tempGrid.setValue(0, 1, Tile.Horizontal);
					tempGrid.setValue(1, 1, Tile.Horizontal);
					tempGrid.setValue(2, 1, Tile.Horizontal);
				}
				case NorthEast -> {
					tempGrid.setValue(1, 0, Tile.Vertical);
					tempGrid.setValue(1, 1, Tile.NorthEast);
					tempGrid.setValue(2, 1, Tile.Horizontal);
				}
				case NorthWest -> {
					tempGrid.setValue(1, 0, Tile.Vertical);
					tempGrid.setValue(1, 1, Tile.NorthWest);
					tempGrid.setValue(0, 1, Tile.Horizontal);
				}
				case SouthEast -> {
					tempGrid.setValue(1, 2, Tile.Vertical);
					tempGrid.setValue(1, 1, Tile.SouthEast);
					tempGrid.setValue(2, 1, Tile.Horizontal);
				}
				case SouthWest -> {
					tempGrid.setValue(1, 2, Tile.Vertical);
					tempGrid.setValue(1, 1, Tile.SouthWest);
					tempGrid.setValue(0, 1, Tile.Horizontal);
				}
			}
		});

		final var handled = new HashMap<Point2D<Integer>, Boolean>();
		grid.fields()
			.stream()
			.filter(f -> f.value() == Tile.Ground)
			.filter(not(f -> handled.containsKey(f.pos())))
			.forEach(field -> {
				final var scaled = Point2D.create(
						(field.pos().getX() * 3) + 1,
						(field.pos().getY() * 3) + 1
				);
				final var enclosed = isEnclosed(searchGrid, scaled);
				handled.put(field.pos(), enclosed);
				// optimization: the whole cloud/cluster can be determined the same
				grid.fields().getCluster4(field.pos(), f -> f.value() == Tile.Ground)
					.forEach(a -> handled.put(a, enclosed));
			});

		return "enclosed = %d".formatted(handled.values().stream().filter(b -> b).count());
	}

	static boolean isEnclosed(final FixGrid<Tile> grid,
							  final Point2D<Integer> start) {

		final var known = new HashSet<Point2D<Integer>>();
		final var q = new LinkedList<Point2D<Integer>>();

		known.add(start);
		q.add(start);

		while (!q.isEmpty()) {
			final var p = q.pop();

			// exit condition: edge of the grid?
			if (p.getAdjacents4().anyMatch(not(grid::hasValue))) {
				return false;
			}

			grid.fields().getAdjacents4(p)
				.filter(a -> grid.getValueRequired(a) == Tile.Ground)
				.forEach(n -> {
					if (!known.contains(n)) {
						q.add(n);
						known.add(n);
					}
				});
		}

		return true;
	}

	static BfsResult bfsForLoop(final Input input) {

		final var grid = input.grid;
		final var start = input.start;
		final var known = new HashSet<Point2D<Integer>>();
		final var q = new LinkedList<Point2D<Integer>>();
		final var paths = new HashMap<Point2D<Integer>, List<Point2D<Integer>>>();
		final var prev = new HashMap<Point2D<Integer>, Point2D<Integer>>();

		known.add(start);
		q.add(start);
		paths.put(start, List.of(start));

		while (!q.isEmpty()) {
			final var p = q.pop();

			if (prev.containsKey(p)) {
				// update path
				final var pp = new ArrayList<>(paths.get(prev.get(p)));
				pp.add(p);
				paths.put(p, List.copyOf(pp));
			}

			// exit condition
			if (prev.containsKey(p) && p.equals(start)) {
				break;
			}

			if (grid.getValue(p).map(Tile::northConnected).orElse(false)) {
				final var n = p.up();
				if (grid.getValue(n).map(Tile::southConnected).orElse(false)) {
					if (!known.contains(n)) {
						q.add(n);
						known.add(n);
						prev.put(n, p);
					}
				}
			}
			if (grid.getValue(p).map(Tile::eastConnected).orElse(false)) {
				final var n = p.right();
				if (grid.getValue(n).map(Tile::westConnected).orElse(false)) {
					if (!known.contains(n)) {
						q.add(n);
						known.add(n);
						prev.put(n, p);
					}
				}
			}
			if (grid.getValue(p).map(Tile::southConnected).orElse(false)) {
				final var n = p.down();
				if (grid.getValue(n).map(Tile::northConnected).orElse(false)) {
					if (!known.contains(n)) {
						q.add(n);
						known.add(n);
						prev.put(n, p);
					}
				}
			}
			if (grid.getValue(p).map(Tile::westConnected).orElse(false)) {
				final var n = p.left();
				if (grid.getValue(n).map(Tile::eastConnected).orElse(false)) {
					if (!known.contains(n)) {
						q.add(n);
						known.add(n);
						prev.put(n, p);
					}
				}
			}
		}

		final var max = paths.values()
							 .stream()
							 .mapToInt(pp -> pp.size() - 1 /*start*/)
							 .max()
							 .orElse(0);

		final var pathL = paths.values()
							   .stream()
							   .filter(pp -> pp.size() == max + 1)
							   .toList();

		final List<Point2D<Integer>> fullPath;
		if (pathL.size() == 1) {
			fullPath = new ArrayList<>(pathL.getFirst());
			final var next = paths.values()
								  .stream()
								  .filter(pp -> pp.size() == max)
								  .filter(not(pp -> pp.getLast().equals(fullPath.get(fullPath.size() - 2))))
								  .findFirst()
								  .orElseThrow();
			next.reversed()
				.stream()
				.filter(not(i -> i.equals(start)))
				.forEach(fullPath::add);
		} else if (pathL.size() == 2) {
			fullPath = new ArrayList<>();
			if (pathL.getFirst().getFirst().equals(start)) {
				fullPath.addAll(pathL.getFirst());
				pathL.getLast().reversed()
					 .stream()
					 .filter(not(i -> i.equals(start)))
					 .forEach(fullPath::add);
			} else if (pathL.getLast().getFirst().equals(start)) {
				fullPath.addAll(pathL.getLast());
				pathL.getFirst().reversed()
					 .stream()
					 .filter(not(i -> i.equals(start)))
					 .forEach(fullPath::add);
			}
		} else {
			throw new IllegalStateException("invalid path state");
		}

		return new BfsResult(fullPath, max);
	}

	private static void render(final FixGrid<Tile> grid, @Nullable Collection<Point2D<Integer>> marker) {
		System.out.println(
				grid.toString((p, tile) -> {
					if (marker != null && marker.contains(p)) {
						return "X";
					}
					return switch (requireNonNull(tile)) {
						case Ground -> ".";
						case Vertical -> "│";
						case Horizontal -> "─";
						case NorthEast -> "└";
						case NorthWest -> "┘";
						case SouthEast -> "┌";
						case SouthWest -> "┐";
						case Unknown -> "?";
					};
				})
		);
	}

}

