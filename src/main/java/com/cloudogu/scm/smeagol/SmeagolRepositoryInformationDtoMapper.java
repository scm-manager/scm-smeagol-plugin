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

import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

import jakarta.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
abstract class SmeagolRepositoryInformationDtoMapper {

  @Inject
  SmeagolLinkBuilder linkBuilder;

  @Mapping(target = "attributes", ignore = true)
  abstract SmeagolRepositoryInformationDto map(SmeagolRepositoryInformation information);

  @ObjectFactory
  SmeagolRepositoryInformationDto create(SmeagolRepositoryInformation information) {
    Links.Builder links = linkingTo().single(link("ui", this.linkBuilder.getUILink(information)));
    return new SmeagolRepositoryInformationDto(links.build());
  }
}
