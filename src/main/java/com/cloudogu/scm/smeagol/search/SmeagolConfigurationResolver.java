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

package com.cloudogu.scm.smeagol.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import sonia.scm.NotFoundException;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class SmeagolConfigurationResolver {

  private static final Logger LOG = LoggerFactory.getLogger(SmeagolConfigurationResolver.class);
  private static final String DEFAULT_DOCS_DIRECTORY = "docs";
  private static final String MARKDOWN_SUFFIX = ".md";

  private final RepositoryService service;
  private String smeagolPath;

  public SmeagolConfigurationResolver(RepositoryService service) {
    this.service = service;
  }

  void readConfig() {
    readConfig(null);
  }

  void readConfig(@Nullable String revision) {
    readSmeagolConfig(revision).ifPresent(
      smeagolConfig -> {
        Yaml yaml = new Yaml();
        Map<String, Object> smeagol = yaml.load(smeagolConfig);
        extractPathFromConfigMap(smeagol);
      }
    );
  }

  private Optional<String> readSmeagolConfig(@Nullable String revision) {
    try {
      CatCommandBuilder catCommand = service.getCatCommand();
      if (revision != null) {
        LOG.trace("using revision {}", revision);
        catCommand.setRevision(revision);
      }
      String content = catCommand.getContent(".smeagol.yml");
      return of(content);
    } catch (NotFoundException e) {
      LOG.trace("no file '.smeagol.yml' found in repository {}", service.getRepository());
    } catch (IOException e) {
      LOG.warn("could not read smeagol configuration for repository {}", service.getRepository());
    }
    return empty();
  }

  Optional<String> getSmeagolPath() {
    return ofNullable(smeagolPath);
  }

  private void extractPathFromConfigMap(Map<String, Object> smeagol) {
    if (smeagol == null) {
      smeagolPath = DEFAULT_DOCS_DIRECTORY;
      LOG.trace("found empty smeagol configuration for repository {}; using default directory {}", service.getRepository(), smeagolPath);
    } else {
      smeagolPath = smeagol.getOrDefault("directory", DEFAULT_DOCS_DIRECTORY).toString();
      LOG.trace("found smeagol configuration with directory {} for repository {}", smeagolPath, service.getRepository());
    }
  }

  boolean isSmeagolDocument(String path) {
    return path.startsWith(smeagolPath) && path.toLowerCase(Locale.ENGLISH).endsWith(MARKDOWN_SUFFIX);
  }
}
