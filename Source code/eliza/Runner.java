package eliza;

import java.util.Scanner;

import eliza.exception.FileFormatException;
import eliza.methods.RandomMethods;

/**
 * The Runner class is the main class and the only one to contain a main method.
 * 
 * @author Ole and Peter
 * 
 */
public class Runner {

	// Minimum sleep time between user input and Eliza replying.
	private static final int MIN_SLEEP = 1500;
	// Maximum sleep time between user input and Eliza replying.
	private static final int MAX_SLEEP = 2000;
	// The prefix used in front of Eliza output.
	private static final String AI_PREFIX = "Eliza: ";
	// The prefix used in front of user input.
	private static final String USER_INPUT_PREFIX = ">>";
	// The default option for whether the delay between user input and
	// engine output.
	private static final boolean DEFAULT_ENABLE_SLEEPING_OPTION = false;
	// The filename of the default script to be used.
	private static final String DEFAULT_SCRIPT = "script.txt";

	/**
	 * The main method which controls user input and engine output.
	 * 
	 * @param args 
	 */
	public static void main(String[] args) {
		String script = DEFAULT_SCRIPT;
		boolean enableSleeping = DEFAULT_ENABLE_SLEEPING_OPTION;

		for (String arg : args) {
			if (arg.startsWith("script=")) {
				script = arg.substring(7);
			} else if (arg.startsWith("sleep=")) {
				enableSleeping = Boolean.parseBoolean(arg.substring(6));
			} else {
				System.out.println("Invalid argument: " + arg);
			}
		}
		Engine engine = null;
		try {
			engine = new Engine(script);
		} catch (FileFormatException e) {
			System.out.println("Script file not formatted properly. "
					+ e.getMessage());
			System.out.println("Now terminating.");
			return;
		}

		String welcomeMessage = engine.getWelcomeMessage();
		System.out.println(AI_PREFIX + welcomeMessage);
		String input;
		String output;
		Scanner in = new Scanner(System.in);
		do {
			System.out.print(USER_INPUT_PREFIX);
			input = in.nextLine();
			// if the user doesn't enter anything: jump to start of loop
			if (input.equals(""))
				continue;
			long start = System.currentTimeMillis();
			output = engine.generateResponse(input);

			if (enableSleeping) {
				long end = System.currentTimeMillis();
				int delay = RandomMethods.random(MIN_SLEEP, MAX_SLEEP);
				int sleepTime = (int) (end - start + delay);
				sleep(sleepTime);
			}
			System.out.println(AI_PREFIX + output);
		} while (engine.isAlive());
	}

	private static void sleep(int sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
		}
	}
}
