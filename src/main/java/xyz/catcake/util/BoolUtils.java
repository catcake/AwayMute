package xyz.catcake.util;

public final class BoolUtils {
	public static boolean ParseIntStrict(final int n) throws IllegalArgumentException {
		if (n == 1) return true;
		if (n == 0) return false;
		throw new IllegalArgumentException(String.format("actual: (%d) expected: [0,1]", n));
	}
}