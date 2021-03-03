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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexLinkEnricherTest {

  @Mock
  private SmeagolLinkBuilder smeagolLinkBuilder;

  @InjectMocks
  private IndexLinkEnricher enricher;

  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;
  @Mock(answer = Answers.RETURNS_SELF)
  private HalAppender.LinkArrayBuilder linkArrayBuilder;

  @Test
  void shouldAppendLink() {
    when(appender.linkArrayBuilder("smeagol")).thenReturn(linkArrayBuilder);
    when(smeagolLinkBuilder.getRepositoriesLink()).thenReturn("/smeagol/repositories");
    when(smeagolLinkBuilder.getConfigurationLink()).thenReturn("/smeagol/configuration");

    enricher.enrich(context, appender);

    verify(linkArrayBuilder).append("repositories", "/smeagol/repositories");
    verify(linkArrayBuilder).build();
    verify(appender).appendLink("smeagolConfig", "/smeagol/configuration");
  }
}
