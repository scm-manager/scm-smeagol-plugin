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

import lombok.Getter;
import sonia.scm.repository.Branch;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;

public class DefaultBranchResolver {

  private final RepositoryService repositoryService;

  public DefaultBranchResolver(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  public Result resolve() throws IOException {
    if (!repositoryService.isSupported(Command.BRANCHES)) {
      return Result.notSupported();
    } else {
      return repositoryService.getBranchesCommand()
        .getBranches()
        .getBranches()
        .stream()
        .filter(Branch::isDefaultBranch)
        .findFirst()
        .map(Branch::getName)
        .map(Result::defaultBranch)
        .orElseGet(Result::empty);
    }
  }

  @Getter
  public static class Result {

    private final boolean empty;
    @Nullable
    private final String defaultBranch;

    private Result(boolean empty, @Nullable String defaultBranch) {
      this.empty = empty;
      this.defaultBranch = defaultBranch;
    }

    public Optional<String> getDefaultBranch() {
      return Optional.ofNullable(defaultBranch);
    }

    public static Result empty() {
      return new Result(true, null);
    }

    public static Result notSupported() {
      return new Result(false, null);
    }

    public static Result defaultBranch(String branch) {
      return new Result(false, branch);
    }
  }


}
