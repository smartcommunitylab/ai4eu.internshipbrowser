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
package eu.ai4eu.ai4citizen.internshipbrowser.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author raman
 *
 */
@JsonInclude(Include.NON_NULL)
public class ActivityClustering {

	private List<ActivityCluster> clusters;


	public List<ActivityCluster> getClusters() {
		return clusters;
	}

	public void setClusters(List<ActivityCluster> clusters) {
		this.clusters = clusters;
	}
	

	public static class ActivityCluster {
		private String type;
		private List<ActivityClusterAssignment> assignments;
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public List<ActivityClusterAssignment> getAssignments() {
			return assignments;
		}
		public void setAssignments(List<ActivityClusterAssignment> assignments) {
			this.assignments = assignments;
		}
	}
	
	public static class ActivityClusterAssignment {
		private Set<String> keys;
		private Set<String> activities;
		public Set<String> getKeys() {
			return keys;
		}
		public void setKeys(Set<String> keys) {
			this.keys = keys;
		}
		public Set<String> getActivities() {
			if (activities == null) activities = new HashSet<>();
			return activities;
		}
		public void setActivities(Set<String> activities) {
			this.activities = activities;
		}
	}
}
