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
package eu.ai4eu.ai4citizen.internshipbrowser.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.ai4eu.ai4citizen.internshipbrowser.model.Activity;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityAssignment;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityClustering;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityTemplate.CLUSTER;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentActivityPreference;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentProfile;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan;
import eu.ai4eu.ai4citizen.internshipbrowser.service.BrowserService;
import eu.ai4eu.ai4citizen.internshipbrowser.service.GroupBuilderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Internshp Browser controller
 * @author raman
 *
 */
@RestController
@Api(tags = { "Internship Browser API" })
public class BrowserController {

	@Autowired
	private BrowserService service;
	@Autowired
	private GroupBuilderService groupBuilder;
	
	@GetMapping("/api/profile")
	@ApiOperation(value="Get all student profiles")
	public ResponseEntity<List<StudentProfile>> getProfiles() {
		return ResponseEntity.ok(service.getProfiles());
	}

	@GetMapping("/api/profile/{studentId:.*}")
	@ApiOperation(value="Get student profile")
	public ResponseEntity<StudentProfile> getProfile(@PathVariable String studentId) {
		return ResponseEntity.ok(service.getProfile(studentId));
	}
	@GetMapping("/api/profile/institute/{instituteId:.*}")
	@ApiOperation(value="Get student profiles of an institute")
	public ResponseEntity<List<StudentProfile>> getProfilesInInstitute(@PathVariable String instituteId) {
		return ResponseEntity.ok(service.getProfilesInInstitute(instituteId));
	}
	@GetMapping("/api/profile/courseClass/{year}/{courseClass:.*}")
	@ApiOperation(value="Get student profiles of a class")
	public ResponseEntity<List<StudentProfile>> getProfilesInCourseClass(@PathVariable String year, @PathVariable String courseClass) {
		return ResponseEntity.ok(service.getProfilesInClass(courseClass, year));
	}

	@GetMapping("/api/activities/{studentId}/{activityType}/{registrationYear}")
	@ApiOperation(value="Get activities matching student profile")
	public ResponseEntity<ActivityClustering> getMatchingActivities(
			@PathVariable String studentId, 
			@PathVariable String registrationYear, 
			@PathVariable String activityType,
			@RequestParam(required = false) String topic,
			@RequestParam(required = false) String company,
			@RequestParam(required = false) String city
			) {
		return ResponseEntity.ok(service.getMatchingActivities(studentId, registrationYear, activityType, topic, company, city));
	}

	@GetMapping("/api/activities/{studentId}/{activityType}/{registrationYear}/count")
	@ApiOperation(value="Get number of activities matching student profile")
	public ResponseEntity<Long> getMatchingActivitiesCount(@PathVariable String studentId, @PathVariable String registrationYear, @PathVariable String activityType) {
		return ResponseEntity.ok(service.getMatchingActivitiesCount(studentId, registrationYear, activityType));
	}

	@GetMapping("/api/activities/vocabulary/{cluster}")
	@ApiOperation(value="Get list of terms for the specified cluster")
	public ResponseEntity<Set<String>> getVocabulary(@PathVariable CLUSTER cluster) {
		return ResponseEntity.ok(service.getVocabulary(cluster));
	}
	
	@GetMapping("/api/activities")
	@ApiOperation(value="Search activities")
	public ResponseEntity<List<Activity>> searchActivities(@RequestParam(required = false) String q, @RequestParam(required = false) List<String> ids) {
		return ResponseEntity.ok(service.searchActivite(q, ids));
	}

	@GetMapping("/api/activities/{activityId}")
	@ApiOperation(value="Get activity details")
	public ResponseEntity<Activity> getActivityDetails(@PathVariable String activityId) {
		return ResponseEntity.ok(service.getActivity(activityId));
	}

	@GetMapping("/api/plan/{planId:.*}")
	@ApiOperation(value="Get study plan")
	public ResponseEntity<StudyPlan> getStudyPlan(@PathVariable String planId) {
		return ResponseEntity.ok(service.getStudyPlan(planId));
	}
	
	@GetMapping("/api/activityassignment/{activityId:.*}")
	@ApiOperation(value="Get activity assignment")
	public ResponseEntity<ActivityAssignment> getActivityAssignment(@PathVariable String activityId) {
		return ResponseEntity.ok(service.getActivityAssignment(activityId));
	}

	@GetMapping("/api/activityassignment")
	@ApiOperation(value="Get activity assignment")
	public ResponseEntity<List<ActivityAssignment>> getActivityAssignments() {
		return ResponseEntity.ok(service.getActivityAssignments());
	}

	@GetMapping("/api/activityassignment/student/{studentId:.*}")
	@ApiOperation(value="Get student assignment")
	public ResponseEntity<ActivityAssignment> getActivityAssignmentForStudent(@PathVariable String studentId) {
		return ResponseEntity.ok(service.getStudentAssignment(studentId));
	}

	@GetMapping("/api/preferences/{studentId}/{activityType}/{registrationYear}")
	@ApiOperation(value="Get student activity preferences")
	public ResponseEntity<StudentActivityPreference> getActivityPreference(@PathVariable String studentId, @PathVariable String registrationYear, @PathVariable String activityType) {
		return ResponseEntity.ok(service.getActivityPreference(studentId, registrationYear, activityType));
	}
	
	@PostMapping("/api/preferences/{studentId}/{activityType}/{registrationYear}/student")
	@ApiOperation(value="Update student activity preferences")
	public ResponseEntity<StudentActivityPreference> saveActivityPreference(@PathVariable String studentId, @PathVariable String registrationYear, @PathVariable String activityType, @RequestBody Map<String, Object> preferences) {
		return ResponseEntity.ok(service.saveActivityPreference(studentId, registrationYear, activityType, preferences));
	}

	@PostMapping("/api/preferences/{studentId}/{activityType}/{registrationYear}/teacher")
	@ApiOperation(value="Update teacher view of student activity preferences")
	public ResponseEntity<StudentActivityPreference> saveActivityTeacherPreference(@PathVariable String studentId, @PathVariable String registrationYear, @PathVariable String activityType, @RequestBody Map<String, Object> preferences) {
		return ResponseEntity.ok(service.saveActivityTeacherPreference(studentId, registrationYear, activityType, preferences));
	}

	@PostMapping("/api/assign/{year}/{courseClass:.*}")
	@ApiOperation(value="Trigger group building algorithm for a class")
	public ResponseEntity<Void> assignClass(@PathVariable String year, @PathVariable String courseClass) throws Exception {
		groupBuilder.buildClass(courseClass, year);
		return ResponseEntity.ok().build();
	}

}
