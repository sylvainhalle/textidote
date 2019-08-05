/*
    TeXtidote, a linter for LaTeX documents
    Copyright (C) 2018-2019  Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.textidote.render;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

import ca.uqac.lif.textidote.Advice;
import ca.uqac.lif.textidote.Rule;
import ca.uqac.lif.textidote.as.AnnotatedString;
import ca.uqac.lif.textidote.as.Range;
import ca.uqac.lif.textidote.as.Position;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.textidote.render.SinglelineAdviceRenderer;

public class SinglelineAdviceRendererTest {
  @Test
  public void test1() {
    String filename = "file";
    String rulename = "rule";
    String line1 = "foo";
    String line2 = "bar";
    String message = "warning message";
    AnnotatedString as = new AnnotatedString().append(line1).appendNewLine().append(line2).appendNewLine();
    int startLine = 1;
    int startCol = 0;
    int endLine = 1;
    int endCol = 2;
    Position start = new Position(startLine, startCol);
    Position end = new Position(endLine, endCol);
    Range range = new Range(start, end);
    Rule rule = new Rule(rulename) {
      @Override
      public List<Advice> evaluate(/* @ non_null @ */ AnnotatedString s, /* @ non_null @ */ AnnotatedString original)
      {
        return new ArrayList<Advice>();
      }
      public String getDescription() {
          return "";
        }
    };
    ArrayList<Advice> adList = new ArrayList<Advice>();
    Advice ad = new Advice(rule, range, message, filename, line2, 0);
    adList.add(ad);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    AnsiPrinter printer = new AnsiPrinter(baos);
    printer.disableColors();
    SinglelineAdviceRenderer renderer = new SinglelineAdviceRenderer(printer);
    renderer.addAdvice(filename, as, adList);
    renderer.render();
    String output = new String(baos.toByteArray());
    assertNotNull(output);
    String expected = String.format(filename + "(L" + (startLine + 1) + "C" + (startCol + 1) + "-L" + (endLine + 1)
        + "C" + (endCol + 1) + "): " + message + " \"" + line2 + "\"%n");
    assertEquals(expected, output);
  }
}
