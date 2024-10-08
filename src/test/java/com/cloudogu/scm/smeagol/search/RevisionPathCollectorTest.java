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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevisionPathCollectorTest {

  @Mock
  private RepositoryService repositoryService;
  @Mock(answer = RETURNS_SELF)
  private BrowseCommandBuilder browseCommand;
  @Mock
  private SmeagolConfigurationResolver smeagolConfigurationResolver;

  @InjectMocks
  private RevisionPathCollector collector;

  @Test
  void shouldCollectNothingWithoutSmeagolFile() {
    when(smeagolConfigurationResolver.getSmeagolPath()).thenReturn(empty());

    collector.collect("anything");

    assertThat(collector.getPathToDelete()).isEmpty();
    assertThat(collector.getPathToStore()).isEmpty();
  }

  @Nested
  class WithDocumentsTest {

    @BeforeEach
    void setUpFiles() throws IOException {
      when(smeagolConfigurationResolver.getSmeagolPath()).thenReturn(of("docs"));

      when(repositoryService.getBrowseCommand()).thenReturn(browseCommand);
      BrowserResult browserResult = new BrowserResult("42", createTree());
      when(browseCommand.getBrowserResult()).thenReturn(browserResult);
    }

    @AfterEach
    void hasToReadConfig() {
      verify(smeagolConfigurationResolver).readConfig();
    }

    @Test
    void shouldSetRevision() {
      collector.collect("42");

      verify(browseCommand).setRevision("42");
    }

    @Test
    void shouldSetCorrectSmeagolPath() {
      collector.collect("42");

      verify(browseCommand).setPath("docs");
    }

    @Test
    void shouldCollectFilesFromSmeagolDirectory() {
      when(smeagolConfigurationResolver.isSmeagolDocument(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, String.class).endsWith(".md"));
      collector.collect("42");

      Collection<String> pathToStore = collector.getPathToStore();

      assertThat(pathToStore).containsExactlyInAnyOrder(
        "docs/some.md",
        "docs/directory/sub.md"
      );
    }
  }

  private FileObject createTree() {
    FileObject root = new FileObject();
    root.setName("docs");
    root.setPath("docs");
    root.setDirectory(true);

    FileObject md = new FileObject();
    md.setName("some.md");
    md.setPath("docs/some.md");
    md.setDirectory(false);

    FileObject other = new FileObject();
    other.setName("other.jpg");
    other.setPath("docs/other.jpg");
    other.setDirectory(false);

    FileObject directory = new FileObject();
    directory.setName("directory");
    directory.setPath("docs/directory");
    directory.setDirectory(true);

    FileObject inDirectoryMD = new FileObject();
    inDirectoryMD.setName("sub.md");
    inDirectoryMD.setPath("docs/directory/sub.md");
    inDirectoryMD.setDirectory(false);

    directory.setChildren(singletonList(inDirectoryMD));

    root.setChildren(asList(other, md, directory));

    return root;
  }

}
