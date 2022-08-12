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

import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.DefaultBranchChangedEvent;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.search.ReindexRepositoryEvent;
import sonia.scm.search.SearchEngine;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Extension
@SuppressWarnings("UnstableApiUsage")
public class IndexListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(IndexListener.class);

  private final AdministrationContext administrationContext;
  private final RepositoryManager repositoryManager;
  private final SearchEngine searchEngine;

  @Inject
  public IndexListener(AdministrationContext administrationContext, RepositoryManager repositoryManager, SearchEngine searchEngine) {
    this.administrationContext = administrationContext;
    this.repositoryManager = repositoryManager;
    this.searchEngine = searchEngine;
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    administrationContext.runAsAdmin(() -> {
      for (Repository repository : repositoryManager.getAll()) {
        LOG.debug("startup check if index of repository {} requires update", repository);
        submit(repository);
      }
    });
  }

  @Subscribe
  public void handle(PostReceiveRepositoryHookEvent event) {
    LOG.debug("received hook event for repository {}, update index if necessary", event.getRepository());
    submit(event.getRepository());
  }

  @Subscribe
  public void handle(DefaultBranchChangedEvent event) {
    LOG.debug(
      "received default branch changed event for repository {}, update index if necessary",
      event.getRepository()
    );
    submit(event.getRepository());
  }

  @Subscribe
  public void handle(ReindexRepositoryEvent event) {
    LOG.debug(
      "received reindex event for repository {}, update index if necessary",
      event.getItem()
    );
    submit(event.getItem());
  }

  private void submit(Repository repository) {
    if ("git".equals(repository.getType())) {
      searchEngine.forType(SmeagolDocument.class)
        .forResource(repository)
        .update(new IndexerTask(repository));
    } else {
      LOG.debug("skipping non-git repository {}", repository);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // nothing to destroy here
  }
}
