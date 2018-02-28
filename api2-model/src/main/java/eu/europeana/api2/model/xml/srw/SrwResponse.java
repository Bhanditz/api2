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

package eu.europeana.api2.model.xml.srw;

import eu.europeana.api2.v2.model.xml.srw.EchoedSearchRetrieveRequest;
import eu.europeana.api2.v2.model.xml.srw.Records;
import eu.europeana.corelib.utils.Configuration;

import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "searchRetrieveResponse")
public class SrwResponse {
	@Resource
	private static Configuration configuration;

	public static final String NS_SRW = "http://www.loc.gov/zing/srw/";
	public static final String NS_DIAG = "http://www.loc.gov/zing/srw/diagnostic/";
	public static final String NS_XCQL = "http://www.loc.gov/zing/cql/xcql/";
	public static final String NS_MODS = "http://www.loc.gov/mods/v3";
	public static final String NS_EUROPEANA = "https://www.europeana.eu/";
	public static final String NS_ENRICHMENT = "https://www.europeana.eu/schemas/ese/enrichment/";
	public static final String NS_DCTERMS = "http://purl.org/dc/terms/";
	public static final String NS_DC = "http://purl.org/dc/elements/1.1/";
	public static final String NS_DCX = "http://purl.org/dc/elements/1.1/";
	public static final String NS_TEL = "http://krait.kb.nl/coop/tel/handbook/telterms.html";
	public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	@XmlElement
	public String version = "1.1";

	@XmlElement(name = "records")
	public Records records = new Records();

	@SuppressWarnings("unused")
	@XmlElement
	public EchoedSearchRetrieveRequest echoedSearchRetrieveRequest = new EchoedSearchRetrieveRequest();
}
