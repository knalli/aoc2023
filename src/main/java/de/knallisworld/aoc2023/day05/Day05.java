package de.knallisworld.aoc2023.day05;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day05 {

	public static void main(String[] args) {
		printHeader(5);
		printSolution(1, () -> part1(parseInput(readInputLines(5, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(5, "part1"))));
	}

	enum Type {
		seed,
		soil,
		fertilizer,
		water,
		light,
		temperature,
		humidity,
		location
	}

	record Input(List<Long> seeds, Map<String, Group> groups) {
	}

	record Group(Type src, Type dst, List<Mapping> mappings) {

	}

	record Mapping(long dst, long src, long len) {
	}

	static class ExcludeList extends LinkedList<Exclude> {

		@Override
		public boolean add(final Exclude newItem) {
			for (final var item : this) {
				if (item.includes(newItem)) {
					// skip it
					return false;
				}
			}
			final var obsoleteItems = new LinkedList<Exclude>();
			for (final var item : this) {
				if (newItem.includes(item)) {
					obsoleteItems.add(item);
				}
			}
			removeAll(obsoleteItems);
			return super.add(newItem);
		}

	}

	record Exclude(long src, long len) {

		public boolean includes(final Exclude other) {
			if (src <= other.src) {
				if (len == Long.MAX_VALUE) {
					return true;
				} else if (other.len == Long.MAX_VALUE) {
					return false;
				}
				return src + len >= other.src + other.len;
			}
			return false;
		}

	}

	static Input parseInput(final List<String> lines) {
		final var seeds = new ArrayList<Long>();
		final var map = new HashMap<String, Group>();
		for (int i = 0; i < lines.size(); i++) {
			var line = lines.get(i);
			if (line.startsWith("seeds:")) {
				Arrays.stream(line.split(": ")[1].split(" "))
					  .map(Long::parseLong)
					  .forEach(seeds::add);
				continue;
			}
			if (line.endsWith("map:")) {
				final var name = line.split(" ")[0];
				final var src = Type.valueOf(name.split("-to-")[0]);
				final var dst = Type.valueOf(name.split("-to-")[1]);
				final var list = new ArrayList<Mapping>();
				for (i++; i < lines.size(); i++) {
					line = lines.get(i);
					if (line.isEmpty()) {
						break; // go to out loop
					}
					final var nums = Arrays.stream(line.split(" "))
										   .map(Long::parseLong)
										   .toList();
					list.add(new Mapping(nums.get(0), nums.get(1), nums.get(2)));
				}
				map.put(name, new Group(src, dst, List.copyOf(list)));
			}
		}
		return new Input(
				List.copyOf(seeds),
				Map.copyOf(map)
		);
	}

	static String part1(final Input input) {

		// pre-compute for fast access
		final var groupMappings = input
				.groups()
				.values()
				.stream()
				.collect(toMap(Group::src, identity()));

		final var min = input
				.seeds()
				.stream()
				.mapToLong(seed -> findSeedToLocation(seed, groupMappings))
				.min()
				.orElse(0L);

		return "min = %d".formatted(min);
	}

	// 9622622
	// took 727s (parallel)
	// took 930s (parallel + lock)
	// took 196s (non-parallel + lock)
	static String part2(final Input input) {

		// pre-compute for fast access
		final var groupMappings = input
				.groups()
				.values()
				.stream()
				.collect(toMap(Group::src, identity()));

		final var min = new AtomicReference<>(Long.MAX_VALUE);
		final var lock = new ReentrantLock();

		IntStream
				.range(0, input.seeds().size() / 2)
				.mapToLong(i -> (long) i)
				.flatMap(n -> {
					final var i = (int) n * 2;
					final var start = input.seeds().get(i);
					final var end = start + input.seeds().get(i + 1);
					return LongStream.range(start, end);
				})
				.parallel()
				.forEach(seed -> {
					final var r = findSeedToLocation(seed, groupMappings);
					lock.lock();
					if (r < min.get()) {
						min.set(r);
					}
					lock.unlock();
				});

		return "min = %d".formatted(min.get());
	}

	static String part2_wip(final Input input) {

		// pre-compute for fast access
		final var groupMappings = input
				.groups()
				.values()
				.stream()
				.collect(toMap(Group::src, identity()));

		final var min = new AtomicReference<>(Long.MAX_VALUE);

		final var groupMappingsCopy = new HashMap<>(groupMappings);
		final var excludeMappings = new HashMap<Type, List<Exclude>>();
		Arrays.stream(Type.values()).forEach(t -> excludeMappings.put(t, new ExcludeList()));

		final var lock = new ReentrantLock();

		IntStream
				.range(0, input.seeds().size() / 2)
				.mapToLong(i -> (long) i)
				.flatMap(n -> {
					final var i = (int) n * 2;
					final var start = input.seeds().get(i);
					final var end = start + input.seeds().get(i + 1);
					return LongStream.range(start, end);
				})
				.parallel()
				.filter(seed -> {
							return excludeMappings
									.computeIfAbsent(Type.seed, _ -> new ExcludeList())
									.stream()
									.noneMatch(e -> e.src <= seed && seed < e.src + e.len);
						}
				)
				.forEach(seed -> {
					final var result = findSeedToLocation(seed, groupMappingsCopy);
					if (result >= min.get()) {
						return;
					}

					lock.lock();
					if (result < min.get()) {
						min.set(result);

						// reduce options
						var tmpType = Type.location;
						excludeMappings.get(tmpType).add(new Exclude(result, Long.MAX_VALUE));
						while (tmpType != Type.seed) {
							final var dstType = tmpType;
							final var ctx = groupMappingsCopy
									.entrySet()
									.stream()
									.filter(e -> e.getValue().dst() == dstType)
									.findFirst()
									.map(Map.Entry::getValue)
									.orElseThrow();

							final var newMappings = new ArrayList<Mapping>();
							final var newExcludes = new ExcludeList();
							newMappings.addAll(ctx.mappings());
							excludeMappings.get(dstType).forEach(excludeMapping -> {
								for (final var ctxMapping : List.copyOf(newMappings)) {
									if (ctxMapping.dst < excludeMapping.src) {
										if (ctxMapping.dst + ctxMapping.len <= excludeMapping.src) {
											// as-is
										} else if (excludeMapping.len == Long.MAX_VALUE) {
											newMappings.remove(ctxMapping);
											final var len = excludeMapping.src - ctxMapping.dst;
											if (len > 0) {
												newMappings.add(new Mapping(ctxMapping.dst, ctxMapping.src, len));
											}
											newExcludes.add(new Exclude(ctxMapping.src + len, ctxMapping.len - len));
										} else {
											newMappings.remove(ctxMapping);
											final var len = excludeMapping.src - ctxMapping.dst;
											if (len > 0) {
												newMappings.add(new Mapping(ctxMapping.dst, ctxMapping.src, len));
											}
											final var excludeLen = Math.min(ctxMapping.len - len, excludeMapping.len);
											newExcludes.add(new Exclude(ctxMapping.src + len, excludeLen));
											final var restLen = ctxMapping.len - len - excludeLen;
											if (restLen > 0) {
												newMappings.add(new Mapping(ctxMapping.dst + len + excludeLen, ctxMapping.src + len + excludeLen, restLen));
											}
										}
									} else {
										if (excludeMapping.len == Long.MAX_VALUE) {
											newMappings.remove(ctxMapping);
											newExcludes.add(new Exclude(ctxMapping.src, ctxMapping.len));
										} else if (ctxMapping.dst + ctxMapping.len < excludeMapping.src + excludeMapping.len) {
											newMappings.remove(ctxMapping);
											final var len = ctxMapping.dst - excludeMapping.src;
											if (len > 0) {
												newExcludes.add(new Exclude(ctxMapping.src, len));
											}
											newMappings.add(new Mapping(ctxMapping.dst + len, ctxMapping.src + len, ctxMapping.len - len));
										} else {
											newMappings.remove(ctxMapping);
											newExcludes.add(new Exclude(ctxMapping.src, ctxMapping.len));
										}
									}
								}
							});


							groupMappingsCopy.put(ctx.src(), new Group(ctx.src(), ctx.dst(), List.copyOf(newMappings)));
							excludeMappings.put(ctx.src(), newExcludes);
							tmpType = ctx.src();
						}
						System.out.println("min = " + min.get());
					}
					lock.unlock();

				});

		return "min = %d".formatted(min.get());
	}

	static long findSeedToLocation(final long seed, final Map<Type, Group> groupMappings) {
		// look for all mappings finding the mappings

		var value = seed;
		var type = Type.seed;
		while (type != Type.location) {
			final var mappings = groupMappings.get(type);
			type = mappings.dst();
			final var v = value;
			value = mappings.mappings()
							.stream()
							.filter(m -> m.src() <= v && v < m.src() + m.len())
							.findFirst()
							.map(m -> v + (m.dst() - m.src()))
							.orElse(value);
		}

		return value;
	}

}

