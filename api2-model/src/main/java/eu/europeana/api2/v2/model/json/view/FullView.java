/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the
 * European Commission; You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at: http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.model.json.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.*;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.utils.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * A FullView is the API view of FullBean object. The FulLBean is retrieved directly from Mongo, but the API adds and
 * alters specific fields, for example it generates proper API urls for the isShownAt and edmPreview fields
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(alphabetic=true)
public class FullView implements FullBean {

  private static final Logger LOG = LogManager.getLogger(FullView.class);

  private FullBean bean;
  private String profile;
  private String apiKey;
  private Api2UrlService urlService;
  private Date timestampCreated;
  private Date timestampUpdated;
  private boolean urlified = false;

  public FullView(FullBean bean, String profile, String apiKey) {
    this.bean = bean;
    this.profile = profile;
    this.apiKey = apiKey;
    this.urlService = Api2UrlService.getBeanInstance();
    extractTimestampCreated();
    extractTimestampUpdated();
  }

  @Override
  public String getId() {
    return null; // bean.getId();
  }

  @Override
  public String[] getUserTags() {
    return bean.getUserTags();
  }

  @Override
  public List<? extends Place> getPlaces() {
    @SuppressWarnings("unchecked")
    List<Place> items = (List<Place>) bean.getPlaces();
    for (Place item : items) {
      item.setId(null);
    }
    return items;
  }

  @Override
  public void setPlaces(List<? extends Place> places) {}

  @Override
  public List<? extends Agent> getAgents() {
    @SuppressWarnings("unchecked")
    List<Agent> items = (List<Agent>) bean.getAgents();
    for (Agent item : items) {
      item.setId(null);
    }
    return items;
  }

  @Override
  public List<? extends Timespan> getTimespans() {
    @SuppressWarnings("unchecked")
    List<Timespan> items = (List<Timespan>) bean.getTimespans();
    for (Timespan item : items) {
      item.setId(null);
    }
    return items;
  }

  @Override
  public List<? extends Concept> getConcepts() {
    @SuppressWarnings("unchecked")
    List<Concept> items = (List<Concept>) bean.getConcepts();
    for (Concept item : items) {
      item.setId(null);
    }
    return items;
  }

  @Override
  public void setConcepts(List<? extends Concept> concepts) {}

  @Override
  public void setAggregations(List<? extends Aggregation> aggregations) {}

  @Override
  public List<? extends Proxy> getProxies() {
    @SuppressWarnings("unchecked")
    List<Proxy> items = (List<Proxy>) bean.getProxies();
    for (Proxy item : items) {
      item.setId(null);
    }
    return items;
  }

  @Override
  public void setProxies(List<? extends Proxy> proxies) {}

  @Override
  public void setTimespans(List<? extends Timespan> timespans) {}

  @Override
  public List<? extends Aggregation> getAggregations() {
    @SuppressWarnings("unchecked")
    List<Aggregation> items = (List<Aggregation>) bean.getAggregations();
    for (Aggregation item : items) {
      item.setId(null);

      String isShownAt = item.getEdmIsShownAt();
      if (!urlified && isShownAt != null) {
        String provider = item.getEdmProvider().values().iterator().next().get(0);
        String isShownAtLink = urlService.getRedirectUrl(apiKey, isShownAt, provider, bean.getAbout(), profile);
        item.setEdmIsShownAt(isShownAtLink);
        urlified = true; // do this ONLY ONCE
      }

      // remove webresources IDs
      for (int j = 0, lw = item.getWebResources().size(); j < lw; j++) {
        item.getWebResources().get(j).setId(null);
      }
    }
    return items;
  }

  @Override
  public List<? extends BriefBean> getSimilarItems() {
    return null;
  }

  @Override
  public void setSimilarItems(List<? extends BriefBean> similarItems) {}

  @Override
  public List<? extends ProvidedCHO> getProvidedCHOs() {
    @SuppressWarnings("unchecked")
    List<ProvidedCHO> items = (List<ProvidedCHO>) bean.getProvidedCHOs();
    for (ProvidedCHO item : items) {
      item.setId(null);
    }
    return items;
  }

  @Override
  public void setProvidedCHOs(List<? extends ProvidedCHO> providedCHOs) {}

  @Override
  public String getAbout() {
    return bean.getAbout();
  }

    @Override
    public EuropeanaAggregation getEuropeanaAggregation() {
        EuropeanaAggregation europeanaAggregation = bean.getEuropeanaAggregation();
        europeanaAggregation.setId(null);

        // to set proper edmPreview we need to change edmPreview original image urls from Corelib into API thumbnail urls
        String edmPreview = "";
        // first try edmPreview, else edmObject and else edmIsShownBy
        if (StringUtils.isNotEmpty(europeanaAggregation.getEdmPreview())) {
            edmPreview = urlService.getThumbnailUrl(europeanaAggregation.getEdmPreview(), getType());
            LOG.debug("edmPreview found: {}", europeanaAggregation.getEdmPreview());
        } else if (StringUtils.isNotEmpty(this.getAggregations().get(0).getEdmObject())) {
            edmPreview = urlService.getThumbnailUrl(this.getAggregations().get(0).getEdmObject(), getType());
            LOG.debug("No edmPreview, but edmObject found: {}", this.getAggregations().get(0).getEdmObject());
        } else if (StringUtils.isNotEmpty(this.getAggregations().get(0).getEdmIsShownBy())) {
            edmPreview = urlService.getThumbnailUrl(this.getAggregations().get(0).getEdmIsShownBy(), getType());
            LOG.debug("No edmPreview or edmObject, but edmIsShownBy found: {}", this.getAggregations().get(0).getEdmIsShownBy());
        } else {
            LOG.debug("No edmPreview, edmObject or edmIsShownBy found");
        }
        europeanaAggregation.setEdmPreview(edmPreview);

        // set proper landingPage
        europeanaAggregation.setEdmLandingPage(urlService.getRecordPortalUrl(getAbout()));
        return europeanaAggregation;
    }

  @Override
  public void setEuropeanaAggregation(EuropeanaAggregation europeanaAggregation) {}

  @Override
  public String[] getTitle() {
    return bean.getTitle();
  }

  @Override
  public String[] getYear() {
    return bean.getYear();
  }

  @Override
  public String[] getProvider() {
    return bean.getProvider();
  }

  @Override
  public String[] getLanguage() {
    return bean.getLanguage();
  }

  @Override
  public DocType getType() {
    return bean.getType();
  }

  @Override
  public int getEuropeanaCompleteness() {
    return bean.getEuropeanaCompleteness();
  }

  @Override
  public String[] getEuropeanaCollectionName() {
    return bean.getEuropeanaCollectionName();
  }

  public String[] getEdmDatasetName() {
    return getEuropeanaCollectionName();
  }

  @Override
  public String[] getCountry() {
    return bean.getCountry();
  }

  @Override
  public Date getTimestamp() {
    return bean.getTimestamp();
  }

  // unwanted setters

  @Override
  public void setEuropeanaId(ObjectId europeanaId) {}

  @Override
  public void setTitle(String[] title) {}

  @Override
  public void setYear(String[] year) {}

  @Override
  public void setProvider(String[] provider) {}

  @Override
  public void setLanguage(String[] language) {}

  @Override
  public void setType(DocType type) {}

  @Override
  public void setEuropeanaCompleteness(int europeanaCompleteness) {}

  @Override
  public void setAbout(String about) {}

  @Override
  public void setAgents(List<? extends Agent> agents) {}

  @Override
  public void setCountry(String[] country) {}

  @Override
  public void setEuropeanaCollectionName(String[] europeanaCollectionName) {}

  public void extractTimestampCreated() {
    if (timestampCreated == null) {
      timestampCreated =
          bean.getTimestampCreated() != null ? bean.getTimestampCreated() : new Date(0);
    }
  }

  public void extractTimestampUpdated() {
    if (timestampUpdated == null) {
      timestampUpdated =
          bean.getTimestampUpdated() != null ? bean.getTimestampUpdated() : new Date(0);
    }
  }

  @JsonProperty("timestamp_created")
  public String getTimestampCreatedAsISO() {
    return DateUtils.format(timestampCreated);
  }

  @Override
  @JsonProperty("timestamp_created_epoch")
  public Date getTimestampCreated() {
    return timestampCreated;
  }

  @JsonProperty("timestamp_update")
  public String getTimestampUpdatedAsISO() {
    return DateUtils.format(timestampUpdated);
  }

  @Override
  @JsonProperty("timestamp_update_epoch")
  public Date getTimestampUpdated() {
    return timestampUpdated;
  }

  @Override
  public void setTimestampCreated(Date timestampCreated) {}

  @Override
  public void setTimestampUpdated(Date timestampUpdated) {}

  @Override
  public List<? extends License> getLicenses() {
    return bean.getLicenses();
  }

  @Override
  public void setLicenses(List<? extends License> licenses) {

  }

  @Override
  public List<? extends Service> getServices() {
    return bean.getServices();
  }

  @Override
  public void setServices(List<? extends Service> services) {
    bean.setServices(services);
  }

}
