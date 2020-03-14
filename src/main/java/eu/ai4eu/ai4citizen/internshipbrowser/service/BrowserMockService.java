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
package eu.ai4eu.ai4citizen.internshipbrowser.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eu.ai4citizen.internshipbrowser.model.Activity;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityAssignment;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityClustering;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityClustering.ActivityCluster;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityClustering.ActivityClusterAssignment;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityTemplate;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentActivityPreference;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentProfile;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan.StudyActivity;

/**
 * @author raman
 *
 */
@Service
public class BrowserMockService {

	@Value("${mock.path:./src/main/resources/data}")
	private String path;
	
	private StudentProfile profile;
	private List<StudyPlan> plans;
	private List<Activity> activities;
	
	@PostConstruct
	public void initData() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		profile = mapper.readValue(Files.readAllBytes(Paths.get(path+"/profile.json")), StudentProfile.class);
		plans = Arrays.asList(mapper.readValue(Files.readAllBytes(Paths.get(path+"/plans.json")), StudyPlan[].class));
		activities = Arrays.asList(mapper.readValue(Files.readAllBytes(Paths.get(path+"/activities.json")), Activity[].class));
	}
	
	public StudentProfile getProfile(String studentId) {
		return profile;
	}

	public Long getMatchingActivitiesCount(String studentId, String registrationYear, String activityType) {
		final StudentProfile profile = getProfile(studentId);
		return activities.stream()
		.filter(a -> !a.isInternal() && a.getInstituteId().equals(profile.getInstituteId()) /*&& a.getCourse().equals(profile.getCourse()) */&& a.getRegistrationYear().equals(profile.getRegistrationYear()))
		.count();
	}

	public ActivityClustering getMatchingActivities(String studentId, String registrationYear, String activityType) {
		final StudentProfile profile = getProfile(studentId);
		List<Activity> offers = activities.stream()
		.filter(a -> !a.isInternal() && a.getInstituteId().equals(profile.getInstituteId()) /*&& a.getCourse().equals(profile.getCourse()) */&& a.getRegistrationYear().equals(profile.getRegistrationYear()))
		.collect(Collectors.toList());
		
		ActivityClustering clustering = new ActivityClustering();
		clustering.setClusters(new LinkedList<>());
		
		// cluster: course
		ActivityCluster topicCluster = new ActivityCluster();
		clustering.getClusters().add(topicCluster);
		topicCluster.setAssignments(new LinkedList<>());
		topicCluster.setType("topic");
		Map<String, List<Activity>> lists = offers.stream().collect(Collectors.groupingBy(a -> a.getCourse()));
		lists.keySet().forEach(key -> {
			ActivityClusterAssignment assignment = new ActivityClusterAssignment();
			assignment.setKeys(Collections.singleton(key));
			assignment.setActivities(lists.get(key).stream().map(a -> a.getActivityId()).collect(Collectors.toSet()));
			topicCluster.getAssignments().add(assignment);
		});

		// cluster: companyType
		ActivityCluster companyCluster = new ActivityCluster();
		clustering.getClusters().add(companyCluster);
		companyCluster.setAssignments(new LinkedList<>());
		companyCluster.setType("company");
		Set<String> privateBodies = new HashSet<>(Arrays.asList("s.r.l", " srl", " snc", "s.p.a", " spa", "responsabilit", " sas", "s.n.c", "studio"));
		Set<String> publicBodies = new HashSet<>(Arrays.asList("universit", "vigil", "fondazione", "istituto", "comune "));
		
		Map<String, List<Activity>> companyLists = offers.stream().collect(Collectors.groupingBy(a -> {
			String company = a.getCompany().toLowerCase();
			if (privateBodies.stream().anyMatch(b -> company.contains(b))) return "private";
			if (publicBodies.stream().anyMatch(b -> company.contains(b))) return "public";
			return "other";
		}));
		companyLists.keySet().forEach(key -> {
			ActivityClusterAssignment assignment = new ActivityClusterAssignment();
			assignment.setKeys(Collections.singleton(key));
			assignment.setActivities(companyLists.get(key).stream().map(a -> a.getActivityId()).collect(Collectors.toSet()));
			companyCluster.getAssignments().add(assignment);
		});

		// cluster: distance
		ActivityCluster locationCluster = new ActivityCluster();
		clustering.getClusters().add(locationCluster);
		locationCluster.setType("location");
		locationCluster.setAssignments(new LinkedList<>());
		ActivityClusterAssignment dist5 = new ActivityClusterAssignment();
		dist5.setKeys(Collections.singleton("< 5"));
		ActivityClusterAssignment dist10 = new ActivityClusterAssignment();
		dist10.setKeys(Collections.singleton("< 10"));
		ActivityClusterAssignment dist50 = new ActivityClusterAssignment();
		dist50.setKeys(Collections.singleton("< 50"));
		ActivityClusterAssignment dist50p = new ActivityClusterAssignment();
		dist50p.setKeys(Collections.singleton("50 +"));
		offers.forEach(a -> {
			double dist = distance(a.getLatitute(), a.getLongitude(), profile.getLatitute(), profile.getLongitude());
			if (dist < 5) dist5.getActivities().add(a.getActivityId());
			else if (dist < 10) dist10.getActivities().add(a.getActivityId());
			else if (dist < 50) dist50.getActivities().add(a.getActivityId());
			else dist50p.getActivities().add(a.getActivityId());
		});
		if (!dist5.getActivities().isEmpty()) locationCluster.getAssignments().add(dist5);
		if (!dist10.getActivities().isEmpty()) locationCluster.getAssignments().add(dist10);
		if (!dist50.getActivities().isEmpty()) locationCluster.getAssignments().add(dist50);
		if (!dist50p.getActivities().isEmpty()) locationCluster.getAssignments().add(dist50p);
		
		// cluster: city
		ActivityCluster cityCluster = new ActivityCluster();
		clustering.getClusters().add(cityCluster);
		cityCluster.setType("city");
		cityCluster.setAssignments(new LinkedList<>());
		Map<String, List<Activity>> cityLists = offers.stream().collect(Collectors.groupingBy(a ->  extractCity(a.getAddress())));
		cityLists.keySet().forEach(key -> {
			if (!"-".equals(key)) {
				ActivityClusterAssignment assignment = new ActivityClusterAssignment();
				assignment.setKeys(Collections.singleton(key));
				assignment.setActivities(cityLists.get(key).stream().map(a -> a.getActivityId()).collect(Collectors.toSet()));
				cityCluster.getAssignments().add(assignment);
			}
		});

		
		return clustering;
	}
	
	
	
	public StudyPlan getStudyPlan(String planId) {
		StudyPlan plan = plans.stream().filter(p -> p.getPlanId().equals(planId)).findFirst().orElse(null);
		
		if (plan != null) {
			plan.setPlannedActivities(new LinkedList<>());
			for (int i = 3; i <=5; i++) {
				StudyActivity activity = new StudyActivity();
				activity.setCompetences(plan.getCompetences());
				activity.setRegistrationYear(i);
				activity.setType(Activity.TYPE_INTERNSHIP);
				plan.getPlannedActivities().add(activity);
			}
		}
		
		return plan;
	}
	
	public ActivityAssignment getActivityAssignment(String activityId) {
		ActivityAssignment assignment = new ActivityAssignment();
		assignment.setActivityId(activityId);
		assignment.setStatus(ActivityAssignment.STATUS_PENDING);
		Activity activity = activities.stream().filter(a -> a.getActivityId().equals(activityId)).findFirst().orElse(null);
		if (activity == null) return null;
		assignment.setTeamSize(activity.getTeamSize());
		assignment.setUpdated(LocalDateTime.now());
		return assignment;
	}

	public StudentActivityPreference getActivityPreference(String studentId, String registrationYear, String activityType) {
		StudentActivityPreference pref = new StudentActivityPreference();
		pref.setStudentId(studentId);
		ActivityTemplate template = new ActivityTemplate();
		template.setType(activityType);
		StudentProfile profile = getProfile(studentId);
		template.setCourse(profile.getCourse());
		template.setCourseYear(profile.getCourseYear());
		template.setRegistrationYear(Integer.parseInt(registrationYear));
		template.setInstitute(profile.getInstitute());
		template.setInstituteId(profile.getInstituteId());
		template.setPlanId(profile.getPlanId());
		template.setPlanTitle(profile.getPlanTitle());
		pref.setTemplate(template);
		return pref;
	}
	
	public StudentActivityPreference saveActivityPreference(String studentId, String registrationYear, String activityType, Map<String, Integer> preferences) {
		StudentActivityPreference pref = getActivityPreference(studentId, registrationYear, activityType);
		pref.setPreferences(preferences);
		return pref;
	}

	public StudentActivityPreference saveActivityTeacherPreference(String studentId, String registrationYear, String activityType, Map<String, Integer> preferences) {
		StudentActivityPreference pref = getActivityPreference(studentId, registrationYear, activityType);
		pref.setTeacherPreferences(preferences);
		return pref;
	}


	public Activity getActivity(String activityId) {
		Optional<Activity> activity = activities.stream().filter(a-> a.getActivityId().equals(activityId)).findFirst();
		if (activity.isPresent()) return toOffer(activity.get());
		return null;
	}

	
	private Activity toOffer(Activity a) {
		Activity offer = new Activity();
		offer.setAddress(a.getAddress());
		offer.setCompany(a.getCompany());
		offer.setCompetences(a.getCompetences());
		offer.setDescription(a.getDescription());
		offer.setEnd(a.getEnd());
		offer.setStart(a.getStart());
		offer.setInternal(a.isInternal());
		offer.setLatitute(a.getLatitute());
		offer.setLongitude(a.getLongitude());
		offer.setTeamSize(a.getTeamSize());
		offer.setType(a.getType());
		if (offer.isInternal()) {
			offer.setCourse(a.getCourse());
			offer.setCourseYear(a.getCourseYear());
			offer.setInstitute(a.getInstitute());
			offer.setInstituteId(a.getInstituteId());
			offer.setPlanId(a.getPlanId());
			offer.setPlanTitle(a.getPlanTitle());
		}
		return offer;
	}
	
	/**
	 * @param address
	 * @return
	 */
	private static String extractCity(String address) {
		if (address == null) return null;
		Pattern pattern = Pattern.compile(".* \\d{5} ([A-Za-z \\-']+)( \\(\\w+\\))?");
        Matcher matcher = pattern.matcher(address);
		return matcher.find() ? matcher.group(1).trim().toUpperCase() : "-";
	}

	private double distance(double lat1, double lon1, double lat2, double lon2) {
		lat1 = Math.toRadians(lat1);
		lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);
		lon2 = Math.toRadians(lon2);

		double dlon = lon2 - lon1;
		double dlat = lat2 - lat1;

		double a = Math.pow((Math.sin(dlat / 2)), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return 6371 * c;
	}
	
}
