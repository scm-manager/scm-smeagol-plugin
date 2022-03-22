package com.cloudogu.smeagol.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.api.RepositoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmeagolConfigurationResolverTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private RepositoryService service;

  @InjectMocks
  private SmeagolConfigurationResolver resolver;

  @Test
  void getSmeagolPathShouldReturnEmptyIfNoDirectorySpecified() throws Exception {
    when(service.getCatCommand().getContent(".smeagol.yml")).thenReturn("test: 12");

    assertThat(resolver.getSmeagolPath()).isEmpty();
  }

  @Test
  void getSmeagolPathShouldGetDirectoryFromSmeagolConfig() throws Exception {
    when(service.getCatCommand().getContent(".smeagol.yml")).thenReturn("test: 12\ndirectory: docs\n");

    assertThat(resolver.getSmeagolPath()).get().isEqualTo("docs");
  }

}
