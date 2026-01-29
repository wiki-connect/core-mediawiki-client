package org.qrdlife.wikiconnect.mediawiki.client;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.qrdlife.wikiconnect.mediawiki.client.Auth.UserAndPassword;
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
 * <p>Typical usage example:</p>
 * <pre>{@code
 * ActionApi api = new ActionApi("https://en.wikipedia.org/w/api.php")
 *     .setUserAgent("MyBot/1.0")
 *     .setFileCookie(new File("cookies"))
 *     .build();
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
    private final String defaultUserAgent = "wikiconnect-mediawiki-client/1.0";

    /** Custom User-Agent string (if set). */
    private String userAgent = null;

    /** Cookie store for maintaining session persistence. */
    private BasicCookieStore cookieStore;

    /** HTTP context to handle cookies and request state. */
    private final HttpClientContext context;

    /** Authentication handler (optional). */
    private UserAndPassword auth;

    /**
     * Creates a new {@code ActionApi} instance with the given API endpoint.
     *
     * @param apiUrl the full MediaWiki API endpoint URL.
     */
    public ActionApi(String apiUrl) {
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
     * Sets a cookie store that persists session cookies in a file.
     *
     * @param file the file to store cookies.
     * @return this instance for method chaining.
     */
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
     * Associates an {@link UserAndPassword} instance with this API client.
     *
     * @param auth the authentication handler.
     * @return this instance for method chaining.
     */
    public ActionApi setAuth(UserAndPassword auth) {
        this.auth = auth;
        logger.info("Authentication set");
        return this;
    }

    /**
     * Builds the underlying HTTP client and initializes the {@link Requester}.
     * <p>
     * This must be called before sending any requests.
     * </p>
     */
    public void build() {
        try {
            CloseableHttpClient client = HttpClients.custom()
                    .setUserAgent(userAgent == null ? defaultUserAgent : userAgent)
                    .build();
            context.setCookieStore(cookieStore);
            this.requester = new Requester(client, apiUrl, globalParams, context);
            logger.info("ActionApi build completed with userAgent: "
                    + (userAgent == null ? defaultUserAgent : userAgent));
        } catch (Exception e) {
            logger.severe("Error during build: " + e.getMessage());
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
     * Returns the {@link UserAndPassword} instance associated with this API client.
     *
     * @return the authentication handler, or {@code null} if none is set.
     */
    public UserAndPassword getAuth() {
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
