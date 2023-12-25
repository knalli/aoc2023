package de.knallisworld.aoc2023.day25;

import lombok.extern.log4j.Log4j2;
import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;

import java.util.*;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.function.Predicate.not;

@Log4j2
public class Day25 {

	public static void main(String[] args) {
		printHeader(25);
		// expect 54
		printSolution(1, () -> part1(parseInput(readInputLines(25, "part0"))));
		// expect 567606
		printSolution(1, () -> part1(parseInput(readInputLines(25, "part1"))));
	}

	static Map<String, Set<String>> parseInput(final List<String> lines) {
		final var result = new HashMap<String, Set<String>>();
		lines.forEach(line -> {
			final var p = line.split(": ");
			final var s0 = p[0].strip();
			Arrays.stream(p[1].split(" "))
				  .forEach(s1 -> {
					  result.computeIfAbsent(s0, _ -> new HashSet<>())
							.add(s1);
					  result.computeIfAbsent(s1, _ -> new HashSet<>())
							.add(s0);
				  });
		});
		// ensure immutable
		result.keySet().forEach(k -> result.put(k, Set.copyOf(result.get(k))));
		return Map.copyOf(result);
	}

	record Pair<T>(T a, T b) {
	}

	static String randomString() {
		return "" + Math.round(Math.random() * 100000000);
	}

	static Pseudograph<Object, String> buildGraphNetwork(Map<String, Set<String>> wires) {
		final var graph = Pseudograph.createBuilder(String.class);
		wires.forEach((key, values) -> {
			values.forEach(value -> graph.addEdge(key, value, randomString()));
		});
		return graph.build();
	}

	// https://en.wikipedia.org/wiki/Karger%27s_algorithm
	private static Pair<Integer> computeMinCut(final Map<String, Set<String>> wires,
											   Pseudograph<Object, String> from) {
		final var G = (Graph<String, String>) from.clone();
		final var map = new HashMap<String, Set<String>>();
		wires.keySet().forEach(k -> map.put(k, Set.of(k)));
		while (G.vertexSet().size() > 2) {
			final var edge = G.edgeSet().iterator().next();
			final var u = G.getEdgeSource(edge);
			final var v = G.getEdgeTarget(edge);
			final var uv = "%s-%s".formatted(u, v);
			final var ss = new HashSet<String>();
			ss.addAll(map.get(u));
			ss.addAll(map.get(v));
			map.put(uv, Set.copyOf(ss));
			G.addVertex(uv);

			final var edgesToRemove = new HashSet<String>();
			edgesToRemove.addAll(G.edgesOf(u));
			edgesToRemove.addAll(G.edgesOf(v));
			Set.copyOf(G.edgesOf(u))
			   .stream()
			   .map(wu -> {
				   if (G.getEdgeTarget(wu).equals(u)) {
					   return G.getEdgeSource(wu);
				   } else {
					   return G.getEdgeTarget(wu);
				   }
			   })
			   .filter(not(v::equals))
			   .forEach(w -> {
				   G.addEdge(w, uv, randomString());
			   });
			Set.copyOf(G.edgesOf(v))
			   .stream()
			   .map(wv -> {
				   if (G.getEdgeTarget(wv).equals(v)) {
					   return G.getEdgeSource(wv);
				   } else {
					   return G.getEdgeTarget(wv);
				   }
			   })
			   .filter(not(u::equals))
			   .forEach(w -> {
				   G.addEdge(w, uv, randomString());
			   });
			G.removeAllVertices(List.of(u, v));
		}

		final var a = G.vertexSet().stream().skip(0).findFirst().orElseThrow();
		final var b = G.vertexSet().stream().skip(1).findFirst().orElseThrow();
		return new Pair<>(map.get(a).size(), map.get(b).size());
	}

	static String part1(final Map<String, Set<String>> wires) {

		final var g = buildGraphNetwork(wires);
		final var p = computeMinCut(wires, g);

		// expect 567606
		return "len = %d, group1 = %d, group1 = %d, prod = %d".formatted(
				wires.size(),
				p.a,
				p.b,
				p.a * p.b
		);
	}

}

