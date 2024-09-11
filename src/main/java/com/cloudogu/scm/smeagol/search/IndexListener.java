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

import com.cloudogu.scm.smeagol.SmeagolRepositoryFilter;
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

import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

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
    Repository repository = event.getRepository();
    LOG.debug(
      "received reindex event for repository {}, delete and recreate index from scratch",
      repository
    );
    if (SmeagolRepositoryFilter.isPotentiallySmeagolRelevant(repository)) {
      searchEngine.forType(SmeagolDocument.class)
        .forResource(repository)
        .update(new ReindexTask(repository));
    } else {
      LOG.debug("repository {} is not relevant for smeagol, skipping", repository);
    }
  }

  private void submit(Repository repository) {
    if (SmeagolRepositoryFilter.isPotentiallySmeagolRelevant(repository)) {
      searchEngine.forType(SmeagolDocument.class)
        .forResource(repository)
        .update(new IndexerTask(repository));
    } else {
      LOG.debug("repository {} is not relevant for smeagol, skipping", repository);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // nothing to destroy here
  }
}
