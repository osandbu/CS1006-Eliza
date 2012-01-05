package eliza.methods;

import java.util.ArrayList;

import eliza.wrapper.Keyword;

/**
 * A class containing the random number generator methods used by some of the
 * other classes, most notably eliza.Engine.
 * 
 * @author Ole & Peter
 * 
 */
public class RandomMethods {

	/**
	 * Return a random element of an ArrayList of Strings.
	 * 
	 * @param list
	 *            An ArrayList of Strings.
	 * @return A random String from the list.
	 */
	public static String random(ArrayList<String> list) {
		return list.get(random(list.size()));
	}

	/**
	 * Return a random element of an ArrayList of Keywords.
	 * 
	 * @param list
	 *            An ArrayList of Keywords.
	 * @return A random Keywords from the list.
	 */
	public static Keyword random(ArrayList<Keyword> list) {
		return list.get(random(list.size()));
	}

	/**
	 * Returns a random number in the range 0 to max - 1.
	 * 
	 * @param max
	 *            An integer.
	 * @return A random number in the range 0 to max - 1.
	 */
	public static int random(int max) {
		return random(0, max);
	}

	/**
	 * Returns a number in the range min to max - 1.
	 * 
	 * @param min
	 *            An integer.
	 * @param max
	 *            An integer.
	 * @return a number in the range min to max - 1.
	 */
	public static int random(int min, int max) {
		return (int) ((max - min) * Math.random()) + min;
	}
}
