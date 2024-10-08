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

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.util.Optional;

@Slf4j
class IndexSyncWorker {

  private final DefaultBranchResolver defaultBranchResolver;
  private final UpdatePathCollector updatePathCollector;
  private final RevisionPathCollector revisionPathCollector;
  private final IndexStatusStore indexStatusStore;
  private final Indexer indexer;

  private final Repository repository;

  IndexSyncWorker(IndexingContext indexingContext) {
    this(indexingContext.getDefaultBranchResolver(),
      indexingContext.getUpdatePathCollector(),
      indexingContext.getRevisionPathCollector(),
      indexingContext.getIndexStatusStore(),
      indexingContext.getIndexer(),
      indexingContext.getRepository()
    );
  }

  @VisibleForTesting
  IndexSyncWorker(
    DefaultBranchResolver defaultBranchResolver,
    UpdatePathCollector updatePathCollector,
    RevisionPathCollector revisionPathCollector,
    IndexStatusStore indexStatusStore,
    Indexer indexer,
    Repository repository
  ) {
    this.defaultBranchResolver = defaultBranchResolver;
    this.updatePathCollector = updatePathCollector;
    this.revisionPathCollector = revisionPathCollector;
    this.indexStatusStore = indexStatusStore;
    this.indexer = indexer;
    this.repository = repository;
  }

  public void ensureIndexIsUpToDate() throws IOException {
    Optional<IndexStatus> status = indexStatusStore.get(repository);
    Optional<Branch> defaultBranch = defaultBranchResolver.resolve();
    if (!defaultBranch.isPresent()) {
      log.warn("no default branch found for repository {}", repository);
      emptyRepository();
      return;
    }
    if (status.isPresent()) {
      IndexStatus indexStatus = status.get();
      if (indexStatus.getVersion() != SmeagolDocument.VERSION) {
        log.debug(
          "found index of repository {} in version {} required is {}, trigger reindex",
          repository, indexStatus.getVersion(), SmeagolDocument.VERSION
        );
        reIndex(defaultBranch.get());
      } else if (indexStatus.isEmpty()) {
        log.debug(
          "no index status found for repository {}, trigger reindex",
          repository
        );
        reIndex(defaultBranch.get());
      } else if (!indexStatus.getBranch().equals(defaultBranch.get().getName())) {
        log.debug(
          "default branch changed from {} to {} in repository {}, trigger reindex",
          indexStatus.getBranch(), defaultBranch.get().getName(), repository
        );
        reIndex(defaultBranch.get());
      } else {
        ensureIndexIsUpToDate(indexStatus.getRevision(), defaultBranch.get());
      }
    } else {
      log.debug("no index status present for repository {}, trigger reindex", repository);
      reIndex(defaultBranch.get());
    }
  }

  private void ensureIndexIsUpToDate(String from, Branch to) throws IOException {
    if (from.equals(to.getRevision())) {
      log.debug("index of repository {} is up to date", repository);
      return;
    }

    log.debug("start updating index of repository {} from {} to {}", repository, from, to);

    updatePathCollector.collect(from, to.getRevision());

    if (updatePathCollector.isConfigurationChanged()) {
      reIndex(to);
    } else {
      updateIndex(to, updatePathCollector);
    }
  }

  private void updateIndex(Branch branch, PathCollector collector) throws IOException {
    indexer.delete(collector.getPathToDelete());
    indexer.store(branch, collector.getPathToStore());

    indexStatusStore.update(repository, branch);
  }

  private void reIndex(Branch defaultBranch) throws IOException {
    log.debug("start re indexing for repository {}", repository);
    indexer.deleteAll();
    revisionPathCollector.collect(defaultBranch.getRevision());
    updateIndex(defaultBranch, revisionPathCollector);
  }

  void reIndex() throws IOException {
    Optional<Branch> defaultBranch = defaultBranchResolver.resolve();
    if (defaultBranch.isEmpty()) {
      log.warn("no default branch found for repository {}", repository);
      emptyRepository();
      return;
    }
    reIndex(defaultBranch.get());
  }

  private void emptyRepository() {
    log.debug("repository {} looks empty, delete all to clean up", repository);
    indexer.deleteAll();
    indexStatusStore.empty(repository);
  }

}
