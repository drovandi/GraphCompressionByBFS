
package it.uniroma3.dia.gc.exception;

public final class CompressedGraphFormatException extends Exception {

    private static final long serialVersionUID = -8526618586075815884L;

    public CompressedGraphFormatException(String message) {
	super(message);
    }

    public CompressedGraphFormatException(String message, Throwable cause) {
	super(message,cause);
    }

}
