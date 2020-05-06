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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import eu.ai4eu.ai4citizen.internshipbrowser.service.DataInitializer;
import eu.ai4eu.ai4citizen.internshipbrowser.service.GroupBuilderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author raman
 *
 */
@Controller
@Api(tags = { "Internship Browser Admin API" })
public class AdminController {

	@Autowired
	private DataInitializer initializer;
	@Autowired
	private GroupBuilderService groupBuilder;
	
	@PostMapping("/admin/reset")
	@ApiOperation(value="Reset all data")
	public ResponseEntity<Void> resetAll() throws Exception {
		initializer.initData();
		return ResponseEntity.ok().build();
	}

	@PostMapping("/admin/assign")
	@ApiOperation(value="Trigger group building algorithm")
	public ResponseEntity<Void> assignAll() throws Exception {
		groupBuilder.buildAll();
		return ResponseEntity.ok().build();
	}

}
