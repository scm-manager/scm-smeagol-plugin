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

package com.cloudogu.scm.smeagol;

import org.junit.jupiter.api.Test;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static com.google.inject.util.Providers.of;
import static org.assertj.core.api.Assertions.assertThat;

class SmeagolLinkBuilderTest {

  @Test
  void shouldGenerateRepositoriesLink() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));

    SmeagolLinkBuilder smeagolLinkBuilder = new SmeagolLinkBuilder(of(pathInfoStore));

    String link = smeagolLinkBuilder.getRepositoriesLink();

    assertThat(link).isEqualTo("/v2/smeagol/repositories");
  }

  @Test
  void shouldGenerateConfigurationLink() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));

    SmeagolLinkBuilder smeagolLinkBuilder = new SmeagolLinkBuilder(of(pathInfoStore));

    String link = smeagolLinkBuilder.getConfigurationLink();

    assertThat(link).isEqualTo("/v2/smeagol/configuration");
  }
}
