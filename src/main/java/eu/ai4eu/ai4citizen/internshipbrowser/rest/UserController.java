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

import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.io.Resources;

import eu.ai4eu.ai4citizen.internshipbrowser.model.StudentProfile;
import eu.ai4eu.ai4citizen.internshipbrowser.service.BrowserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author raman
 *
 */
@RestController
@Api(tags = { "Internship Browser User API" })
public class UserController {

	@Autowired
	private BrowserService service;

	private Map<String, String> users = new HashMap<>();
	
	@PostConstruct
	public void init() {
		try {
			List<String> lines = Resources.readLines(new URL("classpath:/data/users.csv"), Charset.defaultCharset());
			lines.forEach(l -> users.put(l.substring(0, l.indexOf(',')), l.substring(l.indexOf(',') + 1)));
		} catch (Exception e) {
		}
	}
	
	@GetMapping("/api/user/me")
	@ApiOperation(value="Get current user info")
	public ResponseEntity<StudentProfile> getProfile(@RequestHeader("Authorization") String header) {
		String username = getCurrentUserLogin().orElse(null);
		String studentId = users.getOrDefault(username, "1");
		return ResponseEntity.ok(service.getProfile(studentId));
	}

	
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof Jwt) {
        	Jwt jwt = (Jwt) authentication.getPrincipal();
            return jwt.getClaimAsString("username");
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

}
