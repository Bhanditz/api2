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

package eu.europeana.api2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.abstracts.ApiResponse;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class ApiError extends ApiResponse {

	public boolean success = false;

	public ApiError(String apikey, String error) {
		super(apikey);
		this.error = error;
	}

	public ApiError(String apikey, String error,
					long requestNumber) {
		this(apikey, error);
		this.requestNumber = requestNumber;
	}

	public ApiError(ApiLimitException ex) {
		this(ex.getApikey(), ex.getError(), ex.getRequestNumber());
	}
}
