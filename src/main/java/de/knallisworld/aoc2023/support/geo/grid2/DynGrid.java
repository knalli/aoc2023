package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class DynGrid<P extends Number, T> {

	final Map<Point2D<P>, T> data;

	public static <P extends Number, T> DynGrid<P, T> empty() {
		return new DynGrid<>(new HashMap<>());
	}

	public static <P extends Number, T> DynGrid<P, T> of(final Map<Point2D<P>, T> data) {
		return new DynGrid<>(data);
	}

	public static <P extends Number, T> DynGrid<P, T> copyOf(final Map<Point2D<P>, T> data) {
		return new DynGrid<>(new HashMap<>(data));
	}

	public DynGrid(final Map<Point2D<P>, T> data) {
		this.data = data;
	}

	public boolean has(final Point2D<P> p) {
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

	public long count() {
		return data.size();
	}

	public long count(final BiPredicate<Point2D<P>, T> filter) {
		return data.entrySet()
				   .stream()
				   .filter(e -> filter.test(e.getKey(), e.getValue()))
				   .count();
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public DynGrid<P, T> clone() {
		return DynGrid.copyOf(data);
	}

}
