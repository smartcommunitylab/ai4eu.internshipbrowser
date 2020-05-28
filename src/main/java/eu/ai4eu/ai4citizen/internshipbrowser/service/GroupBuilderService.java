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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eu.ai4citizen.internshipbrowser.model.Activity;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityAssignment;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentActivityPreference;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentProfile;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan;
import eu.ai4eu.ai4citizen.internshipbrowser.model.gb.Competence;
import eu.ai4eu.ai4citizen.internshipbrowser.model.gb.Preference;
import eu.ai4eu.ai4citizen.internshipbrowser.model.gb.Project;
import eu.ai4eu.ai4citizen.internshipbrowser.model.gb.Student;
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
public class GroupBuilderService {

	private static final Logger logger = LoggerFactory.getLogger(GroupBuilderService.class);
	
	@Value("${groupbuilder.endpoint:https://www.eduteams.iiia.csic.es/AI4EU/GroupBuilder}")
	private String endpoint;
	
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

	private RestTemplate restTemplate = new RestTemplate();
	
	@SuppressWarnings("unchecked")
	public void buildAll() throws Exception {
		// convert students
		List<Student> students = profileRepo.findAll()
				.stream().filter(s -> !s.getCompetences().isEmpty()).map(s -> toStudent(s)).collect(Collectors.toList());
		
		Map<String, Object> studentContainer = new HashMap<>();
		students.forEach(s -> studentContainer.put(s.getId(), s));
		logger.info(new ObjectMapper().writeValueAsString(students));
//		String studentGroupId = restTemplate.postForObject(endpoint +  "/students", studentContainer, String.class);
		
		// convert original offers
		List<Project> projects = offerRepo.findAll()
				.stream().filter(p -> !p.getCompetences().isEmpty()).map(o -> toProject(o)).collect(Collectors.toList());
		Map<String, Object> projectContainer = new HashMap<>();
		projects.forEach(s -> projectContainer.put(s.getId(), s));
		logger.info(new ObjectMapper().writeValueAsString(projectContainer));
//		String projectGroupId = restTemplate.postForObject(endpoint +  "/projects", projectContainer, String.class);
		
		// preferences extracted from DV
		List<Preference> preferences = prefRepo.findAll()
			.stream().flatMap(p -> toPreference(p).stream()).collect(Collectors.toList());
		Map<String, Object> prefContainer = new HashMap<>();
		String prefGroupId = null;
		if (preferences.size() > 0) {
			preferences.forEach(p -> prefContainer.put(p.getId(), p));
//			prefGroupId = restTemplate.postForObject(endpoint +  "/preferences", preferences, String.class);
		}
		
		Map<String, Map<String, Object>> container = new HashMap<>();
		container.put("students", studentContainer);
		container.put("projects", projectContainer);
		container.put("preferences", prefContainer);

		logger.info(new ObjectMapper().writeValueAsString(container));
		
//		String url = endpoint + "/team_formation/" +  studentGroupId +"/" + projectGroupId + (prefGroupId != null ? ("/" + prefGroupId) : "");
//		List<Team> assignments = Arrays.asList(restTemplate.getForObject(url, Team[].class));
		
		Map<String, List<String>> assignmentMap = restTemplate.postForObject(endpoint + "/team_formation", container, Map.class);
		assignmentMap.keySet().forEach(a -> {
			ActivityAssignment assignment = new ActivityAssignment();
			assignment.setActivityId(a);
			assignment.setStatus("assigned");
			assignment.setTeamSize(assignmentMap.get(a).size());
			assignment.setUpdated(LocalDateTime.now());
			assignment.setStudents(new HashSet<>(assignmentMap.get(a)));
			assignmentRepo.save(assignment);
		});
	}
	
	private Student toStudent(StudentProfile p) {
		Student student = new Student();
		student.setId(p.getStudentId());
		student.setSchool(p.getInstitute());
		student.setName(p.fullName());
		student.setCompetences(new HashMap<>());

		List<Competence> competences = new LinkedList<Competence>();
		competences.addAll(p.getCompetences().stream().map(c -> toCompetence(c, 1d)).collect(Collectors.toList()));
		Set<String> directCompetences = p.getCompetences() != null ? p.getCompetences().stream().map(c -> c.getId()).collect(Collectors.toSet()): new HashSet<>();
		StudyPlan plan = planRepo.findById(p.getPlanId()).orElse(null);
		if (plan != null) {
			plan.getCompetences().forEach(c -> {
				if (!directCompetences.contains(c.getId())) {
					competences.add(toCompetence(c, 0.5));
				}
			});
		}
		competences.forEach(c -> student.getCompetences().put(c.getId(), c.getLevel()));
		return student;
	}
	

	private Competence toCompetence(eu.ai4eu.ai4citizen.internshipbrowser.model.Competence c, Double level) {
		Competence res = new Competence();
		res.setDescription(c.getDescription());
		res.setId(c.getId());
		res.setName(c.getTitle());
		res.setWeight(1d);
		res.setLevel(level);
		return res;
	}

	private Project toProject(Activity offer) {
		Project p = new Project();
		List<Competence> competences = offer.getCompetences().stream().map(c -> toCompetence(c, 1d)).collect(Collectors.toList());
		p.setCompetences(new HashMap<>());
		competences.forEach(c -> {
			p.getCompetences().put(c.getId(), new Double[] {c.getWeight(), 1d});
		});
		p.setDescription(offer.getDescription());
		p.setId(offer.getActivityId());
		p.setInstitute(offer.getCompany());
		p.setInterview(false);
		p.setTeamsize(offer.getTeamSize());
		return p;
	}
	
	private List<Preference> toPreference(StudentActivityPreference p) {
		final List<Preference> res = new LinkedList<>();
		p.getPreferences().keySet().forEach(key -> {
			Activity a = offerRepo.findById(key).orElse(null);
			if (a != null) {
				Preference pref = new Preference();
				pref.setId(p.getId());
				pref.setValue((Integer)p.getPreferences().get(key));
				pref.setSid(p.getStudentId());
				pref.setPid(a.getActivityId());
				res.add(pref);
			}
		});
		return res;
	}
}
