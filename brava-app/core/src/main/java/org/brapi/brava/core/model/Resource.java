package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.core.validation.AuthorizationMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Getter
@Setter
/**
 * A resource containing the information related
 * to one endpoint (url, and user it belongs to).
 */
public class Resource implements Comparable<Resource> {

    @NonNull
    private String id;
    @NonNull
    private String url;
    @NonNull
    private AuthorizationMethod authorizationMethod ;

    private String crop;
    private String collectionName ;
    private String email;

    private ValidationFrequency frequency = null;
    private boolean confirmed = false;
    private boolean isPublic = false;
    private String name ;
    private String description ;
    private Provider provider;
    private String certificate ;
    private String logo ;

    public Resource(String url) throws MalformedURLException {
        this(UUID.randomUUID().toString(), url);
    }

    public Resource(String id, String url) throws MalformedURLException {
        setId(id);
        setUrl(url);
    }

    public void setUrl(String url) throws MalformedURLException {
        URL u = new URL(url); // check if valid URL

        this.url = url ;
    }

    @Override
    public int compareTo(Resource other) {
        return this.name.compareTo(other.name); //Sort alphabetically by name.
    }
}