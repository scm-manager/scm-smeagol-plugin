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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import sonia.scm.plugin.Extension;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentProvider;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Extension
public class SmeagolUserAgentProvider implements UserAgentProvider {

  private static final String SMEAGOL_PREFIX = "smeagol/";

  @VisibleForTesting
  static final UserAgent SMEAGOL = UserAgent
    .scmClient("Smeagol")
    .basicAuthenticationCharset(StandardCharsets.UTF_8)
    .build();


  @Override
  public UserAgent parseUserAgent(String userAgentString) {
    String lowerUserAgent = toLower(userAgentString);

    if (lowerUserAgent.startsWith(SMEAGOL_PREFIX)) {
      return SMEAGOL;
    }
    return null;
  }

  private String toLower(String value) {
    return Strings.nullToEmpty(value).toLowerCase(Locale.ENGLISH);
  }

}
