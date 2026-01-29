package org.qrdlife.wikiconnect.mediawiki.client.Auth;

/**
 * Interface defining the contract for authentication mechanisms.
 */
public interface Auth {
    /**
     * Attempts to log in using the configured credentials.
     *
     * @return true if login was successful, false otherwise.
     * @throws Exception if an error occurs during the login process.
     */
    boolean login() throws Exception;

    /**
     * Logs out the current user.
     *
     * @throws Exception if an error occurs during logout.
     */
    void logout() throws Exception;

    /**
     * Checks if the user is currently logged in.
     *
     * @return true if the user is logged in, false otherwise.
     * @throws Exception if an error occurs while checking login status.
     */
    boolean isLoggedIn() throws Exception;

    /**
     * Retrieves the real username as recognized by the server.
     *
     * @return the real username.
     * @throws Exception if an error occurs while fetching the username.
     */
    String getRUsername() throws Exception;

    /**
     * Retrieves the username provided during initialization.
     *
     * @return the username.
     */
    String getUsername();

    /**
     * Retrieves the authentication headers to be added to the request.
     * 
     * @return A map of header names and values.
     * @throws Exception if an error occurs while generating headers.
     */
    java.util.Map<String, String> getAuthHeaders() throws Exception;
}