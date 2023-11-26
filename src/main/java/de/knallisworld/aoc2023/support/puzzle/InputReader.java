package de.knallisworld.aoc2023.support.puzzle;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class InputReader {

	@SneakyThrows
	public static String readInputFirstLine(final int day,
											final String name) {
		try (final var reader = new BufferedReader(new InputStreamReader(buildInputStream(day, name)))) {
			return reader.readLine();
		}
	}

	@SneakyThrows
	public static <T> Stream<T> readInputFirstLine(final int day,
												   final String name,
												   final Function<String, Stream<T>> transformer) {
		return transformer.apply(readInputFirstLine(day, name));
	}

	@SneakyThrows
	public static List<String> readInputLines(final int day,
											  final String name) {
		try (final var reader = new BufferedReader(new InputStreamReader(buildInputStream(day, name)))) {
			final var result = new ArrayList<String>();
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				result.add(line);
			}
			return List.copyOf(result);
		}
	}

	@SneakyThrows
	public static <T> List<List<T>> readInputLines(final int day,
												   final String name,
												   final Function<String, Stream<T>> transformer) {
		try (final var reader = new BufferedReader(new InputStreamReader(buildInputStream(day, name)))) {
			final var result = new ArrayList<List<T>>();
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				result.add(transformer.apply(line).toList());
			}
			return List.copyOf(result);
		}
	}

	static String buildResourcePath(final int day, final String name) {
		return "day%02d/%s.txt".formatted(day, name);
	}

	private static InputStream buildInputStream(final int day, final String name) {
		return requireNonNull(InputReader.class.getClassLoader().getResourceAsStream(buildResourcePath(day, name)));
	}

}
