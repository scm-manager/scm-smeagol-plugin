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

import sonia.scm.NotFoundException;
import sonia.scm.repository.Branch;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

class RepositoryInformationComputer {

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
      return new RepositoryInformation(repository, defaultBranch, smeagolWiki);
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
        .setPath(".smeagol.yml")
        .setDisableLastCommit(true)
        .getBrowserResult().getFile() != null;
    } catch (NotFoundException e) {
      return false;
    } catch (IOException e) {
      throw new InternalRepositoryException(service.getRepository(), "Could not browse for .smeagol.yml", e);
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
