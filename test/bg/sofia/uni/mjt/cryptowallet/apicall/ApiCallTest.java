package bg.sofia.uni.mjt.cryptowallet.apicall;

import bg.sofia.uni.fmi.mjt.cryptowallet.apicall.ApiCall;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiCallTest {

    @Mock
    public HttpClient httpClient;
    @Mock
    public HttpResponse<String> response;

    public ApiCall apiCall;

    @Test
    public void testGetResponse() throws IOException, InterruptedException {
        apiCall = new ApiCall(httpClient, "testKey");
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);
        assertEquals(response, apiCall.getResponse(), "Response is not valid.");
    }
}