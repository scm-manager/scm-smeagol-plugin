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

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final SmeagolRepositoryStore store;
  private final SmeagolConfiguration configuration;

  @Inject
  public RepositoryLinkEnricher(SmeagolRepositoryStore store, SmeagolConfiguration configuration) {
    this.store = store;
    this.configuration = configuration;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    if (configuration.get().isEnabled()) {
      Repository repository = context.oneRequireByType(Repository.class);
      if (shouldEnrich(repository)) {
        appender.appendLink("smeagolWiki", createSmeagolUrl(repository));
      }
    }
  }

  private boolean shouldEnrich(Repository repository) {
    return SmeagolRepositoryFilter.isPotentiallySmeagolRelevant(repository)
      && store.getFor(repository).isWikiEnabled();
  }

  private String createSmeagolUrl(Repository repository) {
    return HttpUtil.concatenate(configuration.get().getSmeagolUrl(), repository.getId());
  }
}
