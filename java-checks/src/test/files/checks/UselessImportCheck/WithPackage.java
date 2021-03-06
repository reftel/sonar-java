@ParametersAreNonnullByDefault
package checks.UselessImportCheck;
import a.b.c.Foo;
import a.b.c.Bar;
import a.b.c.Baz;
import a.b.c.Qux;
import a.b.c.ReferencedFromJavadoc;
import a.b.c.NonCompliant; // Noncompliant [[sc=1;ec=27]]
import NonCompliant2;               // Noncompliant
import static a.b.c.Foo.d;
import a.b.c.*;
import static a.b.c.Foo.*;
import a.b.c.MyException;
import a.b.c.MyException2;
import java.lang.String;            // Noncompliant {{Remove this unnecessary import: java.lang classes are always implicitly imported.}}
import java.lang.*;                 // Noncompliant {{Remove this unnecessary import: java.lang classes are always implicitly imported.}}
import a.b.c.Foo;                   // Noncompliant {{Remove this duplicated import.}}
;
import checks.UselessImportCheck.*;              // Noncompliant {{Remove this unnecessary import: same package classes are always implicitly imported.}}
import checks.UselessImportCheckClose.*;
import static checks.UselessImportCheck.Foo.*;
import checks.UselessImportCheck.foo.*;
import pkg.NonCompliant1;           // Noncompliant
import pkg.CompliantClass1;
import pkg.CompliantClass2;
import pkg.CompliantClass3;
import pkg.CompliantClass4;
import java.lang.reflect.Array;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nonnull;
class Foo2 extends Foo {
  Bar a = new Baz<String>();
  Map<@Nonnull String, @Nonnull String> modulesMap = new HashMap<>();
  @Qux
  void test() throws MyException, MyException2 {
  }
  // ReferencedFromJavadoc
  @a.b.c.NonCompliant
  a.b.c.NonCompliant foo(a.b.c.NonCompliant bar) {
    List<CompliantClass1> ok = ImmutableList.<CompliantClass4>of();
    Class ok2 = CompliantClass2.class;
    CompliantClass3.staticMethod("OK");
    pkg.NonCompliant1 ok3;
    Array ok4;
    tottttt a;
    System.out.println(something.t);
    foo(ArrayList::new);
    return new a.b.c.NonCompliant();
  }
  void foo(@Nullable int x){}
}
