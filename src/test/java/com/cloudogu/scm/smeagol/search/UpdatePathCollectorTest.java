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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Added;
import sonia.scm.repository.Copied;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;
import sonia.scm.repository.api.ModificationsCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePathCollectorTest {

  @Mock
  private RepositoryService repositoryService;
  @Mock(answer = RETURNS_SELF)
  private ModificationsCommandBuilder modificationsCommand;
  @Mock
  private SmeagolConfigurationResolver smeagolConfigurationResolver;

  @InjectMocks
  private UpdatePathCollector collector;

  @Test
  void shouldCollectNothingWithoutSmeagolFile() {
    when(smeagolConfigurationResolver.getSmeagolPath()).thenReturn(empty());

    collector.collect("23", "42");

    assertThat(collector.getPathToDelete()).isEmpty();
    assertThat(collector.getPathToStore()).isEmpty();
  }

  @Nested
  class WithModificationsTest {

    @BeforeEach
    void setUpFiles() {
      when(smeagolConfigurationResolver.getSmeagolPath()).thenReturn(of("docs"));

      when(repositoryService.getModificationsCommand()).thenReturn(modificationsCommand);
    }

    @Nested
    class WithChangedDocumentsTest {

      @BeforeEach
      void mockModifiedDocuments() throws IOException {
        when(modificationsCommand.getModifications()).thenReturn(createModifications());
      }

      @Test
      void shouldSetRevisions() {
        collector.collect("23", "42");

        verify(modificationsCommand).baseRevision("23");
        verify(modificationsCommand).revision("42");
      }

      @Test
      void shouldCollectFilesFromSmeagolDirectory() {
        when(smeagolConfigurationResolver.isSmeagolDocument(any()))
          .thenAnswer(invocation -> invocation.getArgument(0, String.class).startsWith("docs/"));

        collector.collect("23", "42");

        assertThat(collector.getPathToStore()).containsExactlyInAnyOrder(
          "docs/a.md",
          "docs/m.md",
          "docs/c.md",
          "docs/y.md"
        );
        assertThat(collector.getPathToDelete()).containsExactlyInAnyOrder(
          "docs/r.md",
          "docs/x.md"
        );
        InOrder inOrder = inOrder(smeagolConfigurationResolver);
        inOrder.verify(smeagolConfigurationResolver).readConfig("42");
        inOrder.verify(smeagolConfigurationResolver).getSmeagolPath();
      }

      private Modifications createModifications() {
        return new Modifications(
          "42",
          new Added("docs/a.md"),
          new Modified("docs/m.md"),
          new Copied("docs/a.md", "docs/c.md"),
          new Removed("docs/r.md"),
          new Renamed("docs/x.md", "docs/y.md"),
          new Added("somewhere/a.md"),
          new Modified("somewhere/m.md"),
          new Copied("somewhere/a.md", "somewhere/c.md"),
          new Removed("somewhere/r.md"),
          new Renamed("somewhere/x.md", "somewhere/y.md")
        );
      }
    }

    @Test
    void shouldDetectChangedSmeagolConfig() throws IOException {
      when(modificationsCommand.getModifications())
        .thenReturn(
          new Modifications(
            "42",
            new Modified(".smeagol.yml")
          ));

      collector.collect("23", "42");

      assertThat(collector.isConfigurationChanged()).isTrue();
    }

    @Test
    void shouldDetectDeletedSmeagolConfig() throws IOException {
      when(modificationsCommand.getModifications())
        .thenReturn(
          new Modifications(
            "42",
            new Removed(".smeagol.yml")
          ));

      collector.collect("23", "42");

      assertThat(collector.isConfigurationChanged()).isTrue();
    }
  }
}
