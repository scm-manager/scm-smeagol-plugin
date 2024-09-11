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
      invocation -> new RepositoryInformation("main", true)
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
