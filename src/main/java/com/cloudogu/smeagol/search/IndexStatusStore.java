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

import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Optional;

import static com.cloudogu.smeagol.search.IndexStatus.EMPTY;


@Singleton
public class IndexStatusStore {

  private static final String STORE_NAME ="content-search-status";

  private final DataStore<IndexStatus> store;

  @Inject
  public IndexStatusStore(DataStoreFactory storeFactory) {
    this.store = storeFactory.withType(IndexStatus.class).withName(STORE_NAME).build();
  }

  public void empty(Repository repository) {
    update(repository, EMPTY);
  }

  private IndexStatus status(String revision) {
    return new IndexStatus(revision, Instant.now(), SmeagolDocument.VERSION);
  }

  public void update(Repository repository, String revision) {
    store.put(repository.getId(), status(revision));
  }

  void update(Repository repository, String revision, int version) {
    IndexStatus status = status(revision);
    status.setVersion(version);
    store.put(repository.getId(), status);
  }

  public Optional<IndexStatus> get(Repository repository) {
    return store.getOptional(repository.getId());
  }

}

