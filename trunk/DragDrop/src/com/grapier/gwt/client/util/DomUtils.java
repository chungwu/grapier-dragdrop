package com.grapier.gwt.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

public class DomUtils {

  /**
   * Returns scroll top of the document body
   */
  public static native int getScrollTop() /*-{
    return $doc.body.scrollTop;
  }-*/;
  
  /**
   * Returns scroll left of the document body
   */
  public static native int getScrollLeft() /*-{
    return $doc.body.scrollLeft;
  }-*/;
  
  public static int getOffsetLeft(Widget widget) {
    return DOM.getIntAttribute(widget.getElement(), "offsetLeft");
  }
  
  public static int getOffsetTop(Widget widget) {
    return DOM.getIntAttribute(widget.getElement(), "offsetTop");
  }
  
  /**
   * Returns true if o1.equals(o2), but null-safe
   */
  public static boolean nullSafeEquals(Object o1, Object o2) {
    if (o1 == null) {
      return o2 == null;
    } else {
      return o1.equals(o2);
    }
  }

  /**
   * Sets the style attribute of a td element
   */
  public static void setCellStyleAttribute(HTMLTable table, int row, int col, String name, String value) {
    DOM.setStyleAttribute(table.getCellFormatter().getElement(row, col), name, value);
  }

  /**
   * Sets an attribute of a td element
   */
  public static void setCellAttribute(HTMLTable table, int row, int col, String name, String value) {
    DOM.setAttribute(table.getCellFormatter().getElement(row, col), name, value);    
  }
  
  /**
   * Returns a String that is the delimiter-separated representation
   * of the objects in String form
   */
  public static String join(List objects, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    
    for (int i=0; i<objects.size(); i++) {
      buffer.append(objects.get(i).toString());
      if (i < objects.size() - 1) {
        buffer.append(delimiter);
      }
    }
    
    return buffer.toString();
  }
  
  /**
   * Creates a new list
   */
  public static List newList() {
    return new ArrayList();
  }

  /**
   * Creates a list filled with the argument objects
   */
  public static List newList(Object[] objects) {
    List list = newList();
    for (int i=0; i<objects.length; i++) {
      list.add(objects[i]);
    }
    return list;
  }

  /**
   * Creates a new empty Map
   */
  public static Map newMap() {
    return new HashMap();
  }

  /**
   * Creates a useless ClickListener that Window.alerts "Unimplemented!"
   * when invoked.
   */
  public static ClickListener createUselessClickListener() {
    return new ClickListener() {
      public void onClick(Widget sender) {
        Window.alert("Unimplemented!");
      }
    };
  }
}