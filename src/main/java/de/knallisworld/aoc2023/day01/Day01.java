package de.knallisworld.aoc2023.day01;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.lang.StreamUtils.doLog;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

@Log4j2
public class Day01 {

	public static void main(String[] args) {
		printHeader(1);
		printSolution(1, () -> {
			return sumAllDigits(
					readInputLines(1, "part1"),
					Day01::extractDigitsFromString
			);
		});
		printSolution(1, () -> {
			return sumAllDigits(
					readInputLines(1, "part1"),
					Day01::extractDigitsFromString2
			);
		});
	}

	record Repl(String key, String value) {
	}

	static final List<Repl> NAMED_DIGITS = List.of(
			new Repl("one", "1"),
			new Repl("two", "2"),
			new Repl("three", "3"),
			new Repl("four", "4"),
			new Repl("five", "5"),
			new Repl("six", "6"),
			new Repl("seven", "7"),
			new Repl("eight", "8"),
			new Repl("nine", "9")
	);

	private static int sumAllDigits(final List<String> input,
									final Function<String, String> extractor) {
		return input
				.stream()
				.map(extractor)
				.filter(StringUtils::hasText)
				.map(doLog(line -> log.trace("line.pre {}", line)))
				.map(fullStr -> {
					var result = "";
					final var parts = fullStr.split("");
					if (parts.length == 1) {
						result = parts[0] + parts[0];
					} else {
						result = parts[0] + parts[parts.length - 1];
					}
					return String.join("", result);
				})
				.map(doLog(line -> log.trace("line.post {}", line)))
				.mapToInt(Integer::parseInt)
				.sum();
	}

	static String extractDigitsFromString(final String str) {
		return String.join(
				"",
				Arrays.stream(str.split(""))
					  .filter(Day01::onlyDigits)
					  .toArray(String[]::new)
		);
	}

	static String extractDigitsFromString2(final String str) {
		record Piece(int pos, String val) {
		}
		final var temp = new ArrayList<Piece>();
		for (var i = 0; i < str.length(); i++) {
			if (onlyDigits(str.charAt(i))) {
				temp.add(new Piece(i, str.substring(i, i + 1)));
			}
		}
		final var patternCache = new HashMap<String, Pattern>();
		NAMED_DIGITS.forEach(repl -> {
			final var pattern = patternCache.computeIfAbsent(repl.key(), k -> Pattern.compile(k, Pattern.DOTALL));
			pattern.matcher(str)
				   .results()
				   .forEach(s -> {
					   temp.add(new Piece(s.start(), repl.value()));
				   });
		});
		return extractDigitsFromString(
				temp.stream()
					.sorted(comparing(Piece::pos))
					.map(Piece::val)
					.collect(joining())
		);
	}

	static boolean onlyDigits(final String s) {
		if (s.length() != 1) {
			return false;
		}
		return onlyDigits(s.toCharArray()[0]);
	}

	static boolean onlyDigits(final char c) {
		return '0' <= c && c <= '9';
	}

}
