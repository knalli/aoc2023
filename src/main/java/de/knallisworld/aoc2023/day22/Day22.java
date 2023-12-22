package de.knallisworld.aoc2023.day22;

import lombok.extern.log4j.Log4j2;

import java.util.*;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputParser.str2int;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.Comparator.comparingInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day22 {

	public static void main(String[] args) {
		printHeader(22);
		printSolution(1, () -> part1(parseInput(readInputLines(22, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(22, "part0"))));
		printSolution(2, () -> part2(parseInput(readInputLines(22, "part1"))));
	}

	record Point3D(int x, int y, int z) {
		public static Point3D ofIntegers(final Integer... ints) {
			if (ints.length != 3) {
				throw new IllegalArgumentException("invalid length of arguments");
			}
			return new Point3D(ints[0], ints[1], ints[2]);
		}

		public static Point3D ofInts(final int... ints) {
			if (ints.length != 3) {
				throw new IllegalArgumentException("invalid length of arguments");
			}
			return new Point3D(ints[0], ints[1], ints[2]);
		}

		@Override
		public String toString() {
			return "(%d,%d,%d)".formatted(x, y, z);
		}

		public Point3D add(final Point3D other) {
			return new Point3D(
					x + other.x,
					y + other.y,
					z + other.z
			);
		}

	}

	record Box(Point3D from, Point3D to) {
		@Override
		public String toString() {
			return "%s~%s".formatted(from, to);
		}

		public int minZ() {
			return from.z;
		}

		public int maxZ() {
			return to.z;
		}

		public Area areaZ() {
			return new Area(
					new Range(from.x, to.x),
					new Range(from.y, to.y)
			);
		}

		public boolean blocks(final Box other) {
			if (to.z + 1 != other.from.z) {
				return false;
			}
			return areaZ().overlaps(other.areaZ());
		}
	}

	record Range(int begin, int endInclusive) {

		public boolean contains(final int n) {
			return begin <= n && n <= endInclusive;
		}

		public boolean overlaps(final Range other) {
			return other.begin <= endInclusive && begin <= other.endInclusive;
		}
	}

	record Area(Range a, Range b) {
		public boolean overlaps(final Area other) {
			return a.overlaps(other.a) && b.overlaps(other.b);
		}
	}

	static List<Box> parseInput(final List<String> lines) {
		return lines
				.stream()
				.map(line -> {
					final var p = line.split("~");
					final var from = str2int(p[0]).toArray(Integer[]::new);
					final var to = str2int(p[1]).toArray(Integer[]::new);
					return new Box(
							Point3D.ofInts(Math.min(from[0], to[0]), Math.min(from[1], to[1]), Math.min(from[2], to[2])),
							Point3D.ofInts(Math.max(from[0], to[0]), Math.max(from[1], to[1]), Math.max(from[2], to[2]))
					);
				})
				.toList();
	}

	static Map<Box, List<Box>> buildSupportsMap(final Collection<Box> boxes) {
		return boxes
				.stream()
				.collect(toMap(identity(), box -> {
					return boxes
							.stream()
							.filter(b -> b != box)
							.filter(other -> box.maxZ() + 1 == other.minZ())
							.filter(other -> box.areaZ().overlaps(other.areaZ()))
							.toList();
				}));
	}

	static Map<Box, List<Box>> buildSupportedMap(final Collection<Box> boxes) {
		return boxes
				.stream()
				.collect(toMap(identity(), box -> {
					return boxes
							.stream()
							.filter(b -> b != box)
							.filter(other -> box.minZ() - 1 == other.maxZ())
							.filter(other -> box.areaZ().overlaps(other.areaZ()))
							.toList();
				}));
	}

	private static int processFalling(final List<Box> boxes) {
		// holds which boxes has fallen (value > 0)
		// any key indicates a fallen box, the value indicates how often
		final var counts = new HashMap<Box, Integer>();
		while (true) {
			boxes.sort(comparingInt(Box::minZ));
			boolean changed = false;
			for (final var box : boxes) {
				if (box.minZ() == 1) {
					// always at minimum
					continue;
				}
				final var boxAreaZ = box.areaZ();
				int z = 1; // try to fall to minimum
				boolean blocked = false;
				for (final var other : boxes) {
					if (box == other) {
						continue;
					}
					if (!(other.maxZ() < box.minZ())) {
						// cant be relevant
						continue;
					}
					if (box.blocks(other)) {
						// is blocked by this
						blocked = true;
						break;
					}
					if (boxAreaZ.overlaps(other.areaZ())) {
						// update z
						z = Math.max(other.maxZ() + 1, z);
					}
				}
				if (!blocked && z < box.minZ()) {
					final var d = box.minZ() - z;
					final var box2 = new Box(
							new Point3D(
									box.from.x,
									box.from.y,
									box.from.z - d
							),
							new Point3D(
									box.to.x,
									box.to.y,
									box.to.z - d
							)
					);
					counts.put(box2, counts.getOrDefault(box, 0) + 1);
					counts.remove(box);
					boxes.remove(box);
					boxes.add(box2);
					log.debug(() -> "Falling box %s -> %s".formatted(box, box2));
					changed = true;
					break;
				}
			}
			if (!changed) {
				break;
			}
		}
		return counts.size();
	}

	static String part1(final List<Box> input) {
		final var boxes = new ArrayList<>(input);
		processFalling(boxes);
		final var supports = buildSupportsMap(boxes);
		final var supportedBy = buildSupportedMap(boxes);
		final var count = boxes
				.stream()
				.filter(box -> {
					final var supporting = supports.get(box);
					// no supporting at all, can be removed
					if (supporting.isEmpty()) {
						return true;
					}
					// all supporting boxes must rely on at least one other
					return supporting
							.stream()
							.noneMatch(other -> supportedBy.get(other).size() == 1);
				})
				.count();
		return "sum = %d".formatted(count);
	}

	static String part2(final List<Box> input) {
		final var boxes = new ArrayList<>(input);
		processFalling(boxes);

		final var supports = buildSupportsMap(boxes);
		final var supportedBy = buildSupportedMap(boxes);

		final var sum = boxes
				.stream()
				.parallel()
				.mapToInt(box -> {
					final var l = new ArrayList<>(boxes);
					l.remove(box);
					return processFalling(l);
				})
				.sum();

		return "sum = %d".formatted(sum);
	}

}

