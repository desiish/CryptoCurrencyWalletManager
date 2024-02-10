package bg.sofia.uni.fmi.mjt.cryptowallet.apicall;

import bg.sofia.uni.fmi.mjt.cryptowallet.asset.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.AvailableAssets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Set;

public class ApiCallRunnable implements Runnable {
    private final Gson gson;
    private final ApiCall apiCall;
    private final AvailableAssets availableAssets;

    public ApiCallRunnable(ApiCall apiCall, AvailableAssets availableAssets) {
        this.apiCall = apiCall;
        this.availableAssets = availableAssets;
        gson = new Gson();
    }

    @Override
    public void run() {
        HttpResponse<String> response = apiCall.getResponse();
        if (response != null) {
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                Type type = new TypeToken<Set<Asset>>() { }.getType();
                Set<Asset> data = gson.fromJson(response.body(), type);
                availableAssets.updateAvailableAssets(data);
            } else {
                throw new RuntimeException("A problem occurred with response, response code " +
                    response.statusCode() + "was given.");
            }
        } else {
            throw new RuntimeException("Response from API was null.");
        }
    }
}
