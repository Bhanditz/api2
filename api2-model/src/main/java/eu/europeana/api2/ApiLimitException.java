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

package eu.europeana.api2;

/**
 * Exception that is thrown when the apikey is invalid, or if the number of requests if over it's daily maximum
 */
public class ApiLimitException extends Exception {

    private static final long serialVersionUID = 1L;

    private String apikey;
    private String error;
    private long requestNumber;
    private int httpStatus;

    public ApiLimitException(String apikey, String error) {
        super();
        this.apikey = apikey;
        this.error = error;
    }

    public ApiLimitException(String apikey, String error, long requestNumber) {
        this(apikey, error);
        this.requestNumber = requestNumber;
    }

    public ApiLimitException(String apikey, String error, long requestNumber, int httpStatus) {
        this(apikey, error, requestNumber);
        this.httpStatus = httpStatus;
    }

    public String getApikey() {
        return apikey;
    }

    public String getError() {
        return error;
    }

    public long getRequestNumber() {
        return requestNumber;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
