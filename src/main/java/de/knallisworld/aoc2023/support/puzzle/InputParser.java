package de.knallisworld.aoc2023.support.puzzle;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

public class InputParser {

	public static Stream<Integer> str2int(final String str) {
		return str2int(str, ",");
	}

	public static Stream<Integer> str2int(final String str, final String separator) {
		return Arrays.stream(str.split(separator))
					 .map(String::strip)
					 .filter(StringUtils::hasText)
					 .map(Integer::parseInt);
	}

	public static Stream<Long> str2long(final String str) {
		return str2long(str, ",");
	}

	public static Stream<Long> str2long(final String str, final String separator) {
		return Arrays.stream(str.split(separator))
					 .map(String::strip)
					 .filter(StringUtils::hasText)
					 .map(Long::parseLong);
	}

}
