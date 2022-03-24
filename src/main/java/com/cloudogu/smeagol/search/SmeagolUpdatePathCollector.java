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

import com.cloudogu.scm.search.UpdatePathCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Optional;

public class SmeagolUpdatePathCollector extends UpdatePathCollector {

  private static final Logger LOG = LoggerFactory.getLogger(SmeagolUpdatePathCollector.class);

  private final SmeagolConfigurationResolver smeagolConfigurationResolver;

  private boolean configurationChanged = false;

  public SmeagolUpdatePathCollector(RepositoryService repositoryService, SmeagolConfigurationResolver smeagolConfigurationResolver) {
    super(repositoryService);
    this.smeagolConfigurationResolver = smeagolConfigurationResolver;
  }

  @Override
  public void collect(String from, String to) throws IOException {
    smeagolConfigurationResolver.readConfig();
    Optional<String> smeagolPath = smeagolConfigurationResolver.getSmeagolPath();
    if (!smeagolPath.isPresent()) {
      return;
    }

    super.collect(from, to);
  }

  @Override
  protected void store(String path) {
    if (smeagolConfigurationResolver.isSmeagolDocument(path)) {
      super.store(path);
    } else if (path.equals(".smeagol.yml")) {
      changedConfigurationDetected();
    }
  }

  @Override
  protected void delete(String path) {
    if (smeagolConfigurationResolver.isSmeagolDocument(path)) {
      super.delete(path);
    } else if (path.equals(".smeagol.yml")) {
      changedConfigurationDetected();
    }
  }

  private void changedConfigurationDetected() {
    LOG.debug("detected changed smeagol configuration file in repository {}", getRepositoryService().getRepository());
    configurationChanged = true;
  }

  public boolean isConfigurationChanged() {
    return configurationChanged;
  }
}
