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

package com.cloudogu.smeagol;

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

import javax.inject.Inject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cloudogu.smeagol.SmeagolRepositoryFilter.isPotentiallySmeagolRelevant;
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

  private final Map<String, RepositoryInformation> repositoryInformation = new HashMap<>();

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
    if (isPotentiallySmeagolRelevant(repository)) {
      RepositoryInformation information = computer.compute(repository);
      repositoryInformation.put(repository.getId(), information);
    }
  }

  @Subscribe
  public void detectRepositoryChanges(RepositoryEvent event) {
    switch (event.getEventType()) {
      case DELETE:
        repositoryInformation.remove(event.getItem().getId());
        break;
      case CREATE:
        if (isPotentiallySmeagolRelevant(event.getItem())) {
          repositoryInformation.put(event.getItem().getId(), computer.compute(event.getItem()));
        }
        break;
      default: // nothing to do
    }
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
