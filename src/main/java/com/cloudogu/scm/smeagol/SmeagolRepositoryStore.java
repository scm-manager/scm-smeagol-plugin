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

package com.cloudogu.scm.smeagol;

import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.Initable;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.Comparables;
import sonia.scm.web.security.AdministrationContext;

import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

@EagerSingleton
@Extension
class SmeagolRepositoryStore implements Initable {

  private static final Logger LOG = LoggerFactory.getLogger(SmeagolRepositoryStore.class);

  private final CountDownLatch initializeLatch = new CountDownLatch(1);

  private final AdministrationContext administrationContext;
  private final RepositoryInformationInitializer informationInitializer;
  private final RepositoryManager repositoryManager;
  private final RepositoryInformationComputer computer;

  private final Map<String, RepositoryInformation> repositoryInformation = new ConcurrentHashMap<>();


  @Inject
  SmeagolRepositoryStore(AdministrationContext administrationContext, RepositoryInformationInitializer informationInitializer, RepositoryManager repositoryManager, RepositoryInformationComputer computer) {
    this.administrationContext = administrationContext;
    this.informationInitializer = informationInitializer;
    this.repositoryManager = repositoryManager;
    this.computer = computer;
  }

  @Override
  public void init(SCMContextProvider unused) {
    administrationContext.runAsAdmin(this::init);
  }

  @Subscribe
  public void detectCodeChanges(PostReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();
    if (SmeagolRepositoryFilter.isPotentiallySmeagolRelevant(repository)) {
      recompute(repository);
    }
  }

  @Subscribe
  public void detectRepositoryChanges(RepositoryEvent event) {
    switch (event.getEventType()) {
      case DELETE:
        repositoryInformation.remove(event.getItem().getId());
        break;
      case CREATE:
        if (SmeagolRepositoryFilter.isPotentiallySmeagolRelevant(event.getItem())) {
          recompute(event.getItem());
        }
        break;
      default: // nothing to do
    }
  }

  private void recompute(Repository repository) {
    repositoryInformation.compute(repository.getId(), (id, oldInformation) -> computer.compute(repository));
  }

  /**
   * Returns the repositories given by {@link RepositoryManager#getAll(int, int)} with additional
   * smeagol relevant information.
   */
  List<SmeagolRepositoryInformation> getRepositories() {
    return repositoryManager.getAll(SmeagolRepositoryFilter::isPotentiallySmeagolRelevant, createComparator())
      .stream()
      .map(this::getFor)
      .collect(toList());
  }

  SmeagolRepositoryInformation getFor(Repository repository) {
    waitForInitialization();
    return new SmeagolRepositoryInformation(
      repository,
      repositoryInformation.computeIfAbsent(repository.getId(), id -> computer.compute(repository))
    );
  }

  private void waitForInitialization() {
    try {
      initializeLatch.await();
    } catch (InterruptedException e) {
      LOG.warn("Got interrupted while waiting for initialization", e);
      Thread.currentThread().interrupt();
    }
  }

  private void init() {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    try {
      LOG.info("Starting initialization of smeagol repository information");
      executorService
        .submit(informationInitializer)
        .get()
        .forEach(repositoryInformation::put);
    } catch (InterruptedException e) {
      LOG.warn("Got interrupted while initializing repository information", e);
      // Restore interrupted state...
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      LOG.warn("Got an exception while initializing repository information", e);
    } finally {
      LOG.info("Finished initialization of smeagol repository information");
      initializeLatch.countDown();
      executorService.shutdown();
    }
  }

  private Comparator<Repository> createComparator() {
    return Comparables.comparator(Repository.class, "namespace").thenComparing(Comparables.comparator(Repository.class, "name"));
  }
}
