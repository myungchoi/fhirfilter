package edu.gatech.chai.fhir.fhirfilter.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilterData {
	final static Logger logger = LoggerFactory.getLogger(FilterData.class);

	private Long id;
	private String profileName;
	private JSONObject jsonObject;

	public FilterData(String jsonString) {
		if (jsonString == null || jsonString.isEmpty()) {
			throw new IllegalArgumentException("JSON String cannot be null or empty");
		}
		
		try {
			this.jsonObject = new JSONObject(jsonString);
		} catch (JSONException e) {
			logger.error(e.getMessage());
			throw new JSONException(e);
		}

		// Set meta information.
		if (!jsonObject.isNull("id")) {
			String id = jsonObject.getString("id");
			setId(Long.valueOf(id));
		}

		if (!jsonObject.isNull("profile_name")) {
			String profileName = jsonObject.getString("profile_name");
			this.profileName = profileName;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProfileName() {
		return this.profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public String toString() {
		return jsonObject.toString();
	}

}
