package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class FixGrid<T> {

	private final T[][] data;

	public FixGrid(final Class<T> type, final int initialHeight, final int initialWidth) {
		this.data = createData(type, initialHeight, initialWidth);
	}

	@SuppressWarnings("unchecked")
	static <T> T[][] createData(Class<T> type, int initialHeight, int initialWidth) {
		final var arrayType = (Class<T[]>) type.arrayType();
		T[][] data = (T[][]) Array.newInstance(arrayType, initialHeight);
		for (int i = 0; i < initialHeight; i++) {
			data[i] = (T[]) Array.newInstance(type, initialWidth);
		}
		return data;
	}

	public void setValue(final Point2D<Integer> p, final T value) {
		setValue(p.getX(), p.getY(), value);
	}

	public void setValue(final int x, final int y, T value) {
		data[y][x] = value;
	}

	public boolean hasValue(final Point2D<Integer> p) {
		return hasValue(p.getX(), p.getY());
	}

	public boolean hasValue(final int x, final int y) {
		return data[y][x] != null;
	}

	public T getValueRequired(final Point2D<Integer> p) {
		return getValueRequired(p.getX(), p.getY());
	}

	public T getValueRequired(final int x, final int y) {
		return requireNonNull(data[y][x]);
	}

	public FieldsView<T> fields() {
		return new FieldsView<>(this);
	}

	public static class FieldsView<T> {

		public record Field<T>(Point2D<Integer> pos, T value) {
		}

		private final FixGrid<T> grid;

		public FieldsView(final FixGrid<T> grid) {
			this.grid = grid;
		}

		public Stream<Field<T>> stream() {
			return IntStream
					.range(0, grid.data.length)
					.boxed()
					.flatMap(y -> IntStream
							.range(0, grid.data[y].length)
							.boxed()
							.map(x -> Point2D.create(x, y))
							.filter(grid::hasValue)
							.map(p -> new Field<>(p, grid.getValueRequired(p)))
					);
		}

		public void forEach(final Consumer<Field<T>> consumer) {
			stream().forEach(consumer);
		}

	}

}
