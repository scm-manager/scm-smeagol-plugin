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
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setId("id-1");
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(configuration.get()).thenReturn(createConfig(true));
    when(store.getFor(repository)).thenReturn(new SmeagolRepositoryInformationDto(repository, new RepositoryInformation("develop", true)));

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
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(configuration.get()).thenReturn(createConfig(true));
    when(store.getFor(repository)).thenReturn(new SmeagolRepositoryInformationDto(repository, new RepositoryInformation("develop", false)));

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
