package org.qrdlife.wikiconnect.mediawiki.client.Auth;

import java.util.logging.Logger;
import org.json.JSONObject;

import org.qrdlife.wikiconnect.mediawiki.client.ActionApi;
import org.qrdlife.wikiconnect.mediawiki.client.Requester;

public class OAuthOwnerConsumer implements Auth {
    private final String AccessToken;
    private final ActionApi api;
    private final Requester requester;
    private final Logger logger = Logger.getLogger(OAuthOwnerConsumer.class.getName());

    public OAuthOwnerConsumer(String accessToken, ActionApi api) {
        this.AccessToken = accessToken;
        this.api = api;
        this.requester = api.getRequester();
        this.api.setAuth(this);
        logger.info("OAuthOwnerConsumer initialized with access token");
    }

    @Override
    public boolean login() throws Exception {
        return isLoggedIn();
    }

    @Override
    public void logout() throws Exception {
        // No-op for OAuth owner-only consumer as tokens are static
    }

    @Override
    public String getUsername() {
        // This might need to be fetched via API if not known, but for now we return
        // null or implement a fetch
        try {
            return getRUsername();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String cachedUsername = null;

    @Override
    public String getRUsername() throws Exception {
        if (cachedUsername != null) {
            return cachedUsername;
        }

        java.util.Map<String, Object> perms = java.util.Map.of(
                "dir", "user",
                "meta", "userinfo",
                "format", "json");

        String response = requester.get("query", perms);
        JSONObject res = new JSONObject(response);
        JSONObject userinfo = res.getJSONObject("query").getJSONObject("userinfo");
        cachedUsername = userinfo.getString("name");
        return cachedUsername;
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        try {
            getRUsername();
            return true;
        } catch (Exception e) {
            logger.warning("OAuth login check failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public java.util.Map<String, String> getAuthHeaders() throws Exception {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Authorization", "Bearer " + AccessToken);
        return headers;
    }
}
