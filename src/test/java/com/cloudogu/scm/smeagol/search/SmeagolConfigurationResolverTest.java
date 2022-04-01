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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.api.RepositoryService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmeagolConfigurationResolverTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private RepositoryService service;

  @InjectMocks
  private SmeagolConfigurationResolver resolver;

  @ParameterizedTest
  @MethodSource("provideTestDataForGetSmeagolPath")
  void getSmeagolPath(String smeagolConfig, String expected) throws Exception {
    when(service.getCatCommand().getContent(".smeagol.yml"))
      .thenReturn(smeagolConfig);

    resolver.readConfig();

    assertThat(resolver.getSmeagolPath()).get().isEqualTo(expected);
  }

  private static Stream<Arguments> provideTestDataForGetSmeagolPath() {
    return Stream.of(
      Arguments.of("test: 12", "docs"),
      Arguments.of("", "docs"),
      Arguments.of("test: 12\ndirectory: dokumente\n", "dokumente")
    );
  }

  @Test
  void getSmeagolPathShouldReturnEmptyResultWithoutSmeagolFile() throws Exception {
    when(service.getCatCommand().getContent(".smeagol.yml"))
      .thenThrow(NotFoundException.class);

    resolver.readConfig();

    assertThat(resolver.getSmeagolPath()).isEmpty();
  }

  @Test
  void getSmeagolPathShouldNotSetAnyRevisionIfNotGiven() throws Exception {
    when(service.getCatCommand().getContent(".smeagol.yml"))
      .thenReturn("");

    resolver.readConfig();

    verify(service.getCatCommand(), never()).setRevision("42");
  }

  @Test
  void getSmeagolPathShouldUseRevision() throws Exception {
    when(service.getCatCommand().getContent(".smeagol.yml"))
      .thenReturn("");

    resolver.readConfig("42");

    verify(service.getCatCommand()).setRevision("42");
  }

  @ParameterizedTest
  @MethodSource("provideTestDataForIsSmeagolDocument")
  void isSmeagolDocument(String smeagolConfig, String path, boolean expected) throws Exception {
    when(service.getCatCommand().getContent(".smeagol.yml"))
      .thenReturn(smeagolConfig);

    resolver.readConfig();

    assertThat(resolver.isSmeagolDocument(path)).isEqualTo(expected);
  }

  private static Stream<Arguments> provideTestDataForIsSmeagolDocument() {
    return Stream.of(
      Arguments.of("", "docs/a.md", true),
      Arguments.of("", "docs/A.MD", true),
      Arguments.of("", "docs/b.png", false),
      Arguments.of("", "other/a.md", false),
      Arguments.of("test: 12\ndirectory: dokumente\n", "dokumente/a.md", true),
      Arguments.of("test: 12\ndirectory: dokumente\n", "dokumente/A.MD", true),
      Arguments.of("test: 12\ndirectory: dokumente\n", "dokumente/b.png", false),
      Arguments.of("test: 12\ndirectory: dokumente\n", "docs/a.md", false)
    );
  }
}
