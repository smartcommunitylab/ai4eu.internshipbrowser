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

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityAssignment;

/**
 * @author raman
 *
 */
public interface AssignmentRepository extends MongoRepository<ActivityAssignment, String> {

	
	@Query("{students: ?0}")
	ActivityAssignment findByStudentId(String studentId);
	@Query("{students: {$in:?0}}")
	List<ActivityAssignment> findByStudentIdIn(Set<String> studentIds);

}