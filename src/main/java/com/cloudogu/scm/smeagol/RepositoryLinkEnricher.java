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

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.util.HttpUtil;

import jakarta.inject.Inject;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final SmeagolRepositoryStore store;
  private final SmeagolConfiguration configuration;

  @Inject
  public RepositoryLinkEnricher(SmeagolRepositoryStore store, SmeagolConfiguration configuration) {
    this.store = store;
    this.configuration = configuration;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    if (configuration.get().isEnabled()) {
      Repository repository = context.oneRequireByType(Repository.class);
      if (shouldEnrich(repository)) {
        appender.appendLink("smeagolWiki", createSmeagolUrl(repository));
      }
    }
  }

  private boolean shouldEnrich(Repository repository) {
    return SmeagolRepositoryFilter.isPotentiallySmeagolRelevant(repository)
      && store.getFor(repository).isWikiEnabled();
  }

  private String createSmeagolUrl(Repository repository) {
    return HttpUtil.concatenate(configuration.get().getSmeagolUrl(), repository.getId());
  }
}
