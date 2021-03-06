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
package org.sonar.javascript.tree.impl.statement;

import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.sonar.javascript.tree.impl.JavaScriptTree;
import org.sonar.javascript.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.javascript.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.javascript.api.tree.statement.StatementTree;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitor;

public class CaseClauseTreeImpl extends JavaScriptTree implements CaseClauseTree {

  private final SyntaxToken caseKeyword;
  private final ExpressionTree expression;
  private final SyntaxToken colon;
  private final List<StatementTree> statements;

  public CaseClauseTreeImpl(InternalSyntaxToken caseKeyword, ExpressionTree expression, InternalSyntaxToken colon) {
    this.caseKeyword = caseKeyword;
    this.expression = expression;
    this.colon = colon;
    this.statements = Collections.emptyList();
  }

  public CaseClauseTreeImpl(InternalSyntaxToken caseKeyword, ExpressionTree expression, InternalSyntaxToken colon, List<StatementTree> statements) {
    this.caseKeyword = caseKeyword;
    this.expression = expression;
    this.colon = colon;
    this.statements = statements;
  }

  @Override
  public SyntaxToken keyword() {
    return caseKeyword;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public SyntaxToken colonToken() {
    return colon;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Override
  public Kind getKind() {
    return Kind.CASE_CLAUSE;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.forArray(caseKeyword, expression, colon),
      statements.iterator()
    );
  }

  @Override
  public void accept(DoubleDispatchVisitor visitor) {
    visitor.visitCaseClause(this);
  }
}
