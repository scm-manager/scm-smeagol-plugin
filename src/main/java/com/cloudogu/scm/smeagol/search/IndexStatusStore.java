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

import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.Optional;

@Singleton
public class IndexStatusStore {

  private static final String STORE_NAME = "smeagol-search-status";

  private final DataStore<IndexStatus> store;

  @Inject
  public IndexStatusStore(DataStoreFactory storeFactory) {
    this.store = storeFactory.withType(IndexStatus.class).withName(STORE_NAME).build();
  }

  void empty(Repository repository) {
    store.put(repository.getId(), IndexStatus.createEmpty());
  }

  public void update(Repository repository, Branch branch) {
    store.put(repository.getId(), status(branch));
  }

  private IndexStatus status(Branch branch) {
    return new IndexStatus(branch.getRevision(), branch.getName(), Instant.now(), SmeagolDocument.VERSION);
  }

  Optional<IndexStatus> get(Repository repository) {
    return store.getOptional(repository.getId());
  }
}

