package de.knallisworld.aoc2023.day02;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.lang.StreamUtils.doLog;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.lang.Math.max;

@Log4j2
public class Day02 {

	public static void main(String[] args) {
		printHeader(2);
		printSolution(1, () -> part1(
				readInputLines(2, "part1")
						.stream()
						.map(Day02::parseGame)
						.toList(),
				new Set(14, 13, 12)
		));
		printSolution(2, () -> part2(
				readInputLines(2, "part1")
						.stream()
						.map(Day02::parseGame)
						.toList()
		));
	}

	static String part1(final List<Game> games, final Set constraints) {
		final var filter = findPossibleGames(games, constraints).toList();
		final var count = filter.size();
		final var sum = filter
				.stream()
				.map(doLog((game) -> log.trace("Game " + game.number())))
				.mapToInt(Game::number)
				.sum();
		return "all games = %d, possible games = %d, sum = %d".formatted(games.size(), count, sum);
	}

	static String part2(final List<Game> games) {
		final var power = games
				.stream()
				.mapToInt(game -> {
					final var fewest = game
							.sets()
							.stream()
							.reduce(Set.ZERO, (set1, set2) -> new Set(
									max(set1.blue(), set2.blue()),
									max(set1.green(), set2.green()),
									max(set1.red(), set2.red())
							));
					// power
					return fewest.blue() * fewest.green() * fewest.red();
				})
				.sum();
		return "power = %d".formatted(power);
	}

	static Stream<Game> findPossibleGames(final List<Game> games, final Set constraints) {
		return games
				.stream()
				.filter(game -> game.sets()
									.stream()
									.allMatch(constraints::fits));
	}

	// by accident, earlier solution because I failed to read the instructions
	static Stream<Game> findPossibleGames_VariantSum(final List<Game> games, final Set constraints) {
		return games
				.stream()
				.filter(game -> {
					final var sum = game
							.sets()
							.stream()
							.reduce(Set.ZERO, (set1, set2) -> new Set(
									set1.blue() + set2.blue(),
									set1.green() + set2.green(),
									set1.red() + set2.red()
							));
					return constraints.fits(sum);
				});
	}

	static Game parseGame(final String line) {
		try (final var scanner = new Scanner(line)) {
			Assert.state("Game".equals(scanner.next()), "invalid line");
			final var game = scanner.next();
			final var gameNr = Integer.parseInt(game.substring(0, game.length() - 1));

			record Cube(String color, int amount) {
			}

			return new Game(
					gameNr,
					Arrays.stream(scanner.nextLine().strip().split(";"))
						  .map(String::strip)
						  .map(s -> {
							  final var cubes = Arrays
									  .stream(s.split(","))
									  .map(String::strip)
									  .map(str -> {
										  final var p = str.split(" ");
										  return new Cube(
												  p[1],
												  Integer.parseInt(p[0])
										  );
									  })
									  .toList();
							  // verify
							  if (cubes.stream()
									   .noneMatch(c -> List.of("blue", "green", "red").contains(c.color))) {
								  throw new IllegalStateException("invalid color");
							  }
							  return new Set(
									  cubes.stream()
										   .filter(d -> "blue".equals(d.color()))
										   .findFirst()
										   .map(Cube::amount)
										   .orElse(0),
									  cubes.stream()
										   .filter(d -> "green".equals(d.color()))
										   .findFirst()
										   .map(Cube::amount)
										   .orElse(0),
									  cubes.stream()
										   .filter(d -> "red".equals(d.color()))
										   .findFirst()
										   .map(Cube::amount)
										   .orElse(0)
							  );
						  })
						  .toList()
			);
		}
	}

	record Game(int number, List<Set> sets) {
	}

	record Set(int blue, int green, int red) {

		public static final Set ZERO = new Set(0, 0, 0);

		public boolean fits(final Set o) {
			return o.blue <= blue && o.green <= green && o.red <= red;
		}

	}

}

