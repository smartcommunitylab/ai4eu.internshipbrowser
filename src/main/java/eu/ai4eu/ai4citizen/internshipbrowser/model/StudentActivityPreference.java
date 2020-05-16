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

import java.util.Map;

import org.springframework.data.annotation.Id;

/**
 * @author raman
 *
 */
public class StudentActivityPreference {

	@Id
	private String id;
	
	private String studentId;
	private ActivityTemplate template;
	private Map<String, Object> preferences;
	private Map<String, Object> teacherPreferences;
	
	public String getStudentId() {
		return studentId;
	}
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	public ActivityTemplate getTemplate() {
		return template;
	}
	public void setTemplate(ActivityTemplate template) {
		this.template = template;
	}
	public Map<String, Object> getPreferences() {
		return preferences;
	}
	public void setPreferences(Map<String, Object> preferences) {
		this.preferences = preferences;
	}
	public Map<String, Object> getTeacherPreferences() {
		return teacherPreferences;
	}
	public void setTeacherPreferences(Map<String, Object> teacherPreferences) {
		this.teacherPreferences = teacherPreferences;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
