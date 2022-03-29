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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.cloudogu.smeagol.search.DefaultBranchResolver.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LatestRevisionResolverTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryService;

  @Mock
  private DefaultBranchResolver defaultBranchResolver;

  @InjectMocks
  private LatestRevisionResolver resolver;

  @Test
  void shouldReturnEmptyIfDefaultBranchResolverReturnEmpty() throws IOException {
    when(defaultBranchResolver.resolve()).thenReturn(Result.empty());

    Optional<Branch> branch = resolver.resolve();
    assertThat(branch).isEmpty();
  }

  @Test
  void shouldReturnEmptyIfNoChangesetCouldBeFound() throws IOException {
    when(defaultBranchResolver.resolve()).thenReturn(Result.notSupported());

    changesets();

    Optional<Branch> branch = resolver.resolve();
    assertThat(branch).isEmpty();
  }

  @Test
  void shouldUseDefaultBranch() throws IOException {
    when(defaultBranchResolver.resolve()).thenReturn(Result.defaultBranch("develop"));

    LogCommandBuilder logCommand = mock(LogCommandBuilder.class);
    when(
      repositoryService.getLogCommand()
        .setPagingLimit(1)
        .setDisablePreProcessors(true)
        .setDisableCache(true)
    ).thenReturn(logCommand);

    when(logCommand.getChangesets()).thenReturn(new ChangesetPagingResult(0, Collections.emptyList()));

    Optional<Branch> revision = resolver.resolve();
    assertThat(revision).isEmpty();

    verify(logCommand).setBranch("develop");
  }

  @Test
  void shouldReturnLatestRevision() throws IOException {
    when(defaultBranchResolver.resolve()).thenReturn(Result.notSupported());

    changesets(new Changeset("42", 1L, Person.toPerson("trillian")));

    Optional<Branch> branch = resolver.resolve();
    assertThat(branch).get().extracting("revision").isEqualTo("42");
  }

  private void changesets(Changeset... changesets) throws IOException {
    when(
      repositoryService.getLogCommand()
        .setPagingLimit(1)
        .setDisablePreProcessors(true)
        .setDisableCache(true)
        .getChangesets()
    ).thenReturn(new ChangesetPagingResult(changesets.length, Arrays.asList(changesets)));
  }
}
