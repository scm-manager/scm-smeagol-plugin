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

import lombok.Getter;
import sonia.scm.repository.Repository;

@Getter
@SuppressWarnings("java:S2160") // we have no definition for equals/hashCode
class SmeagolRepositoryInformation {

  private final String namespace;
  private final String name;
  private final String id;
  private final String type;
  private final String description;
  private final String defaultBranch;
  private final boolean wikiEnabled;

  SmeagolRepositoryInformation(Repository repository, RepositoryInformation information) {
    this.namespace = repository.getNamespace();
    this.name = repository.getName();
    this.id = repository.getId();
    this.description = repository.getDescription();
    this.type = repository.getType();
    this.defaultBranch = information.getDefaultBranch();
    this.wikiEnabled = information.isWikiEnabled();
  }
}
