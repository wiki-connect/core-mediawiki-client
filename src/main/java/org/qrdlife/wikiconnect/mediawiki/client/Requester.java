package org.qrdlife.wikiconnect.mediawiki.client;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.qrdlife.wikiconnect.mediawiki.client.Exception.EmptyResponseException;
import org.qrdlife.wikiconnect.mediawiki.client.Exception.UsageException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles HTTP communication with the MediaWiki API.
 * <p>
 * This class provides methods to perform {@code GET} and {@code POST}
 * requests to the MediaWiki Action API and ensures that responses are
 * validated for errors.
 * </p>
 *
 * <p>
 * Typical usage:
 * </p>
 * 
 * <pre>{@code
 * Requester requester = new Requester(client, "https://en.wikipedia.org/w/api.php", globalParams, context);
 * String response = requester.post("login", Map.of("lgname", "user", "lgpassword", "pass"));
 * }</pre>
 */
public class Requester {

    /** The underlying Apache HTTP client used to send requests. */
    private final CloseableHttpClient httpClient;

    /** The base API URL (e.g., <a href="https://example.org/w/api.php">...</a>). */
    private final String apiUrl;

    /** Optional global parameters added to every request (e.g., format=json). */
    private final Map<String, Object> globalParams;

    /** The HTTP context used for managing cookies and session state. */
    private final HttpClientContext context;

    /** Auth handler */
    private org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth auth;

    /**
     * Creates a new {@code Requester}.
     *
     * @param httpClient   the HTTP client used for communication.
     * @param apiUrl       the MediaWiki API endpoint.
     * @param globalParams global parameters added to all requests (may be null).
     * @param context      HTTP context used for cookies/session management.
     */
    public Requester(CloseableHttpClient httpClient, String apiUrl, Map<String, Object> globalParams,
            HttpClientContext context) {
        this.httpClient = httpClient;
        this.apiUrl = apiUrl;
        this.globalParams = globalParams;
        this.context = context;
    }

    /**
     * Sets the authentication handler for this requester.
     * 
     * @param auth The {@link org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth}
     *             instance to use.
     */
    public void setAuth(org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth auth) {
        this.auth = auth;
    }

    /**
     * Sends a {@code GET} request to the API.
     *
     * @param action the API action (e.g., "query", "login").
     * @param params parameters for the request.
     * @return the raw JSON response as a string.
     * @throws Exception if the request fails or the response indicates an error.
     */
    public String get(String action, Map<String, Object> params) throws Exception {
        return sendRequest("GET", action, params);
    }

    /**
     * Sends a {@code POST} request to the API.
     *
     * @param action the API action (e.g., "edit", "login").
     * @param params parameters for the request.
     * @return the raw JSON response as a string.
     * @throws Exception if the request fails or the response indicates an error.
     */
    public String post(String action, Map<String, Object> params) throws Exception {
        return sendRequest("POST", action, params);
    }

    /**
     * Builds and executes an HTTP request to the MediaWiki API.
     *
     * @param method the HTTP method ("GET" or "POST").
     * @param action the API action (e.g., "query", "edit").
     * @param params the request parameters.
     * @return the raw JSON response as a string.
     * @throws EmptyResponseException if no response is received from the server.
     * @throws UsageException         if the API response contains an error object.
     * @throws Exception              if an unexpected error occurs during request
     *                                execution.
     */
    private String sendRequest(String method, String action, Map<String, Object> params) throws Exception {
        ClassicRequestBuilder builder;

        if ("POST".equalsIgnoreCase(method)) {
            builder = ClassicRequestBuilder.post();
            builder.setCharset(StandardCharsets.UTF_8);
        } else if ("GET".equalsIgnoreCase(method)) {
            builder = ClassicRequestBuilder.get();
            builder.setCharset(StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        if (auth != null) {
            Map<String, String> authHeaders = auth.getAuthHeaders();
            for (Map.Entry<String, String> entry : authHeaders.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        builder.setUri(new URI(apiUrl));
        builder.addParameter("action", action);

        java.util.Map<String, Object> requestParams = new java.util.HashMap<>();

        if (globalParams != null) {
            for (Map.Entry<String, Object> entry : globalParams.entrySet()) {
                requestParams.put(entry.getKey(), entry.getValue());
            }
        }
        requestParams.putAll(params);

        for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
            builder.addParameter(entry.getKey(), entry.getValue().toString());
        }

        ClassicHttpResponse response = null;
        try {
            ClassicHttpRequest request = builder.build();
            response = httpClient.executeOpen(null, request, context);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseText = EntityUtils.toString(entity);
                checkForErrors(responseText, action);
                return responseText;
            } else {
                throw new EmptyResponseException("Failed to get server response");
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Checks the API response for errors and throws exceptions if necessary.
     *
     * @param response the raw JSON response.
     * @param action   the API action that was executed.
     * @throws UsageException if the response contains an error or indicates
     *                        failure.
     * @throws JSONException  if the response cannot be parsed as valid JSON.
     */
    private void checkForErrors(String response, String action) throws UsageException, JSONException {
        JSONObject result = new JSONObject(response);

        if (result.has("error")) {
            JSONObject error = result.getJSONObject("error");
            throw new UsageException(
                    error.optString("code", "unknown"),
                    error.optString("info", "No error information provided"),
                    response);
        }

        if (result.has(action)) {
            JSONObject _action = result.getJSONObject(action);

            if ("Failed".equals(_action.optString("result"))) {
                throw new UsageException(action + "_failed", _action.optString("reason"), response);
            }
        }
    }
}
