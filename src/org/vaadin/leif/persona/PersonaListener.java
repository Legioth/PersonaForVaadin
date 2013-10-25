package org.vaadin.leif.persona;

public interface PersonaListener {
    public void onLogin(PersonaLoginEvent event);

    public void onLogout(PersonaEvent event);

    public void onCancel(PersonaEvent event);

    public void onError(PersonaErrorEvent event);
}
