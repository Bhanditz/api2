package eu.europeana.api2.web.model.json;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import eu.europeana.api2.web.model.json.common.Profile;
import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.solr.bean.impl.IdBeanImpl;

public class BriefView extends IdBeanImpl implements BriefBean {

	@Value("#{europeanaProperties['api.utm.campaign']}")
	private String utmCampaign = "default";

	private final Logger log = Logger.getLogger(getClass().getName());

	private String id;
	private Date timestamp;
	private String[] provider;
	private String[] edmDataProvider;
	private String[] edmObject;
	private int europeanaCompleteness;
	private DocType docType;
	private String[] language;
	private String[] year;
	private String[] rights;
	private String[] title;
	private String[] dcCreator;
	private String[] dcContributor;
	private String[] edmPlace;
	private List<Map<String, String>> edmPlacePrefLabel;
	private Float edmPlaceLatitude;
	private Float edmPlaceLongitude;
	private String[] edmTimespan;
	private List<Map<String, String>> edmTimespanLabel;
	private String[] edmTimespanBegin;
	private String[] edmTimespanEnd;
	private String[] edmAgentTerm;
	private List<Map<String, String>> edmAgentLabel;
	private String[] dctermsHasPart;
	private String[] dctermsSpatial;
	private String[] edmPreview;
	private boolean isOptedOut;

	private String profile;
	private List<String> thumbnails;

	public BriefView(BriefBean bean, String profile) {
		id = bean.getId();
		timestamp = bean.getTimestamp();

		title = bean.getTitle();
		dcCreator = bean.getDcCreator();
		docType = bean.getType();
		year = bean.getYear();
		edmPlaceLatitude = bean.getEdmPlaceLatitude();
		edmPlaceLongitude = bean.getEdmPlaceLongitude();
		provider = bean.getProvider();
		edmDataProvider = bean.getDataProvider();
		rights = bean.getRights();

		edmObject = bean.getEdmObject();
		europeanaCompleteness = bean.getEuropeanaCompleteness();
		language = bean.getLanguage();
		dcContributor = bean.getDcContributor();
		edmPlace = bean.getEdmPlace();
		edmPlacePrefLabel = bean.getEdmPlaceLabel();
		edmTimespan = bean.getEdmTimespan();
		edmTimespanLabel = bean.getEdmTimespanLabel();
		edmTimespanBegin = bean.getEdmTimespanBegin();
		edmTimespanEnd = bean.getEdmTimespanEnd();
		edmAgentTerm = bean.getEdmAgent();
		edmAgentLabel = bean.getEdmAgentLabel();
		dctermsHasPart = bean.getDctermsHasPart();
		dctermsSpatial = bean.getDctermsSpatial();
		isOptedOut = bean.isOptedOut();
		edmPreview = bean.getEdmPreview();
		this.profile = profile;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	@Override
	public String[] getTitle() {
		return title;
	}

	@Override
	public String[] getEdmObject() {
		return null;
	}

	@Override
	public String[] getYear() {
		return year;
	}

	@Override
	public String[] getProvider() {
		return provider;
	}

	@Override
	public String[] getDataProvider() {
		return edmDataProvider;
	}

	@Override
	public String[] getLanguage() {
		if (profile.equals(Profile.MINIMAL)) {
			return null;
		}
		return language;
	}

	@Override
	public String[] getRights() {
		return rights;
	}

	@Override
	public DocType getType() {
		return docType;
	}

	@Override
	public String[] getDctermsSpatial() {
		return dctermsSpatial;
	}

	@Override
	public int getEuropeanaCompleteness() {
		return europeanaCompleteness;
	}

	@Override
	public String[] getEdmPlace() {
		return edmPlace;
	}

	@Override
	public List<Map<String, String>> getEdmPlaceLabel() {
		return edmPlacePrefLabel;
	}

	@Override
	public Float getEdmPlaceLatitude() {
		return edmPlaceLatitude;
	}

	@Override
	public Float getEdmPlaceLongitude() {
		return edmPlaceLongitude;
	}

	@Override
	public String[] getEdmTimespan() {
		if (profile.equals(Profile.MINIMAL)) {
			return null;
		}
		return edmTimespan;
	}

	@Override
	public List<Map<String, String>> getEdmTimespanLabel() {
		if (profile.equals(Profile.MINIMAL)) {
			return null;
		}
		return edmTimespanLabel;
	}

	@Override
	public String[] getEdmTimespanBegin() {
		if (profile.equals(Profile.MINIMAL)) {
			return null;
		}
		return edmTimespanBegin;
	}

	@Override
	public String[] getEdmTimespanEnd() {
		if (profile.equals(Profile.MINIMAL)) {
			return null;
		}
		return edmTimespanEnd;
	}

	@Override
	public String[] getEdmAgent() {
		return edmAgentTerm;
	}

	@Override
	public List<Map<String, String>> getEdmAgentLabel() {
		return edmAgentLabel;
	}

	@Override
	public String[] getDctermsHasPart() {
		return dctermsHasPart;
	}

	@Override
	public String[] getDcCreator() {
		return dcCreator;
	}

	@Override
	public String[] getDcContributor() {
		return dcContributor;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Boolean isOptedOut() {
		return isOptedOut;
	}

	public List<String> getThumbnails() {
		if (thumbnails == null) {
			thumbnails = new ArrayList<String>();
			for (String object : edmObject) {
				String tn = StringUtils.defaultIfBlank(object, "");
				StringBuilder url = new StringBuilder("http://europeanastatic.eu/api/image?");
				try {
					url.append("uri=").append(URLEncoder.encode(tn, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				url.append("&size").append(ThumbSize.LARGE);
				url.append("&type").append(getType().toString());
				thumbnails.add(url.toString());
			}
		}
		return thumbnails;
	}

	public String getLink() {
		StringBuilder url = new StringBuilder("http://portal2/api2/record/");
		url.append(getId());
		url.append(".json?utm_source=api&utm_medium=api&utm_campaign=").append(utmCampaign);
		return url.toString();
	}

	@Override
	public String[] getEdmPreview() {
		return edmPreview;
	}
}
