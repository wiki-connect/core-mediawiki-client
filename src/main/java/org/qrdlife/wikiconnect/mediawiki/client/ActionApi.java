package org.qrdlife.wikiconnect.mediawiki.client;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.qrdlife.wikiconnect.mediawiki.client.cookie.FileCookieJar;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides a client wrapper for interacting with the MediaWiki Action API.
 * <p>
 * This class manages API configuration, session handling (via cookies),
 * global request parameters, and authentication integration.
 * It builds and initializes a {@link Requester} which is used
 * to send API requests.
 * </p>
 *
 * <p>
 * Typical usage example:
 * </p>
 * 
 * <pre>{@code
 * ActionApi api = new ActionApi("https://en.wikipedia.org/w/api.php")
 *         .setUserAgent("MyBot/1.0")
 *         .setFileCookie(new File("cookies"))
 *         .build();
 *
 * Auth auth = new Auth("username", "password", api);
 * if (auth.login()) {
 *     String token = api.getToken("csrf");
 *     System.out.println("CSRF Token: " + token);
 * }
 * }</pre>
 */
public class ActionApi {

    private static final Logger logger = Logger.getLogger(ActionApi.class.getName());

    /** The base URL of the MediaWiki API endpoint. */
    private final String apiUrl;

    /** Optional global parameters to be included with all API requests. */
    private Map<String, Object> globalParams = null;

    /** The HTTP requester responsible for executing API calls. */
    private Requester requester;

    /** The default User-Agent string for requests. */
    private static final String DEFAULT_USER_AGENT = "wikiconnect-mediawiki-client/1.0";

    /** Custom User-Agent string (if set). */
    private String userAgent = null;

    /** Cookie store for maintaining session persistence. */
    private BasicCookieStore cookieStore;

    /** HTTP context to handle cookies and request state. */
    private final HttpClientContext context;

    /** Authentication handler (optional). */
    private org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth auth;

    /** Configured RequestConfig for timeouts. */
    private org.apache.hc.client5.http.config.RequestConfig requestConfig = null;

    /**
     * Creates a new {@code ActionApi} instance with the given API endpoint.
     *
     * @param apiUrl the full MediaWiki API endpoint URL.
     * @throws IllegalArgumentException if {@code apiUrl} is null, empty, or has an invalid scheme.
     */
    public ActionApi(String apiUrl) {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("API URL cannot be null or empty");
        }
        try {
            java.net.URI uri = new java.net.URI(apiUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Unsupported API URL scheme: " + scheme);
            }
            if (scheme.equalsIgnoreCase("http")) {
                logger.warning("Using unencrypted HTTP protocol for MediaWiki API: " + apiUrl);
            }
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid API URL: " + apiUrl, e);
        }

        this.apiUrl = apiUrl;
        this.context = HttpClientContext.create();
        this.cookieStore = new BasicCookieStore();
        logger.info("ActionApi initialized with apiUrl: " + apiUrl);
    }

    /**
     * Sets global request parameters that will be applied to every API call.
     *
     * @param params map of global parameters (e.g., "format=json").
     * @return this instance for method chaining.
     * @throws IllegalArgumentException if {@code params} is null or empty.
     */
    public ActionApi setGlobalPerms(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            logger.warning("Permissions map is null or empty");
            throw new IllegalArgumentException("Perms map cannot be null or empty");
        }
        this.globalParams = params;
        logger.info("Global permissions set: " + params);
        return this;
    }

    /**
     * Sets a custom cookie store for maintaining session persistence.
     *
     * @param cookieStore the {@link BasicCookieStore} to be used.
     * @return this instance for method chaining.
     */
    public ActionApi setCookieStore(BasicCookieStore cookieStore) {
        this.cookieStore = cookieStore;
        logger.info("Cookie store set");
        return this;
    }

    /**
     * Sets a cookie store that persists session cookies in a file.
     *
     * @param file the file to store cookies.
     * @return this instance for method chaining.
     * @deprecated Use {@link #setCookieStore(BasicCookieStore)} instead.
     */
    @Deprecated
    public ActionApi setFileCookie(File file) {
        this.cookieStore = new FileCookieJar(file);
        logger.info("File cookie set with file: " + file.getPath());
        return this;
    }

    /**
     * Sets a custom User-Agent string for HTTP requests.
     *
     * @param userAgent the User-Agent string.
     * @return this instance for method chaining.
     */
    public ActionApi setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        logger.info("User agent set: " + userAgent);
        return this;
    }

    /**
     * Sets connect and socket timeouts for the HTTP client.
     *
     * @param connectTimeoutMs the connection timeout in milliseconds.
     * @param socketTimeoutMs the socket/response timeout in milliseconds.
     * @return this instance for method chaining.
     */
    public ActionApi setTimeout(int connectTimeoutMs, int socketTimeoutMs) {
        this.requestConfig = org.apache.hc.client5.http.config.RequestConfig.custom()
                .setConnectTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(connectTimeoutMs))
                .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(socketTimeoutMs))
                .build();
        logger.info("Timeouts set - Connect: " + connectTimeoutMs + "ms, Socket: " + socketTimeoutMs + "ms");
        return this;
    }

    /**
     * Associates an {@link org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth}
     * instance with this API client.
     *
     * @param auth the authentication handler.
     * @return this instance for method chaining.
     */
    public ActionApi setAuth(org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth auth) {
        this.auth = auth;
        if (requester != null) {
            requester.setAuth(auth);
        }
        logger.info("Authentication set");
        return this;
    }

    /**
     * Builds the underlying HTTP client and initializes the {@link Requester}.
     * <p>
     * This must be called before sending any requests.
     * </p>
     *
     * @return this instance for method chaining.
     * @throws IllegalStateException if building the client fails.
     */
    public ActionApi build() {
        try {
            CloseableHttpClient client = HttpClients.custom()
                    .setUserAgent(userAgent == null ? DEFAULT_USER_AGENT : userAgent)
                    .setDefaultRequestConfig(requestConfig != null ? requestConfig : org.apache.hc.client5.http.config.RequestConfig.custom()
                            .setConnectTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(30))
                            .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(30))
                            .build())
                    .build();
            context.setCookieStore(cookieStore);
            this.requester = new Requester(client, apiUrl, globalParams, context);
            if (this.auth != null) {
                this.requester.setAuth(this.auth);
            }
            logger.info("ActionApi build completed with userAgent: "
                    + (userAgent == null ? DEFAULT_USER_AGENT : userAgent));
            return this;
        } catch (Exception e) {
            logger.severe("Error during build: " + e.getMessage());
            throw new IllegalStateException("Failed to build ActionApi client", e);
        }
    }

    /**
     * Returns the configured MediaWiki API endpoint URL.
     *
     * @return the API URL.
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Requests a token from the MediaWiki API.
     *
     * @param type the token type (e.g., "login", "csrf").
     * @return the requested token string.
     * @throws Exception if the request fails.
     */
    public String getToken(String type) throws Exception {
        logger.info("Requesting token for type: " + type);
        return new Token(requester).get(type);
    }

    /**
     * Returns the {@link org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth}
     * instance associated with this API client.
     *
     * @return the authentication handler, or {@code null} if none is set.
     */
    public org.qrdlife.wikiconnect.mediawiki.client.Auth.Auth getAuth() {
        return auth;
    }

    /**
     * Returns the {@link Requester} instance used by this {@code ActionApi}.
     * <p>
     * The {@code Requester} is responsible for performing the actual HTTP
     * requests (e.g., GET, POST) to the MediaWiki API endpoints.
     * </p>
     *
     * @return the {@code Requester} associated with this API instance.
     */
    public Requester getRequester() {
        return requester;
    }

}
