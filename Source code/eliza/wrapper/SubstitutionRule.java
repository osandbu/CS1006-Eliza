package eliza.wrapper;

/**
 * SubstitutionRule is a wrapper class which holds information about pre- and
 * post-substitution rules.
 * 
 * @author Ole & Peter
 */
public class SubstitutionRule {
	private String find;
	private String replacement;

	/**
	 * Constructor which constructs a new substitution rule.
	 * 
	 * @param find
	 *            The phrase to look for in a string.
	 * @param replacement
	 *            The replacement phrase.
	 */
	public SubstitutionRule(String find, String replacement) {
		this.find = find;
		this.replacement = replacement;
	}

	public String getFind() {
		return find;
	}

	public String getReplacement() {
		return replacement;
	}
}
