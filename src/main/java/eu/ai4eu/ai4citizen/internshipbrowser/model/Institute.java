/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package eu.ai4eu.ai4citizen.internshipbrowser.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;

/**
 * @author raman
 *
 */
public class Institute {

	@Id
	private String instituteId;
	
	private String referent, address;
	private double[] coordinates;
	
	private Map<String, Integer> hours = new HashMap<>();

	/**
	 * @return the instituteId
	 */
	public String getInstituteId() {
		return instituteId;
	}

	/**
	 * @param instituteId the instituteId to set
	 */
	public void setInstituteId(String instituteId) {
		this.instituteId = instituteId;
	}

	/**
	 * @return the referent
	 */
	public String getReferent() {
		return referent;
	}

	/**
	 * @param referent the referent to set
	 */
	public void setReferent(String referent) {
		this.referent = referent;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the coordinates
	 */
	public double[] getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(double[] coordinates) {
		this.coordinates = coordinates;
	}

	/**
	 * @return the hours
	 */
	public Map<String, Integer> getHours() {
		return hours;
	}

	/**
	 * @param hours the hours to set
	 */
	public void setHours(Map<String, Integer> hours) {
		this.hours = hours;
	}
	
}
