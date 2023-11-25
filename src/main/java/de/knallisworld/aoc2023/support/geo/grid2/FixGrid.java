package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.lang.reflect.Array;

public class Grid<T> {

	private T[][] data;

	public Grid(final Class<T> type, final int initialHeight, final int initialWidth) {
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

	public Grid<T> setValue(final Point2D<Integer> p, final T value) {
		return setValue(p.x(), p.y(), value);
	}

	public Grid<T> setValue(final int x, final int y, T value) {
		data[y][x] = value;
		return this;
	}

	public T getValueRequired(final Point2D<Integer> p) {
		return getValueRequired(p.x(), p.y());
	}

	public T getValueRequired(final int x, final int y) {
		return data[y][x];
	}

}
