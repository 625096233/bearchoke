/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bearchoke.platform.server.frontend.web.controller;


import com.bearchoke.platform.server.common.ApplicationMediaType;
import com.bearchoke.platform.server.common.config.AppLocalConfig;
import com.bearchoke.platform.server.common.config.WebSecurityConfig;
import com.bearchoke.platform.server.common.web.config.WebMvcConfig;
import com.bearchoke.platform.server.frontend.web.config.MockServerConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



/**
 * Created by Bjorn Harvold
 * <p/>
 * Date: 7/28/14
 * <p/>
 * Time: 3:26 PM
 * <p/>
 * Responsibility:
 */
@Log4j2
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes =
        {
                MockServerConfig.class,
                WebSecurityConfig.class,
                WebMvcConfig.class,
                AppLocalConfig.class
        }
)
@ActiveProfiles("local")
@TestExecutionListeners(listeners={ServletTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class})
public class GreetingControllerTest extends AbstractControllerTest {

    private static final String USER = "user";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    @Qualifier("springSessionRepositoryFilter")
    private Filter springSessionRepositoryFilter;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilters(springSessionRepositoryFilter, springSecurityFilterChain)
                .build();
    }

    @Test
    public void testVersionedGreeting() throws Exception {
        log.info("Testing GreetingController.testVersionedGreeting...");

        this.mockMvc.perform(get("/api/greeting").param("name", USER).accept(ApplicationMediaType.APPLICATION_BEARCHOKE_V1_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(ApplicationMediaType.APPLICATION_BEARCHOKE_V1_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Hello, " + USER + "!"));

        log.info("Testing GreetingController.testVersionedGreeting SUCCESSFUL");
    }

    @Test
    public void testSecuredGreeting() throws Exception {
        log.info("Testing GreetingController.testSecuredGreeting...");

        this.mockMvc.perform(get("/api/secured/greeting")
                .with(csrf())
                .with(regularUser())
                .accept(ApplicationMediaType.APPLICATION_BEARCHOKE_V1_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(ApplicationMediaType.APPLICATION_BEARCHOKE_V1_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Hello, " + user.getUsername() + ". You have the role of ROLE_USER!"))
                .andExpect(authenticated().withRoles("USER"))
        ;

        log.info("Testing GreetingController.testSecuredGreeting SUCCESSFUL");
    }

    @Test
    public void testUnauthenticated() throws Exception {
        log.info("Testing hitting a secured url while unauthenticated...");

        this.mockMvc.perform(get("/api/secured/greeting")
                .with(csrf()))
                .andExpect(unauthenticated())
        ;

        log.info("Testing hitting a secured url while unauthenticated SUCCESS");
    }
}

