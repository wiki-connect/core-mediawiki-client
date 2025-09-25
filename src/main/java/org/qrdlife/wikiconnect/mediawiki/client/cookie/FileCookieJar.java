package org.qrdlife.wikiconnect.mediawiki.client.cookie;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                for (Cookie cookie : (List<Cookie>) obj) {
                    addCookie(cookie);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // log or ignore, but don't crash
            e.printStackTrace();
        }
    }

    /**
     * Saves the current cookies to the file.
     * The cookies are serialized and written to disk.
     */
    private void saveCookies() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            List<Cookie> cookies = new ArrayList<>(getCookies());
            oos.writeObject(cookies);
        } catch (IOException e) {
            e.printStackTrace();
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
