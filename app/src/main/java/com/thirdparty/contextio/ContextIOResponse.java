package com.thirdparty.contextio;

import java.util.Map;

import org.scribe.model.Response;

/**
 * 
 * @author Thomas Taschauer | tomtasche.at
 *
 */
public class ContextIOResponse {

	private int code;
	private Map<String, String> headers;
	private Map<String, String> requestHeaders;
	private Map<String, String> responseHeaders;
	private String contentType;
	private Response rawResponse;
	boolean hasError;
	
	
	public ContextIOResponse(int code, Map<String, String> requestHeaders, Map<String, String> responseHeaders, Response rawResponse) {
		this.code = code;
		this.requestHeaders = requestHeaders;
		this.responseHeaders = responseHeaders;
		this.rawResponse = rawResponse;
		this.contentType = rawResponse.getHeader("Content-Type");
		// TODO: this.headers = ;
	}
	
	
	public void decodeResponse() {
		if (code != 200 || !contentType.equals("application/json")) {
			hasError = true;
		} else {
			// TODO: decode json response to rawResponse
			
			// TODO: if (array_key_exists('messages', $this->decodedResponse) && (count($this->decodedResponse['messages']) > 0)) hasError = true;
		}
	}


	@Override
	public String toString() {
		return "ContextIOResponse [code=" + code + ", headers=" + headers
				+ ", requestHeaders=" + requestHeaders + ", responseHeaders="
				+ responseHeaders + ", contentType=" + contentType
				+ ", rawResponse=" + rawResponse + ", hasError=" + hasError
				+ ", response=" + rawResponse.getBody() + "]";
	}


	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}


	public Map<String, String> getHeaders() {
		return headers;
	}


	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}


	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}


	public void setRequestHeaders(Map<String, String> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}


	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}


	public void setResponseHeaders(Map<String, String> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}


	public String getContentType() {
		return contentType;
	}


	public void setContentType(String contentType) {
		this.contentType = contentType;
	}


	public Response getRawResponse() {
		return rawResponse;
	}


	public void setRawResponse(Response rawResponse) {
		this.rawResponse = rawResponse;
	}


	public boolean isHasError() {
		return hasError;
	}


	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}
}
