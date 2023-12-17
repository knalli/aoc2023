package de.knallisworld.aoc2023.support.geo.grid2;

import de.knallisworld.aoc2023.day16.Day16;
import de.knallisworld.aoc2023.support.geo.Point2D;

public enum Direction {

	North(Point2D.create(0, -1)),
	East(Point2D.create(1, 0)),
	South(Point2D.create(0, 1)),
	West(Point2D.create(-1, 0));

	private final Point2D<Integer> offset;

	Direction(Point2D<Integer> offset) {
		this.offset = offset;
	}

	public Point2D<Integer> offset() {
		return offset;
	}

	public Direction left() {
		return switch (this) {
			case North -> West;
			case West -> South;
			case South -> East;
			case East -> North;
		};
	}

	public Direction right() {
		return switch (this) {
			case North -> East;
			case East -> South;
			case South -> West;
			case West -> North;
		};
	}

}
