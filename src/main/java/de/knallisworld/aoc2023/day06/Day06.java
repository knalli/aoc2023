package de.knallisworld.aoc2023.day06;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputParser.str2int;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day06 {

	public static void main(String[] args) {
		printHeader(6);
		printSolution(1, () -> part1(parseInput(readInputLines(6, "part1"))));
		printSolution(2, () -> part1(parseInput2(readInputLines(6, "part1"))));
	}

	record Input(List<Race> races) {
	}

	record Race(long duration, long distance) {
	}

	record Lap(long speed, long distance) {
	}

	static Input parseInput(final List<String> lines) {
		Assert.state(lines.size() == 2, "invalid line length");
		Assert.state(lines.get(0).startsWith("Time:"), "invalid line 1");
		Assert.state(lines.get(1).startsWith("Distance:"), "invalid line 2");
		final var timings = str2int(lines.get(0).substring(lines.get(0).indexOf(":") + 1).strip(), " ")
				//.map(Integer::longValue)
				.toList();
		final var distances = str2int(lines.get(1).substring(lines.get(1).indexOf(":") + 1).strip(), " ")
				//.map(Integer::longValue)
				.toList();
		Assert.state(timings.size() == distances.size(), "invalid input dimensions");
		return new Input(
				IntStream.range(0, timings.size())
						 .boxed()
						 .map(i -> new Race(timings.get(i), distances.get(i)))
						 .toList()
		);
	}

	static Input parseInput2(final List<String> lines) {
		Assert.state(lines.size() == 2, "invalid line length");
		Assert.state(lines.get(0).startsWith("Time:"), "invalid line 1");
		Assert.state(lines.get(1).startsWith("Distance:"), "invalid line 2");
		final var timings = List.of(Long.parseLong(
				lines.get(0)
					 .substring(lines.get(0).indexOf(":") + 1)
					 .strip()
					 .replace(" ", "")
		));
		final var distances = List.of(Long.parseLong(
				lines.get(1)
					 .substring(lines.get(1).indexOf(":") + 1)
					 .strip()
					 .replace(" ", "")
		));
		return new Input(
				IntStream.range(0, timings.size())
						 .boxed()
						 .map(i -> new Race(timings.get(i), distances.get(i)))
						 .toList()
		);
	}

	static String part1(final Input input) {
		final var result = input
				.races()
				.stream()
				.map(race -> {
					return LongStream.range(0, race.duration)
									 .boxed()
									 .map(speed -> new Lap(
											speed,
											(race.duration - speed) * speed
									))
									 .filter(lap -> lap.distance > race.distance)
									 .count();
				})
				.mapToInt(Long::intValue)
				.reduce(1, (a, b) -> a * b);
		return "result = %d".formatted(result);
	}

}

