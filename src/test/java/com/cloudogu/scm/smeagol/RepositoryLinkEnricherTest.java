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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryLinkEnricherTest {

  @Mock
  private SmeagolRepositoryStore store;
  @Mock
  private SmeagolConfiguration configuration;

  @InjectMocks
  private RepositoryLinkEnricher enricher;

  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;

  @Test
  void shouldAppendSmeagolLink() {
    Repository repository = RepositoryTestData.createHeartOfGold("git");
    repository.setId("id-1");
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(configuration.get()).thenReturn(createConfig(true));
    when(store.getFor(repository)).thenReturn(new SmeagolRepositoryInformation(repository, new RepositoryInformation("develop", true)));

    enricher.enrich(context, appender);

    verify(appender).appendLink("smeagolWiki", "http://wiki.com/smeagol/id-1");
  }

  @Test
  void shouldNotAppendSmeagolLinkWhenNotEnabled() {
    when(configuration.get()).thenReturn(createConfig(false));

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  void shouldNotAppendSmeagolLinkWhenNotSmeagolRepository() {
    Repository repository = RepositoryTestData.createHeartOfGold("git");
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(configuration.get()).thenReturn(createConfig(true));
    when(store.getFor(repository)).thenReturn(new SmeagolRepositoryInformation(repository, new RepositoryInformation("develop", false)));

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  void shouldNotAppendSmeagolLinkWhenNotSmeagolRelevant() {
    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(configuration.get()).thenReturn(createConfig(true));

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  private SmeagolConfiguration.Config createConfig(boolean enabled) {
    SmeagolConfiguration.Config config = new SmeagolConfiguration.Config();
    config.setEnabled(enabled);
    config.setSmeagolUrl("http://wiki.com/smeagol");
    return config;
  }
}
