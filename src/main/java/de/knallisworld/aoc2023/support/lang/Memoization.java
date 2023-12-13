package de.knallisworld.aoc2023.support.lang;

import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static de.knallisworld.aoc2023.support.lang.MemoizationOptions.withDefault;

public class Memoization {

	public static <I, O> Function<I, O> memoize(final Function<I, O> loader) {
		return memoize(loader, withDefault());
	}

	/**
	 * Memoize the output for each input. The memorization is based on the input's equality.
	 * <p>
	 * Non-present values won't be cached.
	 *
	 * @param loader actual loading function
	 * @param <I>    input type
	 * @param <O>    output type
	 * @return curried function
	 */
	public static <I, O> Function<I, O> memoize(final Function<I, O> loader,
												final Consumer<MemoizationOptions.MemoizationOptionsBuilder> configurer) {
		final Function<I, Optional<O>> memoize = memoize0(
				input -> Optional.ofNullable(loader.apply(input)),
				configurer
		);
		return input -> memoize.apply(input).orElse(null);
	}

	/**
	 * Memoize the output for each input. The memorization is based on the input's equality.
	 *
	 * @param loader     actual loading function
	 * @param configurer configurer
	 * @param <I>        input type
	 * @param <O>        output type
	 * @return curried function
	 */
	static <I, O> MemoizationFunction<I, O> memoize0(final Function<I, Optional<O>> loader,
													 final Consumer<MemoizationOptions.MemoizationOptionsBuilder> configurer) {

		final var optionsBuilder = MemoizationOptions.builder();
		configurer.accept(optionsBuilder);
		final var options = optionsBuilder.build();

		record Item<T>(@Nullable T value) {
		}

		final Map<I, Item<O>> cache;
		{
			Map<I, Item<O>> temp;
			if (options.getExpectedSize() <= 3) {
				temp = new LinkedHashMap<>();
			} else {
				temp = new HashMap<>();
			}
			if (options.isThreadSafe()) {
				temp = Collections.synchronizedMap(temp);
			}
			cache = temp;
		}

		return input -> {
			if (cache.containsKey(input)) {
				return Optional.ofNullable(cache.get(input).value());
			}
			final var output = loader.apply(input).orElse(null);
			if (output != null || options.isIncludeEmptyValues()) {
				cache.put(input, new Item<>(output));
			}
			return Optional.ofNullable(cache.get(input))
						   .map(Item::value);
		};
	}

}
