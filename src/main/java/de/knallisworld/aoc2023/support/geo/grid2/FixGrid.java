package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class FixGrid<T> {

	private final Class<T> type;
	private final T[][] data;

	public FixGrid(final Class<T> type, final int initialHeight, final int initialWidth) {
		this.type = type;
		this.data = createData(type, initialHeight, initialWidth);
	}

	public static <T> FixGrid<T> create(final Class<T> type, final int initialHeight, final int initialWidth) {
		return new FixGrid<>(type, initialHeight, initialWidth);
	}

	public static <T> FixGrid<T> extrapolated(final FixGrid<T> src,
											  final int scale,
											  final BiConsumer<FieldsView.Field<T>, FixGrid<T>> valueExtrapolator) {
		if (scale < 1) {
			throw new IllegalArgumentException("scale must be greater than 1");
		}
		final var dst = new FixGrid<>(src.type, src.data.length * scale, src.data[0].length * scale);
		IntStream.range(0, src.data.length)
				 .forEach(srcY -> {
					 IntStream.range(0, src.data[srcY].length)
							  .forEach(srcX -> {
								  final var dstOffset = Point2D.create(srcX * scale, srcY * scale);
								  final var dstCenter = dstOffset.downRight();
								  dst.setValue(dstCenter, src.getValueRequired(srcX, srcY));

								  final var temp = FixGrid.create(src.type, scale, scale);
								  valueExtrapolator.accept(
										  new FieldsView.Field<>(
												  dstCenter,
												  dst.getValueRequired(dstCenter)
										  ),
										  temp
								  );

								  temp.fields()
									  .forEach(field -> {
										  dst.setValue(
												  dstOffset.getX() + field.pos().getX(),
												  dstOffset.getY() + field.pos().getY(),
												  field.value()
										  );
									  });
							  });
				 });
		return dst;
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

	public static <T> FixGrid<T> copy(FixGrid<T> from) {
		final var clone = create(from.type, from.getHeight(), from.getWidth());
		from.fields().forEach(f -> clone.setValue(f.pos, f.value));
		return clone;
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
		if (!(0 <= y && y < data.length)) {
			return false;
		}
		if (!(0 <= x && x < data[y].length)) {
			return false;
		}
		return data[y][x] != null;
	}

	public T getValueRequired(final Point2D<Integer> p) {
		return getValueRequired(p.getX(), p.getY());
	}

	public T getValueRequired(final int x, final int y) {
		return requireNonNull(data[y][x]);
	}

	public Optional<T> getValue(final Point2D<Integer> p) {
		return getValue(p.getX(), p.getY());
	}

	public Optional<T> getValue(final int x, final int y) {
		if (hasValue(x, y)) {
			return Optional.of(requireNonNull(data[y][x]));
		} else {
			return Optional.empty();
		}
	}

	public FieldsView<T> fields() {
		return new FieldsView<>(this);
	}

	public void fill(T value) {
		IntStream.range(0, data.length)
				 .forEach(y -> IntStream.range(0, data[y].length)
										.forEach(x -> data[y][x] = value));
	}

	public int getHeight() {
		return data.length;
	}

	public int getWidth() {
		return data[0].length;
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

		public Stream<Point2D<Integer>> getAdjacents4(final Point2D<Integer> p) {
			return p.getAdjacents4()
					.filter(grid::hasValue);
		}

		public Stream<Point2D<Integer>> getCluster4(final Point2D<Integer> p,
													final Predicate<Field<T>> filter) {

			final var cluster = new HashSet<Point2D<Integer>>();
			final var q = new LinkedList<Point2D<Integer>>();
			q.add(p);
			while (!q.isEmpty()) {
				final var n = q.pop();
				cluster.add(n);
				getAdjacents4(n)
						.filter(not(cluster::contains))
						.filter(not(q::contains))
						.filter(a -> filter.test(new Field<>(a, grid.getValueRequired(a))))
						.forEach(q::add);
			}
			return cluster.stream();
		}

		public Stream<Point2D<Integer>> getAdjacents8(final Point2D<Integer> p) {
			return p.getAdjacents8()
					.filter(grid::hasValue);
		}

	}

	public String toString(final BiFunction<Point2D<Integer>, T, String> renderer) {

		final var sb = new StringBuilder();

		IntStream.range(0, data.length)
				 .forEach(y -> {
					 IntStream.range(0, data[y].length)
							  .forEach(x -> {
								  final var p = Point2D.create(x, y);
								  final var v = getValueRequired(p);
								  sb.append(renderer.apply(p, v));
							  });
					 sb.append("\n");
				 });

		return sb.toString();
	}

}
