package de.knallisworld.aoc2023.day18;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.Direction;
import de.knallisworld.aoc2023.support.geo.grid2.DynGrid;
import de.knallisworld.aoc2023.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;

@Log4j2
public class Day18 {

	public static void main(String[] args) {
		printHeader(18);
		printSolution(1, () -> part1(parseInput(readInputLines(18, "part1"))));
		printSolution(2, () -> part2(decodeInput(parseInput(readInputLines(18, "part1")))));
	}

	record Input(List<Instruction> instructions) {
	}

	record Instruction(Direction dir, int amount, String color) {
	}

	static Input parseInput(final List<String> lines) {
		return new Input(
				lines.stream()
					 .map(line -> {
						 final var p = line.split(" ");
						 return new Instruction(
								 switch (p[0]) {
									 case "U" -> Direction.North;
									 case "R" -> Direction.East;
									 case "D" -> Direction.South;
									 case "L" -> Direction.West;
									 default -> throw new IllegalStateException("invalid input");
								 },
								 Integer.parseInt(p[1]),
								 p[2].substring(1, p[2].length() - 1)
						 );
					 })
					 .toList()
		);
	}

	static String part1(final Input input) {

		final var grid = FixGrid.of(
				buildGridByInstructions(input.instructions),
				Boolean.class,
				false
		);
		fillEnclosedGridAreas(grid);

		final var filled = grid.fields()
							   .stream()
							   .filter(FixGrid.FieldsView.Field::value)
							   .count();

		return "filled amount = %d".formatted(filled);
	}

	static Input decodeInput(final Input input) {
		return new Input(
				input.instructions
						.stream()
						.map(ins -> new Instruction(
								switch (ins.color.charAt(6)) {
									case '0' -> Direction.East;
									case '1' -> Direction.South;
									case '2' -> Direction.West;
									case '3' -> Direction.North;
									default -> throw new IllegalStateException("invalid input");
								},
								HexFormat.fromHexDigits(ins.color, 1, 6),
								""
						))
						.toList()
		);
	}

	static String part2(final Input input) {

		// count with shoelace / gauss
		// https://de.wikipedia.org/wiki/Gau%C3%9Fsche_Trapezformel
		var p = Point2D.create(0, 0);
		var sum = 0L;
		var perimeter = 0L;
		for (final var ins : input.instructions) {
			var n = p;
			n = n.add(ins.dir.offset().times(ins.amount));
			perimeter += ins.amount;
			// here we go. yeah, here was an int overflow (hidden, unseen for.. wait.. hours)
			sum += p.getX().longValue() * n.getY() - p.getY().longValue() * n.getX();
			p = n;
		}
		if (!p.equals(Point2D.create(0, 0))) {
			throw new IllegalStateException("invalid state");
		}

		// Gauss
		// 2*A = SUM((x1 * y2) - (x2 * y1))
		final var area = sum / 2;
		// Pick's theorem: A = len(i) + (len(b) / 2) - 1
		// len(i) == filled (searching)
		// len(b) == perimeter
		final var filled = area + 1 + perimeter / 2;

		return "filled amount = %d".formatted(filled);
	}

	static DynGrid<Integer, Boolean> buildGridByInstructions(final List<Instruction> instructions) {
		final var dynGrid = DynGrid.<Integer, Boolean>empty();
		var p = Point2D.create(0, 0);
		dynGrid.setValue(p, true);
		for (final var ins : instructions) {
			for (var i = 0; i < ins.amount; i++) {
				p = p.add(ins.dir.offset());
				dynGrid.setValue(p, true);
			}
		}
		return dynGrid;
	}

	static void fillEnclosedGridAreas(final FixGrid<Boolean> grid) {
		// build reverted map for filling
		final var reverted = FixGrid.copy(grid);
		Stream.of(
					  reverted.fields().topEdge(),
					  reverted.fields().rightEdge(),
					  reverted.fields().bottomEdge(),
					  reverted.fields().leftEdge()
			  )
			  .flatMap(identity())
			  .distinct()
			  .filter(not(grid::getValueRequired))
			  .forEach(fp -> {
				  reverted.fields().getCluster4(fp, not(FixGrid.FieldsView.Field::value))
						  .forEach(c -> reverted.setValue(c, true));
			  });
		// fields left (not filled) what we are looking for
		reverted.fields()
				.stream()
				.filter(not(FixGrid.FieldsView.Field::value))
				.forEach(f -> grid.setValue(f.pos(), true));
	}

	private static void renderGrid(FixGrid<Boolean> grid) {
		System.out.println(grid.toString((_, v) -> {
			if (v) {
				return "#";
			} else {
				return " ";
			}
		}));
	}

}

