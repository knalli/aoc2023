package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
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
		return getAdjacents8(p, false);
	}

	public Stream<Point2D<P>> getAdjacents8(final Point2D<P> p, boolean includeEmpty) {
		return p.getAdjacents8()
				.filter(p1 -> includeEmpty || has(p1));
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

	public String toString(final BiFunction<Point2D<P>, T, String> renderer) {
		return toString(renderer, () -> "");
	}

	public String toString(final BiFunction<Point2D<P>, T, String> renderer,
						   final Supplier<String> emptyRenderer) {
		final var minX = getMinX();
		final var minY = getMinY();
		final var maxX = getMaxX();
		final var maxY = getMaxY();

		final var sb = new StringBuilder();

		final var topLeft = minX.min(minY);
		final var bottomRight = maxX.max(maxY);

		topLeft.untilY(bottomRight)
			   .flatMap(p -> {
				   final Stream<Optional<Point2D<P>>> concat = Stream.concat(
						   p.untilX(maxX).map(Optional::of),
						   Stream.of(Optional.empty())
				   );
				   return concat;
			   })
			   .forEach(opt -> {
				   opt.ifPresentOrElse(
						   p -> {
							   getValue(p).ifPresentOrElse(
									   v -> {
										   sb.append(renderer.apply(p, v));
									   },
									   () -> sb.append(emptyRenderer.get())
							   );
						   },
						   () -> sb.append("\n")
				   );
			   });

		return sb.toString();
	}

	private Point2D<P> getMinY() {
		return data.keySet()
				   .stream()
				   .min(comparing(p -> p.getY().longValue()))
				   .orElseThrow();
	}

	private Point2D<P> getMaxY() {
		return data.keySet()
				   .stream()
				   .max(comparing(p -> p.getY().longValue()))
				   .orElseThrow();
	}

	private Point2D<P> getMaxX() {
		return data.keySet()
				   .stream()
				   .max(comparing(p -> p.getX().longValue()))
				   .orElseThrow();
	}

	private Point2D<P> getMinX() {
		return data.keySet()
				   .stream()
				   .min(comparing(p -> p.getX().longValue()))
				   .orElseThrow();
	}

	public FieldsView<P, T> fields() {
		return new FieldsView<>(this);
	}

	public long minY() {
		return getMinY().getY().longValue();
	}

	public long maxY() {
		return getMaxY().getY().longValue();
	}

	public long minX() {
		return getMinX().getX().longValue();
	}

	public long maxX() {
		return getMaxX().getX().longValue();
	}

	public static class FieldsView<P extends Number, T> {

		private final DynGrid<P, T> grid;

		public FieldsView(DynGrid<P, T> grid) {
			this.grid = grid;
		}

		public Stream<Field<P, Optional<T>>> withinRowRangeInclusive(final Point2D<P> begin,
																	 final Point2D<P> end) {
			return Stream
					.iterate(
							begin,
							p -> {
								// next possible?
								return p.getX().longValue() <= end.getX().longValue();
							},
							Point2D::right
					)
					.map(p -> Field.create(p, grid.getValue(p)));
		}

		public Stream<Field<P, T>> groupInRow(final Point2D<P> p) {
			// find left start
			var start = p;
			while (grid.has(start.left())) {
				start = start.left();
			}
			// find right end
			var end = p;
			while (grid.has(end.right())) {
				end = end.right();
			}
			return grid
					.fields()
					.withinRowRangeInclusive(start, end)
					.flatMap(a -> a.value().stream()
								   .map(v -> Field.create(a.position(), v)));
		}

		public record Row<P extends Number, T>(
				P row,
				List<Field<P, Optional<T>>> fields
		) {

			public List<Field<P, T>> filledFields() {
				return fields.stream()
							 .flatMap(f -> f.value()
											.stream()
											.map(v -> Field.create(f.position(), v)))
							 .toList();
			}

		}

		public record Field<P extends Number, T>(
				Point2D<P> position,
				T value
		) {

			public static <P extends Number, T> Field<P, T> create(Point2D<P> position, T value) {
				return new Field<>(position, value);
			}

		}

		public Stream<FieldsView.Field<P, T>> stream() {
			return Map.copyOf(grid.data)
					  .entrySet()
					  .stream()
					  .map(e -> new Field<>(e.getKey(), e.getValue()));
		}

		public Stream<Row<P, T>> rows() {
			final var minX = grid.getMinX();
			final var maxX = grid.getMaxX();
			final var minY = grid.getMinY();
			final var maxY = grid.getMaxY();
			final var topLeft = minX.min(minY);
			final var bottomRight = maxX.max(maxY);
			return Stream.iterate(
								 topLeft,
								 p -> {
									 // next possible?
									 return p.getY().longValue() <= maxY.getY().longValue();
								 },
								 Point2D::down
						 )
						 .map(currentY -> {
							 final var list = Stream
									 .iterate(
											 currentY,
											 p -> {
												 // next possible?
												 return p.getX().longValue() <= bottomRight.getX().longValue();
											 },
											 Point2D::right
									 )
									 .map(p -> Field.create(p, grid.getValue(p)))
									 .toList();
							 return new Row<>(
									 currentY.getY(),
									 list
							 );
						 });
		}

	}

}
