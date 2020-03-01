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

/**
 * @author raman
 *
 */
public class StudentProfile {

	@Id
	private String studentId;
	
	private String name, surname, fiscalCode;
	private String address;
	private Double latitute, longitude;
	
	private String course, planTitle, planId, courseYear, institute, instituteId, courseClass;
	private Integer registrationYear;
	
	private List<Competence> competences;
	
	private List<Activity> completeActivities;
	private List<Activity> currentActivities;
	private List<ActivityTemplate> futureActivities;
	public String getStudentId() {
		return studentId;
	}
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getFiscalCode() {
		return fiscalCode;
	}
	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Double getLatitute() {
		return latitute;
	}
	public void setLatitute(Double latitute) {
		this.latitute = latitute;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	public String getPlanTitle() {
		return planTitle;
	}
	public void setPlanTitle(String planTitle) {
		this.planTitle = planTitle;
	}
	public String getPlanId() {
		return planId;
	}
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	public String getCourseYear() {
		return courseYear;
	}
	public void setCourseYear(String courseYear) {
		this.courseYear = courseYear;
	}
	public String getInstitute() {
		return institute;
	}
	public void setInstitute(String institute) {
		this.institute = institute;
	}
	public String getInstituteId() {
		return instituteId;
	}
	public void setInstituteId(String instituteId) {
		this.instituteId = instituteId;
	}
	public Integer getRegistrationYear() {
		return registrationYear;
	}
	public void setRegistrationYear(Integer registrationYear) {
		this.registrationYear = registrationYear;
	}
	public List<Competence> getCompetences() {
		return competences;
	}
	public void setCompetences(List<Competence> competences) {
		this.competences = competences;
	}
	public List<Activity> getCompleteActivities() {
		return completeActivities;
	}
	public void setCompleteActivities(List<Activity> completeActivities) {
		this.completeActivities = completeActivities;
	}
	public List<Activity> getCurrentActivities() {
		return currentActivities;
	}
	public void setCurrentActivities(List<Activity> currentActivities) {
		this.currentActivities = currentActivities;
	}
	public List<ActivityTemplate> getFutureActivities() {
		return futureActivities;
	}
	public void setFutureActivities(List<ActivityTemplate> futureActivities) {
		this.futureActivities = futureActivities;
	}
	public String getCourseClass() {
		return courseClass;
	}
	public void setCourseClass(String courseClass) {
		this.courseClass = courseClass;
	}
	
}
