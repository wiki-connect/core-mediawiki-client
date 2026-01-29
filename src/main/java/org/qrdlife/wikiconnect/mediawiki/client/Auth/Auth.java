package org.qrdlife.wikiconnect.mediawiki.client.Auth;

public interface Auth {
    boolean login() throws Exception;

    void logout() throws Exception;

    boolean isLoggedIn() throws Exception;

    String getRUsername() throws Exception;

    String getUsername();

    /**
     * Retrieves the authentication headers to be added to the request.
     * 
     * @return A map of header names and values.
     * @throws Exception if an error occurs while generating headers.
     */
    java.util.Map<String, String> getAuthHeaders() throws Exception;
}