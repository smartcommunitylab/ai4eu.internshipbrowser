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
package eu.ai4eu.ai4citizen.internshipbrowser.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentActivityPreference;

/**
 * @author raman
 *
 */
public interface PreferenceRepository extends MongoRepository<StudentActivityPreference, String> {

	@Query("{studentId: ?0, 'template.registrationYear': ?1, 'template.type': ?2}")
	StudentActivityPreference findByStudentIdAndRegistrationYearAndActivityType(String studentId, int year, String type);
	
}
