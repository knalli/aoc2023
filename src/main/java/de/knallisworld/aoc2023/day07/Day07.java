package de.knallisworld.aoc2023.day07;

import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day07 {

	public static void main(String[] args) {
		printHeader(7);
		printSolution(1, () -> part1(parseInput(readInputLines(7, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(7, "part1"))));
	}

	record Hand(List<Card> cards, long bid) {
	}

	record Card(char c) {

		@Override
		public String toString() {
			return String.valueOf(c);
		}
	}

	static List<Hand> parseInput(final List<String> lines) {
		return lines
				.stream()
				.map(line -> {
					final var parts = line.split(" ");
					return new Hand(
							IntStream.range(0, parts[0].length())
									 .boxed()
									 .map(i -> new Card(parts[0].charAt(i)))
									 .toList(),
							Long.parseLong(parts[1])
					);
				})
				.toList();
	}

	static String part1(final List<Hand> hands) {

		final var cmp = new HandScoringComparator(false)
				.thenComparing(new HandSortingComparator(CardStrengthComparator.INSTANCE));

		final var sorted = hands
				.stream()
				.sorted(cmp)
				.toList();

		var winnings = 0L;
		for (int i = 0; i < sorted.size(); i++) {
			final var rank = i + 1;
			winnings += (sorted.get(i).bid * rank);
		}

		return "hands = %d, winnings = %d".formatted(hands.size(), winnings);
	}

	static String part2(final List<Hand> hands) {

		final var cmp = new HandScoringComparator(true)
				.thenComparing(new HandSortingComparator(CardStrengthJokerAlwaysWeakestComparator.INSTANCE));

		final var sorted = hands
				.stream()
				.sorted(cmp)
				.toList();

		var winnings = 0L;
		for (int i = 0; i < sorted.size(); i++) {
			final var rank = i + 1;
			winnings += (sorted.get(i).bid * rank);
		}

		return "hands = %d, winnings = %d".formatted(hands.size(), winnings);
	}

	static class HandScoringComparator implements Comparator<Hand> {

		final boolean part2;

		public HandScoringComparator(boolean part2) {
			this.part2 = part2;
		}

		@Override
		public int compare(final Hand h1, final Hand h2) {
			final Integer s1 = score(h1);
			final Integer s2 = score(h2);
			return s1.compareTo(s2);
		}

		private Map<Character, Integer> getCountMap(final Hand hand) {
			final var counts = new HashMap<Character, Integer>();
			hand.cards.forEach(card -> counts.put(card.c(), counts.getOrDefault(card.c(), 0) + 1));
			return counts;
		}

		Integer score(final Hand hand) {

			final var allCounts = getCountMap(hand);

			final Map<Character, Integer> counts;
			if (!part2) {
				counts = allCounts;
			} else {
				final int joker = allCounts.getOrDefault('J', 0);
				counts = switch (joker) {
					case 0 -> allCounts;
					case 5 -> Map.of('1', 5);
					default -> {
						final var x = new HashMap<>(allCounts);
						x.remove('J');
						final var max = x
								.entrySet()
								.stream()
								.max(Map.Entry.comparingByValue())
								.map(Map.Entry::getKey)
								.orElseThrow();
						x.put(max, x.get(max) + joker);
						yield x;
					}
				};
			}

			// 7: Five of a kind
			if (counts.containsValue(5)) {
				return 7;
			}

			// 6: Four of a kind
			if (counts.containsValue(4)) {
				return 6;
			}

			// 5: Full house
			if (counts.containsValue(3) && counts.containsValue(2)) {
				return 5;
			}

			// 4: Three of a kind
			if (counts.containsValue(3)) {
				return 4;
			}

			// 3: Two pair
			if (counts.size() == 3 && counts.containsValue(2)) {
				return 3;
			}

			// 2: One pair
			if (counts.containsValue(2)) {
				return 2;
			}

			// 1: High card
			return 1;
		}

	}

	static class CardStrengthComparator implements Comparator<Card> {

		static final Comparator<Card> INSTANCE = new CardStrengthComparator();

		@Override
		public int compare(final Card c1, final Card c2) {
			return getChar(c1) - getChar(c2);
		}

		private static char getChar(final Card c) {
			final var v = c.c();
			return switch (v) {
				// small hack allowing order by char
				case 'T' -> 'A';
				case 'J' -> 'B';
				case 'Q' -> 'C';
				case 'K' -> 'D';
				case 'A' -> 'E';
				default -> v;
			};
		}

	}

	static class CardStrengthJokerAlwaysWeakestComparator implements Comparator<Card> {

		static final Comparator<Card> INSTANCE = new CardStrengthJokerAlwaysWeakestComparator();

		@Override
		public int compare(final Card c1, final Card c2) {
			return getChar(c1) - getChar(c2);
		}

		private static char getChar(final Card c) {
			final var v = c.c();
			return switch (v) {
				// small hack allowing order by char
				case 'T' -> 'A';
				case 'J' -> '1'; // weaker than 2
				case 'Q' -> 'C';
				case 'K' -> 'D';
				case 'A' -> 'E';
				default -> v;
			};
		}

	}

	static class HandSortingComparator implements Comparator<Hand> {

		final Comparator<Card> cardComparator;

		public HandSortingComparator(final Comparator<Card> cardComparator) {
			this.cardComparator = cardComparator;
		}

		@Override
		public int compare(final Hand h1, final Hand h2) {
			return compareValues(
					h1.cards,
					h2.cards
			);
		}

		int compareValues(final List<Card> value,
						  final List<Card> other) {
			final var len1 = value.size();
			final var len2 = other.size();
			final var lim = Math.min(len1, len2);
			for (var k = 0; k < lim; k++) {
				final var c1 = value.get(k);
				final var c2 = other.get(k);
				if (!c1.equals(c2)) {
					return cardComparator.compare(c1, c2);
				}
			}
			return len1 - len2;
		}

	}

}

