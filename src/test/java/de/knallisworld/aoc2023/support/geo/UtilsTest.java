package de.knallisworld.aoc2023.support.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

	@Test
	void manhattenDistance() {
		final var p1 = Point2D.create(1, 2);
		final var p2 = Point2D.create(4, 4);
		assertThat(Utils.manhattenDistance(p1, p2))
				.isNotNull()
				.isEqualTo(5);
	}
}
