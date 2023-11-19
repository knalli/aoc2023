package de.knallisworld.aoc2023.day00;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class Day00Test {

	@Test
	void test_part1_SimpleFib() {
		assertThat(Day00.part1_SimpleFib(8))
				.isEqualTo(21L);
	}

	@Test
	void test_part2_ComplexFib() {
		assertThat(Day00.part2_ComplexFib(8))
				.extracting(BigInteger::longValue)
				.isEqualTo(21L);
	}

}
