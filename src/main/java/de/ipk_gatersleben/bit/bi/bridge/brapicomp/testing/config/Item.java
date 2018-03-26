package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.Test;

/**
 * An Item is essentially one request and it accompanying tests.
 */
public class Item {
    private String name;
    private String endpoint;
    private String description;
    private Request request;
    private List<Event> event;
	private List<String> requires;
	private List<Param> parameters;

	public Item() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setEvent(List<Event> event) {
        this.event = event;
    }

    public String getDescription() {
        return description;
    }

    public Request getRequest() {
        return request;
    }

    public List<Event> getEvent() {
        return event;
    }

    /**
     * Generates an Item containing a basic tests which includes status code check,
     * content type check and schema test.
     *
     * @param method  String with REST method. GET, POST...
     * @param baseUrl
     * @param test
     * @throws URISyntaxException
     */
    public Item(String method, String baseUrl, Test test) throws URISyntaxException {
        baseUrl = baseUrl.replaceAll("/$", ""); //Make sure there is no trailing slash;
        setRequest(new Request(method, baseUrl + test.getEndpoint().toString()));
        setName(test.getEndpoint());
        setDescription("");
        List<String> exec = new ArrayList<String>();
        exec.add("StatusCode:200:breakiffalse");
        exec.add("ContentType:application/json");
        exec.add("Schema:/metadata");
        exec.add("Schema:" + test.getSchema());
        List<Event> ev = new ArrayList<Event>();
        ev.add(new Event(exec));
        setEvent(ev);
    }

	public List<String> getRequires() {
		return requires;
	}

    public void setRequires(List<String> requires) {
		this.requires = requires;
	}

	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * @param endpoint the endpoint to set
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

    public List<Param> getParameters() {
		return parameters;
	}

	public void setParameters(List<Param> parameters) {
		this.parameters = parameters;
	}
}
