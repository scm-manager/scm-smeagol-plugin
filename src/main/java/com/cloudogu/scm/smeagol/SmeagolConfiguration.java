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

import lombok.Getter;
import lombok.Setter;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlRootElement;

@Singleton
public class SmeagolConfiguration {

  private final ConfigurationStore<Config> configStore;

  @Inject
  public SmeagolConfiguration(ConfigurationStoreFactory storeFactory) {
    this.configStore = storeFactory.withType(Config.class).withName("smeagol").build();
  }

  public Config get() {
    return configStore.getOptional().orElse(new Config());
  }

  public void set(Config config) {
    ConfigurationPermissions.write("smeagol").check();
    configStore.set(config);
  }

  @Getter
  @Setter
  @XmlRootElement(name = "smeagol")
  public static class Config {
    private boolean enabled;
    private boolean navLinkEnabled;
    private String smeagolUrl = "";
  }
}
