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
