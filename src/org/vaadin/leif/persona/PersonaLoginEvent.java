package org.vaadin.leif.persona;

import java.util.Date;

public class PersonaLoginEvent extends PersonaEvent {

    private final String email;
    private final String audience;
    private final Date expires;
    private final String issuer;

    public PersonaLoginEvent(Persona source, String email, String audience,
            Date expires, String issuer) {
        super(source);
        this.email = email;
        this.audience = audience;
        this.expires = expires;
        this.issuer = issuer;
    }

    public String getEmail() {
        return email;
    }

    public String getAudience() {
        return audience;
    }

    public Date getExpires() {
        return expires;
    }

    public String getIssuer() {
        return issuer;
    }

}
