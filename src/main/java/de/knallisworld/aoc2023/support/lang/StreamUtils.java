package de.knallisworld.aoc2023.support.lang;

import java.util.function.Consumer;
import java.util.function.Function;

public class StreamUtils {

	public static <T> Function<T, T> doLog(final Consumer<T> consumer) {
		return value -> {
			consumer.accept(value);
			return value;
		};
	}

}
