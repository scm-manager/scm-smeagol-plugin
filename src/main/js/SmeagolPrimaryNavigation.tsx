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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Link, Links } from "@scm-manager/ui-types";

type Props = {
  links: Links;
};

const SmeagolPrimaryNavigation: FC<Props> = ({ links }) => {
  const [t] = useTranslation("plugins");

  const smeagolLinks = links.smeagol as Link[] | undefined;
  const smeagolLink = smeagolLinks?.find(link => link.name === "smeagolRoot");
  if (!smeagolLink) {
    return null;
  }

  return (
    <a className="navbar-item" href={smeagolLink.href}>
      {t("scm-smeagol-plugin.nav-link")}
    </a>
  );
};

export default SmeagolPrimaryNavigation;
