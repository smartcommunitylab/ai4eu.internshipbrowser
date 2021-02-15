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
package eu.ai4eu.ai4citizen.internshipbrowser.model.gb;

import java.util.Map;

/**
 * @author raman
 *
 */
public class Project {

	private String id;
	private String institute, description;
	private Integer size;
	private Boolean interview;
	private Map<String, Double[]> competences;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getInstitute() {
		return institute;
	}
	public void setInstitute(String institute) {
		this.institute = institute;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(Integer size) {
		this.size = size;
	}
	public Boolean getInterview() {
		return interview;
	}
	public void setInterview(Boolean interviewRequired) {
		this.interview = interviewRequired;
	}
	public Map<String, Double[]> getCompetences() {
		return competences;
	}
	public void setCompetences(Map<String, Double[]> competences) {
		this.competences = competences;
	}
}
