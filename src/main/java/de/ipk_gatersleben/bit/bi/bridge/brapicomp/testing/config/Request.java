package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;

/**
 * A request specifies url and method to call.
 */
public class Request {
	private String url;
	private String method;
	public Request() {
	}
	public Request(String method, String url) {
		setMethod(method);
		setUrl(url);
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getUrl() {
		return url;
	}
	public String getMethod() {
		return method;
	}
	
}
