
package it.uniroma3.dia.gc.exception;

public final class MapFileFormatException extends Exception {

    private static final long serialVersionUID = 5284317941496088838L;

    public MapFileFormatException(String message) {
	super(message);
    }

    public MapFileFormatException(String message, Throwable cause) {
	super(message,cause);
    }

}
