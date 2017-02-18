
package it.uniroma3.dia.gc.exception;

public final class MapFileMissingException extends Exception {

    private static final long serialVersionUID = 5719057161397637671L;

    public MapFileMissingException(String message) {
	super(message);
    }

    public MapFileMissingException(String message, Throwable cause) {
	super(message,cause);
    }

}
