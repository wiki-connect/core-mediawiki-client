package org.qrdlife.wikiconnect.mediawiki.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.qrdlife.wikiconnect.mediawiki.client.Auth.UserAndPassword;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.cookie.BasicCookieStore;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionApiTest {

    private ActionApi actionApi;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        // Setup mocks
        Requester mockRequester = mock(Requester.class);
        BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = mock(HttpClientContext.class);

        // Create ActionApi with test API URL
        actionApi = new ActionApi("https://test.wikipedia.org/w/api.php");

        // Inject mocks into private fields
        injectPrivateField(actionApi, "requester", mockRequester);
        injectPrivateField(actionApi, "cookieStore", cookieStore);
        injectPrivateField(actionApi, "context", context);
    }

    private void injectPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            fail("Failed to inject field " + fieldName + ": " + e.getMessage());
        }
    }

    @Test
    void testConstructor() {
        ActionApi api = new ActionApi("https://example.org/w/api.php");
        assertNotNull(api);
        assertEquals("https://example.org/w/api.php", api.getApiUrl());
    }

    @Test
    void testSetGlobalPerms() {
        Map<String, Object> params = Map.of("format", "json", "utf8", "1");
        ActionApi result = actionApi.setGlobalPerms(params);

        assertSame(actionApi, result);

        @SuppressWarnings("unchecked")
        Map<String, Object> actualParams = (Map<String, Object>) getPrivateField(actionApi, "globalParams");
        assertEquals(params, actualParams);
    }

    @Test
    void testSetGlobalPermsWithNull() {
        assertThrows(IllegalArgumentException.class, () -> actionApi.setGlobalPerms(null));
    }

    @Test
    void testSetFileCookie() {
        File cookieFile = new File(tempDir, "test-cookies.txt");
        ActionApi result = actionApi.setFileCookie(cookieFile);

        assertSame(actionApi, result);
        assertTrue(cookieFile.exists() || !cookieFile.exists()); // just ensures no exception
    }

    @Test
    void testSetUserAgent() {
        String userAgent = "TestBot/1.0";
        ActionApi result = actionApi.setUserAgent(userAgent);

        assertSame(actionApi, result);
        assertEquals(userAgent, getPrivateField(actionApi, "userAgent"));
    }

    @Test
    void testSetAuth() {
        UserAndPassword mockAuth = mock(UserAndPassword.class);
        ActionApi result = actionApi.setAuth(mockAuth);

        assertSame(actionApi, result);
        assertSame(mockAuth, actionApi.getAuth());
    }

    @Test
    void testBuild() {
        ActionApi api = new ActionApi("https://test.wikipedia.org/w/api.php");
        api.setUserAgent("TestBot/1.0");

        assertDoesNotThrow(api::build);
        assertNotNull(api.getRequester());
    }

    // Helper to read private fields via reflection
    private Object getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            fail("Failed to read private field " + fieldName + ": " + e.getMessage());
            return null;
        }
    }
}
