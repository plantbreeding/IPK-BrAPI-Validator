package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing;

import io.restassured.response.ValidatableResponse;

public class RestAssuredRequest {
	private ValidatableResponse vr;
	private long timestamp;
	public ValidatableResponse getVr() {
		return vr;
	}
	public void setVr(ValidatableResponse vr) {
		this.vr = vr;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public RestAssuredRequest(ValidatableResponse vr, long timestamp) {
		this.vr = vr;
		this.timestamp = timestamp;
	}
}
