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

import org.apache.shiro.SecurityUtils;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;

@Extension
@Enrich(Index.class)
class IndexLinkEnricher implements HalEnricher {

  private final SmeagolLinkBuilder smeagolLinkBuilder;
  private final SmeagolConfiguration configuration;

  @Inject
  public IndexLinkEnricher(SmeagolLinkBuilder smeagolLinkBuilder,  SmeagolConfiguration configuration) {
    this.smeagolLinkBuilder = smeagolLinkBuilder;
    this.configuration = configuration;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    HalAppender.LinkArrayBuilder smeagolArray = appender.linkArrayBuilder("smeagol");
    smeagolArray
      .append("repositories", smeagolLinkBuilder.getRepositoriesLink());
    if (configuration.get().isEnabled() && configuration.get().isNavLinkEnabled()) {
      smeagolArray.append("smeagolRoot", configuration.get().getSmeagolUrl());
    }
    smeagolArray.build();
    if (SecurityUtils.getSubject().isAuthenticated()) {
      appender.appendLink("smeagolConfig", smeagolLinkBuilder.getConfigurationLink());
    }
  }
}
