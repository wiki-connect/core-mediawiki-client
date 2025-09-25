package org.qrdlife.wikiconnect.mediawiki.client.cookie;

import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.cookie.Cookie;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileCookieJarTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = File.createTempFile("cookies", ".dat");
        tempFile.deleteOnExit();
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testAddAndPersistCookie() {
        FileCookieJar jar = new FileCookieJar(tempFile);
        BasicClientCookie cookie = new BasicClientCookie("test", "value");
        cookie.setDomain("example.com");
        jar.addCookie(cookie);

        // Reload from file
        FileCookieJar loadedJar = new FileCookieJar(tempFile);
        List<Cookie> cookies = loadedJar.getCookies();
        assertEquals(1, cookies.size());
        assertEquals("test", cookies.get(0).getName());
        assertEquals("value", cookies.get(0).getValue());
        assertEquals("example.com", cookies.get(0).getDomain());
    }

    @Test
    void testClearCookies() {
        FileCookieJar jar = new FileCookieJar(tempFile);
        BasicClientCookie cookie = new BasicClientCookie("test", "value");
        cookie.setDomain("example.com");
        jar.addCookie(cookie);

        jar.clear();

        FileCookieJar loadedJar = new FileCookieJar(tempFile);
        assertTrue(loadedJar.getCookies().isEmpty());
    }

    @Test
    void testLoadFromNonexistentFile() {
        File nonExistent = new File(tempFile.getParent(), "doesnotexist.dat");
        assertFalse(nonExistent.exists());
        FileCookieJar jar = new FileCookieJar(nonExistent);
        assertTrue(jar.getCookies().isEmpty());
    }
}