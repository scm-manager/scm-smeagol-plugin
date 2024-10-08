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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexLinkEnricherTest {

  @Mock
  private SmeagolLinkBuilder smeagolLinkBuilder;

  @InjectMocks
  private IndexLinkEnricher enricher;

  @Mock
  private Subject subject;
  @Mock
  private SmeagolConfiguration configuration;
  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;
  @Mock(answer = Answers.RETURNS_SELF)
  private HalAppender.LinkArrayBuilder linkArrayBuilder;

  @BeforeEach
  void initSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void cleanup() {
    ThreadContext.unbindSubject();
  }

  @BeforeEach
  void mockLinks() {
    lenient().when(appender.linkArrayBuilder("smeagol")).thenReturn(linkArrayBuilder);
    lenient().when(smeagolLinkBuilder.getRepositoriesLink()).thenReturn("/smeagol/repositories");
    lenient().when(smeagolLinkBuilder.getConfigurationLink()).thenReturn("/smeagol/configuration");
    lenient().when(configuration.get()).thenReturn(new SmeagolConfiguration.Config());
  }

  @Test
  void shouldAppendSmeagolLink() {
    enricher.enrich(context, appender);

    verify(linkArrayBuilder).append("repositories", "/smeagol/repositories");
    verify(linkArrayBuilder, never()).append(eq("smeagolRoot"), anyString());
    verify(linkArrayBuilder).build();
  }

  @Test
  void shouldNotAppendConfigLinkWithoutUser() {
    when(subject.isAuthenticated()).thenReturn(false);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(eq("smeagolConfig"), anyString());
  }

  @Test
  void shouldAppendConfigForUsersLink() {
    when(subject.isAuthenticated()).thenReturn(true);

    enricher.enrich(context, appender);

    verify(appender).appendLink("smeagolConfig", "/smeagol/configuration");
  }

  @Test
  void shouldAppendRootLink() {
    SmeagolConfiguration.Config config = new SmeagolConfiguration.Config();
    config.setNavLinkEnabled(true);
    config.setEnabled(true);
    String expectedUrl = "http://localhost:8080/smeagol";
    config.setSmeagolUrl(expectedUrl);

    when(configuration.get()).thenReturn(config);
    enricher.enrich(context, appender);

    verify(linkArrayBuilder).append("smeagolRoot", expectedUrl);
  }
}
