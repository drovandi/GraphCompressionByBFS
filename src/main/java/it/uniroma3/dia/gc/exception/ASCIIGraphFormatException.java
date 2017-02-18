
package it.uniroma3.dia.gc.exception;

public final class ASCIIGraphFormatException extends Exception {

    private static final long serialVersionUID = 7288650850087350925L;

    public ASCIIGraphFormatException(String message) {
	super(message);
    }

    public ASCIIGraphFormatException(String message, Throwable cause) {
	super(message,cause);
    }

}
