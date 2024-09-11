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

import com.google.common.io.ByteStreams;
import sonia.scm.repository.Branch;
import sonia.scm.repository.api.RepositoryService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SmeagolDocumentFactory {

  public SmeagolDocument create(RepositoryService repositoryService, Branch branch, String path) throws IOException {
    try (InputStream content = repositoryService.getCatCommand().setRevision(branch.getRevision()).getStream(path)) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ByteStreams.copy(content, output);
      return new SmeagolDocument(branch, path, repositoryService.getRepository().getId(), output.toString("UTF-8"));
    }
  }
}
