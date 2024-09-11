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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexSyncWorkerTest {

  @Mock
  private DefaultBranchResolver defaultBranchResolver;
  @Mock
  private UpdatePathCollector updatePathCollector;
  @Mock
  private RevisionPathCollector revisionPathCollector;
  @Mock
  private IndexStatusStore indexStatusStore;
  @Mock
  private Indexer indexer;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  private IndexSyncWorker worker;

  @BeforeEach
  void initWorker() {
    worker = new IndexSyncWorker(
      defaultBranchResolver,
      updatePathCollector,
      revisionPathCollector,
      indexStatusStore,
      indexer,
      repository
    );
  }

  @Test
  void shouldSkipIfDefaultBranchIsInvalid() throws IOException {
    when(defaultBranchResolver.resolve())
      .thenReturn(empty());

    worker.ensureIndexIsUpToDate();

    verify(indexer).deleteAll();
    verify(indexStatusStore).empty(repository);
  }

  @Nested
  class WithDefaultBranchTest {

    private final Branch branch = Branch.defaultBranch("main", "42", 0L);

    @BeforeEach
    void mockDefaultBranch() throws IOException {
      when(defaultBranchResolver.resolve())
        .thenReturn(of(branch));
    }

    @Nested
    class WithReIndexTest {

      private final List<String> pathsFromDefaultBranch = new ArrayList<>();

      @BeforeEach
      void mockDefaultBranch() {
        lenient().when(revisionPathCollector.getPathToStore()).thenReturn(pathsFromDefaultBranch);
      }

      @Test
      void shouldReIndexIfNoStatusIsPresent() throws IOException {
        when(indexStatusStore.get(repository))
          .thenReturn(empty());

        worker.ensureIndexIsUpToDate();

        verifyReIndex();
      }

      @Test
      void shouldReIndexIfStoredVersionIsNotCurrent() throws IOException {
        when(indexStatusStore.get(repository))
          .thenReturn(of(new IndexStatus("23", "main", Instant.now(), 0)));

        worker.ensureIndexIsUpToDate();

        verifyReIndex();
      }

      @Test
      void shouldReIndexIfStoredVersionIsEmpty() throws IOException {
        when(indexStatusStore.get(repository))
          .thenReturn(of(IndexStatus.createEmpty()));

        worker.ensureIndexIsUpToDate();

        verifyReIndex();
      }

      @Test
      void shouldReIndexIfStoredVersionIsForOtherBranch() throws IOException {
        when(indexStatusStore.get(repository))
          .thenReturn(of(new IndexStatus("23", "master", Instant.now(), SmeagolDocument.VERSION)));

        worker.ensureIndexIsUpToDate();

        verifyReIndex();
      }

      @Test
      void shouldReIndexIfConfigurationChanged() throws IOException {
        when(indexStatusStore.get(repository))
          .thenReturn(of(new IndexStatus("23", "main", Instant.now(), SmeagolDocument.VERSION)));
        when(updatePathCollector.isConfigurationChanged())
          .thenReturn(true);

        worker.ensureIndexIsUpToDate();

        verifyReIndex();
        verify(updatePathCollector).collect("23", "42");
      }

      @Test
      void shouldDoNothingIfIndexIsUpToDate() throws IOException {
        when(indexStatusStore.get(repository))
          .thenReturn(of(new IndexStatus("42", "main", Instant.now(), SmeagolDocument.VERSION)));

        worker.ensureIndexIsUpToDate();

        verifyNoInteractions(updatePathCollector);
        verifyNoInteractions(revisionPathCollector);
        verifyNoInteractions(indexer);
        verify(indexStatusStore, never()).empty(any());
      }

      @Test
      void shouldReIndex() throws IOException {
        worker.reIndex();
        verifyReIndex();
      }

      private void verifyReIndex() throws IOException {
        verify(indexer).deleteAll();
        verify(revisionPathCollector).collect("42");
        verify(indexer).store(same(branch), same(pathsFromDefaultBranch));
      }
    }

    @Test
    void shouldUpdateIndex() throws IOException {
      when(indexStatusStore.get(repository))
        .thenReturn(of(new IndexStatus("23", "main", Instant.now(), SmeagolDocument.VERSION)));
      ArrayList<String> pathsToDelete = new ArrayList<>();
      when(updatePathCollector.getPathToDelete())
        .thenReturn(pathsToDelete);
      ArrayList<String> pathsToStore = new ArrayList<>();
      when(updatePathCollector.getPathToStore())
        .thenReturn(pathsToStore);

      worker.ensureIndexIsUpToDate();

      verify(updatePathCollector).collect("23", "42");
      verify(indexer).delete(same(pathsToDelete));
      verify(indexer).store(same(branch), same(pathsToStore));
    }
  }
}
