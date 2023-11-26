package de.knallisworld.aoc2023.support.puzzle;

import java.util.Arrays;
import java.util.stream.Stream;

public class InputParser {

	public static Stream<Integer> str2int(final String str) {
		return str2int(str, ",");
	}

	public static Stream<Integer> str2int(final String str, final String separator) {
		return Arrays.stream(str.split(separator))
					 .map(String::strip)
					 .map(Integer::parseInt);
	}

}
