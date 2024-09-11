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

import lombok.Getter;
import sonia.scm.repository.Repository;

@Getter
@SuppressWarnings("java:S2160") // we have no definition for equals/hashCode
class SmeagolRepositoryInformation {

  private final String namespace;
  private final String name;
  private final String id;
  private final String type;
  private final String description;
  private final String defaultBranch;
  private final boolean wikiEnabled;

  SmeagolRepositoryInformation(Repository repository, RepositoryInformation information) {
    this.namespace = repository.getNamespace();
    this.name = repository.getName();
    this.id = repository.getId();
    this.description = repository.getDescription();
    this.type = repository.getType();
    this.defaultBranch = information.getDefaultBranch();
    this.wikiEnabled = information.isWikiEnabled();
  }
}
