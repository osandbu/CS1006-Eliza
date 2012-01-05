package eliza.wrapper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Keyword class is mostly used as a wrapper, containing information about
 * the keyword itself, its priority and decomposition rules. It is also used to
 * check if a keyword matches an input sentence.
 * 
 * @author Ole & Peter
 */
public class Keyword implements Comparable<Keyword> {
	private String keyword;
	private int priority;
	private ArrayList<Decomposition> decomp;

	/**
	 * Constructor which initialized a new Keyword object with a given keyword,
	 * priority and a set of decomposition rules.
	 * 
	 * @param keyword
	 *            The term to look for in input strings.
	 * @param priority
	 *            Integer representation of the priority of this keyword (1 is
	 *            max, 10 is min).
	 * @param decomp
	 *            An array of decomposition rules.
	 */
	public Keyword(String keyword, int priority, ArrayList<Decomposition> decomp) {
		this.keyword = keyword;
		this.priority = priority;
		this.decomp = decomp;
	}

	public String getKeyword() {
		return keyword;
	}

	public String toString() {
		return keyword;
	}

	public ArrayList<Decomposition> getDecomp() {
		return decomp;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Compare this keyword with another Keyword. Used for sorting purposes.
	 * 
	 * @see Comparable#compareTo(Keyword)
	 */
	public int compareTo(Keyword other) {
		return this.priority - other.priority;
	}

	public boolean matches(String sentence) {
		if (!Pattern.compile("\\b" + keyword + "\\b").matcher(sentence).find())
			return false;
		for (Decomposition d : getDecomp()) {
			String rule = d.getRule();
			rule = formatDecomposition(rule);
			Matcher m = Pattern.compile(rule).matcher(sentence);
			if (m.find())
				return true;
		}
		return false;
	}

	/**
	 * Format the decomposition rule so that it can be used in a regular
	 * expression.
	 * 
	 * @param rule
	 *            A decomposition rule.
	 * @return A regex-compatible decomposition rule.
	 */
	public String formatDecomposition(String rule) {
		rule = rule.toLowerCase();
		// $ refers to synonyms
		rule = rule.replace("$", getKeyword());
		// star refers to any number of characters
		rule = rule.replace("*", "(.*)");
		// replace spaces with whitespace
		rule = rule.replace(" ", "\\s");
		// the rule refers to the whole input sentence
		return "^" + rule + "$";
	}
}
