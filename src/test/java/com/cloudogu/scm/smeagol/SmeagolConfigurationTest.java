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

import com.cloudogu.scm.smeagol.SmeagolConfiguration.Config;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class SmeagolConfigurationTest {

  private final SmeagolConfiguration configuration = new SmeagolConfiguration(new InMemoryConfigurationStoreFactory());


  @Test
  void shouldCreateDefaultConfiguration() {
    Config config = configuration.get();

    assertThat(config).isNotNull();
  }


  @Nested
  class WithSubject {

    private Subject subject = mock(Subject.class);

    @BeforeEach
    void initSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    void cleanup() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldSetConfiguration() {
      Config config = new Config();
      config.setSmeagolUrl("http://smeagol.com/");
      configuration.set(config);

      Config storedConfig = configuration.get();

      assertThat(storedConfig.getSmeagolUrl()).isEqualTo("http://smeagol.com/");
    }

    @Test
    void shouldCheckCredentials() {
      doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:smeagol");

      Config newConfig = new Config();
      assertThrows(AuthorizationException.class, () -> configuration.set(newConfig));
    }
  }
}
