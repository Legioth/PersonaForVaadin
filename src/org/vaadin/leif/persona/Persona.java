package org.vaadin.leif.persona;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.JavaScriptExtensionState;
import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;

/**
 * 
 */
@JavaScript({ "https://login.persona.org/include.orig.js", "personaConnector.js" })
public class Persona extends AbstractJavaScriptExtension {
    private LinkedHashSet<PersonaListener> listeners = new LinkedHashSet<PersonaListener>();
    private final String audience;

    public static class PersonaState extends JavaScriptExtensionState {
        public String backgroundColor;
        public String privacyPolicy;
        public String returnTo;
        public String siteLogo;
        public String siteName;
        public String termsOfService;

        public String domId = "persona" + new Random().nextInt();
    }

    public interface PersonaRpc extends ServerRpc {
        public void onlogin(String assertion);

        public void onlogout();

        public void oncancel();
    }

    @Override
    public PersonaState getState() {
        return (PersonaState) super.getState();
    }

    public Persona(UI ui, String audience) {
        this.audience = audience;

        callFunction("watch");

        registerRpc(new PersonaRpc() {
            @Override
            public void onlogout() {
                fireLogoutEvent();
            }

            @Override
            public void onlogin(String assertion) {
                checkAssertion(assertion);
            }

            @Override
            public void oncancel() {
                fireCancelEvent();
            }
        });

        addFunction("error", new JavaScriptFunction() {
            @Override
            public void call(JSONArray arguments) throws JSONException {
                String message = null;
                if (arguments.length() > 0) {
                    message = arguments.getString(0).toString();
                }
                fireError(new PersonaErrorEvent(Persona.this, message));
            }
        });

        extend(ui);
    }

    public Resource getLoginResource() {
        return new ExternalResource("javascript:window['" + getState().domId
                + "'].request()");
    }

    public Resource getLogoutResource() {
        return new ExternalResource("javascript:window['" + getState().domId
                + "'].logout()");
    }

    protected void checkAssertion(String assertion) {
        try {
            JSONObject result = fetchVerification(assertion);
            System.out.println(result);
            String status = result.getString("status");
            if (status.equals("okay")) {
                PersonaLoginEvent event = new PersonaLoginEvent(this,
                        result.getString("email"),
                        result.getString("audience"), new Date(
                                result.getLong("expires")),
                        result.getString("issuer"));
                fireLoginEvent(event);
            } else {
                fireError(new PersonaErrorEvent(this,
                        result.getString("reason")));
            }
        } catch (Exception e) {
            fireError(new PersonaErrorEvent(this, e));
        }
    }

    private JSONObject fetchVerification(String assertion) throws Exception {
        HttpsURLConnection connection = null;
        OutputStreamWriter writer = null;
        InputStream inputStream = null;
        try {
            connection = (HttpsURLConnection) new URL(
                    "https://verifier.login.persona.org/verify").openConnection();
            connection.setDoOutput(true);
            writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("assertion=" + URLEncoder.encode(assertion, "UTF-8")
                    + "&audience=" + URLEncoder.encode(audience, "UTF-8"));
            writer.flush();
            writer.close();

            if (connection.getResponseCode() != 200) {
                System.out
                        .println(IOUtils.toString(connection.getErrorStream()));
            }
            inputStream = connection.getInputStream();
            String response = IOUtils.toString(inputStream);
            return new JSONObject(response);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(inputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void addPersonaListener(PersonaListener listener) {
        listeners.add(listener);
    }

    public void removePersonaListener(PersonaListener listener) {
        listeners.remove(listener);
    }

    protected void fireLoginEvent(PersonaLoginEvent event) {
        for (PersonaListener l : getListeners()) {
            l.onLogin(event);
        }
    }

    protected void fireError(PersonaErrorEvent event) {
        for (PersonaListener l : getListeners()) {
            l.onError(event);
        }
    }

    protected void fireLogoutEvent() {
        PersonaEvent event = new PersonaEvent(this);
        for (PersonaListener l : getListeners()) {
            l.onLogout(event);
        }
    }

    protected void fireCancelEvent() {
        PersonaEvent event = new PersonaEvent(this);
        for (PersonaListener l : getListeners()) {
            l.onCancel(event);
        }
    }

    private List<PersonaListener> getListeners() {
        return new ArrayList<PersonaListener>(listeners);
    }

    /**
     * Absolute path or URL to the web site's privacy policy. If provided, then
     * {@link #setTermsOfService(String)} must also be provided. When both
     * termsOfService and privacyPolicy are given, the login dialog informs the
     * user that, by continuing,
     * "you confirm that you accept this site's Terms of Use and Privacy Policy."
     * The dialog provides links to the the respective policies. If
     * termsOfService is not provided, this parameter has no effect.
     * 
     * @param privacyPolcyUrl
     */
    public void setPrivacyPolicy(String privacyPolcyUrl) {
        getState().privacyPolicy = privacyPolcyUrl;
    }

    public String getPrivacyPolicy() {
        return getState().privacyPolicy;
    }

    /**
     * Absolute path or URL to the web site's terms of service. If provided,
     * then {@link #setPrivacyPolicy(String)} must also be provided. When both
     * termsOfService and privacyPolicy are given, the login dialog informs the
     * user that, by continuing,
     * "you confirm that you accept this site's Terms of Use and Privacy Policy."
     * The dialog provides links to the the respective policies. If
     * privacyPolicy is not provided, this parameter has no effect.
     * 
     * @param termsOfServiceUrl
     */
    public void setTermsOfService(String termsOfServiceUrl) {
        getState().termsOfService = termsOfServiceUrl;
    }

    public String getTermsOfService() {
        return getState().termsOfService;
    }

    /**
     * Plain text name of your site to show in the login dialog. Unicode and
     * whitespace are allowed, but markup is not.
     * 
     * @param siteName
     */
    public void setSiteName(String siteName) {
        getState().siteName = siteName;
    }

    public String getSiteName() {
        return getState().siteName;
    }

    /**
     * Absolute path to an image to show in the login dialog. The path must
     * begin with '/' and must be available over SSL. Larger images will be
     * scaled down to fit within 100x100 pixels.
     * 
     * @param siteLogoUrl
     */
    public void setSiteLogo(String siteLogoUrl) {
        getState().siteLogo = siteLogoUrl;
    }

    public String getSiteLogo() {
        return getState().siteLogo;
    }

    /**
     * Absolute path to send new users to after they've completed email
     * verification for the first time. The path must begin with '/'. This
     * parameter only affects users who are certified by Mozilla's fallback
     * Identity Provider.
     * 
     * 
     * @param returnToUrl
     */
    public void setReturnTo(String returnToUrl) {
        getState().returnTo = returnToUrl;
    }

    public String getReturnTo() {
        return getState().returnTo;
    }

    public void setBackgroundColor(String backgroundColor) {
        getState().backgroundColor = backgroundColor;
    }

    public String getBackgroundColor() {
        return getState().backgroundColor;
    }
}
