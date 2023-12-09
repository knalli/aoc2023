package de.knallisworld.aoc2023.day09;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputParser.str2long;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day09 {

	public static void main(String[] args) {
		printHeader(9);
		printSolution(1, () -> part1(parseInput(readInputLines(9, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(9, "part1"))));
	}

	static List<List<Long>> parseInput(final List<String> lines) {
		return lines.stream()
					.filter(StringUtils::hasText)
					.map(line -> str2long(line, " ").toList())
					.toList();
	}

	static String part1(final List<List<Long>> lines) {
		final var sum = lines
				.stream()
				.mapToLong(Day09::findNextInSeries)
				.sum();
		return "sum = %d".formatted(sum);
	}

	static String part2(final List<List<Long>> lines) {
		final var sum = lines
				.stream()
				.mapToLong(Day09::findPrevInSeries)
				.sum();
		return "sum = %d".formatted(sum);
	}

	private static long findNextInSeries(final List<Long> numbers) {

		if (numbers.stream().allMatch(n -> n == 0)) {
			return 0;
		}

		final var diffNumbers = IntStream
				.range(1, numbers.size())
				.boxed()
				.map(i -> numbers.get(i) - numbers.get(i - 1))
				.toList();

		return numbers.getLast() + findNextInSeries(diffNumbers);
	}

	private static long findPrevInSeries(final List<Long> numbers) {

		if (numbers.stream().allMatch(n -> n == 0)) {
			return 0;
		}

		final var diffNumbers = IntStream
				.range(1, numbers.size())
				.boxed()
				.map(i -> numbers.get(i) - numbers.get(i - 1))
				.toList();

		return numbers.getFirst() - findPrevInSeries(diffNumbers);
	}

}

