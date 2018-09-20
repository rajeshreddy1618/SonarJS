import { rules } from "eslint-plugin-sonarjs";
import { Linter, SourceCode } from "eslint";
import { Rule } from "./eslint-runner";

const eslint = new Linter();
eslint.defineRules(rules);

export function getIssues(
  sourceCode: SourceCode,
  rules: Rule,
  filepath: string
) {
  return eslint
    .verify(sourceCode, { rules }, filepath)
    .map(({ nodeType, severity, fatal, fix, ...issueMessage }) => issueMessage);
}