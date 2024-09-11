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
import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.search.Id;
import sonia.scm.search.Index;

import jakarta.inject.Inject;
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
