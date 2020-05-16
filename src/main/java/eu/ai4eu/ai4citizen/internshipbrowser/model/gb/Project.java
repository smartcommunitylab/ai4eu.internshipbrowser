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

import java.util.List;

/**
 * @author raman
 *
 */
public class Project {

	private Integer id;
	private String institute, description;
	private Integer teamsize;
	private Boolean interviewRequired;
	private List<Competence> competences;

	public List<Competence> getCompetences() {
		return competences;
	}
	public void setCompetences(List<Competence> competences) {
		this.competences = competences;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
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
	public Integer getTeamsize() {
		return teamsize;
	}
	public void setTeamsize(Integer teamsize) {
		this.teamsize = teamsize;
	}
	public Boolean getInterviewRequired() {
		return interviewRequired;
	}
	public void setInterviewRequired(Boolean interviewRequired) {
		this.interviewRequired = interviewRequired;
	}
}