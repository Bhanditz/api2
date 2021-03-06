package eu.europeana.api2.model.utils;

import eu.europeana.corelib.definitions.ApplicationContextContainer;
import eu.europeana.corelib.definitions.EuropeanaStaticUrl;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.web.service.impl.EuropeanaUrlBuilder;
import eu.europeana.corelib.web.utils.UrlBuilder;
import org.apache.commons.lang.StringUtils;


/**
 * For generating often used links. Note that if an alternative portal.baseurl or api2.baseurl is defined in the
 * europeana.properties file then those base urls are used
 * @author Patrick Ehlert
 * Created on 10-10-2018
 */

public class Api2UrlService {

    private String portalBaseUrl;
    private String api2BaseUrl;

    /**
     * Provides quick access to this class
     * @return instance of the Api2UrlBuilder bean
     */
    public static Api2UrlService getBeanInstance() {
        return ApplicationContextContainer.getBean(Api2UrlService.class);
    }

    public Api2UrlService(String portalBaseUrl, String api2BaseUrl) {
        this.portalBaseUrl = portalBaseUrl;
        this.api2BaseUrl = api2BaseUrl;
    }

    /**
     * @return either the default or alternative configured base url for the main Europeana website (a.k.a. Portal)
     */
    public String getPortalBaseUrl() {
        if (StringUtils.isEmpty(portalBaseUrl)) {
            return EuropeanaStaticUrl.EUROPEANA_PORTAL_URL;
        }
        return portalBaseUrl;
    }

    /**
     * @return either the default or alternative configured base url for the API
     */
    public String getApi2BaseUrl() {
        if (StringUtils.isEmpty(api2BaseUrl)) {
            return "https://api.europeana.eu";
        }
        return api2BaseUrl;
    }

    /**
     * Generates an url to retrieve a record from the Europeana website
     * @param collectionId
     * @param itemId
     * @return url as String
     */
    public String getRecordPortalUrl(String collectionId, String itemId) {
        return getRecordPortalUrl("/" + collectionId + "/" +itemId);
    }

    /**
     * Generates an url to retrieve a record from the Europeana website
     * @param europeanaId
     * @return url as String
     */
    public String getRecordPortalUrl(String europeanaId) {
        UrlBuilder url = new UrlBuilder(this.getPortalBaseUrl())
                .addPath("portal", "record")
                .addPage(europeanaId + ".html");
        return url.toString();
    }

    /**
     * Generates an old-style url to retrieve a record from the Europeana website
     * Note that the EuropeanaId database still contains a lot of these urls as 'oldId'
     * @param europeanaId
     * @return url as String
     */
    public String getRecordResolveUrl(String europeanaId) {
        UrlBuilder url = new UrlBuilder(this.getPortalBaseUrl())
                .addPath("resolve", "record")
                .addPage(europeanaId);
        return url.toString();
    }

    /**
     * Generates an url to retrieve record JSON data from the Record API
     * @param collectionId
     * @param itemId
     * @param wskey
     * @return url as String
     */
    public String getRecordApi2Url(String collectionId, String itemId, String wskey) {
        return getRecordApi2Url("/" + collectionId + "/" +itemId, wskey);
    }

    /**
     * Generates an url to retrieve record JSON data from the Record API
     * @param europeanaId
     * @param wskey
     * @return url as String
     */
    public String getRecordApi2Url(String europeanaId, String wskey) {
        UrlBuilder url = new UrlBuilder(getApi2BaseUrl())
                .addPath("api", "v2", "record")
                .addPage(europeanaId + ".json")
                .addParam("wskey", wskey);
        return url.toString();
    }

    /**
     * Generates an url to retrieve a thumbnail with default size from the Europeana Thumbnail Storage
     * The url is processed eventually by the ThumbnailController in the API2 project.
     * @param uri uri of original thumbnail. A null value can be provided but will result in a not-working thumbnail-url
     *            so for proper working an uri is required.
     * @param type defaults to IMAGE (optional)
     * @return url as String
     */
    public String getThumbnailUrl(String uri, DocType type) {
        return getThumbnailUrl(uri, null, type);
    }

    /**
     * Generates an url to retrieve a thumbnail from the Europeana Thumbnail Storage
     * The url is process eventually by the ThumbnailController in the API2 project.
     * @param uri uri of original thumbnail. A null value can be provided but will result in a not-working thumbnail-url
     *            so for proper working an uri is required.
     * @param size either w200 or w400, other values are ignored (optional)
     * @param type defaults to IMAGE (optional)
     * @return url as String
     */
    public String getThumbnailUrl(String uri, String size, DocType type) {
        UrlBuilder url = EuropeanaUrlBuilder.getThumbnailUrl(uri, size, type);
        String newBaseUrl = this.getApi2BaseUrl();
        url.setProtocol(newBaseUrl);
        url.setDomain(newBaseUrl);
        return url.toString();
    }


    public String getRedirectUrl(String wskey, String isShownAtUri, String provider, String europeanaId, String profile) {
        UrlBuilder url = new UrlBuilder(this.getApi2BaseUrl())
                .addPath("api", String.valueOf(wskey), "redirect").disableTrailingSlash()
                .addParam("shownAt", isShownAtUri)
                // Note that provider and id are not required paramaters for the RedirectController, but sent along for
                // logging purposes.
                .addParam("provider", provider)
                .addParam("id", this.getRecordResolveUrl(europeanaId))
                // Not sure the profile parameter still serves any purpose, can probably be removed
                .addParam("profile", profile);
        return url.toString();
    }

}
