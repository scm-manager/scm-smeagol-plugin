/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.smeagol;

import com.cloudogu.smeagol.SmeagolConfiguration.Config;
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
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
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
    Config config = new Config();
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
    when(configuration.get()).thenReturn(new Config());
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
