package eu.europeana.api2.v2.service;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick Ehlert on 29-8-17.
 */
@Configuration
public class MicrosoftVisionService {

    public static final String API_ENDPOINT = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/analyze";

    private static final Logger LOG = Logger.getLogger(MicrosoftVisionService.class);

    private static HttpClient httpclient = HttpClientBuilder.create().build();

    @Value("${vision.microsoft.apikey}")
    private String apikey;

    private static String TEST1 = "{\n" +
            "\n" +
            "    \"metadata\": {\n" +
            "        \"width\": 1024,\n" +
            "        \"format\": \"Jpeg\",\n" +
            "        \"height\": 683\n" +
            "    },\n" +
            "    \"requestId\": \"5cb8a430-fbd1-4d6c-aadf-404da185b9dc\",\n" +
            "    \"description\": {\n" +
            "        \"captions\": [\n" +
            "            {\n" +
            "                \"confidence\": 0.9735205878320508,\n" +
            "                \"text\": \"a group of people walking in front of a building\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"tags\": [\n" +
            "            \"building\",\n" +
            "            \"outdoor\",\n" +
            "            \"road\",\n" +
            "            \"people\",\n" +
            "            \"mountain\",\n" +
            "            \"group\",\n" +
            "            \"walking\",\n" +
            "            \"city\",\n" +
            "            \"standing\",\n" +
            "            \"man\",\n" +
            "            \"front\",\n" +
            "            \"large\",\n" +
            "            \"top\",\n" +
            "            \"street\",\n" +
            "            \"riding\",\n" +
            "            \"sitting\",\n" +
            "            \"park\",\n" +
            "            \"bench\",\n" +
            "            \"clock\",\n" +
            "            \"hill\",\n" +
            "            \"tall\",\n" +
            "            \"old\",\n" +
            "            \"bus\",\n" +
            "            \"tower\",\n" +
            "            \"train\",\n" +
            "            \"parked\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"categories\": [\n" +
            "        {\n" +
            "            \"score\": 0.265625,\n" +
            "            \"name\": \"building_\",\n" +
            "            \"detail\": {\n" +
            "                \"landmarks\": [\n" +
            "                    {\n" +
            "                        \"confidence\": 0.999429643,\n" +
            "                        \"name\": \"Potala Palace\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"score\": 0.03515625,\n" +
            "            \"name\": \"outdoor_\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"score\": 0.453125,\n" +
            "            \"name\": \"outdoor_oceanbeach\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"faces\": [ ],\n" +
            "    \"tags\": [\n" +
            "        {\n" +
            "            \"confidence\": 0.991840124130249,\n" +
            "            \"name\": \"building\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"confidence\": 0.9830043315887451,\n" +
            "            \"name\": \"sky\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"confidence\": 0.9808586239814758,\n" +
            "            \"name\": \"outdoor\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"confidence\": 0.7771872878074646,\n" +
            "            \"name\": \"people\"\n" +
            "        }\n" +
            "    ]\n" +
            "\n" +
            "}";

    // TODO add OCR / handwriting recognition?

    public JSONObject analyze(String url, Double confidence) {

        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Provided image url is empty!");
        }

        try {
            URIBuilder builder = new URIBuilder(API_ENDPOINT);

            // Request parameters. All of them are optional.
            builder.setParameter("visualFeatures", "Tags,Categories,Description,Faces");
            builder.setParameter("details", "Landmarks,Celebrities");
            builder.setParameter("language", "en");

            // Prepare the URI for the REST API call.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apikey);

            // Request body.
            StringEntity reqEntity = new StringEntity("{\"url\":\"" + url + "\"}");
            request.setEntity(reqEntity);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Vision request = " + request.getURI());
                LOG.debug("  url  = " + url);
                LOG.debug("  json = " + reqEntity);
            }

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Format and display the JSON response.
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("MS vision unfiltered response:\n" + json.toString(2));
                }
                if (confidence == null) {
                    return json;
                }

                JSONObject filteredJson = filterResponse(json, confidence);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("MS vision filtered response:\n" + filteredJson.toString(2));
                }
                return filteredJson;
            }
        } catch (Exception e) {
            LOG.error("Error analyzing image", e);
        }
        return null;
    }

    /**
     * Filters out all tags, categories and descriptions in the Microsoft Vision json response that have a confidence/score
     * lower than the specified value
     * @param json
     * @param minConfidence
     * @return
     * @throws IOException
     */
    public JSONObject filterResponse(JSONObject json, Double minConfidence) throws IOException {
        // filter description/caption
        ObjectMapper mapper = new ObjectMapper();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtering with confidence "+minConfidence);
        }

        Map jsonMap = mapper.readValue(json.toString(), Map.class);
        if (json.has("tags")) {
            json.put("tags", filterList((List<Object>) jsonMap.get("tags"), minConfidence, "confidence"));
        }

        JSONArray categories = json.optJSONArray("categories");
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                LOG.debug("  category = "+category);
                //celebrities and landmarks are part of categories (under detail)
                // TODO category can have much lower score than a detail, so then detail will be filtered out (see TEST1 JSON)
                JSONObject detail = category.optJSONObject("detail");
                if (detail != null) {
                    Map detailMap = mapper.readValue(detail.toString(), Map.class);
                    if (detail.has("landmarks")) {
                        detail.put("landmarks", filterList((List<Object>) detailMap.get("landmarks"), minConfidence, "confidence"));
                    }
                    if (detail.has("celebrities")) {
                        detail.put("celebrities", filterList((List<Object>) detailMap.get("celebrities"), minConfidence, "confidence"));
                    }
                }
            }

            json.put("categories", filterList((List<Object>) jsonMap.get("categories"), minConfidence, "score"));
        }
        if (json.has("description")) {

            JSONObject description = json.getJSONObject("description");
            if (description.has("captions")) {
                Map descriptionMap = mapper.readValue(description.toString(), Map.class);
                description.put("captions", filterList((List<Object>) descriptionMap.get("captions"), minConfidence, "confidence"));
            }
            // we also remove all tags under description since they do not have any score
            if (description.has("tags")) {
                description.remove("tags");
            }
        }
        return json;
    }


    private List<Object> filterList(List<Object> list, Double minConfidence, String fieldName) {
        return Arrays.asList(list.stream().filter(item -> Double.valueOf(((Map)item).get(fieldName).toString()) > minConfidence).toArray());
    }

    /**
     * For debug purposes
     * @param args
     */
    public static void main(String[] args) {
        MicrosoftVisionService service = new MicrosoftVisionService();
        service.apikey = "4eea3202476e4dc5a1e2bb77bcac3ad2";

        JSONObject test1 = new JSONObject(TEST1);

        LOG.error(test1);
        try {
            LOG.error(service.filterResponse(test1, 0.7));
        }
        catch (IOException e) {
            LOG.error("Error filtering response", e);
        }
        //service.analyze("https://2culture.locloudhosting.net/files/original/a09970a27c8bad51442a0dcdb97f9e1c.jpg", 0.7);
    }


}
