package com.thirdparty.contextio;

import java.util.HashMap;
import java.util.Map;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class ContextIOApi extends DefaultApi10a {

	static final String ENDPOINT = "api.context.io";

	// region > key (property, programmatic)
	String key;

//	@Programmatic
	public String getKey() {
		return key;
	}

//	@Programmatic
	public void setKey(String key) {
		this.key = key;
	}

	// endregion

	// region > secret (property, programmatic)
	String secret;

//	@Programmatic
	public String getSecret() {
		return secret;
	}

//	@Programmatic
	public void setSecret(String secret) {
		this.secret = secret;
	}

	// endregion

	// region > ssl (property, programmatic)
	boolean ssl;

//	@Programmatic
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * Specify whether or not API calls should be made over a secure connection.
	 * HTTPS is used on all calls by default.
	 * 
	 * @param ssl
	 *            Set to false to make calls over HTTP, true to use HTTPS
	 */
//	@Programmatic
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	// endregion

	// region > apiVersion (property, programmatic)
	String apiVersion;

//	@Programmatic
	public String getApiVersion() {
		return apiVersion;
	}

	/**
	 * Set the API version. By default, the latest official version will be used
	 * for all calls.
	 * 
	 * @param apiVersion
	 *            Context.IO API version to use
	 */
//	@Programmatic
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	// endregion

	// region > saveHeaders (property, programmatic)
	boolean saveHeaders;

//	@Programmatic
	public boolean isSaveHeaders() {
		return saveHeaders;
	}

//	@Programmatic
	public void setSaveHeaders(boolean saveHeaders) {
		this.saveHeaders = saveHeaders;
	}

	// endregion

	// region > authHeaders (property, programmatic)
	boolean authHeaders;

//	@Programmatic
	public boolean isAuthHeaders() {
		return authHeaders;
	}

	/**
	 * Specify whether OAuth parameters should be included as URL query
	 * parameters or sent as HTTP Authorization headers. The default is URL
	 * query parameters.
	 * 
	 * @param authHeaders
	 *            Set to true to use HTTP Authorization headers, false to use
	 *            URL query params
	 */
//	@Programmatic
	public void setAuthHeaders(boolean authHeaders) {
		this.authHeaders = authHeaders;
	}

	// endregion

	private String accountId;
	
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	// region > lastResponse (property, programmatic, read-only)
	ContextIOResponse lastResponse;

	/**
	 * Returns the ContextIOResponse object for the last API call.
	 * 
	 * @return ContextIOResponse
	 */
//	@Programmatic
	public ContextIOResponse getLastResponse() {
		return lastResponse;
	}

//	@Programmatic
	public void setLastResponse(ContextIOResponse lastResponse) {
		this.lastResponse = lastResponse;
	}

	// endregion

	// region > build_baseurl, build_url
//	@Programmatic
	public String build_baseurl() {
		String url = "https"; //htoth
//		if (ssl) {
//			url = "https";
//		}

		return url + "://" + ENDPOINT + "/" + apiVersion + '/';
	}

//	@Programmatic
	public String build_url(String action) {
		String  url = build_baseurl() + action;
		return url;
	}

	// endregion

//	@Programmatic
	public Map<String, String> filterParams(Map<String, String> givenParams,
			String[] validParams) {
		Map<String, String> filteredParams = new HashMap<String, String>();

		for (String validKey : validParams) {
			for (String givenKey : givenParams.keySet()) {
				if (givenKey.equalsIgnoreCase(validKey)) {
					filteredParams.put(validKey, givenParams.get(givenKey));
				}
			}
		}

		return filteredParams;
	}

	@Override
	public String getAccessTokenEndpoint() {
		throw new UnsupportedOperationException("No endpoint for access token.");
	}

	@Override
	public String getAuthorizationUrl(Token arg0) {
		throw new UnsupportedOperationException("No authorization url.");
	}

	@Override
	public String getRequestTokenEndpoint() {
		throw new UnsupportedOperationException(
				"No endpoint for request token.");
	}
}
