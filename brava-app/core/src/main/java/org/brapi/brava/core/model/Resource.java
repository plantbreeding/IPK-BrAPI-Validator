package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Resource - contains the information related
 * to one endpoint (url, and user it belongs to).
 */
@Getter
@Setter
@NoArgsConstructor
public class Resource {
	private String url;
	private String accessToken;

	public Resource(String url, String accessToken) throws MalformedURLException {
		setUrl(url) ;
		setAccessToken(accessToken);
	}

	public void setUrl(String url) throws MalformedURLException {
		URL u = new URL(url);

		this.url = url ;
	}

}
