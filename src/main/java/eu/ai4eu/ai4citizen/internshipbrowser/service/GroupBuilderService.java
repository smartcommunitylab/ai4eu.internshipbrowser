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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import eu.ai4eu.ai4citizen.internshipbrowser.model.gb.Team;
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
	
	public void buildAll() throws Exception {
		// convert students
		List<Student> students = profileRepo.findAll()
				.stream().map(s -> toStudent(s)).collect(Collectors.toList());
		
		logger.info(new ObjectMapper().writeValueAsString(students));
		restTemplate.postForObject(endpoint +  "/students", students, String.class);
		
		// convert original offers
		List<Project> projects = offerRepo.findAll()
				.stream().map(o -> toProject(o)).collect(Collectors.toList());
		restTemplate.postForObject(endpoint +  "/projects", projects, String.class);
		
		// preferences extracted from DV
		List<Preference> preferences = prefRepo.findAll()
			.stream().flatMap(p -> toPreference(p).stream()).collect(Collectors.toList());
		if (preferences.size() > 0) {
			restTemplate.postForObject(endpoint +  "/preferences", preferences, String.class);
		}
		
		List<Team> assignments = Arrays.asList(restTemplate.getForObject(endpoint + "/groupbuilder", Team[].class));
		assignments.forEach(a -> {
			ActivityAssignment assignment = new ActivityAssignment();
			assignment.setActivityId(a.getProject().getId()+"");
			assignment.setStatus("assigned");
			assignment.setTeamSize(a.getProject().getTeamsize());
			assignment.setUpdated(LocalDateTime.now());
			assignment.setStudents(a.getAssigned_students().stream().map(s -> s.getId()+"").collect(Collectors.toSet()));
			assignmentRepo.save(assignment);
		});
	}
	
	private Student toStudent(StudentProfile p) {
		Student student = new Student();
		student.setId(Integer.parseInt(p.getStudentId()));
		student.setSchool(p.getInstitute());
		student.setName(p.fullName());
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
		student.setCompetences(competences);
		return student;
	}
	

	private Competence toCompetence(eu.ai4eu.ai4citizen.internshipbrowser.model.Competence c, Double level) {
		Competence res = new Competence();
		res.setDescription(c.getDescription());
		res.setId(Integer.parseInt(c.getId()));
		res.setName(c.getTitle());
		res.setWeight(1d);
		res.setLevel(level);
		return res;
	}

	private Project toProject(Activity offer) {
		Project p = new Project();
		p.setCompetences(offer.getCompetences().stream().map(c -> toCompetence(c, 1d)).collect(Collectors.toList()));
		p.setDescription(offer.getDescription());
		p.setId(Integer.parseInt(offer.getActivityId()));
		p.setInstitute(offer.getCompany());
		p.setInterviewRequired(false);
		p.setTeamsize(offer.getTeamSize());
		return p;
	}
	
	private List<Preference> toPreference(StudentActivityPreference p) {
		final List<Preference> res = new LinkedList<>();
		p.getPreferences().keySet().forEach(key -> {
			Activity a = offerRepo.findById(key).orElse(null);
			if (a != null) {
				Preference pref = new Preference();
				pref.setPreference_value(p.getPreferences().get(key));
				pref.setStudent(toStudent(profileRepo.findById(p.getStudentId()).orElse(null)));
				pref.setProject(toProject(a));
				res.add(pref);
			}
		});
		return res;
	}
}
