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

import com.cloudogu.scm.search.IndexStatusStore;
import com.cloudogu.scm.search.Indexer;
import com.cloudogu.scm.search.IndexingContext;
import com.cloudogu.scm.search.RevisionPathCollector;
import com.cloudogu.scm.search.UpdatePathCollector;
import sonia.scm.repository.api.RepositoryService;

class SmeagolIndexingContext extends IndexingContext<SmeagolDocument> {

  SmeagolIndexingContext(RepositoryService repositoryService, IndexStatusStore indexStatusStore, Indexer<SmeagolDocument> indexer) {
    super(repositoryService, indexStatusStore, indexer, 1);
  }

  @Override
  public UpdatePathCollector getUpdatePathCollector() {
    return new SmeagolUpdatePathCollector(getRepositoryService(), new SmeagolConfigurationResolver(getRepositoryService()));
  }

  @Override
  public RevisionPathCollector getRevisionPathCollector() {
    return new SmeagolRevisionPathCollector(getRepositoryService(), new SmeagolConfigurationResolver(getRepositoryService()));
  }
}
