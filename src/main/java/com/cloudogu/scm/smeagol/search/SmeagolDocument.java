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

package com.cloudogu.scm.smeagol.search;

import lombok.Getter;
import sonia.scm.repository.Branch;
import sonia.scm.search.Indexed;
import sonia.scm.search.IndexedType;

@Getter
@IndexedType
@SuppressWarnings("UnstableApiUsage")
public class SmeagolDocument {
  public static final int VERSION = 2;

  @Indexed(type = Indexed.Type.STORED_ONLY)
  private final String revision;

  @Indexed(type = Indexed.Type.STORED_ONLY)
  private final String path;

  @Indexed(type = Indexed.Type.STORED_ONLY)
  private final String branch;

  @Indexed(type = Indexed.Type.STORED_ONLY)
  private final String repositoryId;

  @Indexed(
    defaultQuery = true,
    highlighted = true,
    analyzer = Indexed.Analyzer.DEFAULT
  )
  private final String content;

  public SmeagolDocument(Branch branch, String path, String repositoryId, String content) {
    this.revision = branch.getRevision();
    this.path = path;
    this.branch = branch.getName();
    this.repositoryId = repositoryId;
    this.content = content;
  }
}
