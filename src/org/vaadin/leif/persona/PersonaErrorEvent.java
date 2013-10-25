package org.vaadin.leif.persona;

public class PersonaErrorEvent extends PersonaEvent {

    private final String message;
    private final Throwable cause;

    public PersonaErrorEvent(Persona source, String message) {
        this(source, null, message);
    }

    public PersonaErrorEvent(Persona source, Throwable cause) {
        this(source, cause, cause.getLocalizedMessage());
    }

    public PersonaErrorEvent(Persona source, Throwable cause, String message) {
        super(source);
        this.cause = cause;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }

}
