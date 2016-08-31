package javax.annotation;

import org.apache.commons.lang.NullArgumentException;

@interface Nonnull {}
@interface Nullable {}

public abstract class CrossProcedural {
  
  private boolean aNull;

  public void caller(@Nonnull Object c) {
    checkState();
    Object a = aNull ? null : new String("a");
    Object b = new String("b");
    Object selected = select(a, b, c);
    if (a == b) {
      if (selected == c) {  // Noncompliant {{Change this condition so that it does not always evaluate to "true"}}
        log("Same");
      }
    } else if (a != null) {
      if (selected == a) { // Noncompliant {{Change this condition so that it does not always evaluate to "true"}}
        log("Same again");
      }
    }
    selected = select(a, a, c);
    if (selected == c) {  // Noncompliant {{Change this condition so that it does not always evaluate to "true"}}
      log("Same");
    }
    Object x = new String("x");
    Object y = new String("y");
    selected = select(x, y, c);
    if (selected != c) {
      if (x == y) {  // Noncompliant {{Change this condition so that it does not always evaluate to "false"}}
        log("Same");
      }
    }
  }
  
  public void callerBracketing(Integer a, Integer b) {
    if (bracketing(a, b)) {
      if (a > b) {  // Noncompliant {{Change this condition so that it does not always evaluate to "false"}}
        log("redundant");
      }
    }
    if (b < a) {
      if (bracketing(a, b)) {  // Noncompliant {{Change this condition so that it does not always evaluate to "false"}}
        log("still redundant");
      }
    }
  }

  public Object select(Object pa, Object pb, Object pc) {
    if (pa == pb) {
      return pc;
    } else {
      return pa == null ? new String("x") : pa;
    }
  }
  
  public void callerCompute(Object a) {
    if (a == null) {
      compute(a);  // Noncompliant
    }
  }
  
  public void callerComputeObject(Object a) {
    if(a != null ) {
      return computeObject(a).hashCode(); // Noncompliant {{NullPointerException might be thrown as 'computeObject' is nullable here}}
    } else {
      return -1;
    }
  }
  
  public int callerIsNull(@Nullable Object a){
    if(isNull(a)) {
      return -1;
    } else {
      return a.hashCode();  //Compliant, we don't expect to get any issue here
    }
  }
  
  public void callerAbstract() {
    otherMethod();
    log("Abstract method called");
    Object a = new String("a");
    abstractMethodWithParameters(a, null);
    log("Abstract method with parameters called");
    nativeMethodWithParameters(a, null);
    log("Native method with parameters called");
  }
  
  public void callerVarArgs() {
    Object a = new String("a");
    Object b = new String("b");
    Object c = new String("b");
    Object d = new String("b");
    int n = varArguments(a, b, c, d);
    log(Integer.toString(n));
  }
  
  public void callerGetObjectOrNull() {
    Object clazz = ObjectFactory.getObject();
    if (clazz == null) {
      clazz = getObjectOrNull();
      if (clazz != null) {
        ObjectFactory.cache(clazz);
      } 
    }
    if (clazz != null) {
      throw new IllegalStateException();
    }
  }
  
  public Object callerComputeObjectOrException() {
    Object b = ObjectFactory.getObject();
    Object a = computeObjectOrException(b);
    return a.toString();
  }
  
  public void callerIsNullDirect(Object b){
    if (isNullDirect(b)) {
      if (b == null) {  // Noncompliant {{Change this condition so that it does not always evaluate to "true"}}
        log("redundant");
      }
    }
  }

  public void callerPotentialCause(boolean state) {
    if (state) {
      potentialCause("Yes, we can!", state);  // Compliant
    } else {
      potentialCause(null, state);  // Compliant
    }
    potentialCause(null, true);  // Noncompliant {{Parameter 1 of method 'potentialCause' may be null and shall cause a NullPointerException at line 140 of that method}}
  }

  private void potentialCause(Object a, boolean error) {
    if (error) {
      a.hash();
    }
  }

  private boolean isNull(Object b){
    if( b == null ) {
      return true;
    } else {
      return false;
    }
  }
  
  private int compute(Object b){
    return b.hashCode();
  }

  public Object computeObject(Object b){
    if( b != null ) {
      return null;
    } else {
      return new Object();
    }
  }
  
  private Object getObjectOrNull() {
    if(ObjectFactory.connected()) {
      return ObjectFactory.getObject();
    }
    return null;
  }
  
  private Object computeObjectOrException(Object b) {
    if (b == null) {
      throw new NullArgumentException();
    }
    return b.toString();
  }

  private void log(String string) {
  }

  private void checkState() {
  }
  
  private int varArguments(Object a, @Nullable Object... objects) {
    int n = 1;
    for (Object object : objects) {
      n += object.hashCode();
    }
    return n;
  }
  
  private boolean bracketing(Integer a, Integer b) {
    int c = 0;
    if (a > c) {
      return false;
    }
    if (b < c) {
      return false;
    }
    if (a + b < c) {
      log("check");
    }
    return true;
  }
  
  private boolean isNullDirect(Object a) {
    return a == null;
  }

  public abstract boolean otherMethod();

  public abstract boolean abstractMethodWithParameters(Object a, Object b);

  public native boolean nativeMethodWithParameters(Object a, Object b);
}