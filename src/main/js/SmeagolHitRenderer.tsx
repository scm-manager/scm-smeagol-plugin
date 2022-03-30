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
  isValueHitField,
  Notification,
  RepositoryAvatar,
  TextHitField,
  useBooleanHitFieldValue,
  useStringHitFieldValue
} from "@scm-manager/ui-components";
import { Hit as HitType } from "@scm-manager/ui-types";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

const Container = styled(Hit.Content)`
  overflow-x: scroll;
`;

const FileContent = styled.div`
  border: 1px solid #dbdbdb;
  border-radius: 0.25rem;
`;

type SyntaxHighlighting = {
  modes: {
    ace?: string;
    codemirror?: string;
    prism?: string;
  };
};

const ContentMessage: FC = ({ children }) => <div className="has-background-info-light p-4 is-size-7">{children}</div>;

const BinaryContent: FC = () => {
  const [t] = useTranslation("plugins");
  return <ContentMessage>{t("scm-smeagol-plugin.hit.binary")}</ContentMessage>;
};

const EmptyContent: FC = () => {
  const [t] = useTranslation("plugins");
  return <ContentMessage>{t("scm-smeagol-plugin.hit.empty")}</ContentMessage>;
};

const isEmpty = (hit: HitType) => {
  const content = hit.fields["content"];
  return !content || (isValueHitField(content) && content.value === "");
};

const useDeterminedLanguage = (hit: HitType) => {
  const language = useStringHitFieldValue(hit, "codingLanguage");
  const syntaxHighlighting = hit._embedded?.syntaxHighlighting as SyntaxHighlighting;
  if (syntaxHighlighting) {
    return (
      syntaxHighlighting.modes.prism || syntaxHighlighting.modes.codemirror || syntaxHighlighting.modes.ace || language
    );
  }
  return language;
};

const TextContent: FC<HitProps> = ({ hit }) => {
  const language = useDeterminedLanguage(hit);
  if (isEmpty(hit)) {
    return <EmptyContent />;
  } else {
    return (
      <pre>
        <code>
          <TextHitField hit={hit} field="content" truncateValueAt={1024} syntaxHighlightingLanguage={language}>
            <EmptyContent />
          </TextHitField>
        </code>
      </pre>
    );
  }
};

const Content: FC<HitProps> = ({ hit }) => {
  const binary = useBooleanHitFieldValue(hit, "binary");

  if (binary) {
    return <BinaryContent />;
  } else {
    return <TextContent hit={hit} />;
  }
};

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
          <Content hit={hit} />
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
