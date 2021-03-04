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

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryInformationInitializerTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryInformationComputer computer;

  @InjectMocks
  private RepositoryInformationInitializer initializer;

  @Test
  void shouldFilterForRelevantRepositories() {
    when(repositoryManager.getAll())
      .thenReturn(Arrays.asList(
        new Repository("1", "git", "space", "repo_1"),
        new Repository("2", "git", "space", "repo_2"),
        new Repository("3", "hg", "space", "repo_3")
      ));
    when(computer.compute(any())).thenAnswer(
      invocation -> {
        Repository repository = invocation.getArgument(0, Repository.class);
        return new RepositoryInformation("main", true);
      }
    );

    Map<String, RepositoryInformation> result = initializer.call();

    assertThat(result)
      .hasSize(2)
      .containsKeys("1", "2")
      .hasValueSatisfying(isSmeagolRelevant());
  }

  private Condition<RepositoryInformation> isSmeagolRelevant() {
    return new Condition<RepositoryInformation>("is smeagol relevant") {
      public boolean matches(RepositoryInformation tolkienCharacter) {
        return tolkienCharacter.isWikiEnabled();
      }
    };
  }
}
