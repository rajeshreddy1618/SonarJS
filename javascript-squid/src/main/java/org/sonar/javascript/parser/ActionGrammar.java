/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011 SonarSource and Eriks Nukis
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.javascript.parser;

import com.sonar.sslr.api.AstNode;
import org.sonar.javascript.api.EcmaScriptKeyword;
import org.sonar.javascript.api.EcmaScriptPunctuator;
import org.sonar.javascript.api.EcmaScriptTokenType;
import org.sonar.javascript.ast.parser.TreeFactory;
import org.sonar.javascript.model.implementations.expression.ArrayLiteralTreeImpl;
import org.sonar.javascript.model.implementations.expression.LiteralTreeImpl;
import org.sonar.javascript.model.implementations.statement.BlockTreeImpl;
import org.sonar.javascript.model.implementations.statement.BreakStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.CaseClauseTreeImpl;
import org.sonar.javascript.model.implementations.statement.CatchBlockTreeImpl;
import org.sonar.javascript.model.implementations.statement.ContinueStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.DebuggerStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.DefaultClauseTreeImpl;
import org.sonar.javascript.model.implementations.statement.DoWhileStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ElseClauseTreeImpl;
import org.sonar.javascript.model.implementations.statement.EmptyStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ExpressionStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ForInStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ForOfStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ForStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.IfStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.LabelledStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ReturnStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.SwitchStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.ThrowStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.TryStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.VariableDeclarationTreeImpl;
import org.sonar.javascript.model.implementations.statement.VariableStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.WhileStatementTreeImpl;
import org.sonar.javascript.model.implementations.statement.WithStatementTreeImpl;
import org.sonar.javascript.model.interfaces.Tree.Kind;
import org.sonar.javascript.model.interfaces.expression.ExpressionTree;
import org.sonar.javascript.model.interfaces.statement.DebuggerStatementTree;
import org.sonar.javascript.model.interfaces.statement.StatementTree;
import org.sonar.javascript.parser.sslr.GrammarBuilder;

public class ActionGrammar {

  private final GrammarBuilder b;
  private final TreeFactory f;

  public ActionGrammar(GrammarBuilder b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  /**
   * A.4 Statement
   */

  public EmptyStatementTreeImpl EMPTY_STATEMENT() {
    return b.<EmptyStatementTreeImpl>nonterminal(Kind.EMPTY_STATEMENT)
      .is(f.emptyStatement(b.invokeRule(EcmaScriptPunctuator.SEMI)));
  }

  public DebuggerStatementTree DEBUGGER_STATEMENT() {
    return b.<DebuggerStatementTreeImpl>nonterminal(Kind.DEBUGGER_STATEMENT)
      .is(f.debuggerStatement(b.invokeRule(EcmaScriptKeyword.DEBUGGER), b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public VariableStatementTreeImpl VARIABLE_STATEMENT() {
    return b.<VariableStatementTreeImpl>nonterminal(Kind.VARIABLE_STATEMENT)
      .is(f.completeVariableStatement(b.invokeRule(EcmaScriptKeyword.VAR), VARIABLE_DECLARATION_LIST(), b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public VariableStatementTreeImpl VARIABLE_DECLARATION_LIST() {
    return b.<VariableStatementTreeImpl>nonterminal(EcmaScriptGrammar.VARIABLE_DECLARATION_LIST)
      .is(f.newVariableStatement(VARIABLE_DECLARATION(), b.zeroOrMore(f.newTuple1(b.invokeRule(EcmaScriptPunctuator.COMMA), VARIABLE_DECLARATION()))));
  }

  public VariableDeclarationTreeImpl VARIABLE_DECLARATION() {
    return b.<VariableDeclarationTreeImpl>nonterminal(Kind.VARIABLE_DECLARATION)
      .is(f.variableDeclaration(b.firstOf(b.invokeRule(EcmaScriptGrammar.BINDING_IDENTIFIER_INITIALISER), b.invokeRule(EcmaScriptGrammar.BINDING_PATTERN_INITIALISER))));
  }

  public LabelledStatementTreeImpl LABELLED_STATEMENT() {
    return b.<LabelledStatementTreeImpl>nonterminal(Kind.LABELLED_STATEMENT)
      .is(f.labelledStatement(b.invokeRule(EcmaScriptTokenType.IDENTIFIER), b.invokeRule(EcmaScriptPunctuator.COLON), STATEMENT()));
  }

  public ContinueStatementTreeImpl CONTINUE_STATEMENT() {
    return b.<ContinueStatementTreeImpl>nonterminal(Kind.CONTINUE_STATEMENT)
      .is(f.completeContinueStatement(
        b.invokeRule(EcmaScriptKeyword.CONTINUE),
        b.firstOf(
          CONTINUE_WITH_LABEL(),
          CONTINUE_WITHOUT_LABEL())
      ));
  }

  public ContinueStatementTreeImpl CONTINUE_WITH_LABEL() {
    return b.<ContinueStatementTreeImpl>nonterminal()
      .is(f.newContinueWithLabel(
        b.invokeRule(EcmaScriptGrammar.IDENTIFIER_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public ContinueStatementTreeImpl CONTINUE_WITHOUT_LABEL() {
    return b.<ContinueStatementTreeImpl>nonterminal()
      .is(f.newContinueWithoutLabel(b.invokeRule(EcmaScriptGrammar.EOS_NO_LB)));
  }

  public BreakStatementTreeImpl BREAK_STATEMENT() {
    return b.<BreakStatementTreeImpl>nonterminal(Kind.BREAK_STATEMENT)
      .is(f.completeBreakStatement(
        b.invokeRule(EcmaScriptKeyword.BREAK),
        b.firstOf(
          BREAK_WITH_LABEL(),
          BREAK_WITHOUT_LABEL())
      ));
  }

  public BreakStatementTreeImpl BREAK_WITH_LABEL() {
    return b.<BreakStatementTreeImpl>nonterminal()
      .is(f.newBreakWithLabel(
        b.invokeRule(EcmaScriptGrammar.IDENTIFIER_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public BreakStatementTreeImpl BREAK_WITHOUT_LABEL() {
    return b.<BreakStatementTreeImpl>nonterminal()
      .is(f.newBreakWithoutLabel(b.invokeRule(EcmaScriptGrammar.EOS_NO_LB)));
  }

  public ReturnStatementTreeImpl RETURN_STATEMENT() {
    return b.<ReturnStatementTreeImpl>nonterminal(Kind.RETURN_STATEMENT)
      .is(f.completeReturnStatement(
        b.invokeRule(EcmaScriptKeyword.RETURN),
        b.firstOf(
          RETURN_WITH_EXPRESSION(),
          RETURN_WITHOUT_EXPRESSION())
      ));
  }

  public ReturnStatementTreeImpl RETURN_WITH_EXPRESSION() {
    return b.<ReturnStatementTreeImpl>nonterminal()
      .is(f.newReturnWithExpression(
        b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public ReturnStatementTreeImpl RETURN_WITHOUT_EXPRESSION() {
    return b.<ReturnStatementTreeImpl>nonterminal()
      .is(f.newReturnWithoutExpression(b.invokeRule(EcmaScriptGrammar.EOS_NO_LB)));
  }

  public ThrowStatementTreeImpl THROW_STATEMENT() {
    return b.<ThrowStatementTreeImpl>nonterminal(Kind.THROW_STATEMENT)
      .is(f.newThrowStatement(
        b.invokeRule(EcmaScriptKeyword.THROW),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_LB),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public WithStatementTreeImpl WITH_STATEMENT() {
    return b.<WithStatementTreeImpl>nonterminal(Kind.WITH_STATEMENT)
      .is(f.newWithStatement(
        b.invokeRule(EcmaScriptKeyword.WITH),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public BlockTreeImpl BLOCK() {
    return b.<BlockTreeImpl>nonterminal(Kind.BLOCK)
      .is(f.newBlock(b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE), b.optional(b.invokeRule(EcmaScriptGrammar.STATEMENT_LIST)), b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE)));
  }

  public TryStatementTreeImpl TRY_STATEMENT() {
    return b.<TryStatementTreeImpl>nonterminal(Kind.TRY_STATEMENT)
      .is(f.completeTryStatement(
        b.invokeRule(EcmaScriptKeyword.TRY),
        BLOCK(),
        b.firstOf(
          f.newTryStatementWithCatch(CATCH_CLAUSE(), b.optional(FINALLY_CLAUSE())),
          FINALLY_CLAUSE())
      ));
  }

  public TryStatementTreeImpl FINALLY_CLAUSE() {
    return b.<TryStatementTreeImpl>nonterminal(EcmaScriptGrammar.FINALLY)
      .is(f.newTryStatementWithFinally(b.invokeRule(EcmaScriptKeyword.FINALLY), BLOCK()));
  }

  public CatchBlockTreeImpl CATCH_CLAUSE() {
    return b.<CatchBlockTreeImpl>nonterminal(Kind.CATCH_BLOCK)
      .is(f.newCatchBlock(
        b.invokeRule(EcmaScriptKeyword.CATCH),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CATCH_PARAMETER),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        BLOCK()));
  }

  public SwitchStatementTreeImpl SWITCH_STATEMENT() {
    return b.<SwitchStatementTreeImpl>nonterminal(Kind.SWITCH_STATEMENT)
      .is(f.completeSwitchStatement(
        b.invokeRule(EcmaScriptKeyword.SWITCH),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        CASE_BLOCK()));
  }

  public SwitchStatementTreeImpl CASE_BLOCK() {
    return b.<SwitchStatementTreeImpl>nonterminal()
      .is(f.newSwitchStatement(
        b.invokeRule(EcmaScriptPunctuator.LCURLYBRACE),
        b.zeroOrMore(CASE_CLAUSE()),
        b.optional(f.newTuple2(DEFAULT_CLAUSE(), b.zeroOrMore(CASE_CLAUSE()))),
        b.invokeRule(EcmaScriptPunctuator.RCURLYBRACE)));
  }

  public CaseClauseTreeImpl CASE_CLAUSE() {
    return b.<CaseClauseTreeImpl>nonterminal(Kind.CASE_CLAUSE)
      .is(f.caseClause(
        b.invokeRule(EcmaScriptKeyword.CASE),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.COLON),
        b.optional(b.invokeRule(EcmaScriptGrammar.STATEMENT_LIST))));
  }

  public DefaultClauseTreeImpl DEFAULT_CLAUSE() {
    return b.<DefaultClauseTreeImpl>nonterminal(Kind.DEFAULT_CLAUSE)
      .is(f.defaultClause(
        b.invokeRule(EcmaScriptKeyword.DEFAULT),
        b.invokeRule(EcmaScriptPunctuator.COLON),
        b.optional(b.invokeRule(EcmaScriptGrammar.STATEMENT_LIST))));
  }

  public IfStatementTreeImpl IF_STATEMENT() {
    return b.<IfStatementTreeImpl>nonterminal(Kind.IF_STATEMENT)
      .is(f.ifStatement(
        b.invokeRule(EcmaScriptKeyword.IF),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CONDITION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT(),
        b.optional(ELSE_CLAUSE())));
  }

  public ElseClauseTreeImpl ELSE_CLAUSE() {
    return b.<ElseClauseTreeImpl>nonterminal(Kind.ELSE_CLAUSE)
      .is(f.elseClause(
        b.invokeRule(EcmaScriptKeyword.ELSE),
        STATEMENT()));
  }

  public WhileStatementTreeImpl WHILE_STATEMENT() {
    return b.<WhileStatementTreeImpl>nonterminal(Kind.WHILE_STATEMENT)
      .is(f.whileStatement(
        b.invokeRule(EcmaScriptKeyword.WHILE),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CONDITION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public DoWhileStatementTreeImpl DO_WHILE_STATEMENT() {
    return b.<DoWhileStatementTreeImpl>nonterminal(Kind.DO_WHILE_STATEMENT)
      .is(f.doWhileStatement(
        b.invokeRule(EcmaScriptKeyword.DO),
        STATEMENT(),
        b.invokeRule(EcmaScriptKeyword.WHILE),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.CONDITION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  public ExpressionStatementTreeImpl EXPRESSION_STATEMENT() {
    return b.<ExpressionStatementTreeImpl>nonterminal(Kind.EXPRESSION_STATEMENT)
      .is(f.expressionStatement(
        b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_LCURLY_AND_FUNCTION), b.invokeRule(EcmaScriptGrammar.EOS)));
  }

  /**
   * ECMAScript 6
   */
  public ForOfStatementTreeImpl FOR_OF_STATEMENT() {
    return b.<ForOfStatementTreeImpl>nonterminal(Kind.FOR_OF_STATEMENT)
      .is(f.forOfStatement(
        b.invokeRule(EcmaScriptKeyword.FOR),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.firstOf(b.invokeRule(EcmaScriptGrammar.FOR_DECLARATION), b.invokeRule(EcmaScriptGrammar.LEFT_HAND_SIDE_EXPRESSION_NO_LET)),
        b.invokeRule(EcmaScriptGrammar.OF),
        b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public ForInStatementTreeImpl FOR_IN_STATEMENT() {
    return b.<ForInStatementTreeImpl>nonterminal(Kind.FOR_IN_STATEMENT)
      .is(f.forInStatement(
        b.invokeRule(EcmaScriptKeyword.FOR),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),
        b.firstOf(b.invokeRule(EcmaScriptGrammar.FOR_DECLARATION), b.invokeRule(EcmaScriptGrammar.LEFT_HAND_SIDE_EXPRESSION_NO_LET_AND_LBRACKET)),
        b.invokeRule(EcmaScriptKeyword.IN),
        b.invokeRule(EcmaScriptGrammar.EXPRESSION),
        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()));
  }

  public ForStatementTreeImpl FOR_STATEMENT() {
    return b.<ForStatementTreeImpl>nonterminal(Kind.FOR_STATEMENT)
      .is(f.forStatement(
        b.invokeRule(EcmaScriptKeyword.FOR),
        b.invokeRule(EcmaScriptPunctuator.LPARENTHESIS),

        b.optional(b.firstOf(
          b.invokeRule(EcmaScriptGrammar.FOR_VAR_DECLARATION),
          b.invokeRule(ES6(EcmaScriptGrammar.LEXICAL_DECLARATION_NO_IN)),
          b.invokeRule(EcmaScriptGrammar.EXPRESSION_NO_IN_NO_LET_AND_BRACKET))), b.invokeRule(EcmaScriptPunctuator.SEMI),
        b.optional(b.invokeRule(EcmaScriptGrammar.CONDITION)), b.invokeRule(EcmaScriptPunctuator.SEMI),
        b.optional(b.invokeRule(EcmaScriptGrammar.EXPRESSION)),

        b.invokeRule(EcmaScriptPunctuator.RPARENTHESIS),
        STATEMENT()
      ));
  }

  public StatementTree ITERATION_STATEMENT() {
    return b.<StatementTree>nonterminal(EcmaScriptGrammar.ITERATION_STATEMENT)
      .is(b.firstOf(
        DO_WHILE_STATEMENT(),
        WHILE_STATEMENT(),
        FOR_IN_STATEMENT(),
        ES6(FOR_OF_STATEMENT()),
        FOR_STATEMENT()));
  }

  public StatementTree STATEMENT() {
    return b.<StatementTree>nonterminal(EcmaScriptGrammar.STATEMENT)
      .is(b.firstOf(
        BLOCK(),
        VARIABLE_STATEMENT(),
        EMPTY_STATEMENT(),
        LABELLED_STATEMENT(),
        EXPRESSION_STATEMENT(),
        IF_STATEMENT(),
        ITERATION_STATEMENT(),
        CONTINUE_STATEMENT(),
        BREAK_STATEMENT(),
        RETURN_STATEMENT(),
        WITH_STATEMENT(),
        SWITCH_STATEMENT(),
        THROW_STATEMENT(),
        TRY_STATEMENT(),
        DEBUGGER_STATEMENT()));
  }

  /**
   * A.4 [END] Statement
   */

  /**
   * A.3 Expressions
   */

  public LiteralTreeImpl LITERAL() {
    return b.<LiteralTreeImpl>nonterminal(EcmaScriptGrammar.LITERAL)
      .is(b.firstOf(
        f.nullLiteral(b.invokeRule(EcmaScriptKeyword.NULL)),
        f.booleanLiteral(b.firstOf(b.invokeRule(EcmaScriptKeyword.TRUE), b.invokeRule(EcmaScriptKeyword.FALSE))),
        f.numericLiteral(b.invokeRule(EcmaScriptTokenType.NUMERIC_LITERAL)),
        f.stringLiteral(b.invokeRule(EcmaScriptGrammar.STRING_LITERAL)),
        f.regexpLiteral(b.invokeRule(EcmaScriptTokenType.REGULAR_EXPRESSION_LITERAL))));
  }

  public AstNode ARRAY_INITIALISER_ELEMENT() {
    return b.<AstNode>nonterminal(EcmaScriptGrammar.ARRAY_INITIALIZER_ELEMENT)
      .is(f.arrayInitialiserElement(b.optional(b.invokeRule(EcmaScriptPunctuator.ELLIPSIS)), b.invokeRule(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION)));
  }

  public ArrayLiteralTreeImpl ELEMENT_LIST() {
    return b.<ArrayLiteralTreeImpl>nonterminal(EcmaScriptGrammar.ELEMENT_LIST)
      .is(f.newArrayLiteralWithElements(
        b.zeroOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA)),
        ARRAY_INITIALISER_ELEMENT(),
        b.zeroOrMore(
          f.newTuple3(f.newWrapperAstNode(b.oneOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA))), ARRAY_INITIALISER_ELEMENT())),
        b.zeroOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA))));
  }

  public ArrayLiteralTreeImpl ARRAY_LITERAL() {
    return b.<ArrayLiteralTreeImpl>nonterminal(Kind.ARRAY_LITERAL)
      .is(f.completeArrayLiteral(
        b.invokeRule(EcmaScriptPunctuator.LBRACKET),
        b.optional(b.firstOf(
            ELEMENT_LIST(),
            f.newArrayLiteralWithElidedElements(b.oneOrMore(b.invokeRule(EcmaScriptPunctuator.COMMA))))),
        b.invokeRule(EcmaScriptPunctuator.RBRACKET)));
  }

  /**
   * A.3 [END] Expressions
   */

  private <T> T ES6(T object) {
    return object;
  }

}