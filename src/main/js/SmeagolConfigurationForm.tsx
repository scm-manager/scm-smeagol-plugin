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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { InputField, Checkbox } from "@scm-manager/ui-components";

type GlobalConfiguration = {
  smeagolUrl: string;
  enabled: boolean;
  navLinkEnabled: boolean;
  _links: Links;
};

type Props = {
  initialConfiguration: GlobalConfiguration;
  onConfigurationChange: (p1: GlobalConfiguration, p2: boolean) => void;
};

const SmeagolConfigurationForm: FC<Props> = ({ initialConfiguration, onConfigurationChange }) => {
  const [changedConfig, setChangedConfig] = useState(initialConfiguration);
  const [configurationChanged, setConfigurationChanged] = useState(false);
  const [t] = useTranslation("plugins");

  const renderConfigChangedNotification = () => {
    if (configurationChanged) {
      return (
        <div className="notification is-info">
          <button className="delete" onClick={() => setConfigurationChanged(false)} />
          {t("scm-smeagol-plugin.configurationChangedSuccess")}
        </div>
      );
    }
    return null;
  };

  const valueChangeHandler = (value: string, name: string) => {
    const newConfig = { ...changedConfig, [name]: value };
    setChangedConfig(newConfig);
    onConfigurationChange(newConfig, true);
  };

  return (
    <>
      {renderConfigChangedNotification()}
      <Checkbox
        name="enabled"
        label={t("scm-smeagol-plugin.form.enabled")}
        helpText={t("scm-smeagol-plugin.form.enabledHelp")}
        checked={changedConfig.enabled}
        onChange={valueChangeHandler}
      />
      <Checkbox
        name="navLinkEnabled"
        label={t("scm-smeagol-plugin.form.navLinkEnabled")}
        helpText={t("scm-smeagol-plugin.form.navLinkEnabledHelp")}
        checked={changedConfig.navLinkEnabled}
        onChange={valueChangeHandler}
        disabled={!changedConfig.enabled}
      />
      <InputField
        name="smeagolUrl"
        label={t("scm-smeagol-plugin.form.url")}
        helpText={t("scm-smeagol-plugin.form.urlHelp")}
        disabled={!changedConfig.enabled}
        value={changedConfig.smeagolUrl}
        onChange={valueChangeHandler}
        type="url"
      />
    </>
  );
};

export default SmeagolConfigurationForm;
