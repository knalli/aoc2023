package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.lang.reflect.Array;

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
		for (int i = 0; i < initialWidth; i++) {
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

	public T getValueRequired(final Point2D<Integer> p) {
		return getValueRequired(p.getX(), p.getY());
	}

	public T getValueRequired(final int x, final int y) {
		return requireNonNull(data[y][x]);
	}

}
