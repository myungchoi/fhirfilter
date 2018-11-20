package edu.gatech.chai.fhir.fhirfilter.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.chai.fhir.fhirfilter.dao.FhirFilterDaoImpl;
import edu.gatech.chai.fhir.fhirfilter.model.FilterData;

@RestController
@RequestMapping("/apply")
public class ApplyFilterController {
	final static Logger logger = LoggerFactory.getLogger(ApplyFilterController.class);

	@Autowired
	FhirFilterDaoImpl fhirFilterDao;

	@GetMapping("")
	public @ResponseBody ResponseEntity<String> applyFilter(@RequestBody String jsonString) {
		JSONObject originalJSON = null;
		try {
			originalJSON = new JSONObject(jsonString);
		} catch (JSONException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Incorrect JSON Format", HttpStatus.BAD_REQUEST);
		}
		
		// Received JSON should be a FHIR resource.
		if (!originalJSON.has("resourceType")) {
			return new ResponseEntity<>("Invalid FHIR Resource", HttpStatus.BAD_REQUEST);
		}
		
		// Work on orginal data and get only resource part and put them in the list.
		List<JSONObject> originalList = new ArrayList<JSONObject>();
		if ("Bundle".equals(originalJSON.get("resourceType"))) {
			JSONArray originalEntry = originalJSON.getJSONArray("entry");
			for (Object entry: originalEntry) {
				JSONObject resource = (JSONObject) entry;
				originalList.add(resource.getJSONObject("resource"));
			}
		} else {
			originalList.add(originalJSON);
		}
		
		Calendar cal = Calendar.getInstance();
		Long now = cal.getTimeInMillis();

		List<FilterData> filterDatas = fhirFilterDao.getEffectiveFilters(now);
		
		for (FilterData filterData: filterDatas) {
			JSONObject jsonObject = filterData.getJsonObject();
			JSONArray resources = jsonObject.getJSONArray("entry");
			for (Object entry: resources) {
				JSONObject filterResource = (JSONObject) entry;
				
				// Walk over orginal data...
				for (JSONObject oJSON: originalList) {
					redactData(oJSON, filterResource);
				}
			}
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		
		return new ResponseEntity<>(originalJSON.toString(), headers, HttpStatus.OK);
	}
	
	public JSONObject redactData(JSONObject origJSON, JSONObject filterJSON) {
		if (!origJSON.getString("resourceType").equals(filterJSON.getString("resourceType"))) {
			return origJSON;
		}
		
		JSONArray keyArray = filterJSON.names();
		if (keyArray.length() <= 1) {
			// We are removing entire data.
			JSONArray origKeyArray = origJSON.names();
			for (Object key: origKeyArray) {
				origJSON.remove((String)key);
			}
		} else {
			// Walk over the filterJSON key and remove them.
			JSONArray filterKeyArray = filterJSON.names();
			for (Object key: filterKeyArray) {
				origJSON.remove((String)key);
			}
		}

		return origJSON;
	}
}
