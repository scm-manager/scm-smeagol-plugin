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
