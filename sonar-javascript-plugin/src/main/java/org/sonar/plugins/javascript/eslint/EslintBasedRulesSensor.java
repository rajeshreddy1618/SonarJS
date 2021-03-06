/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.javascript.eslint;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.javascript.checks.CheckList;
import org.sonar.javascript.checks.EslintBasedCheck;
import org.sonar.plugins.javascript.JavaScriptChecks;
import org.sonar.plugins.javascript.JavaScriptLanguage;

public class EslintBasedRulesSensor implements Sensor {

  private static final Logger LOG = Loggers.get(EslintBasedRulesSensor.class);
  private static final Gson GSON = new Gson();

  private final String[] rules;
  private final JavaScriptChecks checks;
  private final EslintBridgeServer eslintBridgeServer;

  EslintBasedRulesSensor(CheckFactory checkFactory, EslintBridgeServer eslintBridgeServer) {
    this.checks = JavaScriptChecks.createJavaScriptCheck(checkFactory)
      .addChecks(CheckList.REPOSITORY_KEY, CheckList.getChecks());

    this.rules = this.checks.eslintBasedChecks().stream()
      .map(EslintBasedCheck::eslintKey)
      .toArray(String[]::new);

    this.eslintBridgeServer = eslintBridgeServer;
  }

  @Override
  public void execute(SensorContext context) {
    if (rules.length == 0) {
      LOG.debug("Skipping execution of eslint-based rules because none of them are activated");
      return;
    }

    try {
      eslintBridgeServer.start();
      for (InputFile inputFile : getInputFiles(context)) {
        analyze(inputFile, context);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage());
      LOG.error("Failure during analysis: " + eslintBridgeServer, e);
    } finally {
      eslintBridgeServer.clean();
    }
  }

  private void analyze(InputFile file, SensorContext context) {
    AnalysisRequest analysisRequest = new AnalysisRequest(file, rules);
    try {
      String result = eslintBridgeServer.call(GSON.toJson(analysisRequest));
      AnalysisResponseIssue[] issues = toIssues(result);
      for (AnalysisResponseIssue issue : issues) {
        saveIssue(file, context, issue);
      }
    } catch (IOException e) {
      LOG.error("Failed to get response while analyzing " + file.uri(), e);
    }
  }

  private static AnalysisResponseIssue[] toIssues(String result) {
    try {
      return GSON.fromJson(result, AnalysisResponseIssue[].class);
    } catch (JsonSyntaxException e) {
      LOG.error("Failed to parse: \n-----\n" + result + "\n-----\n");
      return new AnalysisResponseIssue[0];
    }
  }

  private void saveIssue(InputFile file, SensorContext context, AnalysisResponseIssue issue) {
    NewIssue newIssue = context.newIssue();
    NewIssueLocation location = newIssue.newLocation()
      .message(issue.message)
      .on(file);

    if (issue.endLine != null) {
      location.at(file.newRange(issue.line, issue.column, issue.endLine, issue.endColumn));
    } else {
      location.at(file.selectLine(issue.line));
    }

    newIssue.at(
      location)
      .forRule(ruleKey(issue.ruleId))
      .save();
  }

  private RuleKey ruleKey(String eslintKey) {
    RuleKey ruleKey = checks.ruleKeyByEslintKey(eslintKey);
    if (ruleKey == null) {
      throw new IllegalStateException("No SonarJS rule key found for an eslint-based rule " + eslintKey);
    }
    return ruleKey;
  }

  private static Iterable<InputFile> getInputFiles(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    FilePredicate mainFilePredicate = sensorContext.fileSystem().predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(JavaScriptLanguage.KEY));
    return fileSystem.inputFiles(mainFilePredicate);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(JavaScriptLanguage.KEY)
      .name("SonarJS ESLint-based rules execution")
      .onlyOnFileType(Type.MAIN);
  }

  private static class AnalysisRequest {
    String filepath;
    String fileContent;
    String[] rules;

    AnalysisRequest(InputFile file, String[] rules) {
      this.filepath = file.uri().toString();
      this.fileContent = fileContent(file);
      this.rules = rules;
    }

    private static String fileContent(InputFile file) {
      try {
        return file.contents();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  static class AnalysisResponseIssue {
    Integer line;
    Integer column;
    Integer endLine;
    Integer endColumn;
    String message;
    String ruleId;
  }

}
