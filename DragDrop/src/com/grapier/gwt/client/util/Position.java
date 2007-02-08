package com.grapier.gwt.client.util;

public class Position {
  private int row;
  private int col;
  
  public Position(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public int getCol() {
    return col;
  }

  public int getRow() {
    return row;
  }
  
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    
    if (o == null) {
      return false;
    }
    
    if (! (o instanceof Position)) {
      return false;
    }
    
    return this.toString().equals(o.toString());
  }
  
  public String toString() {
    return "(" + getRow() + "," + getCol() + ")";
  }
  
  public int hashCode() {
    return toString().hashCode();
  }
  
  public Position addRow() {
    return new Position(row+1, col);
  }
  
  public Position subtractRow() {
    return new Position(row-1, col);
  }
  
  public Position addCol() {
    return new Position(row, col+1);
  }
  
  public Position subtractCol() {
    return new Position(row, col-1);
  }
}
