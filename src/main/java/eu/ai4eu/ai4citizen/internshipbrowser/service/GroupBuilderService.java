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
import java.util.Collection;
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
import com.google.common.collect.Sets;

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
	
	private static final Set<String> exceptions = Sets.newHashSet(
			"http://data.europa.eu/esco/skill/b2235ce8-d0f0-4954-9b60-e2c1aeeadab5",
			"http://data.europa.eu/esco/skill/f5a2523e-8b1f-4541-b9e8-cd27e2d9cd7b",
			"http://data.europa.eu/esco/skill/da70d0cf-1b0b-43eb-924f-e2a69d0d5390",
			"http://data.europa.eu/esco/skill/268f3f0d-252e-4f61-9d96-1a87f4389ca9",
			"http://data.europa.eu/esco/skill/a0d79341-8398-4019-ad1b-27888ec35aaa",
			"http://data.europa.eu/esco/skill/37a438f3-e28c-4e32-83c5-299f047c1dc9",
			"http://data.europa.eu/esco/skill/cbf01241-d126-402a-af28-2eb81e2e24e8",
			"http://data.europa.eu/esco/skill/c4272323-cce6-4535-ac71-4f6841f4f89a",
			"http://data.europa.eu/esco/skill/06bd47bc-d9f3-4517-8e34-c55c2e81f8c2",
			"http://data.europa.eu/esco/skill/bd2102ea-c8d9-40f6-8327-211450120e96",
			"http://data.europa.eu/esco/skill/32e07436-d3c1-4f0e-aa91-cf987e260a39",
			"http://data.europa.eu/esco/skill/60d48be0-4260-4b2e-85b9-aedff7a9b7d9",
			"http://data.europa.eu/esco/skill/5acf6408-e084-4202-9442-ffcf0119811c",
			"http://data.europa.eu/esco/skill/36bca324-45b8-4a90-925c-9f61f6b7bed4",
			"http://data.europa.eu/esco/skill/e437eba1-3e22-41f2-8703-741e94785cba",
			"http://data.europa.eu/esco/skill/ea357ba5-3d11-441f-841c-7d85ff11dd9e",
			"http://data.europa.eu/esco/skill/73b87890-d2ec-4842-b824-b4851ff12051",
			"http://data.europa.eu/esco/skill/eefe0ee1-273b-470c-b67a-a80667a3cfd4",
			"http://data.europa.eu/esco/skill/b4910ea9-578d-4767-bd68-00a315a64e09",
			"http://data.europa.eu/esco/skill/63d22ad4-b8f9-4910-862f-febfdc2e7f5b",
			"http://data.europa.eu/esco/skill/7a7ea862-b73c-40ee-b495-a5bcdf3cce7d",
			"http://data.europa.eu/esco/skill/2200e940-ad43-4828-8727-47c4afcbc351",
			"http://data.europa.eu/esco/skill/f0c94e76-dead-475b-bde7-99d873bd2b7b",
			"http://data.europa.eu/esco/skill/fba826dc-c801-446b-9bde-b989cea1b1c3",
			"http://data.europa.eu/esco/skill/fba826dc-c801-446b-9bde-b989cea1b1c3"
			);
	
	public void buildAll() throws Exception {
		build(profileRepo.findAll(), offerRepo.findAll(), prefRepo.findAll());
	}
	
	public void buildClass(String institute, String courseClass, String year) throws Exception {
		
		List<StudentActivityPreference> prefs = prefRepo.findAll();
		List<StudentProfile> profiles = profileRepo.findByInstituteIdAndCourseClassAndCourseYear(institute, courseClass, year);
		Set<String> ids = profiles.stream().map(p -> p.getStudentId()).collect(Collectors.toSet());
		prefs = prefs.stream().filter(p -> ids.contains(p.getStudentId())).collect(Collectors.toList());
		build(profiles, offerRepo.findAll(), prefs);
	}
	
	@SuppressWarnings("unchecked")
	private void build (List<StudentProfile> profiles, List<Activity> offers, List<StudentActivityPreference> dbPrefs) throws Exception {
		// convert students
		List<Student> students = profiles
				.stream().filter(s -> !s.getCompetences().isEmpty()).map(s -> toStudent(s)).collect(Collectors.toList());
		
		Map<String, Object> studentContainer = new HashMap<>();
		students.forEach(s -> studentContainer.put(s.getId(), s));
		logger.info(new ObjectMapper().writeValueAsString(students));
//		String studentGroupId = restTemplate.postForObject(endpoint +  "/students", studentContainer, String.class);
		
		// convert original offers
		List<Project> projects = offers
				.stream().filter(p -> !p.getCompetences().isEmpty()).map(o -> toProject(o)).collect(Collectors.toList());
		Map<String, Object> projectContainer = new HashMap<>();
		projects.forEach(s -> projectContainer.put(s.getId(), s));
		logger.info(new ObjectMapper().writeValueAsString(projectContainer));
//		String projectGroupId = restTemplate.postForObject(endpoint +  "/projects", projectContainer, String.class);
		
		// preferences extracted from DV
		List<Preference> preferences = dbPrefs
			.stream().flatMap(p -> toPreference(p).stream()).collect(Collectors.toList());
		Map<String, Object> prefContainer = new HashMap<>();
		if (preferences.size() > 0) {
			preferences.forEach(p -> prefContainer.put(p.getId(), p));
//			prefGroupId = restTemplate.postForObject(endpoint +  "/preferences", preferences, String.class);
		}
		logger.info(new ObjectMapper().writeValueAsString(prefContainer));
		
		Map<String, Object> container = new HashMap<>();
		container.put("Students", studentContainer);
		container.put("Projects", projectContainer);
		container.put("Preferences", prefContainer);
		container.put("OntologyID", "Precalc-ESCO-demo");
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
//		student.setSchool(p.getInstitute());
//		student.setName(p.fullName());
		student.setCompetences(new HashMap<>());

		List<Competence> competences = new LinkedList<Competence>();
		competences.addAll(p.getCompetences().stream().filter(c -> c.getEscoId() != null && !exceptions.contains(c.getEscoId())).map(c -> toCompetence(c, 1d)).collect(Collectors.toList()));
		Set<String> directCompetences = p.getCompetences() != null ? p.getCompetences().stream().map(c -> c.getId()).collect(Collectors.toSet()): new HashSet<>();
		StudyPlan plan = planRepo.findByInstituteIdAndCourse(p.getInstituteId(), p.getCourse()).stream().findFirst().orElse(null);
		if (plan != null) {
			plan.getCompetences().forEach(c -> {
				if (c.getEscoId() != null && !exceptions.contains(c.getEscoId()) && !directCompetences.contains(c.getId())) {
					competences.add(toCompetence(c, 0.5));
				}
			});
		}
		competences.forEach(c -> student.getCompetences().put(c.getId(), new double[] {c.getWeight(), 1d}));
		return student;
	}
	

	private Competence toCompetence(eu.ai4eu.ai4citizen.internshipbrowser.model.Competence c, Double level) {
		Competence res = new Competence();
		res.setDescription(c.getDescription());
		// TODO change to ESCO
		res.setId(c.getEscoId());
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
			if (c.getId() != null && !exceptions.contains(c.getId())) {
				p.getCompetences().put(c.getId(), new Double[] {c.getWeight(), 1d});
			}
		});
//		p.setDescription(offer.getDescription());
		p.setId(offer.getActivityId());
//		p.setInstitute(offer.getCompany());
//		p.setInterview(false);
		p.setSize(offer.getTeamSize());
		return p;
	}
	
	@SuppressWarnings("unchecked")
	private List<Preference> toPreference(StudentActivityPreference p) {
		final List<Preference> res = new LinkedList<>();
		// TODO consider cluster preferences ?
		if (p.getPreferences() != null && p.getPreferences().containsKey("ilike")) {
			Collection<String> c = (Collection<String>) p.getPreferences().get("ilike");
			int i = 0;
			int dim = c.size() <= 5 ? 1 : ((c.size() / 5) + 1); 
			// max pref = 10, min pref = 6
			for (String key : c) {
				Activity a = offerRepo.findById(key).orElse(null);
				if (a != null && a.getCompetences() != null && !a.getCompetences().isEmpty()) {
					Preference pref = new Preference();
					pref.setId(p.getId());
					int cell = Math.round(i / dim);
					pref.setValue(10 - cell);
					pref.setSid(p.getStudentId());
					pref.setPid(a.getActivityId());
					res.add(pref);
					i++;
				}
			}
		}
		return res;
	}
}
