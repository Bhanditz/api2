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

package eu.europeana.api2.v2.model.xml.rss;

import eu.europeana.corelib.utils.Configuration;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlElement;

@SuppressWarnings("unused")
public class ChannelImage {
	@Resource
	private Configuration configuration;

	protected EuropeanaUrlService urlService;

	@XmlElement(name = "title")
	private String title = "Europeana Open Search";

	@XmlElement(name = "link")
	private String link = configuration.getPortalUrl();

	@XmlElement(name = "url")
	private String url = configuration.getPortalUrl() + "/portal/sp/img/europeana-logo-en.png";


	public ChannelImage(){

	}

//	private int width = 206;
//
//	private int height = 123;
}
