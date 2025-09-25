package org.qrdlife.wikiconnect.mediawiki.client;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    private Requester mockRequester;
    private Token token;

    @BeforeEach
    void setUp() {
        mockRequester = mock(Requester.class);
        token = new Token(mockRequester);
    }

    @Test
    void testGetReturnsToken() throws Exception {
        String type = "login";
        String expectedToken = "abc123+\\\\"; // double backslash for Java string, which becomes a single backslash in JSON
        String jsonResponse = "{ \"query\": { \"tokens\": { \"logintoken\": \"" + expectedToken + "\" } } }";

        when(mockRequester.get(eq("query"), anyMap())).thenReturn(jsonResponse);

        String actualToken = token.get(type);

        assertEquals("abc123+\\", actualToken); // expectedToken in Java is "abc123+\\"
        verify(mockRequester).get(eq("query"), anyMap());
    }


    @Test
    void testGetReturnsNullIfTokenMissing() throws Exception {
        String type = "csrf";
        String jsonResponse = "{ \"query\": { \"tokens\": { } } }";

        when(mockRequester.get(eq("query"), anyMap())).thenReturn(jsonResponse);

        String actualToken = token.get(type);

        assertNull(actualToken);
    }

    @Test
    void testGetThrowsJSONExceptionOnMalformedJson() throws Exception {
        String type = "login";
        String malformedJson = "{ not a valid json }";

        when(mockRequester.get(eq("query"), anyMap())).thenReturn(malformedJson);

        assertThrows(JSONException.class, () -> token.get(type));
    }
}