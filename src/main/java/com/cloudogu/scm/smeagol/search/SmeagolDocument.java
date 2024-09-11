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

package com.cloudogu.scm.smeagol.search;

import lombok.Getter;
import sonia.scm.repository.Branch;
import sonia.scm.search.Indexed;
import sonia.scm.search.IndexedType;

@Getter
@IndexedType(repositoryScoped = true, namespaceScoped = true)
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
