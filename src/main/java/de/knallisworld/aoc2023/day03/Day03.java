package de.knallisworld.aoc2023.day03;

import de.knallisworld.aoc2023.support.geo.Point2D;
import de.knallisworld.aoc2023.support.geo.grid2.DynGrid;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static de.knallisworld.aoc2023.support.cli.Commons.*;
import static de.knallisworld.aoc2023.support.lang.StreamUtils.doLog;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

@Log4j2
public class Day03 {

	public static void main(String[] args) {
		printHeader(3);
		final var grid = compileObject("Grid", () -> readIntoGrid(readInputLines(3, "part1")));
		printSolution(1, () -> part1(grid.clone()));
		printSolution(2, () -> part2(grid.clone()));
	}

	static DynGrid<Integer, Character> readIntoGrid(final List<String> lines) {
		final var grid = DynGrid.<Integer, Character>empty();
		for (int y = 0; y < lines.size(); y++) {
			final var line = lines.get(y);
			for (int x = 0; x < line.length(); x++) {
				final var value = line.charAt(x);
				Assert.state(value != ' ', "invalid input character");
				if (value != '.') {
					grid.setValue(Point2D.create(x, y), value);
				}
			}
		}
		return grid;
	}

	record Parsed(DynGrid<Integer, Character> symbols,
				  DynGrid<Integer, Character> numbers) {
	}

	static Parsed parse(final DynGrid<Integer, Character> grid) {
		final Predicate<Character> isNumberValue = v -> '0' <= v && v <= '9';
		final Predicate<DynGrid.FieldsView.Field<Integer, Character>> isNumberField = field -> isNumberValue.test(field.value());

		// storing the actual valid numbers separately
		final var numbers = DynGrid.<Integer, Character>empty();
		// storing the actual symbols separately
		final var symbols = DynGrid.<Integer, Character>empty();

		grid.fields()
			.stream()
			.filter(not(isNumberField))
			.forEach(f -> {
				symbols.setValue(f.position(), f.value());
			});

		// for each field in the grid
		symbols.fields()
			   .stream()
			   .forEach(f -> {
				   grid.getAdjacents8(f.position())
					   // must exists
					   .map(aPos -> DynGrid.FieldsView.Field.create(aPos, grid.getValueRequired(aPos)))
					   .filter(isNumberField) // speeding up, maybe already processed
					   .forEach(aField -> {
						   // mark this number (includes all adjacents)
						   final var q = new ArrayDeque<Point2D<Integer>>();
						   q.add(aField.position());
						   while (!q.isEmpty()) {
							   final var t = q.pop();
							   // copy value to valid-grid
							   numbers.setValue(t, grid.getValueRequired(t));
							   // mark value as removed
							   grid.clearValue(t);
							   // add all of this point's adjacents to the queue
							   grid.getAdjacents4(t)
								   .filter(t2 -> isNumberValue.test(grid.getValueRequired(t2)))
								   .forEach(q::add);
						   }
					   });
			   });

		return new Parsed(symbols, numbers);
	}

	static String part1(final DynGrid<Integer, Character> grid) {

		final var parsed = parse(grid);

		final var numbers =
				parsed.numbers().fields()
					  .rows()
					  .flatMap(row -> {
						  record Wrapper(int position, String value) {
						  }
						  return Arrays.stream(
											   row.fields()
												  .stream()
												  .map(f -> new Wrapper(
														  f.position().getX(),
														  f.value()
														   .map(Object::toString)
														   .orElse(" ")
												  ))
												  .sorted(comparing(Wrapper::position))
												  .map(Wrapper::value)
												  .collect(joining())
												  .split(" ")
									   )
									   .filter(StringUtils::hasText);
					  })
					  .map(doLog(s -> log.trace("found: %s".formatted(s))))
					  .map(Integer::parseInt)
					  .toList();

		final var sum = numbers
				.stream()
				.mapToInt(s -> s)
				.sum();

		return "len(numbers) = %d, sum = %d".formatted(numbers.size(), sum);
	}

	static String part2(final DynGrid<Integer, Character> grid) {

		final var parsed = parse(grid);

		final var gears = parsed
				.symbols()
				.fields()
				.stream()
				// filter for gears only
				.filter(f -> f.value() == '*')
				.map(symbolField -> {
					final var symbolCloud = parsed
							.numbers()
							.getAdjacents8(symbolField.position(), true)
							.toList();
					record Wrapper(Point2D<Integer> p, Set<Point2D<Integer>> cloud) {
					}
					return parsed
							.numbers()
							.getAdjacents8(symbolField.position())
							.map(a -> new Wrapper(
									a,
									parsed.numbers()
										  .fields().groupInRow(a)
										  .map(DynGrid.FieldsView.Field::position)
										  .filter(symbolCloud::contains)
										  .collect(toSet())
							))
							.collect(groupingBy(Wrapper::cloud))
							.values()
							.stream()
							// grouped by the cloud, use only first point of it
							.flatMap(s -> s.stream().findFirst().stream())
							.map(Wrapper::p)
							.toList();
				})
				// filter for exactly 2 different points
				.filter(s -> s.size() == 2)
				.toList();

		final var numbers = gears
				.stream()
				.map(pp -> {
					Assert.state(pp.size() == 2, "must be 2 points always");
					final var nn = pp
							.stream()
							.map(p -> Integer.parseInt(
									parsed.numbers()
										  .fields()
										  .groupInRow(p)
										  .map(s -> s.value().toString())
										  .collect(joining())
							))
							.toList();
					return nn.get(0) * nn.get(1);
				})
				.toList();

		final var sum = numbers
				.stream()
				.mapToInt(Integer::intValue)
				.sum();

		return "gears = %d, sum = %d".formatted(gears.size(), sum);
	}

}

