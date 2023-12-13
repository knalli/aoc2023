package de.knallisworld.aoc2023.support.lang;

import lombok.Builder;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
@Builder
public class MemoizationOptions {

	/**
	 * Expected size of cached items. May influences internals.
	 */
	@Builder.Default
	private int expectedSize = Integer.MAX_VALUE;

	/**
	 * Indicates whether empty/null values should be cached.
	 */
	@Builder.Default
	private boolean includeEmptyValues = false;

	/**
	 * Indicates whether the resolver must be thread-safe.
	 */
	@Builder.Default
	private boolean threadSafe = false;

	/**
	 * default options
	 */
	public static Consumer<MemoizationOptionsBuilder> withDefault() {
		return o -> {
		};
	}

	/**
	 * Indicates whether empty/null values will be cached.
	 */
	public static Consumer<MemoizationOptionsBuilder> includeEmptyValues() {
		return o -> o.includeEmptyValues(true);
	}

	/**
	 * Expected size of cached items. May influences internals.
	 */
	public static Consumer<MemoizationOptionsBuilder> onlyOnce() {
		return o -> o.expectedSize(1);
	}

	/**
	 * Indicates whether the resolver have to thread safe.
	 */
	public static Consumer<MemoizationOptionsBuilder> threadSafe() {
		return o -> o.threadSafe(true);
	}

}
