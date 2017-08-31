package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.v2.service.GoogleVisionService;
import eu.europeana.api2.v2.service.MicrosoftVisionService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.UnsupportedEncodingException;

/**
 * @author Patrick Ehlert on 29-8-17.
 */
@RestController
@RequestMapping(value = "/v2")
public class VisionController {

    private static final Logger LOG = Logger.getLogger(ObjectController.class);

    private MicrosoftVisionService msVisionService;
    private GoogleVisionService gVisionService;

    @Autowired
    public VisionController(MicrosoftVisionService msVisionService, GoogleVisionService gVisionService) {
        this.msVisionService = msVisionService;
        this.gVisionService = gVisionService;
    }

    @RequestMapping(value = "/vision.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String analyzeImage(@RequestParam(value = "uri", required = true) String imageUrl,
                               @RequestParam(value = "service", required = false, defaultValue = "microsoft") String servicename,
                               @RequestParam(value = "confidence", required = false) String confidenceValue
                    ) throws UnsupportedEncodingException{

        if (StringUtils.isEmpty(imageUrl)) {
            throw new IllegalArgumentException("Image url is empty");
        }
        Double confidence = null;
        if (!StringUtils.isEmpty(confidenceValue)) {
            confidence = Double.valueOf(confidenceValue);
        }
        String url = java.net.URLDecoder.decode(imageUrl, "UTF-8");
        LOG.info("Image url = "+url+", confidence = "+confidence);

        if ("microsoft".equalsIgnoreCase(servicename)) {
            return msVisionService.analyze(url, confidence).toString();
        } else {
            return gVisionService.analyze(url).toString();
        }
    }




}
