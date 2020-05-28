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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eu.ai4citizen.internshipbrowser.model.Activity;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityTemplate;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentProfile;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.OfferRepository;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.PlanRepository;
import eu.ai4eu.ai4citizen.internshipbrowser.repository.StudentProfileRepository;

/**
 * @author raman
 *
 */
@Service
public class DataInitializer {

	private static final int STUDENTS_PER_PLAN = 10;

	@Value("${mock.path:./src/main/resources/data}")
	private String path;
	
	private static final double[] center = new double[] {46.069346, 11.119817};
	private static final double max = 75000;
	
	@Autowired
	private PlanRepository planRepo;
	@Autowired
	private StudentProfileRepository profileRepo;
	@Autowired
	private OfferRepository offerRepo;

	public void initData() throws Exception {
		
		planRepo.deleteAll();
		profileRepo.deleteAll();
		offerRepo.deleteAll();
		
		ObjectMapper mapper = new ObjectMapper();
		
		// read study plans
		List<StudyPlan> plans = Arrays.asList(mapper.readValue(Files.readAllBytes(Paths.get(path+"/plans.json")), StudyPlan[].class))
				.stream().filter(p -> p.getCompetences() != null && !p.getCompetences().isEmpty()).collect(Collectors.toList());
		
		
		List<StudentProfile> profiles = Arrays.asList(mapper.readValue(Files.readAllBytes(Paths.get(path+"/profile.json")), StudentProfile[].class));
		Optional<Integer> max = profiles.stream().map(s -> Integer.parseInt(s.getStudentId())).max((a,b) -> a - b);
		profiles.forEach(p -> profileRepo.save(p));

		// generate students: N students per plan 
		int count = max.isPresent() ? max.get() : 0;
		for (StudyPlan p : plans) {
			p = planRepo.save(p);
			for (int i = 0; i < STUDENTS_PER_PLAN; i++) {
				StudentProfile profile = generateStudent(p, count++);
				profileRepo.save(profile);
			}
		};
		
		// read activities and store as offers
		Arrays.asList(mapper.readValue(Files.readAllBytes(Paths.get(path+"/activities.json")), Activity[].class))
				.stream().filter(a -> !a.isInternal()/* && a.getCompetences() != null && !a.getCompetences().isEmpty()*/).collect(Collectors.toList())
				.forEach(a -> offerRepo.save(a));

	}

	/**
	 * random location, registration year 4 o 5, and in case of 5 random half of the plan competences
	 * @param idx 
	 * @param p
	 * @return
	 */
	private StudentProfile generateStudent(StudyPlan plan, int idx) {
		StudentProfile p = new StudentProfile();
		p.setStudentId(""+(idx + 1));
		p.setFiscalCode(p.getStudentId());
		p.setAddress("via Sommarive 18, Trento");
		p.setCourse(plan.getCourse());
		p.setCourseClass("TEST");
		p.setCourseYear("2019-2020");
		p.setRegistrationYear(Math.random() < 0.5 ? 4 : 5);
		
		ActivityTemplate a = new ActivityTemplate();
		a.setCourse(plan.getCourse());
		a.setCourseYear(p.getCourseYear());
		a.setInstitute(plan.getInstitute());
		a.setPlanId(plan.getPlanId());
		a.setPlanTitle(plan.getTitle());
		a.setRegistrationYear(p.getRegistrationYear());
		a.setInternal(false);
		a.setType(ActivityTemplate.TYPE_INTERNSHIP);
		p.setFutureActivities(Collections.singletonList(a));
		p.setInstitute(plan.getInstitute());
		p.setInstituteId(plan.getInstituteId());
		p.setName("Nome" + idx);
		p.setSurname("Cognome" + idx);
		p.setPlanId(plan.getPlanId());
		p.setPlanTitle(plan.getTitle());
		double[] loc = getRandomLocation();
		p.setLatitute(loc[0]);
		p.setLongitude(loc[1]);
		
		if (p.getRegistrationYear() == 5) {
			p.setCompetences(new LinkedList<>());
			Set<Integer> used = new HashSet<>();
			while (used.size() < plan.getCompetences().size() / 2) {
				int pos = (int) (Math.random() * plan.getCompetences().size());
				if (!used.contains(pos)) {
					used.add(pos);
					p.getCompetences().add(plan.getCompetences().get(pos));
				}
			}
		}
		return p;
	}
	
	private double[] getRandomLocation() {
	    Random random = new Random();

	    // Convert radius from meters to degrees
	    double radiusInDegrees = max / 111000f;

	    double u = random.nextDouble();
	    double v = random.nextDouble();
	    double w = radiusInDegrees * Math.sqrt(u);
	    double t = 2 * Math.PI * v;
	    double x = w * Math.cos(t);
	    double y = w * Math.sin(t);

	    // Adjust the x-coordinate for the shrinking of the east-west distances
	    double new_x = x / Math.cos(Math.toRadians(center[1]));

	    double foundLongitude = new_x + center[0];
	    double foundLatitude = y + center[1];
	    return new double[] {foundLongitude, foundLatitude};
	}

}
