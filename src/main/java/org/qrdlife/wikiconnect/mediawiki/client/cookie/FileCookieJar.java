package org.qrdlife.wikiconnect.mediawiki.client.cookie;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A persistent cookie store backed by a file.
 * <p>
 * This class extends {@link BasicCookieStore} and adds support for
 * loading cookies from a file during initialization, and saving them
 * whenever cookies are added or cleared.
 * </p>
 *
 * <p>Use this class when you want to maintain session persistence
 * across application runs (e.g., MediaWiki logins).</p>
 *
 * <p>Typical usage example:</p>
 * <pre>{@code
 * FileCookieJar cookieJar = new FileCookieJar(new File("cookies.dat"));
 * }</pre>
 */
public class FileCookieJar extends BasicCookieStore {

    private static final Logger logger = Logger.getLogger(FileCookieJar.class.getName());

    /** The file used to persist cookies between sessions. */
    private final File file;

    /**
     * Constructs a new {@code FileCookieJar} with the given file.
     * <p>
     * If the file exists and is non-empty, cookies will be loaded automatically.
     * </p>
     *
     * @param file the file used for saving and loading cookies.
     */
    public FileCookieJar(File file) {
        this.file = file;
        this.loadCookies();
    }

    /**
     * Loads cookies from the file if it exists and is non-empty.
     * If the file is empty or corrupted, the store will simply start empty.
     */
    private void loadCookies() {
        if (!file.exists() || file.length() == 0) {
            return; // start with an empty store
        }

        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JSONArray jsonArray = new JSONArray(new JSONTokener(isr));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.optString("name", null);
                String value = obj.optString("value", null);
                if (name != null) {
                    BasicClientCookie cookie = new BasicClientCookie(name, value);
                    cookie.setDomain(obj.optString("domain", null));
                    cookie.setPath(obj.optString("path", null));
                    if (obj.has("expiry")) {
                        cookie.setExpiryDate(Instant.ofEpochMilli(obj.getLong("expiry")));
                    }
                    if (obj.has("secure")) {
                        cookie.setSecure(obj.getBoolean("secure"));
                    }
                    super.addCookie(cookie);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load cookies from file: " + file.getPath(), e);
        }
    }

    /**
     * Saves the current cookies to the file.
     * The cookies are serialized to JSON and written to disk.
     */
    private void saveCookies() {
        JSONArray jsonArray = new JSONArray();
        for (Cookie cookie : getCookies()) {
            JSONObject obj = new JSONObject();
            obj.put("name", cookie.getName());
            obj.put("value", cookie.getValue());
            obj.put("domain", cookie.getDomain());
            obj.put("path", cookie.getPath());
            if (cookie.getExpiryInstant() != null) {
                obj.put("expiry", cookie.getExpiryInstant().toEpochMilli());
            }
            obj.put("secure", cookie.isSecure());
            jsonArray.put(obj);
        }

        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            jsonArray.write(osw, 2, 0);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save cookies to file: " + file.getPath(), e);
        }
    }

    /**
     * Adds a cookie to the store and persists the updated list to the file.
     *
     * @param cookie the cookie to be added.
     */
    @Override
    public void addCookie(Cookie cookie) {
        super.addCookie(cookie);
        saveCookies();
    }

    /**
     * Clears all cookies from the store and updates the file
     * with the empty cookie list.
     */
    @Override
    public void clear() {
        super.clear();
        saveCookies();
    }
}
