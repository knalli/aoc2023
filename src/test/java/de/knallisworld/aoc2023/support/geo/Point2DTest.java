package de.knallisworld.aoc2023.support.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Point2DTest {

	@Test
	void adjacents4() {
		final var p = Point2D.create(3, 7);
		final var adjacents = p.getAdjacents4().toList();
		assertThat(adjacents)
				.isNotNull()
				.hasSize(4)
				.containsOnly(
						Point2D.create(2, 7),
						Point2D.create(3, 6),
						Point2D.create(3, 8),
						Point2D.create(4, 7)
				);
	}

	@Test
	void adjacents8() {
		final var p = Point2D.create(3, 7);
		final var adjacents = p.getAdjacents8().toList();
		assertThat(adjacents)
				.isNotNull()
				.hasSize(8)
				.containsOnly(
						Point2D.create(2, 6),
						Point2D.create(2, 7),
						Point2D.create(2, 8),
						Point2D.create(3, 6),
						Point2D.create(3, 8),
						Point2D.create(4, 6),
						Point2D.create(4, 7),
						Point2D.create(4, 8)
				);
	}

}
