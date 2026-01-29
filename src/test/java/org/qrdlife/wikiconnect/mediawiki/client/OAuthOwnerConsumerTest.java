package org.qrdlife.wikiconnect.mediawiki.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qrdlife.wikiconnect.mediawiki.client.Auth.OAuthOwnerConsumer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthOwnerConsumerTest {

    @Mock
    private ActionApi mockApi;

    @Mock
    private Requester mockRequester;

    private OAuthOwnerConsumer auth;
    private final String testToken = "testAccessToken123";
    private final String testRUsername = "OAuthUser";

    @BeforeEach
    void setUp() {
        when(mockApi.getRequester()).thenReturn(mockRequester);
        auth = new OAuthOwnerConsumer(testToken, mockApi);
    }

    @Test
    void testGetAuthHeaders() throws Exception {
        Map<String, String> headers = auth.getAuthHeaders();

        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertTrue(headers.containsKey("Authorization"));
        assertEquals("Bearer " + testToken, headers.get("Authorization"));
    }

    @Test
    void testIsLoggedInSuccess() throws Exception {
        when(mockRequester.get(eq("query"), any(Map.class)))
                .thenReturn("{"
                        + "\"query\": {"
                        + "\"userinfo\": {"
                        + "\"id\": 12345,"
                        + "\"name\": \"" + testRUsername + "\""
                        + "}"
                        + "}"
                        + "}");

        boolean result = auth.isLoggedIn();

        assertTrue(result);
        assertEquals(testRUsername, auth.getRUsername());
        verify(mockRequester, times(1)).get(eq("query"), any(Map.class));
    }

    @Test
    void testIsFailedLogin() throws Exception {
        // login() just calls isLoggedIn() in current implementation
        when(mockRequester.get(eq("query"), any(Map.class)))
                .thenReturn("{"
                        + "\"query\": {"
                        + "\"userinfo\": {"
                        + "\"id\": 12345,"
                        + "\"name\": \"" + testRUsername + "\""
                        + "}"
                        + "}"
                        + "}");

        boolean result = auth.login();
        assertTrue(result);
    }

    @Test
    void testLogout() throws Exception {
        // Logout is no-op, just verifying it doesn't throw
        assertDoesNotThrow(() -> auth.logout());
    }

    @Test
    void testGetRUsername() throws Exception {
        when(mockRequester.get(eq("query"), any(Map.class)))
                .thenReturn("{"
                        + "\"query\": {"
                        + "\"userinfo\": {"
                        + "\"id\": 12345,"
                        + "\"name\": \"" + testRUsername + "\""
                        + "}"
                        + "}"
                        + "}");

        String username = auth.getRUsername();
        assertEquals(testRUsername, username);
    }
}
