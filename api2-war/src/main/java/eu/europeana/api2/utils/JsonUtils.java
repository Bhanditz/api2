/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class to generate Json output
 */
public class JsonUtils {

    private static final Logger LOG = LogManager.getLogger(JsonUtils.class);

    /** Create a single objectMapper we can reuse because that's more efficient,
     * https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static ModelAndView toJson(Object object) {
        return toJson(object, null);
    }

    public static ModelAndView toJson(Object object, String callback) {
        try {
            // Note that writeValueAsString is the slowest way of returning results. It's probably faster to use the
            // response outputstream, but that would require changing the way all API controllers return data
            return toJson(OBJECT_MAPPER.writeValueAsString(object), callback);
        } catch (IOException e) {
            String msg = "Json Generation Exception: " + e.getMessage();
            LOG.error(msg, e);
            ModelAndView error = toJson(msg, callback);
            error.addObject("success", false);
            error.addObject("error", e.getMessage());
            return error;
        }
    }

    public static ModelAndView toJson(String json, String callback) {
        String resultPage = "json";
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("json", json);
        if (StringUtils.isNotBlank(callback)) {
            resultPage = "jsonp";
            model.put("callback", callback);
        }
        return new ModelAndView(resultPage, model);
    }


}
