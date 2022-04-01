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

package com.cloudogu.scm.smeagol.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.search.Id;
import sonia.scm.search.Index;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
class Indexer {

  private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

  private final SmeagolDocumentFactory smeagolDocumentFactory;
  private final Index<SmeagolDocument> index;
  private final RepositoryService repositoryService;
  private final Repository repository;

  @Inject
  Indexer(SmeagolDocumentFactory smeagolDocumentFactory, Index<SmeagolDocument> index, RepositoryService repositoryService) {
    this.smeagolDocumentFactory = smeagolDocumentFactory;
    this.index = index;
    this.repositoryService = repositoryService;
    this.repository = repositoryService.getRepository();
  }

  void store(Branch branch, Collection<String> paths) throws IOException {
    if (paths.isEmpty()) {
      return;
    }

    for (String path : paths) {
      LOG.trace("store {} to index", path);
      SmeagolDocument fileContent = smeagolDocumentFactory.create(repositoryService, branch, path);
      index.store(id(path), permission(), fileContent);
    }
  }

  void delete(Collection<String> paths) {
    if (paths.isEmpty()) {
      return;
    }
    Index.Deleter<SmeagolDocument> deleter = index.delete();
    for (String path : paths) {
      LOG.trace("delete {} from index", path);
      deleter.byId(id(path));
    }
  }

  void deleteAll() {
    index.delete().by(Repository.class, repository).execute();
  }

  private String permission() {
    return RepositoryPermissions.pull(repository).asShiroString();
  }

  private Id<SmeagolDocument> id(String path) {
    return Id.of(SmeagolDocument.class, path).and(Repository.class, repository);
  }

}
