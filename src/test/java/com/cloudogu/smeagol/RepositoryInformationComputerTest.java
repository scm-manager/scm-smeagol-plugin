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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Branches;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.BranchesCommandBuilder;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.Collections;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.lenient;
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

  @InjectMocks
  private RepositoryInformationComputer computer;

  @BeforeEach
  void setupService() {
    when(serviceFactory.create(REPOSITORY)).thenReturn(service);
    lenient().when(service.getBranchesCommand()).thenReturn(branchesCommandBuilder);
  }

  @Test
  void shouldFindDefaultBranch() throws IOException {
    when(service.isSupported(Command.BRANCHES)).thenReturn(true);
    when(branchesCommandBuilder.getBranches())
      .thenReturn(new Branches(normalBranch("feature", "2"), defaultBranch("develop", "1")));

    RepositoryInformation information = computer.compute(REPOSITORY);

    assertThat(information.getDefaultBranch()).isEqualTo("develop");
  }

  @Test
  void shouldNotFailWhenNoBranchPresent() throws IOException {
    when(service.isSupported(Command.BRANCHES)).thenReturn(true);
    when(branchesCommandBuilder.getBranches())
      .thenReturn(new Branches(emptyList()));

    RepositoryInformation information = computer.compute(REPOSITORY);

    assertThat(information.getDefaultBranch()).isNull();
  }

  @Test
  void shouldNotComputeRepositoryWithoutBranchFeature() throws IOException {
    when(service.isSupported(Command.BRANCHES)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> computer.compute(REPOSITORY));
  }
}
