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
      assertThat(information.isSmeagolWiki()).isFalse();
    }

    @Test
    void shouldFindSmeagolYaml() throws IOException {
      mockEmptyBranches();
      FileObject smeagolYamlFile = new FileObject();
      when(browseCommandBuilder.getBrowserResult()).thenReturn(new BrowserResult("1", smeagolYamlFile));

      RepositoryInformation information = computer.compute(REPOSITORY);

      assertThat(information.isSmeagolWiki()).isTrue();
    }

    @Test
    void shouldHandleMissingSmeagolYaml() throws IOException {
      mockEmptyBranches();
      when(browseCommandBuilder.getBrowserResult()).thenThrow(NotFoundException.class);

      RepositoryInformation information = computer.compute(REPOSITORY);

      assertThat(information.isSmeagolWiki()).isFalse();
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
