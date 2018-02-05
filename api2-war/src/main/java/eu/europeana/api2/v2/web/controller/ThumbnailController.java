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

package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.domain.MediaFile;
import eu.europeana.corelib.web.service.MediaStorageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Retrieves image thumbnails.
 * The thumbnail API doesn't require any form of authentication, providing an API key is optional.
 */
@RestController
public class ThumbnailController {

    private static final Logger LOG = LogManager.getLogger(ThumbnailController.class);

    private static final String IIIF_HOST_NAME = "iiif.europeana.eu";

    @Resource(name = "amazon_S3Service")
    private MediaStorageService amazonS3Service;

    @Resource(name = "bluemix_S3Service")
    private MediaStorageService bluemixS3Service;

    /**
     * Retrieves image thumbnails.
     * @param url optional, the URL of the media resource of which a thumbnail should be returned. Note that the URL should be encoded.
     *            When no url is provided a default thumbnail will be returned
     * @param size optional, the size of the thumbnail, can either be w200 (width 200) or w400 (width 400).
     * @param type optional, type of the default thumbnail (media image) in case the thumbnail does not exists or no url is provided,
     *             can be: IMAGE, SOUND, VIDEO, TEXT or 3D.
     * @param webRequest
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/thumbnail-by-url.json", method = RequestMethod.GET)
    public ResponseEntity<byte[]> thumbnailByUrl(
            @RequestParam(value = "uri", required = true) String url,
            @RequestParam(value = "size", required = false, defaultValue = "w400") String size,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            WebRequest webRequest, HttpServletResponse response) throws IOException {

        long startTime = 0;
        if (LOG.isDebugEnabled()) {
            startTime = System.nanoTime();
            LOG.debug("Thumbnail url = {}, size = {}, type = {}", url, size, type);
        }

        ControllerUtils.addResponseHeaders(response);
        final HttpHeaders headers = new HttpHeaders();
        final String mediaFileId = computeResourceUrl(url, size);

        // First try Bluemix S3 ....
        MediaFile mediaFile = bluemixS3Service.retrieve(mediaFileId, Boolean.TRUE);
        if (mediaFile == null) {
            // if not found then try to download the thumbnail from Amazon S3 ...
            mediaFile = amazonS3Service.retrieve(mediaFileId, Boolean.TRUE);
            if (mediaFile == null && ThumbnailController.isIiifRecordUrl(url)) {
                // if still no luck, try to generate a IIIF thumbnail (see EA-892)
                try {
                    String width = (StringUtils.equalsIgnoreCase(size, "w200") ? "200" : "400");
                    URI iiifUri = ThumbnailController.getIiifThumbnailUrl(url, width);
                    LOG.debug("IIIF url = {} ", iiifUri.getPath());
                    mediaFile = downloadImage(iiifUri);
                    if (mediaFile != null) {
                        LOG.debug("Thumbnail {} generated from IIIF", mediaFileId);
                    }
                } catch (URISyntaxException e) {
                    LOG.error("Error reading IIIF thumbnail url", e);
                } catch (IOException io) {
                    LOG.error("Error retrieving IIIF thumbnail image", io);
                }
            } else {
                LOG.debug("Thumbnail {} found on Amazon S3", mediaFileId);
            }
        } else {
            LOG.debug("Thumbnail {} found on IBM Cloud", mediaFileId);
        }

         // Check if we have an image, if not show default 'type' icon
         byte[] mediaContent;
         ResponseEntity result;
         if (mediaFile == null) {
            headers.setContentType(MediaType.IMAGE_PNG);
            mediaContent = getDefaultThumbnailForNotFoundResourceByType(type);
            result = new ResponseEntity<>(mediaContent, headers, HttpStatus.OK);
        } else {
            // prepare final response
            headers.setContentType(MediaType.IMAGE_JPEG);
            mediaContent = mediaFile.getContent();
            result = new ResponseEntity<>(mediaContent, headers, HttpStatus.OK);

            // finally check if we should return the full response, or a 304
            // the check below automatically sets an ETag and last-Modified in our response header and returns a 304
            // (but only when clients include the If_Modified_Since header in their request)
            if ((mediaFile.getCreatedAt() == null && webRequest.checkNotModified(mediaFile.getContentMd5())) ||
                 (webRequest.checkNotModified(mediaFile.getContentMd5(), mediaFile.getCreatedAt().getMillis()))){
                result = null;
            }
         }

        if (LOG.isDebugEnabled()) {
            Long duration = (System.nanoTime() - startTime) / 1000;
            if (MediaType.IMAGE_PNG.equals(headers.getContentType())) {
                LOG.debug("Total thumbnail request time (missing media): {}",duration);
            } else {
                if (result == null) {
                    LOG.debug("Total thumbnail request time (from s3 + return 304): {}", duration);
                } else {
                    LOG.debug("Total thumbnail request time (from s3 + return 200): {}", duration);
                }
            }
        }
        return result;
    }

    /**
     * Check if the provided url is a thumbnail hosted on iiif.europeana.eu.
     * @param url
     * @return true if the provided url is a thumbnail hosted on iiif.europeana.eu, otherwise false
     */
    public static boolean isIiifRecordUrl(String url) {
        String urlLowercase = url.toLowerCase(Locale.getDefault());
        return (urlLowercase.startsWith("http://" + IIIF_HOST_NAME) || urlLowercase.startsWith("https://" + IIIF_HOST_NAME));
    }

    /**
     * All 3 million IIIF newspaper thumbnails have not been processed yet in CRF (see also Jira EA-892) but the
     * edmPreview field will point to the default IIIF image url, so if we slightly alter that url the IIIF
     * API will generate a thumbnail in the appropriate size for us on-the-fly
     * Note that this is a temporary solution until all newspaper thumbnails are processed by CRF.
     * @param url
     * @param width, desired image width
     * @return thumbnail URI for iiif urls, otherwise null
     * @throws URISyntaxException
     */
    public static URI getIiifThumbnailUrl(String url, String width) throws URISyntaxException {
        // all urls are encoded so they start with either http:// or https://
        // and end with /full/full/0/default.<extension>.
        if (url != null && isIiifRecordUrl(url)) {
            return new URI(url.replace("/full/full/0/default.", "/full/" +width+ ",/0/default."));
        }
        return null;
    }

    /**
     * Download (IIIF) image from external location
     * @param uri
     * @return
     * @throws IOException
     */
    private MediaFile downloadImage(URI uri) throws IOException {
        try (InputStream in = new BufferedInputStream(uri.toURL().openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            // for now we don't do anything with LastModified
            return new MediaFile(null, null, null,getMD5(uri.getPath()), uri.getPath(), null, out.toByteArray(), null, null, null, 0);
        }
    }

    /**
     * Retrieve the default thumbnail image as a byte array
     * @param path
     * @return
     */
    private byte[] getImage(String path) {
        byte[] result = null;
        try (InputStream in = this.getClass().getResourceAsStream(path)){
            result = IOUtils.toByteArray(in);
        } catch (IOException e) {
            LOG.error("Error reading default thumbnail file", e);
        }
        return result;
    }


    private String getMD5(String resourceUrl) {
        String resource = (resourceUrl == null ? "" : resourceUrl);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(resource.getBytes(StandardCharsets.UTF_8));
            final byte[] resultByte = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aResultByte : resultByte) {
                sb.append(Integer.toString((aResultByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error determining MD5 for resource {}", resourceUrl, e);
        }
        return resourceUrl;
    }

    //@Cacheable
    private byte[] getDefaultThumbnailForNotFoundResourceByType(final String type) {
        switch (StringUtils.upperCase(type)) {
            case "IMAGE":
                return getImage("/images/EU_thumbnails_image.png");
            case "SOUND":
                return getImage("/images/EU_thumbnails_sound.png");
            case "VIDEO":
                return getImage("/images/EU_thumbnails_video.png");
            case "TEXT":
                return getImage("/images/EU_thumbnails_text.png");
            case "3D":
                return getImage("/images/EU_thumbnails_3d.png");
            default:
                return getImage("/images/EU_thumbnails_image.png");
        }

    }

    /**
     * Convert the provided url and size into a string representing the id of the media file.
     * @param resourceUrl
     * @param resourceSize
     * @return
     */
    private String computeResourceUrl(final String resourceUrl, final String resourceSize) {
        return getMD5(resourceUrl) + "-" + (StringUtils.equalsIgnoreCase(resourceSize, "w200") ? "MEDIUM" : "LARGE");
    }
}
