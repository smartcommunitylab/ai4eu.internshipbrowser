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

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author raman
 *
 */
@JsonInclude(Include.NON_NULL)
public class Competence {

	@Id
	private String id;
	private boolean custom;
	private String externalId, source, title, description;
	private String eqfLevel, escoId, escoTitle;
	
	private List<Competence> skills, abilities, knowledge;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Competence> getSkills() {
		return skills;
	}

	public void setSkills(List<Competence> skills) {
		this.skills = skills;
	}

	public List<Competence> getAbilities() {
		return abilities;
	}

	public void setAbilities(List<Competence> abilities) {
		this.abilities = abilities;
	}

	public List<Competence> getKnowledge() {
		return knowledge;
	}

	public void setKnowledge(List<Competence> knowledge) {
		this.knowledge = knowledge;
	}

	public String getEqfLevel() {
		return eqfLevel;
	}

	public void setEqfLevel(String eqfLevel) {
		this.eqfLevel = eqfLevel;
	}

	/**
	 * @return the escoId
	 */
	public String getEscoId() {
		return escoId;
	}

	/**
	 * @param escoId the escoId to set
	 */
	public void setEscoId(String escoId) {
		this.escoId = escoId;
	}

	/**
	 * @return the escoTitle
	 */
	public String getEscoTitle() {
		return escoTitle;
	}

	/**
	 * @param escoTitle the escoTitle to set
	 */
	public void setEscoTitle(String escoTitle) {
		this.escoTitle = escoTitle;
	}
	
}
