package de.knallisworld.aoc2023.day19;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day19 {

	public static void main(String[] args) throws Exception {
		printHeader(19);
		printSolution(1, () -> part1(parseInput(readInputLines(19, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(19, "part1"))));
	}

	record Input(List<Workflow> workflows, List<Rating> ratings) {
	}

	interface Workflow {

		String name();

		Optional<String> process(Rating rating);

		List<Condition> conditions();

	}

	interface Condition {
		Optional<String> process(Rating rating);

		String raw();

		Full extractFull();

		record Full(String var, String op, int n, String label) {
			@Override
			public String toString() {
				return "%s%s%d:%s".formatted(var, op, n, label);
			}
		}
	}

	record Rating(int x, int m, int a, int s) {
		public Rating max(final Rating other) {
			return new Rating(
					Math.max(x, other.x),
					Math.max(m, other.m),
					Math.max(a, other.a),
					Math.max(s, other.s)
			);
		}

		public Rating min(final Rating other) {
			return new Rating(
					Math.min(x, other.x),
					Math.min(m, other.m),
					Math.min(a, other.a),
					Math.min(s, other.s)
			);
		}
	}

	static Input parseInput(final List<String> lines) {

		return new Input(
				lines.stream()
					 .takeWhile(not(String::isEmpty))
					 .map(line -> {
						 final var name = line.substring(0, line.indexOf('{'));
						 final var parts = Arrays
								 .stream(line.substring(line.indexOf('{') + 1, line.indexOf('}')).split(","))
								 .map(part -> {
									 if (part.contains(":")) {
										 final var split = part.split(":");
										 final var var = split[0].substring(0, 1);
										 final var op = split[0].substring(1, 2);
										 final var n = Integer.parseInt(split[0].substring(2));
										 return (Condition) new Condition() {

											 @Override
											 public Full extractFull() {
												 return new Full(
														 var,
														 op,
														 n,
														 split[1]
												 );
											 }

											 @Override
											 public Optional<String> process(Rating rating) {
												 final var v = switch (var) {
													 case "x" -> rating.x;
													 case "m" -> rating.m;
													 case "a" -> rating.a;
													 case "s" -> rating.s;
													 default -> throw new IllegalStateException("invalid input");
												 };
												 final var result = switch (op) {
													 case "<" -> v < n;
													 case ">" -> v > n;
													 default -> throw new IllegalStateException("invalid input");
												 };
												 if (result) {
													 return Optional.of(split[1]);
												 }
												 return Optional.empty();
											 }

											 @Override
											 public String raw() {
												 return part;
											 }
										 };
									 } else {
										 return (Condition) new Condition() {
											 @Override
											 public Optional<String> process(final Rating rating) {
												 return Optional.of(part);
											 }

											 @Override
											 public String raw() {
												 return part;
											 }

											 @Override
											 public Full extractFull() {
												 throw new IllegalStateException("invalid state");
											 }
										 };
									 }
								 })
								 .toList();
						 return (Workflow) new Workflow() {
							 @Override
							 public String name() {
								 return name;
							 }

							 @Override
							 public List<Condition> conditions() {
								 return parts;
							 }

							 @Override
							 public Optional<String> process(final Rating rating) {
								 return parts
										 .stream()
										 .flatMap(condition -> condition.process(rating).stream())
										 .findFirst();
							 }
						 };
					 })
					 .toList(),
				lines.stream()
					 .dropWhile(not(String::isEmpty))
					 .skip(1)
					 .map(line -> {
						 final var parts = Arrays
								 .stream(line.substring(1, line.length() - 1).split(","))
								 .map(p -> p.split("="))
								 .collect(toMap(p -> p[0], p -> Integer.parseInt(p[1])));
						 return new Rating(
								 parts.get("x"),
								 parts.get("m"),
								 parts.get("a"),
								 parts.get("s")
						 );
					 })
					 .toList()
		);
	}

	static String part1(final Input input) {
		final var accepted = new ArrayList<Rating>();
		final var rejected = new ArrayList<Rating>();
		final var startWorkflow = input.workflows
				.stream()
				.filter(w -> "in".equals(w.name()))
				.findFirst()
				.orElseThrow();
		input.ratings.forEach(rating -> {
			String result;
			var workflow = startWorkflow;
			while (true) {
				result = workflow.process(rating).orElseThrow();
				if ("A".equals(result)) {
					accepted.add(rating);
					break;
				} else if ("R".equals(result)) {
					rejected.add(rating);
					break;
				} else {
					// find next
					for (final var w : input.workflows) {
						if (w.name().equals(result)) {
							workflow = w;
							break;
						}
					}
				}
			}
		});
		final var sum = accepted.stream()
								.mapToLong(r -> r.x + r.m + r.a + r.s)
								.sum();
		return "accepted = %d, rejected = %d, sum = %d".formatted(
				accepted.size(),
				rejected.size(),
				sum
		);
	}

	record Range(Map<String, Integer> min, Map<String, Integer> max) {

		@Override
		public String toString() {
			return Stream
					.of("x", "m", "a", "s")
					.map(k -> "%s=[%d,%d]".formatted(k, min.get(k), max.get(k)))
					.collect(joining(", "));
		}
	}

	record Rule(String label, Range range) {
	}

	static List<Rule> splitRules(final Condition.Full expr, final Range range) {
		final var rules = new ArrayList<Rule>();
		if (range.min.get(expr.var) < expr.n) {
			final var min = new HashMap<>(range.min);
			final var max = new HashMap<>(range.max);
			max.put(expr.var, ">".equals(expr.op) ? expr.n + 1 : expr.n);
			rules.add(new Rule(
					switch (expr.op) {
						case "<" -> range.min.get(expr.var) < expr.n;
						case ">" -> range.min.get(expr.var) > expr.n;
						default -> throw new IllegalStateException("invalid op");
					}
							? expr.label
							: "",
					new Range(min, max)
			));
		}
		if (range.max.get(expr.var) > expr.n) {
			final var min = new HashMap<>(range.min);
			final var max = new HashMap<>(range.max);
			min.put(expr.var, ">".equals(expr.op) ? expr.n + 1 : expr.n);
			rules.add(new Rule(
					switch (expr.op) {
						case "<" -> range.max.get(expr.var) < expr.n;
						case ">" -> range.max.get(expr.var) > expr.n;
						default -> throw new IllegalStateException("invalid op");
					}
							? expr.label
							: "",
					new Range(min, max)
			));
		}
		return rules;
	}

	static long findCombos(final Function<String, Workflow> workflowGetter,
						   final String workflowName,
						   final List<Range> rangeList) {
		var total = 0L;
		final var newRanges = new ArrayList<Rule>();
		for (var condition : workflowGetter.apply(workflowName).conditions()) {
			if (rangeList.isEmpty()) {
				break;
			}
			final var firstRange = rangeList.removeFirst();
			final var split = condition.raw().contains(":")
					? splitRules(condition.extractFull(), firstRange)
					: List.of(new Rule(condition.raw(), firstRange));
			for (var splitItem : split) {
				switch (splitItem.label) {
					case "" -> rangeList.add(splitItem.range);
					case "R" -> {
						// nothing
					}
					case "A" -> total += Stream.of("x", "m", "a", "s")
											   .mapToLong(v -> splitItem.range.max.get(v) - splitItem.range.min.get(v))
											   .reduce(1L, (a, c) -> a * c);
					default -> newRanges.add(splitItem);
				}
			}
		}

		return total + newRanges
				.stream()
				.mapToLong(newRange -> findCombos(
						workflowGetter,
						newRange.label,
						new ArrayList<>(List.of(newRange.range))
				))
				.sum();
	}


	static String part2(final Input input) {
		final var min = new HashMap<String, Integer>();
		final var max = new HashMap<String, Integer>();
		Stream.of("x", "m", "a", "s").forEach(var -> {
			min.put(var, 1);
			max.put(var, 4001);
		});

		final var workflowMap = input.workflows
				.stream()
				.collect(toMap(Workflow::name, identity()));

		final var distinct = findCombos(workflowMap::get, "in", new ArrayList<>(List.of(new Range(min, max))));
		return "distinct = %d".formatted(distinct);
	}

}

