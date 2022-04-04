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

package com.cloudogu.scm.smeagol;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.config.ConfigurationPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.self;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
abstract class SmeagolConfigurationDtoMapper {

  @Inject
  @VisibleForTesting
  SmeagolLinkBuilder linkBuilder;

  @Mapping(target = "attributes", ignore = true)
  abstract SmeagolConfigurationDto map(SmeagolConfiguration.Config configuration);

  abstract SmeagolConfiguration.Config map(SmeagolConfigurationDto configuration);

  @ObjectFactory
  SmeagolConfigurationDto createDto() {
    Links.Builder links = linkingTo()
      .single(self(this.linkBuilder.getConfigurationLink()));
    if (ConfigurationPermissions.write("smeagol").isPermitted()) {
      links.single(link("update", this.linkBuilder.getConfigurationLink()));
    }
    return new SmeagolConfigurationDto(links.build());
  }
}
