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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.security.AllowAnonymousAccess;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
