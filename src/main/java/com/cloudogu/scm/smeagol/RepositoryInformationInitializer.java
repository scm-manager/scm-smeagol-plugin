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

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

class RepositoryInformationInitializer implements Callable<Map<String, RepositoryInformation>> {

  private final RepositoryManager repositoryManager;
  private final RepositoryInformationComputer computer;

  @Inject
  RepositoryInformationInitializer(RepositoryManager repositoryManager, RepositoryInformationComputer computer) {
    this.repositoryManager = repositoryManager;
    this.computer = computer;
  }

  @Override
  public Map<String, RepositoryInformation> call() {
    return repositoryManager.getAll()
      .stream()
      .filter(SmeagolRepositoryFilter::isPotentiallySmeagolRelevant)
      .collect(Collectors.toMap(Repository::getId, this::buildInformation));
  }

  private RepositoryInformation buildInformation(Repository repository) {
    return computer.compute(repository);
  }
}
