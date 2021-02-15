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

package eu.ai4eu.ai4citizen.internshipbrowser.dto;

/**
 * @author raman
 *
 */
public class AssignRequestDTO {

	private String institute, courseYear, courseClass;

	/**
	 * @return the institute
	 */
	public String getInstitute() {
		return institute;
	}

	/**
	 * @param institute the institute to set
	 */
	public void setInstitute(String institute) {
		this.institute = institute;
	}

	/**
	 * @return the courseYear
	 */
	public String getCourseYear() {
		return courseYear;
	}

	/**
	 * @param courseYear the courseYear to set
	 */
	public void setCourseYear(String courseYear) {
		this.courseYear = courseYear;
	}

	/**
	 * @return the courseClass
	 */
	public String getCourseClass() {
		return courseClass;
	}

	/**
	 * @param courseClass the courseClass to set
	 */
	public void setCourseClass(String courseClass) {
		this.courseClass = courseClass;
	}
	
	
}
