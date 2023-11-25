package de.knallisworld.aoc2023.support.geo;

import lombok.Data;

import java.util.function.BiFunction;
import java.util.stream.Stream;

@Data
public class Point2D<T extends Number> {

	final T x;

	final T y;

	private final BiFunction<T, Integer, T> adder;

	public static <T extends Number> Point2D<T> create(
			final T x,
			final T y
	) {
		return new Point2D<>(
				x,
				y,
				(t, v) -> {
					if (t instanceof Integer i) {
						return (T) Integer.valueOf((i + v));
					}
					if (t instanceof Long l) {
						return (T) Long.valueOf((l + v));
					}
					throw new IllegalStateException("unsupported type");
				}
		);
	}

	public static <T extends Number> Point2D<T> create(
			final T x,
			final T y,
			final BiFunction<T, Integer, T> adder
	) {
		return new Point2D<>(x, y, adder);
	}

	public Point2D(final T x,
				   final T y,
				   final BiFunction<T, Integer, T> adder) {
		this.x = x;
		this.y = y;
		this.adder = adder;
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
				adder.apply(y, -1)
		);
	}

	public Point2D<T> upLeft() {
		return createNew(
				adder.apply(x, -1),
				adder.apply(y, -1)
		);
	}

	public Point2D<T> upRight() {
		return createNew(
				adder.apply(x, 1),
				adder.apply(y, -1)
		);
	}

	public Point2D<T> right() {
		return createNew(
				adder.apply(x, 1),
				y
		);
	}

	public Point2D<T> down() {
		return createNew(
				x,
				adder.apply(y, +1)
		);
	}

	public Point2D<T> downLeft() {
		return createNew(
				adder.apply(x, -1),
				adder.apply(y, 1)
		);
	}

	public Point2D<T> downRight() {
		return createNew(
				adder.apply(x, 1),
				adder.apply(y, 1)
		);
	}

	public Point2D<T> left() {
		return createNew(
				adder.apply(x, -1),
				y
		);
	}

}
