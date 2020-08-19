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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import eu.ai4eu.ai4citizen.internshipbrowser.model.Activity;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityAssignment;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityClustering;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityClustering.ActivityCluster;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityClustering.ActivityClusterAssignment;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityTemplate;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityTemplate.CLUSTER;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentActivityPreference;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentProfile;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan.StudyActivity;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.AssignmentRepository;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.OfferRepository;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.PlanRepository;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.PreferenceRepository;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.StudentProfileRepository;

/**
 * @author raman
 *
 */
@Service
public class BrowserService {

	@Value("${mock.path:./src/main/resources/data}")
	private String path;
	
	@Autowired
	private PlanRepository planRepo;
	@Autowired
	private StudentProfileRepository profileRepo;
	@Autowired
	private OfferRepository offerRepo;
	@Autowired
	private PreferenceRepository prefRepo;
	@Autowired
	private AssignmentRepository assignmentRepo;
	
	public StudentProfile getProfile(String studentId) {
		return profileRepo.findById(studentId).orElse(null);
	}

	public Long getMatchingActivitiesCount(String studentId, String registrationYear, String activityType) {
		final StudentProfile profile = getProfile(studentId);
		return getAllActivities().stream()
		.filter(a -> !a.isInternal() && a.getInstituteId().equals(profile.getInstituteId()) /*&& a.getCourse().equals(profile.getCourse()) */&& a.getRegistrationYear().equals(profile.getRegistrationYear()))
		.count();
	}

	/**
	 * @return
	 */
	private List<Activity> getAllActivities() {
		return offerRepo.findAll();
	}

	public ActivityClustering getMatchingActivities(String studentId, String registrationYear, String activityType) {
		final StudentProfile profile = getProfile(studentId);
		List<Activity> offers = getAllActivities().stream()
		.filter(a -> !a.isInternal() && a.getInstituteId().equals(profile.getInstituteId()) /*&& a.getCourse().equals(profile.getCourse()) */&& a.getRegistrationYear().equals(profile.getRegistrationYear()))
		.collect(Collectors.toList());
		
		ActivityClustering clustering = new ActivityClustering();
		clustering.setClusters(new LinkedList<>());
		
		// cluster: course
		ActivityCluster topicCluster = extractTopics(offers);
		clustering.getClusters().add(topicCluster);

		// cluster: companyType
		ActivityCluster companyCluster = extractCompanyTypes(offers);
		clustering.getClusters().add(companyCluster);

		// cluster: distance
		ActivityCluster locationCluster = extractDistances(profile, offers);
		clustering.getClusters().add(locationCluster);

		// cluster: city
		ActivityCluster cityCluster = extractCities(offers);
		clustering.getClusters().add(cityCluster);

		
		return clustering;
	}

	private ActivityCluster extractCities(List<Activity> offers) {
		ActivityCluster cityCluster = new ActivityCluster();
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
		return cityCluster;
	}

	private ActivityCluster extractDistances(final StudentProfile profile, List<Activity> offers) {
		ActivityCluster locationCluster = new ActivityCluster();
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
		offers.stream().filter(o -> o.getLatitute() != null && o.getLongitude() != null).forEach(a -> {
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
		return locationCluster;
	}

	private ActivityCluster extractCompanyTypes(List<Activity> offers) {
		ActivityCluster companyCluster = new ActivityCluster();
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
		return companyCluster;
	}

	private ActivityCluster extractTopics(List<Activity> offers) {
		ActivityCluster topicCluster = new ActivityCluster();
		topicCluster.setAssignments(new LinkedList<>());
		topicCluster.setType("topic");
		Map<String, List<Activity>> lists = offers.stream().collect(Collectors.groupingBy(a -> a.getCourse()));
		lists.keySet().forEach(key -> {
			ActivityClusterAssignment assignment = new ActivityClusterAssignment();
			assignment.setKeys(Collections.singleton(key));
			assignment.setActivities(lists.get(key).stream().map(a -> a.getActivityId()).collect(Collectors.toSet()));
			topicCluster.getAssignments().add(assignment);
		});
		return topicCluster;
	}
	
	
	
	public StudyPlan getStudyPlan(String planId) {
		StudyPlan plan = planRepo.findById(planId).orElse(null);
		
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
		ActivityAssignment stored = assignmentRepo.findById(activityId).orElse(null);
		return stored;
	}

	public StudentActivityPreference getActivityPreference(String studentId, String registrationYear, String activityType) {
		StudentActivityPreference stored = prefRepo.findByStudentIdAndRegistrationYearAndActivityType(studentId, Integer.parseInt(registrationYear), ActivityTemplate.TYPE_INTERNSHIP);
		if (stored == null) {
			stored = new StudentActivityPreference();
			stored.setStudentId(studentId);
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
			stored.setTemplate(template);
			stored = prefRepo.save(stored);
		}
		return stored;
	}
	
	public StudentActivityPreference saveActivityPreference(String studentId, String registrationYear, String activityType, Map<String, Object> preferences) {
		StudentActivityPreference pref = getActivityPreference(studentId, registrationYear, activityType);
		pref.setPreferences(preferences);
		prefRepo.save(pref);
		
		return pref;
	}

	public StudentActivityPreference saveActivityTeacherPreference(String studentId, String registrationYear, String activityType, Map<String, Object> preferences) {
		StudentActivityPreference pref = getActivityPreference(studentId, registrationYear, activityType);
		pref.setTeacherPreferences(preferences);
		prefRepo.save(pref);
		
		return pref;
	}


	public Activity getActivity(String activityId) {
		Optional<Activity> activity = offerRepo.findById(activityId);
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
		Pattern pattern = Pattern.compile(".* \\d{5}[ \\-]+([A-Za-z \\-']+)( \\(\\w+\\))?");
        Matcher matcher = pattern.matcher(address);
		return matcher.find() ? matcher.group(1).trim().toUpperCase() : "-";
	}

	private double distance(Double lat1, Double lon1, Double lat2, Double lon2) {
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

	/**
	 * @param cluster
	 * @return
	 */
	public Set<String> getVocabulary(CLUSTER cluster) {
		switch (cluster) {
		case city: return extractCities(getAllActivities()).getAssignments().stream().flatMap(ac -> ac.getKeys().stream()).collect(Collectors.toSet());
		case company: return extractCompanyTypes(getAllActivities()).getAssignments().stream().flatMap(ac -> ac.getKeys().stream()).collect(Collectors.toSet());
		case topic: return extractTopics(getAllActivities()).getAssignments().stream().flatMap(ac -> ac.getKeys().stream()).collect(Collectors.toSet());
		case company_name: return getAllActivities().stream().filter(a -> !a.isInternal() && !StringUtils.isEmpty(a.getCompany())).map(a -> a.getCompany()).collect(Collectors.toSet());
		case location: 
		default: return Collections.emptySet();
		}
	}

	/**
	 * @return
	 */
	public List<StudentProfile> getProfiles() {
		return profileRepo.findAll();
	}

	/**
	 * @param studentId
	 * @return
	 */
	public ActivityAssignment getStudentAssignment(String studentId) {
		ActivityAssignment stored = assignmentRepo.findByStudentId(studentId);
		return stored;
	}

	/**
	 * @return
	 */
	public List<ActivityAssignment> getActivityAssignments() {
		return assignmentRepo.findAll();
	}
	
}
