package de.knallisworld.aoc2023.day03;

import org.junit.jupiter.api.Test;

import static de.knallisworld.aoc2023.day03.Day03.*;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static org.assertj.core.api.Assertions.assertThat;

class Day03Test {

	@Test
	void testPart1() {
		assertThat(part1(readIntoGrid(readInputLines(3, "part1"))))
				.isEqualTo("len(numbers) = 1098, sum = 559667");
	}

	@Test
	void testPart2() {
		assertThat(part2(readIntoGrid(readInputLines(3, "part1"))))
				.isEqualTo("gears = 330, sum = 86841457");
	}
}
