package org.vaadin.leif.persona;

import java.util.EventObject;

public class PersonaEvent extends EventObject {

    public PersonaEvent(Persona source) {
        super(source);
    }

    @Override
    public Persona getSource() {
        return (Persona) super.getSource();
    }

}
