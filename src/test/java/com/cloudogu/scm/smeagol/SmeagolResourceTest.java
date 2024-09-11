/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.smeagol;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.resteasy.mock.MockHttpRequest.get;
import static org.jboss.resteasy.mock.MockHttpRequest.put;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmeagolResourceTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private final RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private SmeagolRepositoryStore store;
  @Mock
  private SmeagolLinkBuilder smeagolLinkBuilder;
  @Mock
  private SmeagolConfiguration configuration;
  @Mock
  private Subject subject;

  private final MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void initMocks() {
    SmeagolConfigurationDtoMapperImpl configurationMapper = new SmeagolConfigurationDtoMapperImpl();
    configurationMapper.linkBuilder = smeagolLinkBuilder;
    SmeagolRepositoryInformationDtoMapper informationMapper = new SmeagolRepositoryInformationDtoMapperImpl();
    informationMapper.linkBuilder = smeagolLinkBuilder;
    SmeagolResource resource = new SmeagolResource(store, smeagolLinkBuilder, configuration, configurationMapper, informationMapper);
    dispatcher.addSingletonResource(resource);
  }

  @BeforeEach
  void mockSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void cleanup() {
    ThreadContext.unbindSubject();
  }

  @BeforeEach
  void initLinks() {
    lenient().when(smeagolLinkBuilder.getRepositoriesLink()).thenReturn("/v2/smeagol/repositories");
    lenient().when(smeagolLinkBuilder.getConfigurationLink()).thenReturn("/v2/smeagol/configuration");
    lenient().when(smeagolLinkBuilder.getUILink(any())).thenAnswer(invocation -> {
      SmeagolRepositoryInformation information = invocation.getArgument(0, SmeagolRepositoryInformation.class);
      return format("/repo/%s/%s", information.getNamespace(), information.getName());
    });
  }

  @Test
  void shouldGetRepositories() throws URISyntaxException, UnsupportedEncodingException {
    when(store.getRepositories()).thenReturn(singletonList(new SmeagolRepositoryInformation(REPOSITORY, new RepositoryInformation("main", true))));

    MockHttpRequest request = get("/v2/smeagol/repositories");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_OK);
    assertThat(response.getContentAsString())
      .contains("\"repositories\":[")
      .contains("\"self\":{\"href\":\"/v2/smeagol/repositories\"}")
      .contains("\"namespace\":\"hitchhiker\"")
      .contains("\"wikiEnabled\":true")
      .contains("\"ui\":{\"href\":\"/repo/hitchhiker/HeartOfGold\"}");
  }

  @Test
  void shouldGetSmeagolRepositoriesOnly() throws URISyntaxException, UnsupportedEncodingException {
    when(store.getRepositories()).thenReturn(singletonList(new SmeagolRepositoryInformation(REPOSITORY, new RepositoryInformation("main", false))));

    MockHttpRequest request = get("/v2/smeagol/repositories?wikiEnabled=true");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_OK);
    assertThat(response.getContentAsString())
      .contains("\"repositories\":[]");
  }

  @Test
  void shouldGetConfiguration() throws URISyntaxException, UnsupportedEncodingException {
    SmeagolConfiguration.Config config = new SmeagolConfiguration.Config();
    config.setSmeagolUrl("http://smeagol.com/");
    when(configuration.get()).thenReturn(config);

    MockHttpRequest request = get("/v2/smeagol/configuration");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_OK);
    assertThat(response.getContentAsString())
      .contains("\"smeagolUrl\":\"http://smeagol.com/\"")
      .contains("\"self\":{\"href\":\"/v2/smeagol/configuration\"}")
      .doesNotContain("\"update\"");
  }

  @Test
  void shouldAddConfigurationUpdateLink() throws URISyntaxException, UnsupportedEncodingException {
    when(configuration.get()).thenReturn(new SmeagolConfiguration.Config());
    when(subject.isPermitted("configuration:write:smeagol")).thenReturn(true);

    MockHttpRequest request = get("/v2/smeagol/configuration");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_OK);
    assertThat(response.getContentAsString())
      .contains("\"update\":{\"href\":\"/v2/smeagol/configuration\"}");
  }

  @Test
  void shouldSetConfiguration() throws URISyntaxException {
    MockHttpRequest request =
      put("/v2/smeagol/configuration")
        .contentType("application/json")
        .content("{\"enabled\":true,\"smeagolUrl\":\"http://smeagol.com/\"}".getBytes(UTF_8));

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_NO_CONTENT);
    verify(configuration).set(argThat(argument -> {
      assertThat(argument.getSmeagolUrl()).isEqualTo("http://smeagol.com/");
      assertThat(argument.isEnabled()).isTrue();
      return true;
    }));
  }
}
