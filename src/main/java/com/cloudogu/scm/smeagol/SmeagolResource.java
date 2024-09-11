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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.security.AllowAnonymousAccess;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.util.List;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

@AllowAnonymousAccess
@Path("v2/smeagol/")
public class SmeagolResource {

  private final SmeagolRepositoryStore store;
  private final SmeagolLinkBuilder smeagolLinkBuilder;
  private final SmeagolConfiguration configuration;
  private final SmeagolConfigurationDtoMapper configurationMapper;
  private final SmeagolRepositoryInformationDtoMapper informationMapper;

  @Inject
  SmeagolResource(SmeagolRepositoryStore store,
                  SmeagolLinkBuilder smeagolLinkBuilder,
                  SmeagolConfiguration configuration,
                  SmeagolConfigurationDtoMapper configurationMapper,
                  SmeagolRepositoryInformationDtoMapper informationMapper) {
    this.store = store;
    this.smeagolLinkBuilder = smeagolLinkBuilder;
    this.configuration = configuration;
    this.configurationMapper = configurationMapper;
    this.informationMapper = informationMapper;
  }

  @GET
  @Path("repositories")
  @Produces("application/json")
  public HalRepresentation loadRepositories(@QueryParam("wikiEnabled") boolean smeagolOnly) {
    List<SmeagolRepositoryInformation> repositories = store.getRepositories();
    if (smeagolOnly) {
      repositories = repositories.stream().filter(SmeagolRepositoryInformation::isWikiEnabled).collect(toList());
    }
    return new HalRepresentation(
      linkingTo().single(link("self", smeagolLinkBuilder.getRepositoriesLink())).build(),
      Embedded.embedded("repositories", repositories.stream().map(informationMapper::map).collect(toList()))
    );
  }

  @GET
  @Path("configuration")
  @Produces("application/json")
  public SmeagolConfigurationDto getSmeagolConfiguration() {
    return configurationMapper.map(configuration.get());
  }

  @PUT
  @Path("configuration")
  @Consumes("application/json")
  public void setSmeagolConfiguration(@Valid SmeagolConfigurationDto configurationDto) {
    configuration.set(configurationMapper.map(configurationDto));
  }
}
