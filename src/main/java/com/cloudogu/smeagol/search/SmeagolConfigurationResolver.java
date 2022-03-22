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

package com.cloudogu.smeagol.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class SmeagolConfigurationResolver {

  private static final Logger LOG = LoggerFactory.getLogger(SmeagolConfigurationResolver.class);

  private final RepositoryService service;

  public SmeagolConfigurationResolver(RepositoryService service) {
    this.service = service;
  }

  Optional<String> getSmeagolPath() {
    try {
      String smeagolConfig = service.getCatCommand().getContent(".smeagol.yml");
      ObjectMapper om = new ObjectMapper(new YAMLFactory());
      Smeagol smeagol = om.readValue(smeagolConfig, Smeagol.class);
      return ofNullable(smeagol.getDirectory());
    } catch (IOException e) {
      LOG.warn("could not read smeagol configuration for repository {}", service.getRepository());
      return empty();
    }
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Smeagol {
    String directory;
  }

}
