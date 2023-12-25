package de.knallisworld.aoc2023.day24;

import com.microsoft.z3.Context;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.Status;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day24 {

	static final Precision.DoubleEquivalence PRECISION = Precision.doubleEquivalenceOfEpsilon(1e-6);

	public static void main(String[] args) {
		printHeader(24);
		//printSolution(1, () -> part1(parseInput(readInputLines(24, "part0")), 7, 27));
		// expected 20847
		printSolution(1, () -> part1(parseInput(readInputLines(24, "part1")), 200000000000000D, 400000000000000D));
		// expected 908621716620524
		printSolution(2, () -> part2(parseInput(readInputLines(24, "part1")), 200000000000000D, 400000000000000D));
	}

	record Line3D(Vector3D pos, Vector3D vector) {
	}

	static List<Line3D> parseInput(final List<String> lines) {
		return lines
				.stream()
				.map(str -> {
					// e.g. 19, 13, 30 @ -2,  1, -2
					final var p0 = str.split(" @ ")[0].split(", ");
					final var p1 = str.split(" @ ")[1].split(", ");
					final var pos = Vector3D.of(Long.parseLong(p0[0].strip()), Long.parseLong(p0[1].strip()), Long.parseLong(p0[2].strip()));
					final var vector = Vector3D.of(Long.parseLong(p1[0].strip()), Long.parseLong(p1[1].strip()), Long.parseLong(p1[2].strip()));
					return new Line3D(pos, vector);
				})
				.toList();
	}

	static Vector2D extractAsXyVector(final Vector3D v) {
		return Vector2D.of(v.getX(), v.getY());
	}

	record Pair<T>(T a, T b) {
	}

	static <T> Stream<Pair<T>> forEachPair(final Collection<T> collection) {
		final var l = List.copyOf(collection);
		final var result = new ArrayList<Pair<T>>();
		for (var i = 0; i < collection.size(); i++) {
			for (var j = i + 1; j < collection.size(); j++) {
				result.add(new Pair<>(l.get(i), l.get(j)));
			}
		}
		return result.stream();
	}

	static List<Vector2D> getXYCrossings(final List<Line3D> lines, final Vector2D min, final Vector2D max) {
		return forEachPair(lines)
				.map(pair -> {
					var l1 = pair.a();
					var l2 = pair.b();
					log.debug(() -> "Hailstone A: %s @ %s".formatted(l1.pos, l1.vector));
					log.debug(() -> "Hailstone B: %s @ %s".formatted(l2.pos, l1.vector));
					final var line1 = Lines.fromPointAndDirection(extractAsXyVector(l1.pos()), extractAsXyVector(l1.vector()), PRECISION);
					final var line2 = Lines.fromPointAndDirection(extractAsXyVector(l2.pos()), extractAsXyVector(l2.vector()), PRECISION);
					final var intersection = line1.intersection(line2);
					if (intersection == null) {
						log.debug("Hailstones' paths are parallel; they never intersect.");
						return null;
					}
					if (!(PRECISION.lte(min.getX(), intersection.getX()) && PRECISION.lte(intersection.getX(), max.getX()) && PRECISION.lte(min.getY(), intersection.getY()) && PRECISION.lte(intersection.getY(), max.getY()))) {
						log.debug(() -> "Hailstones' paths will cross outside the test area (at %s).".formatted(intersection));
						return null;
					}
					final var past1 = PRECISION.lt(line1.offset(intersection), line1.getOriginOffset());
					final var past2 = PRECISION.lt(line2.offset(intersection), line2.getOriginOffset());
					if (past1 && past2) {
						log.debug(() -> "Hailstones' paths crossed in the past for both hailstones.");
					} else if (past1) {
						log.debug(() -> "Hailstones' paths crossed in the past for hailstone A.");
						return null;
					} else if (past2) {
						log.debug(() -> "Hailstones' paths crossed in the past for hailstone B.");
						return null;
					}
					log.debug(() -> "Hailstones' paths will cross inside the test area (at %s).".formatted(intersection));
					return intersection;
				})
				.filter(Objects::nonNull)
				.toList();
	}

	// https://github.com/eagely/adventofcode/blob/main/src/main/kotlin/solutions/y2023/Day24.kt
	static List<Vector2D> getXYCrossings2(final List<Line3D> lines, final Vector2D min, final Vector2D max) {
		return forEachPair(lines)
				.map(pair -> {
					var l1 = pair.a();
					var l2 = pair.b();
					log.debug(() -> "Hailstone A: %s @ %s".formatted(l1.pos, l1.vector));
					log.debug(() -> "Hailstone B: %s @ %s".formatted(l2.pos, l1.vector));

					final var fx = l1.pos.getX();
					final var fy = l1.pos.getY();
					final var fdx = l1.vector.getX();
					final var fdy = l1.vector.getY();
					final var fa = l1.vector.getY();
					final var fb = -l1.vector.getX();
					final var fc = (l1.vector.getY() * l1.pos.getX()) - (l1.vector.getX() * l1.pos.getY());
					final var sx = l2.pos.getX();
					final var sy = l2.pos.getY();
					final var sdx = l2.vector.getX();
					final var sdy = l2.vector.getY();
					final var sa = l2.vector.getY();
					final var sb = -l2.vector.getX();
					final var sc = (l2.vector.getY() * l2.pos.getX()) - (l2.vector.getX() * l2.pos.getY());

					if (fa * sb != fb * sa) {
						var x = (fc * sb - sc * fb) / (fa * sb - sa * fb);
						var y = (sc * fa - fc * sa) / (fa * sb - sa * fb);
						if (PRECISION.lte(min.getX(), x) && PRECISION.lte(x, max.getX()) && PRECISION.lte(min.getY(), y) && PRECISION.lte(y, max.getY())) {
							if (PRECISION.gte((x - fx) * fdx, 0D) && PRECISION.gt((y - fy) * fdy, 0D)) {
								if (PRECISION.gte((x - sx) * sdx, 0D) && PRECISION.gt((y - sy) * sdy, 0D)) {
									return Vector2D.of(x, y);
								}
							}
						}
					}
					return null;
				})
				.filter(Objects::nonNull)
				.toList();
	}

	static String part1(final List<Line3D> lines, double min, double max) {
		// TODO actual getXYCrossings wont work for real input, only for demo
		final var crossings = getXYCrossings2(lines, Vector2D.of(min, min), Vector2D.of(max, max));
		return "crossings = %d".formatted(crossings.size());
	}

	static String part2(final List<Line3D> lines, double min, double max) {

		// well, TIL: z3
		// https://github.com/eagely/adventofcode/blob/main/src/main/kotlin/solutions/y2023/Day24.kt
		// install or download the native part via https://github.com/Z3Prover/z3/releases
		// you may have to jump through some security check loops...
		// for the next time this helps a lot solving linear systems

		try (final var ctx = new Context()) {
			final var solver = ctx.mkSolver();
			final var mx = ctx.mkRealConst("mx");
			final var my = ctx.mkRealConst("m");
			final var mz = ctx.mkRealConst("mz");
			final var mxv = ctx.mkRealConst("mxv");
			final var mv = ctx.mkRealConst("mv");
			final var mzv = ctx.mkRealConst("mzv");
			// if the hit for the first 3 is found, it must align with the rest
			IntStream.range(0, 3).forEach(i -> {
				final var line = lines.get(i);
				final var sx = line.pos.getX();
				final var sy = line.pos.getY();
				final var sz = line.pos.getZ();
				final var sxv = line.vector.getX();
				final var syv = line.vector.getY();
				final var szv = line.vector.getZ();
				// each line/hail will probably hit with a different t, so use t0, t1, t2
				final var t = ctx.mkRealConst(STR."t\{i}");
				// build for each of dimensions (x, y, z).
				// the searching point is (mx,my,mz) with a vector (mxv,myv,mzv)
				// there is a t which fits for
				// 1   $mx + ( $mxv * t ) == sx + ( sxv * t )
				// 2   $my + ( $myv * t ) == sy + ( syv * t )
				// 3   $mz + ( $mzv * t ) == sz + ( szv * t )
				solver.add(ctx.mkEq(ctx.mkAdd(mx, ctx.mkMul(mxv, t)), ctx.mkAdd(ctx.mkReal((long) sx), ctx.mkMul(ctx.mkReal((long) sxv), t))));
				solver.add(ctx.mkEq(ctx.mkAdd(my, ctx.mkMul(mv, t)), ctx.mkAdd(ctx.mkReal((long) sy), ctx.mkMul(ctx.mkReal((long) syv), t))));
				solver.add(ctx.mkEq(ctx.mkAdd(mz, ctx.mkMul(mzv, t)), ctx.mkAdd(ctx.mkReal((long) sz), ctx.mkMul(ctx.mkReal((long) szv), t))));
			});
			if (solver.check() != Status.SATISFIABLE) {
				throw new IllegalStateException("invalid solver state: " + solver.check());
			}
			final var model = solver.getModel();
			final var solution = Stream
					.of(mx, my, mz)
					.mapToLong(it -> {
						final var eval = model.eval(it, false);
						if (eval instanceof RatNum rn) {
							return rn.getNumerator().getInt64();
						}
						throw new IllegalStateException();
					})
					.sum();
			return "solution = %d".formatted(solution);
		}
	}

}

