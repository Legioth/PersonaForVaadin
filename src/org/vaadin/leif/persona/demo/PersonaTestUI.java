package org.vaadin.leif.persona.demo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.leif.persona.Persona;
import org.vaadin.leif.persona.PersonaErrorEvent;
import org.vaadin.leif.persona.PersonaEvent;
import org.vaadin.leif.persona.PersonaListener;
import org.vaadin.leif.persona.PersonaLoginEvent;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class PersonaTestUI extends UI {
    private static final Set<String> acceptedHosts = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList("localhost")));

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        layout.setMargin(true);

        String hostHeader = request.getHeader("Host");
        if (hostHeader == null) {
            layout.addComponent(new Label(
                    "Can't verify your hostname - can not continue for security reasons"));
            return;
        }
        hostHeader = hostHeader.replaceAll(":.*", "");
        if (!acceptedHosts.contains(hostHeader)) {
            layout.addComponent(new Label(
                    "Application accessed with unexpected hostname "
                            + hostHeader
                            + ". Is someone attempting to tamper with the Persona audience verification?"));
            return;
        }
        layout.addComponent(new Label("Using " + hostHeader + " as audience."));

        final Persona persona = new Persona(this, hostHeader);
        persona.setSiteName("Test site name");

        final Label statusLabel = new Label("Checking status...");

        final Link loginLink = new Link("", persona.getLoginResource());
        loginLink.setIcon(new ExternalResource(
                "https://login.persona.org/i/sign_in_blue.png"));
        loginLink.setVisible(false);

        final Link logoutButton = new Link("Logout",
                persona.getLogoutResource());

        logoutButton.setVisible(false);

        layout.addComponent(statusLabel);
        layout.addComponent(logoutButton);
        layout.addComponent(loginLink);

        persona.addPersonaListener(new PersonaListener() {

            @Override
            public void onLogout(final PersonaEvent event) {
                statusLabel.setValue("Logged out");
                logoutButton.setVisible(false);
                loginLink.setVisible(true);
            }

            @Override
            public void onLogin(final PersonaLoginEvent event) {
                statusLabel.setValue("Logged in as " + event.getEmail()
                        + " with audience " + event.getAudience()
                        + ". Assertion signed by " + event.getIssuer()
                        + " and is valid until " + event.getExpires());
                logoutButton.setVisible(true);
                loginLink.setVisible(false);
            }

            @Override
            public void onCancel(PersonaEvent event) {
                Notification.show("Login canceled");
            }

            @Override
            public void onError(PersonaErrorEvent event) {
                statusLabel.setValue("An eror occured: " + event.getMessage());
                // Just avoid onReady replacing our status
                Throwable cause = event.getCause();
                if (cause != null) {
                    cause.printStackTrace();
                }
            }
        });
    }
}
