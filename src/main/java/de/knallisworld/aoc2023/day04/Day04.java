package de.knallisworld.aoc2023.day04;

import de.knallisworld.aoc2023.support.puzzle.InputParser;
import de.knallisworld.aoc2023.support.puzzle.InputReader;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;

@Log4j2
public class Day04 {

	public static void main(String[] args) {
		printHeader(4);
		printSolution(1, () -> part1(parseCards(InputReader.readInputLines(4, "part1"))));
		printSolution(2, () -> part2(parseCards(InputReader.readInputLines(4, "part1"))));
	}

	static String part1(final List<Card> cards) {
		final var points = cards
				.stream()
				.mapToInt(card -> {
					final var p = getWinCount(card);
					return Double.valueOf(Math.pow(2, p - 1)).intValue();
				})
				.sum();
		return "points = %d".formatted(points);
	}

	static String part2(final List<Card> inputCards) {
		// list of cardNr -> hold/count
		final var stack = new HashMap<Integer, Integer>();
		for (var i = 0; i < inputCards.size(); i++) {
			stack.put(i, 1);
		}

		for (var i = 0; i < inputCards.size(); i++) {
			final var card = inputCards.get(i);
			final var scale = stack.get(i);
			final var power = getWinCount(card);
			for (var j = i + 1; j <= i + power && j < inputCards.size(); j++) {
				stack.put(j, stack.get(j) + scale);
			}
		}
		final var total = stack.values()
							   .stream()
							   .mapToInt(Integer::intValue)
							   .sum();
		return "total = %d".formatted(total);
	}

	static long getWinCount(final Card card) {
		return card
				.wins()
				.stream()
				.filter(i -> card.all().contains(i))
				.count();
	}

	record Card(List<Integer> wins, List<Integer> all) {
	}

	static Card parseCard(final String str) {
		// Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
		var p = str.split("\\|");
		// ignore "Card $n:" for now
		p[0] = p[0].substring(p[0].indexOf(":") + 1).strip();
		p[1] = p[1].strip();
		return new Card(
				InputParser.str2int(p[0], " ").toList(),
				InputParser.str2int(p[1], " ").toList()
		);
	}

	static List<Card> parseCards(final List<String> lines) {
		return lines.stream()
					.map(Day04::parseCard)
					.toList();
	}

}

