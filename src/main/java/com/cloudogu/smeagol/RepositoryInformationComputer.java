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

import sonia.scm.repository.Branch;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.List;

class RepositoryInformationComputer {

  private final RepositoryServiceFactory serviceFactory;

  RepositoryInformationComputer(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  RepositoryInformation compute(Repository repository) {
    try (RepositoryService service = serviceFactory.create(repository)) {
      if (!service.isSupported(Command.BRANCHES)) {
        throw new IllegalArgumentException("Repository type without branch support not supported");
      }
      List<Branch> branches;
      try {
        branches = service.getBranchesCommand().getBranches().getBranches();
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "Could not load branches", e);
      }
      String defaultBranch = branches
        .stream()
        .filter(Branch::isDefaultBranch)
        .findAny()
        .map(Branch::getName)
        .orElse(null);
      return new RepositoryInformation(repository, defaultBranch);
    }
  }
}
