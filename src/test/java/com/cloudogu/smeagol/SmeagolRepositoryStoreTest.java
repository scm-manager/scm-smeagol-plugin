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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContext;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;

@ExtendWith(MockitoExtension.class)
class SmeagolRepositoryStoreTest {

  private static final Repository REPOSITORY_1 = new Repository("1", "git", "space", "repo_1");
  private static final Repository REPOSITORY_2 = new Repository("2", "git", "space", "repo_2");
  private static final Repository REPOSITORY_3 = new Repository("3", "git", "space", "repo_3");
  private static final List<Repository> ALL_REPOSITORIES = asList(REPOSITORY_1, REPOSITORY_2, REPOSITORY_3);

  @Mock
  private AdministrationContext administrationContext;
  @Mock
  private RepositoryInformationInitializer informationInitializer;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryInformationComputer computer;

  private boolean asAdmin = false;

  @InjectMocks
  private SmeagolRepositoryStore store;

  @BeforeEach
  void initAdminContext() {
    doAnswer(invocation -> {
      asAdmin = true;
      invocation.getArgument(0, PrivilegedAction.class).run();
      asAdmin = false;
      return null;
    }).when(administrationContext).runAsAdmin(any(PrivilegedAction.class));
  }

  @BeforeEach
  void mockRepositories() {
    when(informationInitializer.call()).thenAnswer(
      invocation -> {
        if (asAdmin) {
          return of(
            REPOSITORY_1.getId(), createDefaultInfo(REPOSITORY_1),
            REPOSITORY_2.getId(), createDefaultInfo(REPOSITORY_2),
            REPOSITORY_3.getId(), createDefaultInfo(REPOSITORY_3)
          );
        } else {
          return emptyMap();
        }
      }
    );
    when(repositoryManager.getAll(anyInt(), anyInt())).thenAnswer(
      invocation -> {
        Integer start = invocation.getArgument(0, Integer.class);
        Integer limit = invocation.getArgument(1, Integer.class);
        return ALL_REPOSITORIES.subList(start, start + limit);
      }
    );
  }

  @Test
  void shouldApplyInformation() {
    store.init(null);

    List<SmeagolRepositoryInformationDto> repositories = store.getRepositories(0, 1);

    assertThat(repositories).hasSize(1);
    assertThat(repositories.get(0).getId()).isEqualTo("1");
    assertThat(repositories.get(0).getDefaultBranch()).isEqualTo("main");
  }

  @Test
  void shouldUsePageLimit() {
    store.init(null);

    List<SmeagolRepositoryInformationDto> repositories = store.getRepositories(1, 1);

    assertThat(repositories).hasSize(1);
    assertThat(repositories.get(0).getId()).isEqualTo("2");
  }

  @Nested
  @SuppressWarnings("java:S5979") // false positive
  class ForHookEvents {

    @Mock
    HookContext context;

    @BeforeEach
    void initStore() {
      store.init(null);
    }

    @Test
    void shouldUpdateInformationAfterCodeChange() {
      when(computer.compute(REPOSITORY_1)).thenReturn(createInfo(REPOSITORY_1, true));

      store.detectCodeChanges(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY_1, POST_RECEIVE)));

      SmeagolRepositoryInformationDto information = store.getRepositories(0, 1).get(0);

      assertThat(information.isSmeagolWiki()).isTrue();
    }
  }

  private RepositoryInformation createDefaultInfo(Repository repository) {
    return createInfo(repository, false);
  }

  private RepositoryInformation createInfo(Repository repository, boolean smeagolWiki) {
    return new RepositoryInformation(repository, "main", smeagolWiki);
  }
}
