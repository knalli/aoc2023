package de.knallisworld.aoc2023.day05;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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

	// took 727,332s in my system: 9622622
	static String part2(final Input input) {

		// pre-compute for fast access
		final var groupMappings = input
				.groups()
				.values()
				.stream()
				.collect(toMap(Group::src, identity()));

		final var min = new AtomicReference<>(Long.MAX_VALUE);

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
					if (r < min.get()) {
						min.set(r);
					}
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

