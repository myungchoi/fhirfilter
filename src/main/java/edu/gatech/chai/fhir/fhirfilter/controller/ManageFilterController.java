package edu.gatech.chai.fhir.fhirfilter.controller;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import edu.gatech.chai.fhir.fhirfilter.dao.FhirFilterDaoImpl;
import edu.gatech.chai.fhir.fhirfilter.model.FilterData;

@RestController
@RequestMapping("/manage")
public class ManageFilterController {
	final static Logger logger = LoggerFactory.getLogger(ManageFilterController.class);

	@Autowired
	FhirFilterDaoImpl fhirFilterDao;

	@GetMapping("")
	public @ResponseBody ResponseEntity<String> getFilter() {
		List<FilterData> filterDatas;
		
		filterDatas = fhirFilterDao.get();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String created = dateFormat.format(cal.getTime());

		JSONObject listJson = new JSONObject();
		listJson.put("created", created);
		listJson.put("count", filterDatas.size());
		if (filterDatas.size()>0) {
			JSONArray jsonArray = new JSONArray();
			for (FilterData filterData: filterDatas) {
				jsonArray.put(filterData.getJsonObject());
			}
			listJson.put("list", jsonArray);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<>(listJson.toString(), headers, HttpStatus.OK);
	}

	@GetMapping("{id}")
	public @ResponseBody ResponseEntity<String> getFilterById(@PathVariable String id) {
		Long idLong = Long.valueOf(id);

		FilterData filterData = fhirFilterDao.getById(idLong);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (filterData == null) {
			return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(filterData.toString(), headers, HttpStatus.OK);
		}
	}

	@PostMapping("")
	public @ResponseBody ResponseEntity<String> postFilter(@RequestBody String jsonString) {
		FilterData filterData = null;
		try {
			filterData = new FilterData(jsonString);
		} catch (JSONException e) {
			return new ResponseEntity<>("Incorrect JSON: " + jsonString, HttpStatus.BAD_REQUEST);
		}

		if (filterData.getJsonObject().has("id")) {
			Long id = Long.valueOf(filterData.getJsonObject().getString("id"));
			FilterData existingFilterData = fhirFilterDao.getById(id);
			if (existingFilterData != null) {
				existingFilterData.setEffectiveStartDate(filterData.getEffectiveStartDate());
				existingFilterData.setEffectiveEndDate(filterData.getEffectiveEndDate());
				existingFilterData.setJsonObject(filterData.getJsonObject());
				fhirFilterDao.update(existingFilterData);
				
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				return new ResponseEntity<>(existingFilterData.toString(), headers, HttpStatus.OK);
			}
		}
		int id = fhirFilterDao.save(filterData);

		if (id > 0) {
			URI location = ServletUriComponentsBuilder.fromCurrentServletMapping().path("/manage/{id}").build()
					.expand(id).toUri();
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(location);

			return new ResponseEntity<>(headers, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Failed to save data", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("{id}")
	public @ResponseBody ResponseEntity<String> putFilter(@PathVariable String id, @RequestBody String jsonString) {
		FilterData filterData = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		try {
			filterData = new FilterData(jsonString);
		} catch (JSONException e) {
			return new ResponseEntity<>("Incorrect JSON: " + jsonString, HttpStatus.BAD_REQUEST);
		}

		Long idLong = Long.valueOf(id);
		if (filterData.getId() != idLong) {
			return new ResponseEntity<>("/id is not same as id in content", headers, HttpStatus.BAD_REQUEST);
		}
		
		FilterData existingFilterData = fhirFilterDao.getById(idLong);
		if (existingFilterData == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		existingFilterData.setEffectiveStartDate(filterData.getEffectiveStartDate());
		existingFilterData.setEffectiveEndDate(filterData.getEffectiveEndDate());
		existingFilterData.setJsonObject(filterData.getJsonObject());
		fhirFilterDao.update(existingFilterData);
		
		return new ResponseEntity<>(existingFilterData.toString(), headers, HttpStatus.OK);
	}
}
