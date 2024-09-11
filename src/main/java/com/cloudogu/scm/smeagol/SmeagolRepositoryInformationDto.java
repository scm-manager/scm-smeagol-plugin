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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("java:S2160") // we have no definition for equals/hashCode
public class SmeagolRepositoryInformationDto extends HalRepresentation {

  private String namespace;
  private String name;
  private String id;
  private String type;
  private String description;
  private String defaultBranch;
  private boolean wikiEnabled;

  public SmeagolRepositoryInformationDto(Links links) {
    super(links);
  }
}
