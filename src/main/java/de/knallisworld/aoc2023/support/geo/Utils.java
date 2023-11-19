package de.knallisworld.aoc2023.support.geo;

import static java.lang.Math.abs;

@SuppressWarnings("unused")
public class Utils {

	@SuppressWarnings("SpellCheckingInspection")
	public static <T extends Number> long manhattenDistance(final Point2D<T> a,
															final Point2D<T> b) {
		return abs(b.x().longValue() - a.x().longValue()) + abs(b.y().longValue() - a.y().longValue());
	}

}
