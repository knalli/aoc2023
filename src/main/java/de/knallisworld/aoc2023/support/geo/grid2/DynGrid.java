package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.support.geo.Point2D;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class DynGrid<P extends Number, T> {

	final Map<Point2D<P>, T> data;

	final Set<P> validX;
	final Set<P> validY;

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
		this.validX = new HashSet<>();
		this.validY = new HashSet<>();
		data.keySet()
			.forEach(p0 -> {
				validX.add(p0.getX());
				validY.add(p0.getX());
			});
	}

	public boolean has(final Point2D<P> p) {
		if (!validX.contains(p.getX())) {
			return false;
		}
		if (!validY.contains(p.getY())) {
			return false;
		}
		return data.containsKey(p);
	}

	public boolean has(final P x, final P y) {
		if (!validX.contains(x)) {
			return false;
		}
		if (!validY.contains(y)) {
			return false;
		}
		return has(Point2D.create(x, y));
	}

	public void setValue(final Point2D<P> p,
						 final T value) {
		data.put(p, value);
		validX.add(p.getX());
		validY.add(p.getY());
	}

	public void clearValue(final Point2D<P> p) {
		data.remove(p);
		validX.clear();
		validY.clear();
		data.keySet()
			.forEach(p0 -> {
				validX.add(p0.getX());
				validY.add(p0.getX());
			});
	}

	public T getValueRequired(final Point2D<P> p) {
		if (!has(p)) {
			throw new NullPointerException();
		}
		return requireNonNull(data.get(p));
	}


	public Optional<T> getValue(final Point2D<P> p) {
		if (!has(p)) {
			return Optional.empty();
		}
		return Optional.ofNullable(data.get(p));
	}

	public Optional<T> getValue(final P x, final P y) {
		if (!has(x, y)) {
			return Optional.empty();
		}
		return getValue(Point2D.create(x, y));
	}

	public Stream<Point2D<P>> getAdjacents4(final Point2D<P> p) {
		return getAdjacents4(p, false);
	}

	public Stream<Point2D<P>> getAdjacents4(final Point2D<P> p, final boolean includeEmpty) {
		return p.getAdjacents4()
				.filter(p1 -> includeEmpty || has(p1));
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

		public Stream<FieldsView.Field<P, T>> streamDirect() {
			return grid.data
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

		public Stream<Point2D<P>> getCluster4(final Point2D<P> p,
											  final Predicate<Field<P, T>> filter) {

			final var cluster = new HashSet<Point2D<P>>();
			final var q = new LinkedList<Point2D<P>>();
			q.add(p);
			while (!q.isEmpty()) {
				final var n = q.pop();
				cluster.add(n);
				grid.getAdjacents4(n)
					.filter(not(cluster::contains))
					.filter(not(q::contains))
					.filter(a -> filter.test(new Field<>(a, grid.getValueRequired(a))))
					.forEach(q::add);
			}
			return cluster.stream();
		}

		public Stream<Point2D<P>> getCluster4All(final Point2D<P> p,
												 final Predicate<Field<P, Optional<T>>> filter) {

			final var cluster = new HashSet<Point2D<P>>();
			final var q = new LinkedList<Point2D<P>>();
			q.add(p);
			while (!q.isEmpty()) {
				final var n = q.pop();
				cluster.add(n);
				grid.getAdjacents4(n, true)
					.filter(not(cluster::contains))
					.filter(a -> filter.test(new Field<>(a, grid.getValue(a))))
					.forEach(q::add);
			}
			return cluster.stream();
		}

	}

}
