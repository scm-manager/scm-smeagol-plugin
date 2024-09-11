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
import {
  Hit,
  HitProps,
  Notification,
  RepositoryAvatar,
  TextHitField,
  useStringHitFieldValue
} from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";

const Container = styled(Hit.Content)`
  overflow-x: scroll;
`;

const FileContent = styled.div`
  border: 1px solid #dbdbdb;
  border-radius: 0.25rem;
`;

const TextContent: FC<HitProps> = ({ hit }) => (
  <pre>
    <code>
      <TextHitField hit={hit} field="content" truncateValueAt={1024} syntaxHighlightingLanguage="markdown" />
    </code>
  </pre>
);

const SmeagolHitRenderer: FC<HitProps> = ({ hit }) => {
  const revision = useStringHitFieldValue(hit, "revision");
  const path = useStringHitFieldValue(hit, "path");

  const repository = hit._embedded?.repository;

  if (!revision || !path || !repository) {
    return <Notification type="danger">Found incomplete content search result</Notification>;
  }

  return (
    <Hit>
      <Container>
        <div className="is-flex">
          <RepositoryAvatar repository={repository} size={48} />
          <div className="ml-2">
            <Link to={`/repo/${repository.namespace}/${repository.name}`}>
              <Hit.Title>
                {repository.namespace}/{repository.name}
              </Hit.Title>
            </Link>
            <Link
              className="is-ellipsis-overflow"
              to={`/repo/${repository.namespace}/${repository.name}/code/sources/${revision}/${path}`}
            >
              <TextHitField hit={hit} field="path" />
            </Link>
          </div>
        </div>
        <FileContent className="my-2">
          <TextContent hit={hit} />
        </FileContent>
        <small className="is-size-7">
          Revision:{" "}
          <Link
            className="is-ellipsis-overflow"
            to={`/repo/${repository.namespace}/${repository.name}/code/sources/${revision}`}
          >
            <TextHitField hit={hit} field="revision" />
          </Link>
        </small>
      </Container>
    </Hit>
  );
};

export default SmeagolHitRenderer;
