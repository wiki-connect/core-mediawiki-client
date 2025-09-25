package org.qrdlife.wikiconnect.mediawiki.client;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequesterTest {

    private CloseableHttpClient httpClient;
    private HttpClientContext context;
    private Requester requester;

    @BeforeEach
    void setUp() {

        httpClient = mock(CloseableHttpClient.class);
        context = mock(HttpClientContext.class);
        requester = new Requester(httpClient, "https://example.org/w/api.php", Map.of("formatversion", 2), context);
    }

    @Test
    void testGetSuccess() throws Exception {
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getEntity()).thenReturn(new StringEntity("{\"query\":{\"result\":\"Success\"}}"));
        when(httpClient.executeOpen(any(), any(), any())).thenReturn(response);

        String result = requester.get("query", Map.of("prop", "info"));
        assertTrue(result.contains("\"query\""));
        verify(httpClient).executeOpen(any(), any(), eq(context));
        response.close();
    }

    @Test
    void testPostSuccess() throws Exception {
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getEntity()).thenReturn(new StringEntity("{\"edit\":{\"result\":\"Success\"}}"));
        when(httpClient.executeOpen(any(), any(), any())).thenReturn(response);

        String result = requester.post("edit", Map.of("title", "Test"));
        assertTrue(result.contains("\"edit\""));
        response.close();
    }

    @Test
    void testEmptyResponseThrows() throws Exception {
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getEntity()).thenReturn(null);
        when(httpClient.executeOpen(any(), any(), any())).thenReturn(response);

        assertThrows(org.qrdlife.wikiconnect.mediawiki.client.Exception.EmptyResponseException.class, () ->
                requester.get("query", Map.of())
        );
        response.close();
    }

    @Test
    void testApiErrorThrows() throws Exception {
        String errorJson = new JSONObject()
                .put("error", new JSONObject().put("code", "badrequest").put("info", "Bad request"))
                .toString();
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getEntity()).thenReturn(new StringEntity(errorJson));
        when(httpClient.executeOpen(any(), any(), any())).thenReturn(response);

        assertThrows(org.qrdlife.wikiconnect.mediawiki.client.Exception.UsageException.class, () ->
                requester.get("query", Map.of())
        );
        response.close();
    }

    @Test
    void testActionFailedThrows() throws Exception {
        String failJson = new JSONObject()
                .put("edit", new JSONObject().put("result", "Failed").put("reason", "Permission denied"))
                .toString();
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getEntity()).thenReturn(new StringEntity(failJson));
        when(httpClient.executeOpen(any(), any(), any())).thenReturn(response);

        assertThrows(org.qrdlife.wikiconnect.mediawiki.client.Exception.UsageException.class, () ->
                requester.post("edit", Map.of())
        );
        response.close();
    }
}
