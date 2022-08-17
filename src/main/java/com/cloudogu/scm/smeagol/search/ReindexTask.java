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

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.repository.Repository;
import sonia.scm.search.Index;
import sonia.scm.search.SerializableIndexTask;

import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public class ReindexTask implements SerializableIndexTask<SmeagolDocument> {

  private final Repository repository;

  private IndexSyncer syncer;

  public ReindexTask(Repository repository) {
    this.repository = repository;
  }

  @Inject
  public void setSyncer(IndexSyncer syncer) {
    this.syncer = syncer;
  }

  @VisibleForTesting
  Repository getRepository() {
    return repository;
  }

  @Override
  public void update(Index<SmeagolDocument> index) {
    syncer.reindex(index, repository);
  }
}
