package de.knallisworld.aoc2023.day12;

import de.knallisworld.aoc2023.support.puzzle.InputParser;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.Objects.requireNonNull;

@Log4j2
public class Day12 {

	public static void main(String[] args) {
		printHeader(12);
		printSolution(1, () -> part1(parseInput(readInputLines(12, "part1"))));
		printSolution(2, () -> part2(unfoldOrMakeItMoreComplex(parseInput(readInputLines(12, "part1")))));
	}

	enum Type {
		Unknown,
		Operational,
		Damaged;

		@Override
		public String toString() {
			return switch (this) {
				case Unknown -> "?";
				case Damaged -> "#";
				case Operational -> ".";
			};
		}
	}

	record SpringRow(List<Type> records, List<Integer> groups) {
	}

	static List<SpringRow> parseInput(final List<String> lines) {
		return lines
				.stream()
				.map(str -> str.split(" ", 2))
				.map(parts -> new SpringRow(
						IntStream
								.range(0, parts[0].length())
								.boxed()
								.map(i -> switch (parts[0].charAt(i)) {
									case '?' -> Type.Unknown;
									case '.' -> Type.Operational;
									case '#' -> Type.Damaged;
									default -> throw new IllegalStateException("invalid input");
								})
								.toList(),
						InputParser.str2int(parts[1], ",").toList()
				))
				.toList();
	}

	static List<SpringRow> unfoldOrMakeItMoreComplex(final List<SpringRow> input) {
		return input.stream()
					.map(row -> {
						final var moreRecords = new ArrayList<Type>();
						final var moreGroups = new ArrayList<Integer>();
						IntStream.range(0, 5)
								 .forEach(n -> {
									 if (n > 0) {
										 moreRecords.add(Type.Unknown);
									 }
									 moreRecords.addAll(row.records);
									 moreGroups.addAll(row.groups);
								 });
						return new SpringRow(
								List.copyOf(moreRecords),
								List.copyOf(moreGroups)
						);
					})
					.toList();
	}

	static String part1(final List<SpringRow> input) {
		final var sum = input.stream()
							 .mapToLong(row -> Part1.findVariations(row.records, row.groups).count())
							 .sum();
		return "sum = %d".formatted(sum);
	}

	static String part2(final List<SpringRow> input) {
		final var sum = input.stream()
							 .parallel()
							 .mapToLong(row -> new Part2b().count(row.records, row.groups))
							 .sum();
		return "sum = %d".formatted(sum);
	}

	// helper
	static <T> List<T> sublist(final List<T> input, final int beginIndex, final int endIndex) {
		return input.stream()
					.skip(beginIndex)
					.limit(endIndex - beginIndex)
					.toList();
	}

	static class Part1 {

		static Stream<List<Type>> findVariations(final List<Type> records, final List<Integer> groups) {
			final var result = new Variations<Type>();
			return result.findVariations(result, records)
						 .filter(v -> result.isCandidate(v, groups));
		}

		static class Variations<T> {

			record Wrapper<T>(List<List<T>> items) {
			}

			private final List<Wrapper<T>> data = new ArrayList<>();

			public void append(final List<List<T>> appends) {
				data.add(new Wrapper<>(appends));
			}

			public Stream<List<T>> stream() {
				final var sizes = IntStream.range(0, data.size())
										   .boxed()
										   .map(i -> data.get(i).items.size())
										   .toList();
				return permuteIndices(sizes, 0)
						.map(indices -> {
							final var list = new ArrayList<T>();
							for (int i = 0; i < indices.size(); i++) {
								list.addAll(data.get(i).items.get(indices.get(i)));
							}
							return list;
						});
			}

			// create index permutation avoiding computing temp list segments
			static Stream<List<Integer>> permuteIndices(final List<Integer> in, final int pos) {
				return IntStream
						.range(0, in.get(pos))
						.boxed()
						.flatMap(n -> {
							final var base = new ArrayList<Integer>();
							base.add(n);
							if (pos == in.size() - 1) {
								return Stream.of(base);
							}
							return permuteIndices(in, pos + 1)
									.map(append -> {
										final var t = new ArrayList<>(base);
										t.addAll(append);
										return t;
									});
						});
			}

			Stream<List<Type>> findVariations(final Variations<Type> result, final List<Type> input) {
				var start = 0;
				for (var i = start; i < input.size(); i++) {
					if (input.get(i) == Type.Operational) {
						continue;
					}
					if (start < i) {
						result.append(List.of(sublist(input, start, i)));
					}
					start = i;
					var end = i + 1;
					for (; end < input.size(); end++) {
						if (input.get(end) == Type.Operational) {
							break;
						}
					}
					result.append(findOptions(sublist(input, start, end)));
					i = end;
					start = end;
				}
				return result.stream();
			}

			record FindOptionKey(int records) {
			}

			final Map<FindOptionKey, List<List<Type>>> findOptionsCache = new HashMap<>();

			List<List<Type>> findOptions(final List<Type> input) {
				return findOptionsCache.computeIfAbsent(new FindOptionKey(input.hashCode()), _ -> {
					final var options = new ArrayList<List<Type>>();

					record Item(int pos, List<Type> records) {
					}
					final var q = new LinkedList<Item>();
					q.add(new Item(0, List.of()));
					while (!q.isEmpty()) {
						final var n = q.pop();
						if (n.pos == input.size()) {
							options.add(List.copyOf(n.records));
							continue;
						}
						final var v = input.get(n.pos);
						if (v == Type.Damaged) {
							final var e1 = new Item(n.pos + 1, new ArrayList<>(n.records));
							e1.records.add(Type.Damaged);
							q.add(e1);
						} else if (v == Type.Unknown) {
							final var e1 = new Item(n.pos + 1, new ArrayList<>(n.records));
							e1.records.add(Type.Damaged);
							q.add(e1);
							final var e2 = new Item(n.pos + 1, new ArrayList<>(n.records));
							e2.records.add(Type.Operational);
							q.add(e2);
						}
					}
					return List.copyOf(options);
				});
			}

			record FindCandidateKey(int records, int groups) {
			}

			final Map<FindCandidateKey, Boolean> isCandidateCache = new HashMap<>();

			boolean isCandidate(final List<Type> search, final List<Integer> groups) {
				return isCandidateCache.computeIfAbsent(new FindCandidateKey(search.hashCode(), groups.hashCode()), _ -> {
					var pos = 0;
					for (final var groupLength : groups) {
						if (pos == search.size()) {
							return false;
						}
						while (search.get(pos) != Type.Damaged) {
							pos++;
							if (pos == search.size()) {
								return false;
							}
						}
						final var start = pos;
						while (pos < search.size() && search.get(pos) == Type.Damaged) {
							pos++;
						}
						if (pos - start != groupLength) {
							return false;
						}
					}
					// check: no group left
					for (; pos < search.size(); pos++) {
						if (search.get(pos) == Type.Damaged) {
							return false;
						}
					}

					return true;
				});
			}
		}

	}

	// https://raw.githubusercontent.com/Oupsman/AOC2023/main/d12/advent_12.go
	// Simple & clean code, very understandable.
	static class Part2b {

		final Map<String, Long> cache = new HashMap<>();

		long count(final List<Type> records, final List<Integer> group) {
			final var key = records.toString() + "_" + group.toString();
			if (cache.containsKey(key)) {
				return cache.get(key);
			}
			if (records.isEmpty()) {
				if (group.isEmpty()) {
					return 1;
				}
				return 0;
			}
			return switch (requireNonNull(records.getFirst())) {
				case Unknown -> {
					yield count(
							Stream.concat(
									Stream.of(Type.Operational),
									records.stream().skip(1)
							).toList(),
							group
					) + count(
							Stream.concat(
									Stream.of(Type.Damaged),
									records.stream().skip(1)
							).toList(),
							group
					);
				}
				case Operational -> {
					final var r = count(
							records.stream().dropWhile(i -> i == Type.Operational).toList(),
							group
					);
					cache.put(key, r);
					yield r;
				}
				case Damaged -> {
					if (group.isEmpty()) {
						final var r = 0L;
						cache.put(key, r);
						yield r;
					}
					if (records.size() < group.getFirst()) {
						final var r = 0L;
						cache.put(key, r);
						yield r;
					}
					if (records.stream().limit(group.getFirst()).anyMatch(i -> i == Type.Operational)) {
						final var r = 0L;
						cache.put(key, r);
						yield r;
					}
					if (group.size() > 1) {
						if (records.size() < group.getFirst() + 1 || records.get(group.getFirst()) == Type.Damaged) {
							final var r = 0L;
							cache.put(key, r);
							yield r;
						}
						final var r = count(
								records.subList(group.getFirst() + 1, records.size()),
								group.subList(1, group.size())
						);
						cache.put(key, r);
						yield r;
					} else {
						final var r = count(
								records.subList(group.getFirst(), records.size()),
								group.subList(1, group.size())
						);
						cache.put(key, r);
						yield r;
					}
				}
			};
		}

	}

	static class Part2_ProbalyWorking_ButNoCaching {

		record Range(int begin, int end) {

			public boolean contains(int i) {
				return begin <= i && i < end;
			}

			public int length() {
				return end - begin;
			}

			@Override
			public String toString() {
				return "[%d,%d[".formatted(begin, end);
			}
		}

		record Context(List<Type> records,
					   List<Integer> damagedRecordIndices,
					   List<Integer> groups) {
		}

		long count(final List<Type> records, final List<Integer> groups) {
			return count(
					new Context(
							records,
							IntStream.range(0, records.size())
									 .filter(i -> records.get(i) == Type.Damaged)
									 .boxed()
									 .toList(),
							groups
					),
					0,
					0,
					List.of()
			);
		}

		record Key(int a, int b, int c) {
		}

		final Map<String, Long> cache = new HashMap<>();

		// https://pastebin.com/GWL1tqLD
		long count(final Context context,
				   final int recordStartIndex,
				   final int groupIndex,
				   final List<Range> ranges) {

			//System.out.println("%d %d %s".formatted(recordStartIndex, groupIndex, ranges.toString()));
			if (groupIndex == context.groups.size()) {
				return 0;
			}

			final var key = "%s,%s".formatted(
					context.records.subList(recordStartIndex, context.records.size()),
					context.groups.subList(groupIndex, context.groups.size())
			);
			if (cache.containsKey(key)) {
				return cache.get(key);
			}

			// between each group one space (operational) required
			final int requiredMinLength;
			{
				final var groupsSummed = sublist(context.groups, groupIndex, context.groups.size())
						.stream()
						.mapToInt(Integer::intValue)
						.sum();
				requiredMinLength = groupsSummed + context.groups.size() - groupIndex - 1;
			}
			final var rightBound = context.records.size() - requiredMinLength;
			// won't fit anymore...
			if (recordStartIndex > rightBound) {
				return 0;
			}

			final var recordEndIndex = recordStartIndex + context.groups.get(groupIndex);
			final var maybe = sublist(context.records, recordStartIndex, recordEndIndex);

			// only if this a whole group enclosed only with operational items
			var options = 0L;
			if (maybe.stream().allMatch(i -> i == Type.Damaged || i == Type.Unknown)) {
				if (recordStartIndex == 0 || context.records.get(recordStartIndex - 1) != Type.Damaged) {
					if (recordEndIndex == context.records.size() || context.records.get(recordEndIndex) != Type.Damaged) {
						final var possibleRanges = new ArrayList<>(ranges);
						possibleRanges.add(new Range(recordStartIndex, recordEndIndex));

						// last condition
						if (groupIndex == context.groups.size() - 1) {
							final var rangeMatchingDamaged = context
									.damagedRecordIndices
									.stream()
									.allMatch(i -> possibleRanges.stream().anyMatch(range -> range.contains(i)));
							if (rangeMatchingDamaged) {
								options += 1;
							}
						} else {
							options += count(context, recordEndIndex + 1, groupIndex + 1, List.copyOf(possibleRanges));
						}
					}
				}
			}

			final var result = options + count(context, recordStartIndex + 1, groupIndex, ranges);
			cache.put(key, result);
			return result;
		}

	}

}

