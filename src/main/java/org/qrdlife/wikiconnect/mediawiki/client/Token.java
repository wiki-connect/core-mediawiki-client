package org.qrdlife.wikiconnect.mediawiki.client;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * A helper class for managing and retrieving tokens from the MediaWiki Action
 * API.
 * <p>
 * MediaWiki requires tokens (e.g., login, CSRF) for certain write or
 * authentication actions.
 * This class provides methods to request and extract these tokens from the API.
 * </p>
 */
public class Token {

    private final Requester requester;

    /**
     * Creates a new {@code Token} manager.
     *
     * @param requester The {@link Requester} instance used to communicate with the
     *                  MediaWiki Action API.
     */
    public Token(Requester requester) {
        this.requester = requester;
    }

    /**
     * Retrieves a token of the specified type from the MediaWiki API.
     * Common token types include:
     * <ul>
     * <li>{@code login} – for login operations</li>
     * <li>{@code csrf} – for edit and other write operations</li>
     * </ul>
     *
     * @param type The token type (e.g., "login", "csrf").
     * @return The retrieved token string.
     *
     * @throws JSONException If JSON parsing fails.
     * @throws Exception     For any other unexpected errors.
     */
    public String get(String type) throws JSONException, Exception {
        Map<String, Object> perms = Map.of(
                "meta", "tokens",
                "type", type,
                "format", "json");
        String response = requester.get("query", perms);
        return extractToken(response, type);
    }

    /**
     * Extracts a token of the specified type from a MediaWiki API JSON response.
     *
     * @param response The API response as a raw JSON string.
     * @param type     The token type (e.g., "login", "csrf").
     * @return The extracted token value, or {@code null} if not found.
     *
     * @throws JSONException If the response cannot be parsed as JSON or the
     *                       expected fields are missing.
     */
    private String extractToken(String response, String type) throws JSONException {
        JSONObject json = new JSONObject(response);
        JSONObject tokens = json.getJSONObject("query").getJSONObject("tokens");
        return tokens.optString(type + "token", null);
    }
}
