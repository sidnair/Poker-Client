/**
 * Thrown when the return type specified by the user is not the same as the 
 * return type the method actually gives. Thrown by the AbstractModel's 
 * callModelMethod for debugging purposes.
 * 
 * @author Sid Nair
 *
 */
public class UnexpectedReturnTypeException extends Exception {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 8621548817861144971L;

	/**
	 * Empty constructor.
	 */
	public UnexpectedReturnTypeException() { }
	
	/**
	 * Throws an exception with the message specified by the thrower.
	 * 
	 * @param message message to be printed when exception is thrown.
	 */
	public UnexpectedReturnTypeException(String message) {
		super(message);
	}

}
