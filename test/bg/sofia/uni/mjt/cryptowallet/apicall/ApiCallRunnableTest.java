package bg.sofia.uni.mjt.cryptowallet.apicall;

import bg.sofia.uni.fmi.mjt.cryptowallet.apicall.ApiCall;
import bg.sofia.uni.fmi.mjt.cryptowallet.apicall.ApiCallRunnable;
import bg.sofia.uni.fmi.mjt.cryptowallet.asset.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiCallRunnableTest {
    @Mock
    public HttpResponse<String> response;

    public static AvailableAssets availableAssets;
    public static ApiCallRunnable apiCallRunnable;
    public static ApiCall apiCall;

    @BeforeAll
    public static void setup() {
        apiCall = mock(ApiCall.class);
        availableAssets = new AvailableAssets();
        apiCallRunnable = new ApiCallRunnable(apiCall, availableAssets);
    }

    @Test
    public void testRunUpdatedInformation() {
        when(apiCall.getResponse()).thenReturn(response);
        when(response.statusCode()).thenReturn(HTTP_OK);
        when(response.body()).thenReturn("[\n" +
            "  {\n" +
            "    \"asset_id\": \"BTC\",\n" +
            "    \"name\": \"Bitcoin\",\n" +
            "    \"type_is_crypto\": 1,\n" +
            "    \"price_usd\": 10.0\n" +
            "  }\n" +
            "]");

        apiCallRunnable.run();

        Set<Asset> expectedCryptoCoins = new HashSet<>();
        expectedCryptoCoins.add(new Asset("BTC", "Bitcoin", 1, 10.0));

        assertEquals(expectedCryptoCoins, availableAssets.getAvailableAssets(),
            "Run method is not working properly.");
    }
}
