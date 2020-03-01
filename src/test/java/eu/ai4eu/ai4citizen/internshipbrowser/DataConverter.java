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
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eu.ai4citizen.internshipbrowser.model.Activity;
import eu.ai4eu.ai4citizen.internshipbrowser.model.ActivityTemplate;
import eu.ai4eu.ai4citizen.internshipbrowser.model.Competence;
import eu.ai4eu.ai4citizen.internshipbrowser.model.StudyPlan;

/**
 * @author raman
 *
 */
public class DataConverter {

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
				obj.setEqfLevel(Integer.parseInt(cleanValue(line[3])));
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
			obj.setCourse(cleanValue(line[3]));
			obj.setInstitute(cleanValue(line[2]));
			obj.setInstituteId(cleanValue(line[2]));
			obj.setPlanId(cleanValue(line[0]));
			obj.setTitle(cleanValue(line[1]));
			obj.setStart(new Date(Long.parseLong(cleanValue(line[4]).toString())).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
			obj.setEnd(new Date(Long.parseLong(cleanValue(line[5]).toString())).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
			obj.setCompetences(value.stream().map(l -> {
				Competence comp = new Competence();
				comp.setId(cleanValue(l[6]));
				comp.setTitle(cleanValue(l[14]));
				return comp;
			}).filter(c -> c.getId() != null && !c.getId().equals("NULL")).collect(Collectors.toList()));
			return obj;
		}).collect(Collectors.toList());
		return collect;
	}
	
	public static List<Activity> convertActivities(String url) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(url));
		lines = lines.subList(1, lines.size()).stream().filter(s -> s.split(";").length > 43).collect(Collectors.toList());
		Map<String, List<String[]>> map = lines.stream().map(s -> s.split(";")).collect(Collectors.groupingBy(l -> l[30]));
		List<Activity> collect = map.keySet().stream().map(k -> {
			List<String[]> value = map.get(k);
			String[] line = value.get(0);

			Activity obj = new Activity();
			obj.setActivityId(cleanValue(line[30]));
			obj.setAddress(cleanValue(line[34]));
			obj.setCompany(cleanValue(line[33]));
			obj.setDescription(cleanValue(line[25]));
			obj.setCourse(cleanValue(line[4]));
			obj.setCourseYear(cleanValue(line[2]));
			obj.setStart(new Date(Long.parseLong(cleanValue(line[7]).toString())).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
			obj.setEnd(new Date(Long.parseLong(cleanValue(line[6]).toString())).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
			obj.setInstitute(cleanValue(line[12]));
			obj.setInstituteId(cleanValue(line[12]));
			obj.setInternal(cleanValue(line[11]).equals("1"));
			obj.setLatitute(Double.parseDouble(cleanValue(line[26]).toString()));
			obj.setLongitude(Double.parseDouble(cleanValue(line[27]).toString()));
			obj.setPlanId(cleanValue(line[28]));
			obj.setPlanTitle(cleanValue(line[29]));
			obj.setRegistrationYear(Integer.parseInt(cleanValue(line[1])));
			obj.setTeamSize(3);
			obj.setType(ActivityTemplate.TYPE_INTERNSHIP);
			obj.setCompetences(value.stream().map(l -> {
				Competence comp = new Competence();
				comp.setId(cleanValue(l[35]));
				comp.setTitle(cleanValue(l[43]));
				return comp;
			}).filter(c -> c.getId() != null && !c.getId().equals("NULL")).collect(Collectors.toList()));
			
			return obj;
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
		String convertPlans = mapper.writeValueAsString(convertPlans("./src/test/resources/data/piano_alternanza1.csv"));
		Files.write(Paths.get("./src/test/resources/data/plans.json"), convertPlans.getBytes());
		String convertActivities = mapper.writeValueAsString(convertActivities("./src/test/resources/data/attivita_alternanza1.csv"));
		Files.write(Paths.get("./src/test/resources/data/activities.json"), convertActivities.getBytes());
		String convertCompetences = mapper.writeValueAsString(convertCompetences("./src/test/resources/data/competenza1.csv"));
		Files.write(Paths.get("./src/test/resources/data/competences.json"), convertCompetences.getBytes());
	}
}
