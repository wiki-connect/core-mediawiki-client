package org.qrdlife.wikiconnect.mediawiki.client.Auth;

import org.json.JSONObject;
import org.qrdlife.wikiconnect.mediawiki.client.ActionApi;
import org.qrdlife.wikiconnect.mediawiki.client.Requester;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles user authentication with the MediaWiki API.
 * This class supports:
 * <ul>
 * <li>Logging in with username and password</li>
 * <li>Logging out of the current session</li>
 * <li>Checking if a user is authenticated</li>
 * <li>Fetching the real username as recognized by MediaWiki</li>
 * </ul>
 *
 * <p>
 * Typical usage example:
 * </p>
 * 
 * <pre>{@code
 * ActionApi api = new ActionApi(...);
 * Auth auth = new Auth("username", "password", api);
 * if (auth.login()) {
 *     System.out.println("Logged in as: " + auth.getRUsername());
 *     auth.logout();
 * }
 * }</pre>
 */
public class UserAndPassword implements Auth {

    private static final Logger logger = Logger.getLogger(UserAndPassword.class.getName());

    private final String Username;
    private final String Password;
    private String RUsername = null;
    private final ActionApi api;
    private final Requester requester;

    /**
     * Initializes a new {@code Auth} instance with MediaWiki credentials.
     *
     * @param username the MediaWiki username.
     * @param password the MediaWiki password.
     * @param api      the {@link ActionApi} instance for handling requests.
     */
    public UserAndPassword(String username, String password, ActionApi api) {
        this.Username = username;
        this.Password = password;
        this.api = api;
        this.requester = api.getRequester();
        this.api.setAuth(this);
        logger.info("Auth object created for user: " + username);
    }

    /**
     * Attempts to log in to the MediaWiki API using the provided credentials.
     *
     * @return {@code true} if the login was successful, {@code false} otherwise.
     * @throws Exception if an error occurs while retrieving the login token
     *                   or sending the login request (e.g., network errors, JSON
     *                   parsing issues).
     */
    @Override
    public boolean login() throws Exception {
        logger.info("Attempting to log in user: " + Username);
        if (isLoggedIn()) {
            logger.info("This user is already logged in.");
            return true;
        }

        // Retrieve the login token
        String token = api.getToken("login");

        // Set up the parameters for the login request
        Map<String, Object> perms = Map.of(
                "lgname", Username,
                "lgpassword", Password,
                "lgtoken", token,
                "format", "json");

        // Send the login request to the API
        JSONObject res = new JSONObject(requester.post("login", perms));

        // Check if the login was successful
        String result = res.getJSONObject("login").getString("result");

        if ("Success".equals(result)) {
            RUsername = res.getJSONObject("login").getString("lgusername");
            logger.info("Login successful for user: " + RUsername);
            return true;
        } else {
            logger.warning("Login failed for user: " + Username);
            return false;
        }
    }

    /**
     * Logs out the currently authenticated user from the MediaWiki API.
     *
     * @throws IOException if the user is not logged in.
     * @throws Exception   if an error occurs while retrieving the CSRF token
     *                     or sending the logout request.
     */
    @Override
    public void logout() throws Exception {
        if (!isLoggedIn()) {
            logger.warning("User is not logged in. Cannot perform logout.");
            return;
        }
        logger.info("Attempting to log out user: " + RUsername);

        String token = api.getToken("csrf");

        Map<String, Object> params = Map.of(
                "token", token,
                "format", "json");
        requester.post("logout", params);

        logger.info("User logged out successfully: " + RUsername);
    }

    /**
     * Returns the configured MediaWiki username (the one provided to the
     * constructor).
     *
     * @return the username used during initialization.
     */
    @Override
    public String getUsername() {
        return Username;
    }

    /**
     * Retrieves the actual username recognized by the MediaWiki API.
     * <p>
     * If the username is not already cached, this method will query
     * the API using <code>meta=userinfo</code> and update the cached value.
     * </p>
     *
     * @return the real username of the authenticated user.
     * @throws Exception if the API request fails (e.g., network error, JSON parsing
     *                   error).
     */
    @Override
    public String getRUsername() throws Exception {
        if (RUsername == null) {
            Map<String, Object> perms = Map.of(
                    "meta", "userinfo",
                    "format", "json");
            JSONObject res = new JSONObject(requester.get("query", perms));
            JSONObject userinfo = res.getJSONObject("query").getJSONObject("userinfo");

            RUsername = userinfo.getString("name");
        }
        return RUsername;
    }

    /**
     * Checks if the current user session is authenticated with the MediaWiki API.
     *
     * @return {@code true} if the user is logged in, {@code false} otherwise.
     * @throws Exception if the API request fails (e.g., network error, JSON parsing
     *                   error).
     */
    @Override
    public boolean isLoggedIn() throws Exception {
        logger.info("Checking if user is logged in");

        Map<String, Object> perms = Map.of(
                "meta", "userinfo",
                "format", "json");

        String response = requester.get("query", perms);
        if (response == null || response.isEmpty()) {
            throw new IOException("Empty or null response received from server");
        }
        JSONObject resJSON = new JSONObject(response);
        JSONObject userinfo = resJSON.getJSONObject("query").getJSONObject("userinfo");

        int id = userinfo.getInt("id");
        RUsername = userinfo.getString("name");

        if (id > 0) {
            logger.info("User is logged in: " + RUsername);
            return true;
        } else {
            logger.warning("User is not logged in");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAuthHeaders() throws Exception {
        return new java.util.HashMap<>();
    }
}
