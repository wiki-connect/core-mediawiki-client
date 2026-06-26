package org.qrdlife.wikiconnect.mediawiki.client;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;

import java.nio.charset.StandardCharsets;
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

    @Test
    void testGetRetryOnIOException() throws Exception {
        Requester retryRequester = new Requester(httpClient, "https://example.org/w/api.php", Map.of("formatversion", 2), context, 2);
        String action = "query";
        Map<String, Object> params = Map.of("list", "recentchanges");
        String expectedResponse = "{\"query\":{\"recentchanges\":[]}}";

        ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);

        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContent()).thenReturn(new java.io.ByteArrayInputStream(expectedResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockResponse.getCode()).thenReturn(200);

        when(httpClient.executeOpen(isNull(), any(ClassicHttpRequest.class), eq(context)))
                .thenThrow(new java.io.IOException("Transient Network Failure"))
                .thenReturn(mockResponse);

        String actualResponse = retryRequester.get(action, params);
        assertEquals(expectedResponse, actualResponse);

        verify(httpClient, times(2)).executeOpen(isNull(), any(ClassicHttpRequest.class), eq(context));
    }

    @Test
    void testGetRetryOn5xxError() throws Exception {
        Requester retryRequester = new Requester(httpClient, "https://example.org/w/api.php", Map.of("formatversion", 2), context, 2);
        String action = "query";
        Map<String, Object> params = Map.of("list", "recentchanges");
        String expectedResponse = "{\"query\":{\"recentchanges\":[]}}";

        ClassicHttpResponse mock500Response = mock(ClassicHttpResponse.class);
        when(mock500Response.getCode()).thenReturn(500);

        ClassicHttpResponse mock200Response = mock(ClassicHttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);
        when(mock200Response.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContent()).thenReturn(new java.io.ByteArrayInputStream(expectedResponse.getBytes(StandardCharsets.UTF_8)));
        when(mock200Response.getCode()).thenReturn(200);

        when(httpClient.executeOpen(isNull(), any(ClassicHttpRequest.class), eq(context)))
                .thenReturn(mock500Response)
                .thenReturn(mock200Response);

        String actualResponse = retryRequester.get(action, params);
        assertEquals(expectedResponse, actualResponse);

        verify(httpClient, times(2)).executeOpen(isNull(), any(ClassicHttpRequest.class), eq(context));
        verify(mock500Response).close();
    }

    @Test
    void testPostNoRetryOnIOException() throws Exception {
        Requester retryRequester = new Requester(httpClient, "https://example.org/w/api.php", Map.of("formatversion", 2), context, 2);
        String action = "edit";
        Map<String, Object> params = Map.of("title", "TestPage", "text", "content");

        when(httpClient.executeOpen(isNull(), any(ClassicHttpRequest.class), eq(context)))
                .thenThrow(new java.io.IOException("Failed on POST"));

        assertThrows(java.io.IOException.class, () -> retryRequester.post(action, params));
        verify(httpClient, times(1)).executeOpen(isNull(), any(ClassicHttpRequest.class), eq(context));
    }
}
