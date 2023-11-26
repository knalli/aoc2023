package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class DynGridTest {

	@Test
	void useGrid() {

		enum Type {
			ENEMY,
			PLAYER,
			EMPTY,
			WALL
		}
		record Data(Type a) {
		}

		final var grid = DynGrid.<Integer, Data>empty();

		final var chars = Arrays
				.stream(
						"""
								############
								#...X..X...#
								#...X..P...#
								#...X..X...#
								############
								""".stripIndent().split("\n")
				)
				.map(s -> s.toCharArray())
				.toList();
		IntStream.range(0, chars.size())
				 .forEach(y -> {
					 final var line = chars.get(y);
					 IntStream.range(0, line.length)
							  .forEach(x -> {
								  final var type = switch (line[x]) {
									  case '.' -> Type.EMPTY;
									  case '#' -> Type.WALL;
									  case 'P' -> Type.PLAYER;
									  case 'X' -> Type.ENEMY;
									  default -> null;
								  };
								  if (type != null) {
									  final var value = new Data(
											  type
									  );
									  grid.setValue(Point2D.createInt(x, y), value);
								  }
							  });
				 });
		System.out.println(grid.toString((p, v) -> {
			return switch (v.a()) {
				case WALL -> "🟫";
				case EMPTY -> "🌊";
				case ENEMY -> "👻";
				case PLAYER -> "🤠";
			};
		}));
	}

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
