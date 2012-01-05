package eliza.wrapper;

import java.util.ArrayList;
import java.util.Collections;

import eliza.methods.RandomMethods;

/**
 * The ArrayRandomizer stores an array of strings and has a next() method which
 * selects a random string from the array, but does not return the same string
 * twice until all strings in the array have been returned.
 * 
 * @author Ole & Peter
 * 
 */
public class StringArrayRandomizer {

	private ArrayList<String> values;
	private int max;

	public StringArrayRandomizer(ArrayList<String> arr) {
		values = arr;
		max = arr.size() - 1;
	}

	/**
	 * Selects a random string from the array, but does not return the same
	 * string twice until all strings in the array have been returned.
	 * Precondition: unused array contains at least one element.
	 * 
	 * @return A randomly selected string from the array.
	 */
	public String next() {
		int i = RandomMethods.random(max);
		String result = values.get(i);
		// swap elements in index i and max.
		Collections.swap(values, i, max);
		if (max == 0)
			max = values.size();
		max--;
		return result;
	}
}
