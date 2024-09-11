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
