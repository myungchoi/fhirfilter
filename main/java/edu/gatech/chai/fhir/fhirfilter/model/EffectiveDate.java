package edu.gatech.chai.fhir.fhirfilter.model;

import java.util.Date;

public class EffectiveDate {
	private final String startDate;
	private final String endDate;
	
	public EffectiveDate(String startDate, String endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public String getStartDate() {
		return startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
}
