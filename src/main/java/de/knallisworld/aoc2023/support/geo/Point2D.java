package de.knallisworld.aoc2023.support.geo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNullElse;

@Data
@EqualsAndHashCode(of = {"x", "y"})
public class Point2D<T extends Number> {

	static final Map<Number, Map<Number, Point2D<?>>> CACHE = new WeakHashMap<>();

	static final BiFunction<Number, Integer, Number> DEFAULT_ADDER = (t, addingValue) -> {
		if (t instanceof Integer i) {
			return Integer.valueOf((i + addingValue));
		}
		if (t instanceof Long l) {
			return Long.valueOf((l + addingValue));
		}
		throw new IllegalStateException("unsupported type");
	};

	final T x;

	final T y;

	@Nullable
	private final BiFunction<T, Integer, T> adder;

	/**
	 * Ensure instances are unique, improve memory usage
	 */
	private static <T extends Number> Point2D<T> lookupCache(final T x,
															 final T y,
															 final Supplier<Point2D<T>> creator) {
		return (Point2D<T>) CACHE.computeIfAbsent(x, _ -> new WeakHashMap<>())
								 .computeIfAbsent(y, _ -> creator.get());
	}

	public static <T extends Number> Point2D<T> create(
			final T x,
			final T y
	) {
		return lookupCache(x, y, () -> create0(x, y, null));
	}

	static <T extends Number> Point2D<T> create(
			final T x,
			final T y,
			@Nullable final BiFunction<T, Integer, T> adder
	) {
		return lookupCache(x, y, () -> create0(x, y, adder));
	}

	static <T extends Number> Point2D<T> create0(
			final T x,
			final T y,
			@Nullable final BiFunction<T, Integer, T> adder
	) {
		return new Point2D<>(
				x,
				y,
				adder
		);
	}

	public static Point2D<Integer> createInt(
			final int x,
			final int y
	) {
		return create(x, y, Integer::sum);
	}

	Point2D(final T x,
			final T y,
			@Nullable final BiFunction<T, Integer, T> adder) {
		this.x = x;
		this.y = y;
		this.adder = adder;
	}

	BiFunction<T, Integer, T> adder() {
		return requireNonNullElse(adder, (BiFunction<T, Integer, T>) DEFAULT_ADDER);
	}

	public Stream<Point2D<T>> untilX(final Point2D<T> other) {
		var temp = this;
		final var result = new ArrayList<Point2D<T>>();
		final var otherLong = other.getX().longValue();
		while (temp.getX().longValue() <= otherLong) {
			result.add(temp);
			temp = createNew(adder().apply(temp.x, 1), temp.y);
		}
		return result.stream();
	}

	public Stream<Point2D<T>> untilY(final Point2D<T> other) {
		var temp = this;
		final var result = new ArrayList<Point2D<T>>();
		final var otherLong = other.getY().longValue();
		while (temp.getY().longValue() <= otherLong) {
			result.add(temp);
			temp = createNew(temp.x, adder().apply(temp.y, 1));
		}
		return result.stream();
	}

	public Point2D<T> min(final Point2D<T> other) {
		final var x = min(getX(), other.getX());
		final var y = min(getY(), other.getY());
		return Point2D.create(x, y);
	}

	public Point2D<T> max(final Point2D<T> other) {
		final var x = max(getX(), other.getX());
		final var y = max(getY(), other.getY());
		return Point2D.create(x, y);
	}

	private T min(T a, T b) {
		if (a.longValue() < b.longValue()) {
			return a;
		}
		return b;
	}

	private T max(T a, T b) {
		if (a.longValue() > b.longValue()) {
			return a;
		}
		return b;
	}


	public Stream<Point2D<T>> getAdjacents4() {
		return Stream.of(
				up(),
				right(),
				down(),
				left()
		);
	}

	public Stream<Point2D<T>> getAdjacents8() {
		return Stream.of(
				up(),
				upRight(),
				right(),
				downRight(),
				down(),
				downLeft(),
				left(),
				upLeft()
		);
	}

	Point2D<T> createNew(final T x, final T y) {
		return Point2D.create(
				x,
				y,
				adder
		);
	}

	public Point2D<T> up() {
		return createNew(
				x,
				adder().apply(y, -1)
		);
	}

	public Point2D<T> upLeft() {
		return createNew(
				adder().apply(x, -1),
				adder().apply(y, -1)
		);
	}

	public Point2D<T> upRight() {
		return createNew(
				adder().apply(x, 1),
				adder().apply(y, -1)
		);
	}

	public Point2D<T> right() {
		return createNew(
				adder().apply(x, 1),
				y
		);
	}

	public Point2D<T> down() {
		return createNew(
				x,
				adder().apply(y, +1)
		);
	}

	public Point2D<T> downLeft() {
		return createNew(
				adder().apply(x, -1),
				adder().apply(y, 1)
		);
	}

	public Point2D<T> downRight() {
		return createNew(
				adder().apply(x, 1),
				adder().apply(y, 1)
		);
	}

	public Point2D<T> left() {
		return createNew(
				adder().apply(x, -1),
				y
		);
	}

	@Override
	public String toString() {
		return "(%s/%s)".formatted(x, y);
	}

}
