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

import sonia.scm.NotFoundException;
import sonia.scm.repository.Branch;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

class RepositoryInformationComputer {

  public static final String SMEAGOL_MARKER_FILE = System.getProperty("com.cloudogu.scm.smeagol.markerFile", ".smeagol.yml");

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  RepositoryInformationComputer(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  RepositoryInformation compute(Repository repository) {
    try (RepositoryService service = serviceFactory.create(repository)) {
      if (!service.isSupported(Command.BRANCHES)) {
        throw new IllegalArgumentException("Repository type without branch support not supported");
      }
      List<Branch> branches = loadBranches(repository, service);
      String defaultBranch = findDefaultBranch(branches);
      boolean smeagolWiki = detectSmeagolWiki(service, branches);
      return new RepositoryInformation(defaultBranch, smeagolWiki);
    }
  }

  private boolean detectSmeagolWiki(RepositoryService service, List<Branch> branches) {
    return branches
      .stream()
      .anyMatch(branch -> hasSmeagolFile(service, branch));
  }

  private boolean hasSmeagolFile(RepositoryService service, Branch branch) {
    try {
      return service
        .getBrowseCommand()
        .setRevision(branch.getName())
        .setPath(SMEAGOL_MARKER_FILE)
        .setDisableLastCommit(true)
        .getBrowserResult().getFile() != null;
    } catch (NotFoundException e) {
      return false;
    } catch (IOException e) {
      throw new InternalRepositoryException(service.getRepository(), "Could not browse for " + SMEAGOL_MARKER_FILE, e);
    }
  }

  private List<Branch> loadBranches(Repository repository, RepositoryService service) {
    List<Branch> branches;
    try {
      branches = service.getBranchesCommand().getBranches().getBranches();
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "Could not load branches", e);
    }
    return branches;
  }

  private String findDefaultBranch(List<Branch> branches) {
    return branches
      .stream()
      .filter(Branch::isDefaultBranch)
      .findAny()
      .map(Branch::getName)
      .orElse(null);
  }
}
