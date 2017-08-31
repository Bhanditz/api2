package eu.europeana.api2.v2.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * @author Patrick Ehlert on 29-8-17.
 */
@Configuration
public class GoogleVisionService {

    public static final String API_ENDPOINT = "https://vision.googleapis.com/v1/images:annotate";

    // TODO Activate apikey (need to setup billing account!?)

    private static final String BASIC_REQUEST = "{\n" +
            "  \"requests\":[\n" +
            "    {\n" +
            "      \"image\":{\n" +
            "        \"source\":{\n" +
            "          \"imageUri\": \"<PLACEHOLDER>\"'n" +
            "        },\n" +
            "      },\n" +
            "      \"features\":[\n" +
            "        {\n" +
            "          \"type\":\"LABEL_DETECTION\",\n" +
            "        },\n" +
            "        {\n" +
            "          \"type\":\"LOGO_DETECTION\",\n" +
            "        },\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static final Logger LOG = Logger.getLogger(MicrosoftVisionService.class);

    private static HttpClient httpclient = HttpClientBuilder.create().build();

    @Value("${vision.google.apikey}")
    private String apikey;

    public JSONObject analyze(String url) {
        try {
            URIBuilder builder = new URIBuilder(API_ENDPOINT);

            // Request parameters. Key is required
            builder.setParameter("key", apikey);

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/json");

            // Create request body
            StringEntity reqEntity = new StringEntity(BASIC_REQUEST.replace("<PLACEHOLDER>", url));
            request.setEntity(reqEntity);

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Format and display the JSON response.
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                LOG.info("Google vision response:" + json.toString(2));
                return json;
            }

        } catch (Exception e) {
            LOG.error("Error analyzing image", e);
        }
        return null;
    }
}
