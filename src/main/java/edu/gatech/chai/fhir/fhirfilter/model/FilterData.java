package edu.gatech.chai.fhir.fhirfilter.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.javac.util.Assert;

import edu.gatech.chai.fhir.fhirfilter.dao.FhirFilterDaoImpl;

public class FilterData {
	final static Logger logger = LoggerFactory.getLogger(FilterData.class);

	private Long id;
	private Date effectiveStartDate;
	private Date effectiveEndDate;
	private JSONObject jsonObject;

	public FilterData(String jsonString) {
		Assert.checkNonNull(jsonString, "JSON String cannot be null");
		
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

		if (!jsonObject.isNull("effectiveDate")) {
			JSONObject effectiveDate = jsonObject.getJSONObject("effectiveDate");
			if (!effectiveDate.isNull("startDate")) {
				String startDateString = effectiveDate.getString("startDate");
				try {
					this.effectiveStartDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
				} catch (ParseException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if (!effectiveDate.isNull("endDate")) {
				String startDateString = effectiveDate.getString("endDate");
				try {
					this.effectiveEndDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
				} catch (ParseException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getEffectiveStartDate() {
		return effectiveStartDate;
	}

	public void setEffectiveStartDate(Date effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}

	public Date getEffectiveEndDate() {
		return effectiveEndDate;
	}

	public void setEffectiveEndDate(Date effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
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
