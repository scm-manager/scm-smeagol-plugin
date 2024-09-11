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
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RevisionPathCollector implements PathCollector {

  private static final Logger LOG = LoggerFactory.getLogger(RevisionPathCollector.class);

  private final Set<String> pathToStore = new HashSet<>();
  private final RepositoryService repositoryService;
  private final SmeagolConfigurationResolver smeagolConfigurationResolver;

  public RevisionPathCollector(RepositoryService repositoryService, SmeagolConfigurationResolver smeagolConfigurationResolver) {
    this.repositoryService = repositoryService;
    this.smeagolConfigurationResolver = smeagolConfigurationResolver;
  }

  @Override
  public Collection<String> getPathToDelete() {
    return Collections.emptySet();
  }

  @Override
  public Collection<String> getPathToStore() {
    return pathToStore;
  }

  public void collect(String revision) {
    smeagolConfigurationResolver.readConfig();
    smeagolConfigurationResolver
      .getSmeagolPath()
      .ifPresent(smeagolPath -> collect(revision, smeagolPath));
  }

  private void collect(String revision, String smeagolPath) {
    browseSmeagolDocuments(revision, smeagolPath)
      .ifPresent(result -> collect(result.getFile()));
  }

  private Optional<BrowserResult> browseSmeagolDocuments(String revision, String smeagolPath) {
    LOG.debug("browsing smeagol documents in directory {} in revision {} in repository {}", smeagolPath, revision, repositoryService.getRepository());
    try {
      return of(repositoryService.getBrowseCommand()
        .setDisableSubRepositoryDetection(true)
        .setDisableLastCommit(true)
        .setDisablePreProcessors(true)
        .setLimit(Integer.MAX_VALUE)
        .setRecursive(true)
        .setRevision(revision)
        .setPath(smeagolPath)
        .getBrowserResult());
    } catch (NotFoundException e) {
      LOG.info("configured smeagol directory '{}' not found in revision {} in repository {}", smeagolPath, revision, repositoryService.getRepository());
    } catch (IOException e) {
      LOG.warn("error while browsing documents for repository {} with revision {}", repositoryService.getRepository(), revision, e);
    }
    return empty();
  }

  private void collect(FileObject file) {
    if (file.isDirectory()) {
      for (FileObject child : file.getChildren()) {
        collect(child);
      }
    } else if (smeagolConfigurationResolver.isSmeagolDocument(file.getPath())) {
      pathToStore.add(file.getPath());
    }
  }
}
