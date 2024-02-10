package bg.sofia.uni.fmi.mjt.cryptowallet.apicall;

import bg.sofia.uni.fmi.mjt.cryptowallet.database.Logs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiCall {
    public static final String API_ENDPOINT_SCHEME = "https";
    public static final String API_ENDPOINT_HOST = "rest.coinapi.io";
    public static final String API_ENDPOINT_PATH = "/v1/assets/";

    private final HttpClient httpClient;
    private final String apiKey;

    public ApiCall(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
    }

    public HttpResponse<String> getResponse() {
        HttpResponse<String> httpResponse = null;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_PATH + "APIKEY-" + apiKey, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(), "URI is invalid.");
        } catch (IOException | InterruptedException e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(),
                "Problem occurred while trying to send request to API.");
        }

        return httpResponse;
    }
}