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
import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { InputField, Checkbox } from "@scm-manager/ui-components";

type GlobalConfiguration = {
  smeagolUrl: string;
  enabled: boolean;
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
