package de.knallisworld.aoc2023.support.lang;

import java.util.Optional;
import java.util.function.Function;

public interface MemoizationFunction<I, O> extends Function<I, Optional<O>> {

	/**
	 * Applies this function to the given argument.
	 * Will throw a {@link NullPointerException} if empty.
	 *
	 * @throws NullPointerException if empty
	 * @see #apply(Object)
	 */
	default O applyRequired(I input) {
		return apply(input).orElseThrow();
	}

}
