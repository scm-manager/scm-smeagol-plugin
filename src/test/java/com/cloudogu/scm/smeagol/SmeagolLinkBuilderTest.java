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
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static com.google.inject.util.Providers.of;
import static org.assertj.core.api.Assertions.assertThat;

class SmeagolLinkBuilderTest {

  @Test
  void shouldGenerateRepositoriesLink() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));

    SmeagolLinkBuilder smeagolLinkBuilder = new SmeagolLinkBuilder(of(pathInfoStore));

    String link = smeagolLinkBuilder.getRepositoriesLink();

    assertThat(link).isEqualTo("/v2/smeagol/repositories");
  }

  @Test
  void shouldGenerateConfigurationLink() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));

    SmeagolLinkBuilder smeagolLinkBuilder = new SmeagolLinkBuilder(of(pathInfoStore));

    String link = smeagolLinkBuilder.getConfigurationLink();

    assertThat(link).isEqualTo("/v2/smeagol/configuration");
  }
}
