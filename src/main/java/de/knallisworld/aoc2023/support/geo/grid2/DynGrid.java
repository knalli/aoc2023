package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class DynGrid2D<P extends Number, T> {

	final Map<Point2D<P>, T> data;

	public DynGrid2D() {
		this.data = new HashMap<>();
	}

	public boolean has(final Point2D<P> p ){
		return data.containsKey(p);
	}

	public void setValue(final Point2D<P> p,
						 final T value) {
		data.put(p, value);
	}

	public void clearValue(final Point2D<P> p) {
		data.remove(p);
	}

	public T getValueRequired(final Point2D<P> p) {
		return requireNonNull(data.get(p));
	}


	public Optional<T> getValue(final Point2D<P> p) {
		return Optional.ofNullable(data.get(p));
	}

	public Stream<Point2D<P>> getAdjacents4(final Point2D<P> p) {
		return p.getAdjacents4()
				.filter(this::has);
	}

	public Stream<Point2D<P>> getAdjacents8(final Point2D<P> p) {
		return p.getAdjacents8()
				.filter(this::has);
	}

}
