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
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.of;

class IndexSyncWorker {

  private static final Logger LOG = LoggerFactory.getLogger(IndexSyncWorker.class);

  private final LatestRevisionResolver latestRevisionResolver;
  private final UpdatePathCollector updatePathCollector;
  private final RevisionPathCollector revisionPathCollector;
  private final IndexStatusStore indexStatusStore;
  private final Indexer indexer;

  private final Repository repository;

  IndexSyncWorker(IndexingContext indexingContext) {
    this.latestRevisionResolver = indexingContext.getLatestRevisionResolver();
    this.updatePathCollector = indexingContext.getUpdatePathCollector();
    this.revisionPathCollector = indexingContext.getRevisionPathCollector();
    this.indexStatusStore = indexingContext.getIndexStatusStore();
    this.indexer = indexingContext.getIndexer();
    this.repository = indexingContext.getRepository();
  }

  public void ensureIndexIsUpToDate() throws IOException {
    Optional<IndexStatus> status = indexStatusStore.get(repository);
    Optional<Branch> defaultBranch = latestRevisionResolver.resolve();
    if (status.isPresent()) {
      IndexStatus indexStatus = status.get();
      if (indexStatus.getVersion() != SmeagolDocument.VERSION) {
        LOG.debug(
          "found index of repository {} in version {} required is {}, trigger reindex",
          repository, indexStatus.getVersion(), SmeagolDocument.VERSION
        );
        reIndex(defaultBranch);
      } else if (indexStatus.isEmpty()) {
        reIndex(defaultBranch);
      } else if (defaultBranch.isPresent() && !indexStatus.getBranch().equals(defaultBranch.get().getName())) {
        reIndex(defaultBranch);
      } else if (!defaultBranch.isPresent()) {
        emptyRepository();
      } else {
        ensureIndexIsUpToDate(indexStatus.getRevision(), defaultBranch.get());
      }
    } else {
      LOG.debug("no index status present for repository {}, trigger reindex", repository);
      reIndex(defaultBranch);
    }
  }

  private void ensureIndexIsUpToDate(String from, Branch to) throws IOException {
    if (from.equals(to.getRevision())) {
      LOG.debug("index of repository {} is up to date", repository);
      return;
    }

    LOG.debug("start updating index of repository {} from {} to {}", repository, from, to);

    updatePathCollector.collect(from, to.getRevision());

    if (updatePathCollector.isConfigurationChanged()) {
      reIndex(of(to));
    } else {
      updateIndex(to, updatePathCollector);
    }
  }

  private void updateIndex(Branch branch, PathCollector collector) throws IOException {
    indexer.delete(collector.getPathToDelete());
    indexer.store(branch, collector.getPathToStore());

    indexStatusStore.update(repository, branch);
  }

  private void reIndex(Optional<Branch> defaultBranch) throws IOException {
    LOG.debug("start re indexing for repository {}", repository);
    indexer.deleteAll();

    if (defaultBranch.isPresent()) {
      String revision = defaultBranch.get().getRevision();
      revisionPathCollector.collect(defaultBranch.get().getRevision());
      updateIndex(defaultBranch.get(), revisionPathCollector);
    } else {
      indexStatusStore.empty(repository);
    }
  }

  private void emptyRepository() {
    LOG.debug("repository {} looks empty, delete all to clean up", repository);
    indexer.deleteAll();
    indexStatusStore.empty(repository);
  }

}
