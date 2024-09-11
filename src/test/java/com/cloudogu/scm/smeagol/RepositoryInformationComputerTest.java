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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Branches;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.BranchesCommandBuilder;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.Branch.defaultBranch;
import static sonia.scm.repository.Branch.normalBranch;
import static sonia.scm.repository.RepositoryTestData.createHeartOfGold;

@ExtendWith(MockitoExtension.class)
class RepositoryInformationComputerTest {

  private static final Repository REPOSITORY = createHeartOfGold();

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;

  @Mock
  private BranchesCommandBuilder branchesCommandBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private BrowseCommandBuilder browseCommandBuilder;

  @InjectMocks
  private RepositoryInformationComputer computer;

  @BeforeEach
  void setupService() {
    when(serviceFactory.create(REPOSITORY)).thenReturn(service);
  }

  @Test
  void shouldNotComputeRepositoryWithoutBranchFeature() {
    when(service.isSupported(Command.BRANCHES)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> computer.compute(REPOSITORY));
  }

  @Nested
  class WithCorrectRepository {

    @BeforeEach
    void setupService() {
      when(service.isSupported(Command.BRANCHES)).thenReturn(true);
      when(service.getBranchesCommand()).thenReturn(branchesCommandBuilder);
      when(service.getBrowseCommand()).thenReturn(browseCommandBuilder);
    }

    @Test
    void shouldFindDefaultBranch() throws IOException {
      mockEmptyBrowseResult();
      when(branchesCommandBuilder.getBranches())
        .thenReturn(new Branches(normalBranch("feature", "2"), defaultBranch("develop", "1")));

      RepositoryInformation information = computer.compute(REPOSITORY);

      assertThat(information.getDefaultBranch()).isEqualTo("develop");
    }

    @Test
    void shouldNotFailWhenNoBranchPresent() throws IOException {
      mockEmptyBrowseResult();
      mockEmptyBranches();

      RepositoryInformation information = computer.compute(REPOSITORY);

      assertThat(information.getDefaultBranch()).isNull();
      assertThat(information.isWikiEnabled()).isFalse();
    }

    @Test
    void shouldFindSmeagolYaml() throws IOException {
      mockEmptyBranches();
      FileObject smeagolYamlFile = new FileObject();
      when(browseCommandBuilder.getBrowserResult()).thenReturn(new BrowserResult("1", smeagolYamlFile));

      RepositoryInformation information = computer.compute(REPOSITORY);

      assertThat(information.isWikiEnabled()).isTrue();
    }

    @Test
    void shouldHandleMissingSmeagolYaml() throws IOException {
      mockEmptyBranches();
      when(browseCommandBuilder.getBrowserResult()).thenThrow(NotFoundException.class);

      RepositoryInformation information = computer.compute(REPOSITORY);

      assertThat(information.isWikiEnabled()).isFalse();
    }

    private void mockEmptyBrowseResult() throws IOException {
      when(browseCommandBuilder.getBrowserResult()).thenReturn(new BrowserResult());
    }

    private void mockEmptyBranches() throws IOException {
      when(branchesCommandBuilder.getBranches())
        .thenReturn(new Branches(normalBranch("main", "2")));
    }
  }
}
