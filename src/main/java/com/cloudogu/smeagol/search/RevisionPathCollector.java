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
