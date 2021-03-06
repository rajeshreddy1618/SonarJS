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
package org.sonar.javascript.parser.expressions;

import org.junit.Test;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;

import static org.sonar.javascript.utils.Assertions.assertThat;

public class ObjectLiteralTest {


  @Test
  public void test() {
    assertThat(Kind.OBJECT_LITERAL)
      .matches("{ }")
      .matches("{ propertyName , }")
      .matches("{ ... assignmentExpression }")
      .matches("{ propertyName : assignmentExpression, ... assignmentExpression }")
      .matches("{ propertyName : assignmentExpression , }")
      .matches("{ propertyName : assignmentExpression , }")
      .matches("{ propertyName : assignmentExpression , propertyName : assignmentExpression }")
      .matches("{ propertyName : assignmentExpression , propertyName : assignmentExpression , }")

      .notMatches("{ , }")
      .notMatches("{ propertyName : assignmentExpression , , }");
  }

  @Test
  public void test_keywords_as_identifiers() {
    assertThat(Kind.OBJECT_LITERAL)
      .matches("{ yield , }")
      .matches("{ async , }")
      .matches("{ await , }")

    ;
  }

  @Test
  public void property_with_decorator() {
    assertThat(Kind.OBJECT_LITERAL)
      .matches("{ @decorator method(){} }")
      .matches("{ @decorator set a(){} }")
      .matches("{ @decorator * generator(){} }")
      .matches("{ @decorator method(){}, prop: 'value'}")
    ;
  }

}
