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
class SmeagolRevisionPathCollectorTest {

  @Mock
  private RepositoryService repositoryService;
  @Mock(answer = RETURNS_SELF)
  private BrowseCommandBuilder browseCommand;
  @Mock
  private SmeagolConfigurationResolver smeagolConfigurationResolver;

  @InjectMocks
  private SmeagolRevisionPathCollector collector;

  @Test
  void shouldCollectNothingWithoutSmeagolFile() throws IOException {
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
    void shouldSetRevision() throws IOException {
      collector.collect("42");

      verify(browseCommand).setRevision("42");
    }

    @Test
    void shouldSetCorrectSmeagolPath() throws IOException {
      collector.collect("42");

      verify(browseCommand).setPath("docs");
    }

    @Test
    void shouldCollectFilesFromSmeagolDirectory() throws IOException {
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
