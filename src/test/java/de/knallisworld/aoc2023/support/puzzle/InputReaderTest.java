package de.knallisworld.aoc2023.support.puzzle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputReaderTest {

	@Test
	void readInputFirstLine() {
		assertThat(InputReader.readInputFirstLine(0, "part0"))
				.isNotNull()
				.isEqualTo("foo");
	}

	@Test
	void readInputLines() {
		assertThat(InputReader.readInputLines(0, "part1"))
				.isNotNull()
				.asList()
				.hasSize(2)
				.hasOnlyElementsOfType(String.class)
				.contains("foo")
				.contains("bar");
	}

	@Test
	void readInputFirstLineAsInts() {
		assertThat(InputReader.readInputFirstLineAsInts(0, "part2"))
				.isNotNull()
				.containsExactly(0, 1, 2, 3, 5, 8, 13, 21, 34);
	}

}
