package eliza;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eliza.exception.FileFormatException;
import eliza.methods.RandomMethods;
import eliza.wrapper.StringArrayRandomizer;
import eliza.wrapper.Decomposition;
import eliza.wrapper.Keyword;
import eliza.wrapper.SubstitutionRule;

/**
 * The Engine class is contains the Eliza engine. It requires a script to run,
 * and is used to generate responses to input strings. This is done by applying
 * pre-substitution rules, scanning for keywords and applying the most
 * appropriate decomposition rule.
 * 
 * @author Ole & Peter
 * 
 */
public class Engine {
	private static final String SPACE_UNDERSCORE_STRING = " _";
	private static final String UNDERSCORE_STRING = "_";
	private static final String TWO_STRING = "2";
	private static final String ONE_STRING = "1";
	// Character used in decomposition rules signifying the keyword.
	private static final String KEYWORD_SYNONYM_CHARACTER = "$";
	private static final String SENTENCE_SEPARATOR_REGEX = "[.,:;!?] *";
	private static final String EMPTY_STRING = "";
	private static final String FILTER_REGEX = "[\\W_&&[^ -'.,:;!?]]";
	// Regular expression for word boundary.
	private static final String WORD_BOUNDARY = "\\b";
	// String containing a space.
	private static final String SPACE = " ";
	// Regular expression for two or more spaces.
	private static final String DOUBLE_SPACE_REGEX = " {2,}";
	// Messages Eliza uses to start the conversation.
	private ArrayList<String> welcomeMessages = new ArrayList<String>();
	/*
	 * Messages Eliza uses after it recognizes a quit command (which terminates
	 * the conversation).
	 */
	private ArrayList<String> finalMessages = new ArrayList<String>();
	/*
	 * A list of pre-substitution rules which are applied before checking for
	 * keywords.
	 */
	private ArrayList<SubstitutionRule> presub = new ArrayList<SubstitutionRule>();
	/*
	 * A list of post-substitution rules which are applied during application of
	 * reassembly rules.
	 */
	private ArrayList<SubstitutionRule> postsub = new ArrayList<SubstitutionRule>();
	/*
	 * A list of keywords which are used as the basis for transforming the input
	 * sentence and creating an output sentence.
	 */
	private ArrayList<Keyword> keywords = new ArrayList<Keyword>();
	/*
	 * A list of responses which are used when no applicable keywords are found
	 * in an input phrase.
	 */
	private StringArrayRandomizer otherResponses;
	// A list of commands which Eliza recognises as conversation terminators.
	private ArrayList<String> quitCommands = new ArrayList<String>();

	/*
	 * Used to tell if the conversation has been terminated using a quit
	 * command.
	 */
	private boolean alive = true;

	/**
	 * Read welcome messages and store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	public Engine(String filename) throws FileFormatException {

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			// skip first line
			in.readLine();
			readWelcomeMessages(in);
			readFinalMessages(in);
			readPreSubstitutionRules(in);
			readPostSubstitutionRules(in);
			readKeywords(in);
			// sort keywords by priority
			sort(keywords, 0, keywords.size() - 1);
			readOtherResponses(in);
			readQuitCommands(in);
			// close script file.
			in.close();
		} catch (FileNotFoundException e) {
			System.err
					.println("ERROR: Script file is not in the specified location.");
			System.exit(0);
		} catch (IOException e) {
			System.err.println("ERROR reading script file:\n" + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Read welcome messages and store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	private void readWelcomeMessages(BufferedReader in) throws IOException {
		String line;
		while ((line = in.readLine()) != null && !line.equals(";Final")) {
			welcomeMessages.add(line);
		}
	}

	/**
	 * Read final messages and store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	private void readFinalMessages(BufferedReader in) throws IOException {
		String line;
		while ((line = in.readLine()) != null && !line.equals(";Pre")) {
			finalMessages.add(line);
		}
	}

	/**
	 * Read pre-substitution rules store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	private void readPreSubstitutionRules(BufferedReader in)
			throws IOException, FileFormatException {
		String line;
		while ((line = in.readLine()) != null && !line.equals(";Post")) {
			String[] split = line.split("\t");
			if (split.length != 2) {
				String msg = "Problem in line:\n" + line;
				throw new FileFormatException(msg);
			}
			presub.add(new SubstitutionRule(split[0], split[1]));
		}
	}

	/**
	 * Read post-substitution rules and store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	private void readPostSubstitutionRules(BufferedReader in)
			throws IOException, FileFormatException {
		String line;
		while ((line = in.readLine()) != null && !line.equals(";Keywords")) {
			String[] split = line.split("\t");
			if (split.length != 2) {
				String msg = "Problem in line:\n" + line;
				throw new FileFormatException(msg);
			}
			postsub.add(new SubstitutionRule(split[0], split[1]));
		}
	}

	/**
	 * Read keywords and store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	private void readKeywords(BufferedReader in) throws IOException,
			FileFormatException {
		String line;
		String[] keysAndP = null;
		String[] decompRules = null;
		ArrayList<Decomposition> decomp = null;
		ArrayList<String> reass = null;
		while ((line = in.readLine()) != null) {
			if (line.startsWith("k:") || line.equals(";Other")) {
				if (keysAndP != null) {
					if (decompRules != null && !reass.isEmpty())
					{
						createDecomps(decompRules, decomp, reass);
						decompRules = null;
					}
					addKeys(keysAndP, decomp);
					if (line.equals(";Other")) {
						// break out of while loop
						break;
					}
				}
				line = line.substring(2);
				keysAndP = line.split(SPACE);
				decomp = new ArrayList<Decomposition>();
			} else if (line.startsWith("d:")) {
				if (decompRules != null && !reass.isEmpty())
				{
					createDecomps(decompRules, decomp, reass);
					decompRules = null;
				}
				/*
				 * Remove the "d:" and any whitespace before and after the
				 * string and split it around the slashes.
				 */
				decompRules = line.substring(2).trim().split("/");
				reass = new ArrayList<String>();
			} else if (line.startsWith("r:")) {
				line = line.substring(2);
				reass.add(line);
			}
		}
	}

	/**
	 * Create decomposition objects with given decomposition rules and
	 * reassembly rules, and add them to decomp ArrayList.
	 * 
	 * @param decompRules
	 *            An array of Strings containing decomposition rules.
	 * @param decomp
	 *            An ArrayList of Decomposition rules to which the rules are to
	 *            be added.
	 * @param reass
	 *            An ArrayList of Strings containing reassembly rules.
	 */
	private void createDecomps(String[] decompRules,
			ArrayList<Decomposition> decomp, ArrayList<String> reass) {
		for (String rule : decompRules) {
			Decomposition d = new Decomposition(rule, reass);
			decomp.add(d);
		}
	}

	/**
	 * Parse the priority of a list of keywords and report any errors as
	 * appropriate.
	 * 
	 * @param keysAndP
	 *            An array of strings. The last element should contain the
	 *            priority while the others contain keywords.
	 * @return The priority.
	 * @throws FileFormatException
	 */
	private int parsePriority(String[] keysAndP) throws FileFormatException {
		int priority = 0;
		int index = keysAndP.length - 1;
		try {
			priority = Integer.parseInt(keysAndP[index]);
		} catch (NumberFormatException e) {
			throw new FileFormatException(
					"Script file not formatted correctly. "
							+ "Problem in line with keyword:\n" + keysAndP[0],
					e);
		}
		return priority;
	}

	/**
	 * Adds keywords with same priority and decomposition/reassembly rules to
	 * keywords array.
	 * 
	 * @param keysAndP
	 *            Array of keywords, last element is expected to contain the
	 *            priority and should be ignored.
	 * @param priority
	 *            The priority of this keyword
	 * @param decomp
	 * @throws FileFormatException
	 */
	private void addKeys(String[] keysAndP, ArrayList<Decomposition> decomp)
			throws FileFormatException {
		int priority = parsePriority(keysAndP);
		for (int i = 0; i < keysAndP.length - 1; i++) {
			Keyword key = new Keyword(keysAndP[i], priority, decomp);
			keywords.add(key);
		}
	}

	/**
	 * Read other responses (which are used when no keywords are found in an
	 * input string) and store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	private void readOtherResponses(BufferedReader in) throws IOException {
		String line;
		ArrayList<String> otherResponses = new ArrayList<String>();
		while ((line = in.readLine()) != null && !line.equals(";Quit")) {
			otherResponses.add(line);
		}
		this.otherResponses = new StringArrayRandomizer(otherResponses);
	}

	/**
	 * Read quit commands and store them in memory.
	 * 
	 * @param in
	 *            A BufferedReader reading from the script file.
	 * @throws IOException
	 *             If there is an error reading the file.
	 * @throws FileFormatException
	 *             If the script file is formatted incorrectly.
	 */
	private void readQuitCommands(BufferedReader in) throws IOException {
		String line;
		while ((line = in.readLine()) != null) {
			quitCommands.add(line);
		}
	}

	/**
	 * Returns one of the welcome messages Eliza uses to start a conversation.
	 * 
	 * @return A welcome message.
	 */
	public String getWelcomeMessage() {
		return RandomMethods.random(welcomeMessages);
	}

	/**
	 * Generate a response to an input sentence. Apply pre-substitution rules,
	 * split input into sentences, find highest priority keyword with suitable
	 * decomposition rule and apply a reassembly rule.
	 * 
	 * @param input
	 *            A string containing one or more input sentences.
	 * @return A reply to one of the input sentence(s).
	 */
	public String generateResponse(String input) {
		input = input.trim().toLowerCase();
		// apply pre-substitution rules
		input = preSubstitution(input);
		// remove all non-characters with a few exceptions.
		input = Pattern.compile(FILTER_REGEX).matcher(input).replaceAll(
				EMPTY_STRING);
		input = deleteDoubleSpaces(input);
		// split input string into separate sentences
		String[] sentences = input.split(SENTENCE_SEPARATOR_REGEX);

		for (String sentence : sentences) {
			// is a sentence contains a quit command, quit.
			if (isQuitCommand(sentence)) {
				alive = false;
				return RandomMethods.random(finalMessages);
			}
		}

		/*
		 * A list used to store keywords which match in order to find the most
		 * suitable one.
		 */
		ArrayList<Keyword> possibleKeys = new ArrayList<Keyword>();
		// A HashMap used for mapping keywords with matching input sentences.
		HashMap<Keyword, String> map = new HashMap<Keyword, String>();

		int priority = 11;
		// find keyword(s) for each sentence
		for (String sentence : sentences) {
			for (Keyword key : keywords) {
				if (key.getPriority() > priority)
					// if a keyword has lower priority than one already found,
					// go to next sentence
					break;
				if (key.matches(sentence)) {
					// if the keyword matches the sentence, add to list of
					// possible keywords
					possibleKeys.add(key);
					// add keyword and sentence pair to HashMap
					map.put(key, sentence);
					priority = key.getPriority();
				}
			}
		}
		/*
		 * If none of the input sentences match any of the keywords
		 * decomposition rules.
		 */
		if (possibleKeys.size() == 0) {
			return otherResponse();
		}

		Keyword key = findMaxPriority(possibleKeys);
		// get sentence which matched the keyword
		String sentence = map.get(key);
		String output = applyKeyword(key, sentence);
		// 2 % (1/50) chance of Eliza making a "typo"
		if (RandomMethods.random(50) == 0) {
			output = generateTypo(output);
		}
		return output;
	}

	/**
	 * Make any double spaces into single spaces.
	 * 
	 * @param input
	 *            An input string.
	 * @return The filtered string.
	 */
	private String deleteDoubleSpaces(String input) {
		return Pattern.compile(DOUBLE_SPACE_REGEX).matcher(input).replaceAll(
				SPACE);
	}

	/**
	 * Method determining whether an input sentence contains any quit commands.
	 * 
	 * @param input
	 *            An input sentence.
	 * @return Whether the input sentence contains any of the quit commands.
	 */
	private boolean isQuitCommand(String input) {
		for (String command : quitCommands) {
			// Check if input contains the word with word boundaries on both
			// sides.
			String regex = WORD_BOUNDARY + command.toLowerCase()
					+ WORD_BOUNDARY;
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(input);
			// if the input sentence contains the regex
			if (m.find()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tells whether the Eliza conversation is still going.
	 * 
	 * @return false if the conversation has been terminated, true if not.
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Apply Keyword to a sentence. Precondition: The sentence matches one of
	 * the decomposition rules.
	 * 
	 * @param key
	 *            The keyword whose decomposition and reassembly rules is to be
	 *            applied.
	 * @param sentence
	 *            A sentence to which the decomposition and reassembly rules are
	 *            to be applied.
	 * @return A reassembled response.
	 */
	private String applyKeyword(Keyword key, String sentence) {
		for (Decomposition d : key.getDecomp()) {
			String rule = d.getRule();
			rule = key.formatDecomposition(rule);
			Matcher m = Pattern.compile(rule).matcher(sentence);
			// if the sentence doesn't fit the decomposition rule
			if (!m.find())
				// go to next decomposition rule
				continue;
			String reass = d.getReassembly();
			reass = reass.replace(KEYWORD_SYNONYM_CHARACTER, key.getKeyword());
			int count = m.groupCount();
			/*
			 * If there is a star in the matching decomposition rule and a 1 in
			 * the reassembly rule.
			 */
			if (count >= 1 && reass.contains(ONE_STRING)) {
				String r = m.group(1);
				r = postSubstitution(r);
				reass = reass.replace(ONE_STRING, r);
			}
			/*
			 * If there is a second star in the matching decomposition rule and
			 * a 2 in the reassembly rule.
			 */
			if (count >= 2 && reass.contains(TWO_STRING)) {
				String r = m.group(2);
				r = postSubstitution(r);
				reass = reass.replace(TWO_STRING, r);
			}
			return reass;
		}
		// unreachable as the keyword has already been confirmed to fit
		return null;
	}

	/**
	 * Apply pre-substitution rules to an input String.
	 * 
	 * @param input
	 *            An input string.
	 * @return A string to which the pre-substitution rules have been applied.
	 */
	private String preSubstitution(String input) {
		return applySubstitution(input, presub).toLowerCase();
	}

	/**
	 * Apply post-substitution rules to an input String.
	 * 
	 * @param input
	 *            An input string.
	 * @return A string to which the post-substitution rules have been applied.
	 */
	private String postSubstitution(String output) {
		return applySubstitution(output, postsub);
	}

	/**
	 * Apply substitutions using SubstitutionRule objects.
	 * 
	 * @param str
	 *            The string to which the substitution(s) are to be applied.
	 * @param rules
	 *            The rules which are to be applied.
	 * @param ignoreCase
	 * @return A string to which the substitution rules have been applied.
	 */
	private String applySubstitution(String str,
			ArrayList<SubstitutionRule> rules) {
		String find;
		String replacement;
		for (SubstitutionRule rule : rules) {
			find = rule.getFind().toLowerCase();
			// skip if the string does not contain the phrase
			if (!str.contains(find))
				continue;
			find = WORD_BOUNDARY + find + WORD_BOUNDARY;
			replacement = rule.getReplacement();
			if (replacement.equals(UNDERSCORE_STRING)) {
				replacement = EMPTY_STRING;
			} else {
				/*
				 * In order to avoid the substitution rule to be applied twice,
				 * add _ to each side of the replacement, then remove them after
				 * all substitutions have been made.
				 */
				replacement = UNDERSCORE_STRING + replacement;
			}
			// for it to work with several-word substitutions
			replacement = replacement.replace(SPACE, SPACE_UNDERSCORE_STRING);
			str = Pattern.compile(find).matcher(str).replaceAll(replacement);
		}
		// all substitutions have been made: remove all underscores
		return str.replace(UNDERSCORE_STRING, EMPTY_STRING);
	}

	/**
	 * Finds the keyword(s) of the highest priority in an ArrayList of keys and
	 * returns one of them.
	 * 
	 * @param keys
	 *            An array of keywords.
	 * @return One of the keywords of the highest priority in the ArrayList.
	 */
	private Keyword findMaxPriority(ArrayList<Keyword> keys) {
		int maxPriority = 11;
		ArrayList<Keyword> max = new ArrayList<Keyword>();

		// find which keyword(s) are more significant
		for (int i = 0; i < keys.size(); i++) {
			int prior = keys.get(i).getPriority();
			// if a word of higher priority is found
			if (prior < maxPriority) {
				maxPriority = prior;
				// empty list
				max.clear();
				max.add(keys.get(i));
			} else if (prior == maxPriority) {
				max.add(keys.get(i));
			}
		}
		// choose a random keyword of the highest priority
		return RandomMethods.random(max);
	}

	/**
	 * Returns a string response which is used when no suitable keyword is found
	 * in the input string.
	 * 
	 * @return A string response.
	 */
	private String otherResponse() {
		return otherResponses.next();
	}

	/**
	 * Generate a "typo" by swapping two adjacent characters in a string.
	 * 
	 * @param str
	 *            A string.
	 * @return The same string, but with a "typo".
	 */
	private static String generateTypo(String str) {
		int len = str.length();
		if (len == 2) {
			return new String(new char[] { str.charAt(1), str.charAt(0) });
		}
		int ran = RandomMethods.random(len);
		int other;
		if (ran == 0) {
			other = 1;
		} else if (ran == len - 1) {
			other = len - 2;
		} else {
			boolean before = RandomMethods.random(2) == 1;
			if (before) {
				other = ran - 1;
			} else {
				other = ran + 1;
			}
		}
		// swap
		return swap(str, ran, other);
	}

	/**
	 * Swap the characters in two indexes in a String.
	 * 
	 * @param str
	 *            A string.
	 * @param a
	 *            An index.
	 * @param b
	 *            Another index.
	 * @return
	 */
	private static String swap(String str, int a, int b) {
		char[] chars = str.toCharArray();
		char temp = chars[b];
		chars[b] = chars[a];
		chars[a] = temp;
		return new String(chars);
	}

	/**
	 * Sorts an array using the compareTo method. Modified version of the
	 * algorithm found on:
	 * http://www.mycstutorials.com/articles/sorting/quicksort
	 * 
	 * @param arr
	 *            The array to be sorted
	 * @param start
	 *            The start index of the array partition to be sorted
	 * @param end
	 *            The index of the last element in the array partition to be
	 *            sorted
	 */
	private void sort(ArrayList<Keyword> arr, int start, int end) {
		int i = start;
		int k = end;

		if (end - start >= 1) {
			// set the pivot as the third cell in the first row in the partition
			Keyword pivot = arr.get(start);

			// while the scan indices from left and right have not met
			while (k > i) {
				// from the left, look for the first
				while (arr.get(i).compareTo(pivot) <= 0 && i <= end && k > i) {
					i++;
				}
				// element greater than the pivot
				// from the right, look for the first
				while (arr.get(k).compareTo(pivot) > 0 && k >= start && k >= i) {
					k--;
				}
				// element not greater than the pivot
				if (k > i) // if the left seek index is still smaller than
				{
					swap(arr, i, k);
				}
				// the right index, swap the corresponding elements
			}
			swap(arr, start, k);
			// after the indices have crossed, swap the last element in
			sort(arr, start, k - 1); // quicksort the left partition
			sort(arr, k + 1, end); // quicksort the right partition
		}
	}

	/**
	 * Swaps element in index a and b in the array arr.
	 * 
	 * @param arr
	 *            The array in which the element are to be swapped
	 * @param a
	 *            represents an element in the array of Keywords
	 * @param b
	 *            represents an second element in the array of Keywords
	 */
	private void swap(ArrayList<Keyword> arr, int a, int b) {
		Keyword temp = arr.get(a);
		arr.set(a, arr.get(b));
		arr.set(b, temp);
	}
}
