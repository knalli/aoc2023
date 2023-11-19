package de.knallisworld.aoc2023.day00;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;

public class Day00 {

	public static void main(String[] args) {
		printHeader(0);
		printSolution(1, () -> "The result is " + part1_SimpleFib(42));
		printSolution(2, () -> "The result is " + part2_ComplexFib(123456));
	}

	static long part1_SimpleFib(final long i) {
		final var fib = new Fibonacci<>(
				Long::sum,
				0L,
				1L
		);
		return fib.get(i);
	}

	static BigInteger part2_ComplexFib(long i) {
		final var fib = new Fibonacci<>(
				BigInteger::add,
				BigInteger.ZERO,
				BigInteger.ONE
		);
		return fib.get(i);
	}

	static class Fibonacci<T> {

		final Map<Long, T> cache = new HashMap<>();
		private final BiFunction<T, T, T> adder;

		public Fibonacci(final BiFunction<T, T, T> adder,
						 final T initialZero,
						 final T initialOne) {
			this.adder = adder;
			this.cache.put(0L, initialZero);
			this.cache.put(1L, initialOne);
		}

		public T get(final long n) {
			if (cache.containsKey(n)) {
				return cache.get(n);
			}

			for (var i = 2L; i <= n; i++) {
				final var result = adder.apply(cache.get(i - 1), cache.get(i - 2));
				cache.put(i, result);
			}
			return cache.get(n);
		}

	}

}
