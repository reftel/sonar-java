/*
 * SonarQube Java
 * Copyright (C) 2012-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.java.ast.visitors;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.java.JavaClasspath;
import org.sonar.java.JavaTestClasspath;
import org.sonar.java.SonarComponents;
import org.sonar.java.ast.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SonarSymbolTableVisitorTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private static final String EOL = "\n";
  private List<String> lines;
  private SensorContextTester context;
  private DefaultFileSystem fs;
  private SonarComponents sonarComponents;

  @Before
  public void setUp() throws Exception {
    context = SensorContextTester.create(temp.getRoot());
    fs = context.fileSystem();
    sonarComponents = new SonarComponents(mock(FileLinesContextFactory.class), fs,
      mock(JavaClasspath.class), mock(JavaTestClasspath.class), mock(CheckFactory.class));
    sonarComponents.setSensorContext(context);
  }

  @Test
  public void sonar_symbol_table() throws Exception {
    File file = temp.newFile();
    Files.write(Files.toString(new File("src/test/files/highlighter/SonarSymTable.java"), StandardCharsets.UTF_8).replaceAll("\\r\\n", "\n").replaceAll("\\n", EOL), file, StandardCharsets.UTF_8);
    lines = Files.readLines(file, StandardCharsets.UTF_8);
    String content  = Joiner.on(EOL).join(lines);
    fs.add(new DefaultInputFile("", file.getName()).initMetadata(content));
    JavaAstScanner.scanSingleFileForTests(file, new VisitorsBridge(ImmutableList.of(), sonarComponents.getJavaClasspath(), sonarComponents));
    String componentKey = ":" + file.getName();
    verifyUsages(componentKey, 1, 17, reference(5,2), reference(9,10));
    // Example class declaration
    verifyUsages(componentKey, 4, 6);
    verifyUsages(componentKey, 4, 14);
    // list field
    verifyUsages(componentKey, 5, 15, reference(10, 9));

    // Example empty constructor
    verifyUsages(componentKey, 6, 2);
    // Do not reference constructor of class using this() and super() as long as SONAR-5894 is not fixed
    //verify(symboltableBuilder).newReference(any(Symbol.class), eq(offset(7, 5)));

    // Example list constructor
    verifyUsages(componentKey, 9, 2, reference(7, 4));

    // list local var
    verifyUsages(componentKey, 9, 23, reference(10, 16));
    // method
    verifyUsages(componentKey, 12, 6);
    //label
    verifyUsages(componentKey, 13, 4);
    //Enum
    verifyUsages(componentKey, 16, 7);
    verifyUsages(componentKey, 17, 5);
    // Do not reference constructor of enum as it can leads to failure in analysis as long as SONAR-5894 is not fixed
    //verify(symboltableBuilder).newReference(any(Symbol.class), eq(offset(14, 5)));

    verifyUsages(componentKey, 18, 4, reference(17, 4));
    verifyUsages(componentKey, 21, 3, reference(21, 19));
    verifyUsages(componentKey, 21, 11);
    verifyUsages(componentKey, 21, 21);
  }

  private void verifyUsages(String componentKey, int line, int offset, TextPointer... tps) {
    Collection<TextRange> textRanges = context.referencesForSymbolAt(componentKey, line, offset);
    if(tps.length == 0) {
      // TODO assert correctly that symbol is effectevly created see : SONAR-7850
      assertThat(textRanges).isEmpty();
    } else {
      assertThat(textRanges.stream().map(TextRange::start).collect(Collectors.toList())).isNotEmpty().containsOnly(tps);
    }
  }

  private static TextPointer reference(int line, int column) {
    return new DefaultTextPointer(line, column);
  }

}
