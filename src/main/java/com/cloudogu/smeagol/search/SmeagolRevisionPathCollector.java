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

import com.cloudogu.scm.search.RevisionPathCollector;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Optional;

public class SmeagolRevisionPathCollector extends RevisionPathCollector {

  private final SmeagolConfigurationResolver smeagolConfigurationResolver;

  private String smeagolPath;

  public SmeagolRevisionPathCollector(RepositoryService repositoryService, SmeagolConfigurationResolver smeagolConfigurationResolver) {
    super(repositoryService);
    this.smeagolConfigurationResolver = smeagolConfigurationResolver;
  }

  @Override
  public void collect(String revision) throws IOException {
    smeagolConfigurationResolver.readConfig();
    Optional<String> optionalSmeagolPath = smeagolConfigurationResolver
      .getSmeagolPath();
    if (optionalSmeagolPath.isPresent()) {
      collect(revision, optionalSmeagolPath.get());
    }
  }

  private void collect(String revision, String smeagolPath) throws IOException {
    this.smeagolPath = smeagolPath;
    super.collect(revision);
  }

  @Override
  protected BrowseCommandBuilder createBrowseCommandBuilder(String revision) {
    return super.createBrowseCommandBuilder(revision)
      .setPath(smeagolPath);
  }

  @Override
  protected void store(FileObject file) {
    if (smeagolConfigurationResolver.isSmeagolDocument(file.getPath())) {
      super.store(file);
    }
  }
}
