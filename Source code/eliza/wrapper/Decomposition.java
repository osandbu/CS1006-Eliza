package eliza.wrapper;

import java.util.ArrayList;

/**
 * Wrapper for the decomposition rules and associated reassembly rules.
 * 
 * @author Ole & Peter
 * 
 */
public class Decomposition {
	private String rule;
	private StringArrayRandomizer reassembly;

	/**
	 * Creates a new Decomposition object.
	 * 
	 * @param rule
	 *            The decomposition rule.
	 * @param reassembly
	 *            An ArrayList of reassembly rules.
	 */
	public Decomposition(String rule, ArrayList<String> reassembly) {
		this.rule = rule;
		this.reassembly = new StringArrayRandomizer(reassembly);
	}

	public String getRule() {
		return rule;
	}

	/**
	 * Get a random reassembly rule.
	 * 
	 * @return A reassembly rule.
	 */
	public String getReassembly() {
		return reassembly.next();
	}

}
