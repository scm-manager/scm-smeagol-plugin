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

import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;

class IndexingContext {

  private final RepositoryService repositoryService;
  private final IndexStatusStore indexStatusStore;
  private final Indexer indexer;

  IndexingContext(RepositoryService repositoryService, IndexStatusStore indexStatusStore, Indexer indexer) {
    this.repositoryService = repositoryService;
    this.indexStatusStore = indexStatusStore;
    this.indexer = indexer;
  }

  public Repository getRepository() {
    return repositoryService.getRepository();
  }

  public Indexer getIndexer() {
    return indexer;
  }

  public IndexStatusStore getIndexStatusStore() {
    return indexStatusStore;
  }

  public UpdatePathCollector getUpdatePathCollector() {
    return new UpdatePathCollector(repositoryService, new SmeagolConfigurationResolver(repositoryService));
  }

  public RevisionPathCollector getRevisionPathCollector() {
    return new RevisionPathCollector(repositoryService, new SmeagolConfigurationResolver(repositoryService));
  }

  public DefaultBranchResolver getDefaultBranchResolver() {
    return new DefaultBranchResolver(repositoryService);
  }

}
