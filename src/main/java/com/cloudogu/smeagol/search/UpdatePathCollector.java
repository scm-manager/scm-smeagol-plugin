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
import sonia.scm.repository.Added;
import sonia.scm.repository.Copied;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UpdatePathCollector implements PathCollector {

  private static final Logger LOG = LoggerFactory.getLogger(UpdatePathCollector.class);

  private final Set<String> pathToStore = new HashSet<>();
  private final Set<String> pathToDelete = new HashSet<>();
  private final RepositoryService repositoryService;
  private final SmeagolConfigurationResolver smeagolConfigurationResolver;

  public UpdatePathCollector(RepositoryService repositoryService, SmeagolConfigurationResolver smeagolConfigurationResolver) {
    this.repositoryService = repositoryService;
    this.smeagolConfigurationResolver = smeagolConfigurationResolver;
  }

  @Override
  public Collection<String> getPathToStore() {
    return pathToStore;
  }

  @Override
  public Collection<String> getPathToDelete() {
    return pathToDelete;
  }

  void collect(String from, String to) {
    smeagolConfigurationResolver.readConfig();
    Optional<String> smeagolPath = smeagolConfigurationResolver.getSmeagolPath();
    if (!smeagolPath.isPresent()) {
      return;
    }

    try {
      Modifications modifications = repositoryService.getModificationsCommand()
        .baseRevision(from)
        .revision(to)
        .disablePreProcessors(true)
        .disableCache(true)
        .getModifications();

      collect(modifications);
    } catch (IOException e) {
      LOG.warn("could not load modifications from revision {} to {} in repository {}", from, to, repositoryService.getRepository());
    }
  }

  private void collect(Modifications modifications) {
    added(modifications.getAdded());
    modified(modifications.getModified());
    copied(modifications.getCopied());
    removed(modifications.getRemoved());
    renamed(modifications.getRenamed());
  }

  private void added(List<Added> modifications) {
    for (Added modification : modifications) {
      store(modification.getPath());
    }
  }

  private void modified(List<Modified> modifications) {
    for (Modified modification : modifications) {
      store(modification.getPath());
    }
  }

  private void copied(List<Copied> modifications) {
    for (Copied modification : modifications) {
      store(modification.getTargetPath());
    }
  }

  private void removed(List<Removed> modifications) {
    for (Removed modification : modifications) {
      delete(modification.getPath());
    }
  }

  private void renamed(List<Renamed> modifications) {
    for (Renamed modification : modifications) {
      store(modification.getNewPath());
      delete(modification.getOldPath());
    }
  }

  private void store(String path) {
    if (smeagolConfigurationResolver.isSmeagolDocument(path)) {
      pathToStore.add(path);
    }
  }

  private void delete(String path) {
    if (smeagolConfigurationResolver.isSmeagolDocument(path)) {
      pathToDelete.add(path);
    }
  }

}
