package com.grapier.gwt.client.ui.dragdrop;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.grapier.gwt.client.ui.dragdrop.DragAwareWidget.DragListener;

/**
 * A DragListener that moves an absolute-positioned widget
 * according to drag
 * 
 * @author chungwu
 */
public class DragMoveListener implements DragListener {

  protected Widget widget;
  protected int originalX;
  protected int originalY;

  /**
   * @param widget absolute-positioned widget to move
   */
  public DragMoveListener(Widget widget) {
    this.widget = widget;
  }

  public void startDragging(int mouseX, int mouseY) {
    // remember the original positions
    originalX = DOM.getIntAttribute(widget.getElement(), "offsetLeft");
    originalY = DOM.getIntAttribute(widget.getElement(), "offsetTop");
  }
  
  public void drag(int mouseX, int mouseY, int deltaX, int deltaY) {
    setAbsolutePosition(originalX + deltaX, originalY + deltaY);
  }

  public void endDragging(int mouseX, int mouseY, int deltaX, int deltaY) {
    setAbsolutePosition(originalX + deltaX, originalY + deltaY);  
  }
  
  /**
   * Sets the absolute position of the widget.  We do it through style
   * so we can set position before the widget is added to the DOM tree. 
   */
  protected void setAbsolutePosition(int left, int top) {
    Element elem = widget.getElement();
    DOM.setStyleAttribute(elem, "left", left + "px");
    DOM.setStyleAttribute(elem, "top", top + "px");
  }  
}
