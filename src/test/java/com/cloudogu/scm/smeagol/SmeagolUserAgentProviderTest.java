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
import sonia.scm.web.UserAgent;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SmeagolUserAgentProviderTest {

  private final SmeagolUserAgentProvider userAgentProvider = new SmeagolUserAgentProvider();

  @Test
  void shouldReturnNullIfNotMatching() {
    UserAgent userAgent = userAgentProvider.parseUserAgent("someUserAgentButNotSmeagol");

    assertThat(userAgent).isNull();
  }

  @Test
  void shouldReturnSmeagolUserAgent() {
    UserAgent userAgent = userAgentProvider.parseUserAgent("smeagol/1.2.5");

    assertThat(userAgent).isEqualTo(SmeagolUserAgentProvider.SMEAGOL);

    assertThat(userAgent.isScmClient()).isTrue();
    assertThat(userAgent.getBasicAuthenticationCharset()).isEqualTo(StandardCharsets.UTF_8);
    assertThat(userAgent.getName()).isEqualTo("Smeagol");
    assertThat(userAgent.isBrowser()).isFalse();
  }
}
