package org.brapi.brava.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
@Getter
@Setter
/**
 * Extends Resource providing more details
 */
public class DetailedResource extends Resource implements Comparable<DetailedResource> {
    private String v1url;
    private String v2url;
    private String crop;
    @JsonIgnore
    private String email;
    private String frequency = "weekly";
    @JsonIgnore
    private boolean confirmed = false;
    private int storeprev = 3;
    @JsonIgnore
    private boolean isPublic = false;
    @JsonIgnore
    private boolean submitToRepo = false;
    private String name = ""; // Only used for public endpoints
    private String description = ""; // Only used for public endpoints
    private Provider provider;
    private String certificate = ""; // Only used for public endpoints
    private String logo = ""; // Only used for public endpoints

    @Override
    public int compareTo(DetailedResource other) {
        return this.name.compareTo(other.name); //Sort alphabetically by name.
    }

    public DetailedResource() {
        super();
    }

    public DetailedResource(String url) throws MalformedURLException {
        super();
        this.setUrl(url);
    }

    public DetailedResource(String url, String accessToken) throws MalformedURLException {
        super();
        this.setUrl(url);
        this.setAccessToken(accessToken);
    }

    public DetailedResource(String url, String email, String frequency) throws IllegalArgumentException, MalformedURLException {
        super();
        this.setUrl(url);
        this.setEmail(email);
        this.setFrequency(frequency);
    }

    public void setUrl(String url) throws MalformedURLException {
        URL u = new URL(url);
        this.v1url = u.toString();
    }

    public void setV1url(String url) throws MalformedURLException {
        URL u = new URL(url);
        this.v1url = u.toString();
    }

    public void setV2url(String url) throws MalformedURLException {
        URL u = new URL(url);
        this.v2url = u.toString();
    }

    /**
     * @param frequency Resource's testing frequency.
     */
    public void setFrequency(String frequency) throws IllegalArgumentException {
        if (frequency.equals("weekly") || frequency.equals("monthly") || frequency.equals("daily")) {
            this.frequency = frequency;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setStoreprev(int storeprev) {
        if (storeprev == 3) { // Only three is valid for now
            this.storeprev = storeprev;
        } else {
            throw new IllegalArgumentException();
        }
    }
}