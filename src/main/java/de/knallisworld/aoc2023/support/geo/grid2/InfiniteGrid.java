package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Math.floorMod;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class InfiniteGrid<T> {

	final FixGrid<T> base;

	InfiniteGrid(final FixGrid<T> base) {
		this.base = base;
	}

	public static <T> InfiniteGrid<T> of(final FixGrid<T> fixGrid) {
		return new InfiniteGrid<>(fixGrid);
	}

	public T getValueRequired(final Point2D<Integer> p) {
		return getValueRequired(p.getX(), p.getY());
	}

	public T getValueRequired(final int x, final int y) {
		final var width = base.getWidth();
		final var height = base.getHeight();
		final var rx = floorMod(x, width);
		final var ry = floorMod(y, height);
		return requireNonNull(base.getValueRequired(rx, ry));
	}

	public FieldsView<T> fields() {
		return fields(s -> {
		});
	}

	public FieldsView<T> fields(final Consumer<FieldsView.Settings.SettingsBuilder> configurer) {
		final var builder = FieldsView.Settings.builder();
		configurer.accept(builder);
		return new FieldsView<>(this, builder.build());
	}

	public static class FieldsView<T> {

		@Builder
		@Getter
		public static class Settings {

			@Builder.Default
			private boolean cacheEnabled = true;

		}

		public record Field<T>(Point2D<Integer> pos, T value) {
		}


		private final InfiniteGrid<T> grid;
		private final Settings settings;
		private final Map<Point2D<Integer>, Set<Point2D<Integer>>> adjacents4Cache;

		public FieldsView(final InfiniteGrid<T> grid, final Settings settings) {
			this.grid = grid;
			this.adjacents4Cache = new HashMap<>();
			this.settings = settings;
		}

		public Stream<Point2D<Integer>> getAdjacents4(final Point2D<Integer> p) {

			if (!settings.isCacheEnabled()) {
				return p.getAdjacents4();
			}

			final var x = p.getX();
			final var y = p.getY();
			final var width = grid.base.getWidth();
			final var height = grid.base.getHeight();

			final var rx = floorMod(x, width);
			final var ry = floorMod(y, height);

			final var offsetX = x - rx;
			final var offsetY = y - ry;

			return adjacents4Cache
					.computeIfAbsent(Point2D.create(rx, ry), rp -> rp.getAdjacents4().collect(toSet()))
					.stream()
					.map(rp -> Point2D.create(rp.getX() + offsetX, rp.getY() + offsetY));
		}

	}


}
