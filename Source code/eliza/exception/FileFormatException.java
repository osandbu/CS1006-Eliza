package eliza.exception;

/**
 * FileFormatException is an exception marking a syntax error in the script
 * file. It is sometimes thrown when parsing the script file.
 * 
 * @author Ole & Peter
 * 
 */
@SuppressWarnings("serial")
public class FileFormatException extends Exception {

	/**
	 * Creates a new FileFormatException with a given description.
	 * 
	 * @param message
	 *            A description of the cause of the exception.
	 */
	public FileFormatException(String message) {
		super(message);
	}

	/**
	 * Creates a new FileFormatException with a given description and cause.
	 * 
	 * @param message
	 *            A description of the cause of the exception.
	 * @param cause
	 *            The cause of the exception.
	 */
	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
