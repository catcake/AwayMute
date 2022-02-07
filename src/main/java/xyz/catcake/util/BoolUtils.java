package xyz.catcake.util;

public enum BoolUtils {

	BOOL;

	public boolean ParseIntStrict(final int n) throws IllegalArgumentException {
		if (n == 1) return true;
		if (n == 0) return false;
		throw new IllegalArgumentException(String.format(
				"argument outside of range. actual: (%d) expected: [0,1]", n));
	}
}