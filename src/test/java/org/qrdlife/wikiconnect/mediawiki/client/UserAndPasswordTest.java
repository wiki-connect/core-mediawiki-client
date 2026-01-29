package org.qrdlife.wikiconnect.mediawiki.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qrdlife.wikiconnect.mediawiki.client.Auth.UserAndPassword;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAndPasswordTest {

    @Mock
    private ActionApi mockApi;

    @Mock
    private Requester mockRequester;

    private UserAndPassword auth;
    private final String testUsername = "testuser";
    private final String testPassword = "testpass";
    private final String testToken = "testToken123";
    private final String testRUsername = "TestUser";

    @BeforeEach
    void setUp() {
        when(mockApi.getRequester()).thenReturn(mockRequester);
        auth = new UserAndPassword(testUsername, testPassword, mockApi);
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(mockApi.getToken("login")).thenReturn(testToken);
        when(mockRequester.get("query", Map.of("meta", "userinfo", "format", "json")))
                .thenReturn("{\"query\":{\"userinfo\":{\"id\":0,\"name\":\"*\"}}}\n");
        when(mockRequester.post(eq("login"), any(Map.class)))
                .thenReturn("{"
                        + "\"login\": {"
                        + "\"result\": \"Success\","
                        + "\"lgusername\": \"" + testRUsername + "\""
                        + "}"
                        + "}"
                );

        boolean result = auth.login();

        assertTrue(result);
        assertEquals(testRUsername, auth.getRUsername());
        verify(mockApi).getToken("login");
        verify(mockRequester).post(eq("login"), any(Map.class));
    }

    @Test
    void testLoginFailure() throws Exception {
        when(mockApi.getToken("login")).thenReturn(testToken);
        when(mockRequester.get("query", Map.of("meta", "userinfo", "format", "json")))
                .thenReturn("{\"query\":{\"userinfo\":{\"id\":0,\"name\":\"*\"}}}\n");        when(mockRequester.post(eq("login"), any(Map.class)))
                .thenReturn("{"
                        + "\"login\": {"
                        + "\"result\": \"Failed\","
                        + "\"reason\": \"Incorrect password\""
                        + "}"
                        + "}"
                );

        boolean result = auth.login();

        assertFalse(result);
        verify(mockApi).getToken("login");
        verify(mockRequester).post(eq("login"), any(Map.class));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        // Arrange - login first
        when(mockRequester.get("query", Map.of("meta", "userinfo", "format", "json")))
                .thenReturn("{\"query\":{\"userinfo\":{\"id\":123,\"name\":\"" + testRUsername + "\"}}}\n");
        auth.login();

        // Only stub what is actually called during logout
        when(mockApi.getToken("csrf")).thenReturn(testToken);
        when(mockRequester.post(eq("logout"), any(Map.class)))
                .thenReturn("{ \"logout\": {\"result\": \"Success\"}}");

        assertDoesNotThrow(() -> auth.logout());
        verify(mockApi).getToken("csrf");
        verify(mockRequester).post(eq("logout"), any(Map.class));
    }

    @Test
    void testIsLoggedInWhenNotLoggedIn() throws Exception {
        when(mockRequester.get(eq("query"), any(Map.class)))
                .thenReturn("{"
                        + "\"query\": {"
                        + "\"userinfo\": {"
                        + "\"id\": 0,"
                        + "\"name\": \"\""
                        + "}"
                        + "}"
                        + "}"
                );

        boolean result = auth.isLoggedIn();

        assertFalse(result);
        verify(mockRequester).get(eq("query"), any(Map.class));
    }

    @Test
    void testIsLoggedInWhenLoggedIn() throws Exception {
        when(mockRequester.get(eq("query"), any(Map.class)))
                .thenReturn("{"
                        + "\"query\": {"
                        + "\"userinfo\": {"
                        + "\"id\": 12345,"
                        + "\"name\": \"" + testRUsername + "\""
                        + "}"
                        + "}"
                        + "}"
                );

        boolean result = auth.isLoggedIn();

        assertTrue(result);
        assertEquals(testRUsername, auth.getRUsername());
        verify(mockRequester).get(eq("query"), any(Map.class));
    }

    @Test
    void testGetRUsername() throws Exception {
        when(mockRequester.get(eq("query"), any(Map.class)))
                .thenReturn("{"
                        + "\"query\": {"
                        + "\"userinfo\": {"
                        + "\"name\": \"" + testRUsername + "\""
                        + "}"
                        + "}"
                        + "}"
                );

        String result = auth.getRUsername();

        assertEquals(testRUsername, result);
        verify(mockRequester).get(eq("query"), any(Map.class));
    }

    @Test
    void testGetUsername() {
        String result = auth.getUsername();
        assertEquals(testUsername, result);
    }
}
