package de.knallisworld.aoc2023.support.lang;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtils {

	public static <T> Function<T, T> doLog(final Consumer<T> consumer) {
		return value -> {
			consumer.accept(value);
			return value;
		};
	}

	public static <T> Collector<T, ?, Stream<T>> toShuffledStream() {
		return Collectors.collectingAndThen(Collectors.toList(), collected -> {
			Collections.shuffle(collected);
			return collected.stream();
		});
	}

}
