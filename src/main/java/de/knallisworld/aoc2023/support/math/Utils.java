package de.knallisworld.aoc2023.support.math;

public class Utils {

	public static long lcm(long number1, long number2) {
		if (number1 == 0 || number2 == 0) {
			return 0;
		}
		var absNumber1 = Math.abs(number1);
		var absNumber2 = Math.abs(number2);
		var absHigherNumber = Math.max(absNumber1, absNumber2);
		var absLowerNumber = Math.min(absNumber1, absNumber2);
		var lcm = absHigherNumber;
		while (lcm % absLowerNumber != 0) {
			lcm += absHigherNumber;
		}
		return lcm;
	}

}
