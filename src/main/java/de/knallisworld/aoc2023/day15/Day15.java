package de.knallisworld.aoc2023.day15;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputFirstLine;

@Log4j2
public class Day15 {

	public static void main(String[] args) {
		printHeader(15);
		printSolution(1, () -> part1(readInputFirstLine(15, "part1", s -> Arrays.stream(s.split(","))).toList()));
		printSolution(2, () -> part2(readInputFirstLine(15, "part1", s -> Arrays.stream(s.split(","))).toList()));
	}

	static int hashAlg(final String line, final int base) {
		var current = base;
		for (int i = 0; i < line.length(); i++) {
			current = hashAlg(line.charAt(i), current);
		}
		return current;
	}

	static int hashAlg(final char ch, final int current) {
		var result = current;
		result += ch;
		result *= 17;
		result %= 256;
		return result;
	}

	static String part1(final Collection<String> lines) {
		final var sum = lines
				.stream()
				.mapToInt(line -> hashAlg(line, 0))
				.sum();
		return "result = %d".formatted(sum);
	}

	static String part2(final Collection<String> lines) {

		record Lens(String label, int focalLength) {
		}

		final var boxes = new HashMap<Integer, List<Lens>>();

		for (final var line : lines) {
			final var label = line.split("[=\\-]")[0];
			final var ins = line.substring(label.length(), label.length() + 1);

			final var boxIdx = hashAlg(label, 0);
			final var boxContent = boxes.computeIfAbsent(boxIdx, _ -> new ArrayList<>());

			switch (ins) {
				case "-" -> {
					boxContent.removeIf(l -> l.label.equals(label));
				}
				case "=" -> {
					final var focalLength = Integer.parseInt(line.substring(label.length() + 1));
					boolean found = false;
					for (int i = 0; i < boxContent.size(); i++) {
						if (boxContent.get(i).label.equals(label)) {
							found = true;
							boxContent.set(i, new Lens(label, focalLength));
							break;
						}
					}
					if (!found) {
						boxContent.add(new Lens(label, focalLength));
					}
				}
			}
		}

		// focusing power
		final var sum = IntStream
				.range(0, 256)
				.filter(boxes::containsKey)
				.flatMap(boxId -> IntStream
						.range(0, boxes.get(boxId).size())
						.map(slotId -> (boxId + 1) * (slotId + 1) * boxes.get(boxId).get(slotId).focalLength))
				.sum();
		return "result = %d".formatted(sum);
	}

}

