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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContext;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.HandlerEventType.CREATE;
import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;

@SuppressWarnings("java:S5979") // false positives
@ExtendWith(MockitoExtension.class)
class SmeagolRepositoryStoreTest {

  private final Repository REPOSITORY_1 = new Repository("1", "git", "space", "repo_1");
  private final Repository REPOSITORY_2 = new Repository("2", "hg", "space", "repo_2");
  private final Repository REPOSITORY_3 = new Repository("3", "git", "space", "repo_3");
  private final List<Repository> ALL_REPOSITORIES = new ArrayList<>(asList(REPOSITORY_1, REPOSITORY_2, REPOSITORY_3));

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
  @SuppressWarnings("unchecked")
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
    lenient().when(repositoryManager.getAll(any(Predicate.class), any(Comparator.class)))
      .thenAnswer(
        invocation -> {
          List<Repository> repositories = (List<Repository>) ALL_REPOSITORIES
            .stream()
            .filter(invocation.getArgument(0, Predicate.class))
            .collect(toList());
          repositories.sort(invocation.getArgument(1, Comparator.class));
          return repositories;
        });
  }

  @Test
  void shouldApplyInformation() {
    store.init(null);

    List<SmeagolRepositoryInformation> repositories = store.getRepositories();

    assertThat(repositories).hasSizeGreaterThan(0);
    assertThat(repositories.get(0).getId()).isEqualTo("1");
    assertThat(repositories.get(0).getDefaultBranch()).isEqualTo("main");
  }

  @Test
  void shouldFilterOtherThanGitRepositories() {
    store.init(null);

    List<SmeagolRepositoryInformation> repositories = store.getRepositories();

    assertThat(repositories).hasSize(2);
    assertThat(repositories).extracting("id").contains("1", "3");
  }

  @Nested
  class ForHookEvents {

    @Mock
    HookContext context;

    @BeforeEach
    void initStore() {
      store.init(null);
    }

    @Test
    void shouldUpdateInformationAfterCodeChange() {
      when(computer.compute(REPOSITORY_1)).thenReturn(createInfo(REPOSITORY_1, "other", true));

      store.detectCodeChanges(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY_1, POST_RECEIVE)));

      SmeagolRepositoryInformation information = store.getRepositories().get(0);

      assertThat(information.getDefaultBranch()).isEqualTo("other");
      assertThat(information.isWikiEnabled()).isTrue();
    }

    @Test
    void shouldIgnoreIrrelevantRepositoryType() {
      store.detectCodeChanges(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY_2, POST_RECEIVE)));

      verify(computer, never()).compute(any());
    }
  }

  @Nested
  class ForRepositoryEvents {

    @BeforeEach
    void initStore() {
      store.init(null);
    }

    @Test
    void shouldUpdateInformationAfterCodeChange() {
      Repository newRepository = new Repository("4", "git", "space", "repo_4");
      ALL_REPOSITORIES.add(newRepository);
      when(computer.compute(newRepository)).thenReturn(createInfo(newRepository, "new", true));

      store.detectRepositoryChanges(new RepositoryEvent(CREATE, newRepository));

      SmeagolRepositoryInformation information = store.getRepositories().get(2);

      assertThat(information.getId()).isEqualTo("4");
      assertThat(information.getDefaultBranch()).isEqualTo("new");
      assertThat(information.isWikiEnabled()).isTrue();
    }

    @Test
    void shouldComputeInformationOnDemandIfMissing() {
      Repository newRepository = new Repository("4", "git", "space", "repo_4");
      ALL_REPOSITORIES.add(newRepository);
      when(computer.compute(newRepository)).thenReturn(createInfo(newRepository, "new", true));

      SmeagolRepositoryInformation information = store.getRepositories().get(2);

      assertThat(information.getId()).isEqualTo("4");
      assertThat(information.getDefaultBranch()).isEqualTo("new");
      assertThat(information.isWikiEnabled()).isTrue();
    }
  }

  private RepositoryInformation createDefaultInfo(Repository repository) {
    return createInfo(repository, "main", false);
  }

  private RepositoryInformation createInfo(Repository repository, String branch, boolean smeagolWiki) {
    return new RepositoryInformation(branch, smeagolWiki);
  }
}
