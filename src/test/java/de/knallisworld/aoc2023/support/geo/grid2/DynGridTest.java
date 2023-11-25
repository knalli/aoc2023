package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynGridTest {

	@Test
	void adjacents4() {
		final var grid = DynGrid.<Integer, Boolean>empty();
		final var p0 = Point2D.create(4, 5);
		grid.setValue(p0, true);
		grid.setValue(p0.up(), true);
		grid.setValue(p0.right(), true);
		grid.setValue(p0.right().right(), true);
		assertThat(grid.count())
				.isEqualTo(4);
		assertThat(grid.getAdjacents4(Point2D.create(4, 5)).toList())
				.isNotNull()
				.asList()
				.hasSize(2)
				.containsExactly(
						Point2D.create(4, 4),
						Point2D.create(5, 5)
				);
	}
}
