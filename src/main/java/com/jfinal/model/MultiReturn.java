package com.jfinal.model;

public class MultiReturn<R1, R2, R3> {
  private int value;
  private boolean ok;
  private Exception e;
  private R1 r1;
  private R2 r2;
  private R3 r3;

  public MultiReturn(boolean ok, R1 R1) {
    this.ok = ok;
    this.r1 = R1;
  }

  public MultiReturn(boolean ok, R1 r1, R2 r2) {
    this.ok = ok;
    this.r1 = r1;
    this.r2 = r2;
  }

  public MultiReturn(boolean ok, R1 r1, R2 r2, R3 r3) {
    this.ok = ok;
    this.r1 = r1;
    this.r2 = r2;
    this.r3 = r3;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public boolean isOk() {
    return ok;
  }

  public void setOk(boolean ok) {
    this.ok = ok;
  }

  public Exception getE() {
    return e;
  }

  public void setE(Exception e) {
    this.e = e;
  }

  public R1 getR1() {
    return r1;
  }

  public void setR1(R1 r1) {
    this.r1 = r1;
  }

  public R2 getR2() {
    return r2;
  }

  public void setR2(R2 r2) {
    this.r2 = r2;
  }

  public R3 getR3() {
    return r3;
  }

  public void setR3(R3 r3) {
    this.r3 = r3;
  }
  
  
}
