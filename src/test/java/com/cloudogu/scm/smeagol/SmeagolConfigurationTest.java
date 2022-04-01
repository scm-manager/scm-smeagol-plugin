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
