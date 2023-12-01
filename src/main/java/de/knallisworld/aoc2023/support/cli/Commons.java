package de.knallisworld.aoc2023.support.cli;

import java.io.Serializable;
import java.util.function.Supplier;

public class Commons {

	static final int YEAR = 2023;

	public static void printHeader(long day) {
		System.out.println("#".repeat(21));
		System.out.printf("# 🎄AOC %d day %02d #%n", YEAR, day);
		System.out.println("#".repeat(21));
		System.out.println();
	}

	public static void printSolution(final Serializable part,
									 final Supplier<Serializable> solutionGet) {
		System.out.printf("🎉 Solution of part %s: %s%n", part, solutionGet.get());
	}

}
