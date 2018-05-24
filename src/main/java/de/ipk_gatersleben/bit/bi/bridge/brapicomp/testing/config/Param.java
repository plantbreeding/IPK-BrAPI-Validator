package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;


/**
 * A param is a pair of param and value for REST requests.
 */
public class Param {
	String param;
	String value;
	boolean array = false;
	
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
