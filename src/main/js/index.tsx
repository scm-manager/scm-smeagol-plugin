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

import React from "react";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import { binder } from "@scm-manager/ui-extensions";
import SmeagolConfiguration from "./SmeagolConfiguration";
import SmeagolNavLink from "./SmeagolNavLink";
import SmeagolPrimaryNavigation from "./SmeagolPrimaryNavigation";
import SmeagolHitRenderer from "./SmeagolHitRenderer";

cfgBinder.bindGlobal("/smeagol", "scm-smeagol-plugin.nav-link", "smeagolConfig", SmeagolConfiguration);

const smeagolWikiPredicate = (props: object) => {
  return props.repository && props.repository._links.smeagolWiki;
};

const SmeagolNavLinkFactory = ({ repository }) => {
  return <SmeagolNavLink repository={repository} />;
};

const smeagolPrimaryNavigationPredicate = (props: object) => {
  return props.links.smeagol.find(x => x.name === "smeagolRoot");
};

binder.bind("repository.navigation", SmeagolNavLinkFactory, smeagolWikiPredicate);
binder.bind("primary-navigation", SmeagolPrimaryNavigation, smeagolPrimaryNavigationPredicate);
binder.bind("search.hit.smeagolDocument.renderer", SmeagolHitRenderer);
