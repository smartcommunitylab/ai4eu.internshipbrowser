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

package eu.ai4eu.ai4citizen.internshipbrowser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eu.ai4citizen.internshipbrowser.model.Activity;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityTemplate;
import eu.ai4eu.ai4citizen.internshipbrowser.model.Competence;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentProfile;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan;

/**
 * @author raman
 *
 */
public class DataConverter {

	private static final double[] center = new double[] {46.069346, 11.119817};
	private static final double max = 75000;

	public static List<Competence> convertCompetences(String url) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(url));
		lines = lines.subList(1, lines.size());
		List<Competence> competences = lines.stream().map(lineStr -> {
			String[] line = lineStr.split(";");
			Competence obj = new  Competence();
			boolean isfol = "ISFOL".equals(cleanValue(line[6]));
			obj.setAbilities(Collections.emptyList());
			obj.setCustom(!isfol);
			obj.setTitle(cleanValue(line[8]));
			obj.setDescription(obj.getTitle());
			obj.setId(cleanValue(line[0]));
			obj.setKnowledge(Collections.emptyList());
			obj.setSkills(Collections.emptyList());
			obj.setSource(cleanValue(line[5]));
			if (isfol) {
				obj.setExternalId(cleanValue(line[1]));
				obj.setEqfLevel(cleanValue(line[3]));
			}
			return obj;
		}).collect(Collectors.toList());
		return competences;
		
	}
	public static List<StudyPlan> convertPlans(String url) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(url));
		lines = lines.subList(1, lines.size());
		Map<String, List<String[]>> map = lines.stream().map(s -> s.split(";")).collect(Collectors.groupingBy(l -> l[0]));
		List<StudyPlan> collect = map.keySet().stream().map(k -> {
			List<String[]> value = map.get(k);
			String[] line = value.get(0);

			StudyPlan obj = new StudyPlan(); 
			obj.setPlanId(cleanValue(line[0]));
			obj.setTitle(cleanValue(line[1]));
			obj.setCourse(cleanValue(line[2]));
			obj.setInstitute(cleanValue(line[3]));
			obj.setInstituteId(cleanValue(line[4]));
			obj.setStart(cleanValue(line[5]));
			obj.setEnd(obj.getStart());
			obj.setCompetences(value.stream().map(l -> {
				Competence comp = new Competence();
				comp.setId(cleanValue(l[6]));
				comp.setTitle(cleanValue(l[8]));
				comp.setCustom("true".equalsIgnoreCase(cleanValue(l[7])));
				comp.setEscoId(cleanValue(l[9]));
				comp.setEscoTitle(cleanValue(l[10]));
				return comp;
			}).filter(c -> c.getId() != null && !c.getId().equals("NULL")).collect(Collectors.toList()));
			return obj;
		}).collect(Collectors.toList());
		return collect;
	}
	
	public static List<Activity> convertActivities(String url) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(url));
		lines = lines.subList(1, lines.size());
		
		List<String[]> list = lines.stream().map(s -> s.split(";")).filter(l -> l.length > 1).collect(Collectors.toList());
		
		Map<String, List<String[]>> map = list.stream().collect(Collectors.groupingBy(l -> l[0]));
		List<Activity> collect = map.keySet().stream().map(k -> {
			List<String[]> value = map.get(k);
			String[] line = value.get(0);

			Activity obj = new Activity();
			obj.setActivityId(cleanValue(line[0]));
			obj.setInternal("TRUE".equalsIgnoreCase(cleanValue(line[1])));
			obj.setType(ActivityTemplate.TYPE_INTERNSHIP);
			obj.setCourse(cleanValue(line[3]));
			obj.setCourseYear(cleanValue(line[4]));
			obj.setInstitute(cleanValue(line[5]));
			obj.setInstituteId(cleanValue(line[6]));
			try {
				obj.setRegistrationYear(Integer.parseInt(cleanValue(line[7])));
			} catch (NumberFormatException e) {
				obj.setRegistrationYear(4);
			}
			obj.setStart(cleanValue(line[8]));
			obj.setEnd(cleanValue(line[9]));
			obj.setAddress(cleanValue(line[10]));
			obj.setCompany(cleanValue(line[11]));
			String t = cleanValue(line[14]);
			String d = cleanValue(line[15]);
			if (t == null) t = "";
			obj.setDescription(t);
			if (!StringUtils.isEmpty(d)) obj.setDescription(obj.getDescription() + " - " + d);

			String ls = cleanValue(line[12]);
			if (!StringUtils.isEmpty(ls)) obj.setLatitute(Double.parseDouble(ls));
			ls = cleanValue(line[13]);
			if (!StringUtils.isEmpty(ls)) obj.setLongitude(Double.parseDouble(ls));
			obj.setTeamSize(3);
			obj.setCompetences(value.stream().filter(l -> l.length > 16).map(l -> {
				Competence comp = new Competence();
				comp.setId(cleanValue(l[16]));
				comp.setTitle(cleanValue(l[18]));
				comp.setCustom("true".equalsIgnoreCase(cleanValue(l[17])));
				if (l.length > 20) {
					comp.setEscoId(cleanValue(l[19]));
					comp.setEscoTitle(cleanValue(l[20]));
				}
				return comp;
			}).filter(c -> c.getId() != null && !c.getId().equals("NULL")).collect(Collectors.toList()));
			
			return obj;
		}).collect(Collectors.toList());
		return collect;
	}
	
	private static double[] getRandomLocation() {
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
	
	public static List<StudentProfile> convertStudents(String url) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(url));
		lines = lines.subList(1, lines.size());
		
		List<String[]> list = lines.stream().map(s -> s.split(";")).filter(l -> l.length > 1).collect(Collectors.toList());
		
		Map<String, List<String[]>> map = list.stream().collect(Collectors.groupingBy(l -> l[0]));
		
		final Map<String, Integer> idxMap = new HashMap<>();
		idxMap.put("idx", 10);
		
		List<StudentProfile> collect = map.keySet().stream().map(k -> {
			List<String[]> value = map.get(k);
			String[] line = value.get(0);
			
			int idx = idxMap.get("idx");
			StudentProfile p = new StudentProfile();
			p.setStudentId("_"+idx);
			p.setCourse(cleanValue(line[1]));
			p.setCourseYear(cleanValue(line[2]));
			p.setInstitute(cleanValue(line[3]));
			p.setInstituteId(cleanValue(line[3]));
			p.setCourseClass(cleanValue(line[4]));
			
			p.setAddress("via Sommarive 18, Trento");
			p.setCompleteActivities(Collections.emptyList());
			p.setCurrentActivities(Collections.emptyList());
			p.setFutureActivities(Collections.emptyList());
			p.setFiscalCode(cleanValue(line[0]));
			
			p.setName("Nome" + idx);
			p.setSurname("Cognome" + idx);
			idxMap.put("idx", idx + 1);
			double[] loc = getRandomLocation();
			p.setLatitute(loc[0]);
			p.setLongitude(loc[1]);
			
			try {
				p.setRegistrationYear(Integer.parseInt(cleanValue(line[5])));
			} catch (NumberFormatException e) {
				p.setRegistrationYear(4);
			}
			
			p.setCompetences(value.stream().filter(l -> l.length > 6).map(l -> {
				Competence comp = new Competence();
				comp.setId(cleanValue(l[6]));
				comp.setTitle(cleanValue(l[8]));
				comp.setCustom("true".equalsIgnoreCase(cleanValue(l[7])));
				if (l.length > 10) {
					comp.setEscoId(cleanValue(l[9]));
					comp.setEscoTitle(cleanValue(l[10]));
				}
				return comp;
			}).filter(c -> c.getId() != null && !c.getId().equals("NULL")).collect(Collectors.toList()));

			ActivityTemplate a = new ActivityTemplate();
			a.setCourse(p.getCourse());
			a.setCourseYear(p.getCourseYear());
			a.setInstitute(p.getInstitute());
			a.setInstituteId(p.getInstituteId());
			a.setPlanId(p.getPlanId());
			a.setPlanTitle(p.getPlanTitle());
			a.setRegistrationYear(p.getRegistrationYear());
			a.setInternal(false);
			a.setType(ActivityTemplate.TYPE_INTERNSHIP);
			p.setFutureActivities(Collections.singletonList(a));				
			
			return p;
		}).collect(Collectors.toList());
		return collect;
	}

	
	/**
	 * @param string
	 * @return
	 */
	private static String cleanValue(String string) {
		String res = string.startsWith("\"") ? string.substring(1) : string;
		res = res.endsWith("\"") ? res.substring(0, res.length() - 1) : res;
		if (res.equals("NULL")) return null;
		return res;
	}

	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
//		String convertPlans = mapper.writeValueAsString(convertPlans("./src/test/resources/data/ai4eu-plans_esco.csv"));
//		Files.write(Paths.get("./src/main/resources/data/plans.json"), convertPlans.getBytes());
//		String convertActivities = mapper.writeValueAsString(convertActivities("./src/test/resources/data/ai4eu-offer_activities_esco.csv"));
//		Files.write(Paths.get("./src/main/resources/data/activities.json"), convertActivities.getBytes());
//		String convertCompetences = mapper.writeValueAsString(convertCompetences("./src/test/resources/data/competenza1.csv"));
//		Files.write(Paths.get("./src/test/resources/data/competences.json"), convertCompetences.getBytes());
		String converStudents = mapper.writeValueAsString(convertStudents("./src/test/resources/data/ai4eu-students_competences_esco.csv"));
		Files.write(Paths.get("./src/main/resources/data/profiles.json"), converStudents.getBytes());
	}
}
