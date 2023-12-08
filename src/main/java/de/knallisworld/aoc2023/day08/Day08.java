package de.knallisworld.aoc2023.day08;

import de.knallisworld.aoc2023.support.math.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day08 {

	public static void main(String[] args) {
		printHeader(8);
		printSolution(1, () -> part1(parseInput(readInputLines(8, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(8, "part1"))));
	}

	enum Dir {
		L, R
	}

	record Node(String left, String right) {
	}

	record Input(List<Dir> instructions, Map<String, Node> nodes) {
	}

	static Input parseInput(final List<String> lines) {
		Assert.state(lines.size() > 3, "invalid input size");
		final var line1 = lines.getFirst();
		return new Input(
				IntStream.range(0, line1.length())
						 .boxed()
						 .map(i -> Dir.valueOf(line1.substring(i, i + 1)))
						 .toList(),
				lines.stream()
					 .skip(2)
					 .map(s -> s.split(" = "))
					 .collect(toMap(
							 parts -> parts[0].strip(),
							 parts -> {
								 final var part = parts[1].strip();
								 final var nn = part.substring(1, part.length() - 1).split(", ");
								 return new Node(nn[0].strip(), nn[1].strip());
							 }
					 ))
		);
	}

	static String part1_original(final Input input) {
		final var nextDirGetter = buildDirProvider(input.instructions);
		var node = "AAA";
		int used = 0;
		while (!"ZZZ".equals(node)) {
			used++;
			final var next = input.nodes.get(node);
			node = switch (nextDirGetter.get()) {
				case L -> next.left;
				case R -> next.right;
			};
		}
		return "steps = %d".formatted(used);
	}

	static String part1(final Input input) {
		final var used = findWay(input, "AAA"::equals, n -> n.equals("ZZZ"));
		return "steps = %d".formatted(used);
	}

	static String part2(final Input input) {
		final var used = findWay(input, n -> n.endsWith("A"), n -> n.endsWith("Z"));
		return "steps = %d".formatted(used);
	}

	static long findWay_old(final Input input,
							final Predicate<String> startSelector,
							final Predicate<String> endingSelector) {
		final var workNodes = new ArrayList<String>();
		input.nodes.keySet()
				   .stream()
				   .filter(startSelector)
				   .forEach(workNodes::add);
		long used = 0L;
		while (!workNodes.stream().allMatch(endingSelector)) {
			final var dir = input.instructions.get((int) (used % input.instructions.size()));
			used++;
			for (int i = 0; i < workNodes.size(); i++) {
				final var next = input.nodes.get(workNodes.get(i));
				workNodes.set(i, switch (dir) {
					case L -> next.left;
					case R -> next.right;
				});
			}
		}
		return used;
	}

	static long findWay(final Input input,
						final Predicate<String> startSelector,
						final Predicate<String> endingSelector) {

		final var starts = input.nodes.keySet()
									  .stream()
									  .filter(startSelector)
									  .toList();

		return starts
				.stream()
				.mapToLong(start -> {
					long used = 0L;
					var node = start;
					while (!endingSelector.test(node)) {
						final var dir = input.instructions.get((int) (used % input.instructions.size()));
						used++;
						final var next = input.nodes.get(node);
						node = switch (dir) {
							case L -> next.left;
							case R -> next.right;
						};
					}
					return used;
				})
				// lcm is the king
				// first missed the important info the searches per node will *stop* when reached the destination
				.reduce(1, Utils::lcm);
	}

	static Supplier<Dir> buildDirProvider(final List<Dir> dirs) {
		final var c = new AtomicInteger(0);
		return () -> {
			final var i = c.getAndIncrement();
			return dirs.get(i % dirs.size());
		};
	}

}

