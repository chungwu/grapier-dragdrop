package com.grapier.gwt.client.ui.dragdrop;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.grapier.gwt.client.ui.dragdrop.DragAwareWidget.DragListener;

public class DragPositionMoveListener implements DragListener {

  private Widget widget;
  private DragMoveListener dragMoveListener;
  private String oldPosition;
  private String oldTop;
  private String oldLeft;
  private String oldWidth;
  private String oldHeight;
  
  public DragPositionMoveListener(Widget widget) {
    this(widget, null);
  }
  
  public DragPositionMoveListener(Widget widget, AbsolutePanel constrainingWidget) {
    this.widget = widget;
    
    if (constrainingWidget == null) {    
      this.dragMoveListener = new DragMoveListener(widget);
    } else {
      this.dragMoveListener = new ConstrainedDragMoveListener(widget, constrainingWidget);
    }
  }
  
  public void drag(int mouseX, int mouseY, int deltaX, int deltaY) {
    dragMoveListener.drag(mouseX, mouseY, deltaX, deltaY);
  }

  public void endDragging(int mouseX, int mouseY, int deltaX, int deltaY) {
    dragMoveListener.endDragging(mouseX, mouseY, deltaX, deltaY);
    
    Element element = widget.getElement();
    DOM.setStyleAttribute(element, "left", oldLeft);
    DOM.setStyleAttribute(element, "top", oldTop);
    DOM.setStyleAttribute(element, "width", oldWidth);
    DOM.setStyleAttribute(element, "height", oldHeight);
    DOM.setStyleAttribute(element, "position", oldPosition);
  }

  public void startDragging(int mouseX, int mouseY) {
    Element element = widget.getElement();
    oldPosition = DOM.getStyleAttribute(element, "position");
    oldTop = DOM.getStyleAttribute(element, "top");
    oldLeft = DOM.getStyleAttribute(element, "left");
    oldWidth = DOM.getStyleAttribute(element, "width");
    oldHeight = DOM.getStyleAttribute(element, "height");
    
    DOM.setStyleAttribute(element, "left", widget.getAbsoluteLeft() + "px");
    DOM.setStyleAttribute(element, "top", widget.getAbsoluteTop() + "px");
    DOM.setStyleAttribute(element, "width", widget.getOffsetWidth() + "px");
    DOM.setStyleAttribute(element, "height", widget.getOffsetHeight() + "px");
    DOM.setStyleAttribute(element, "position", "absolute");
    
    dragMoveListener.startDragging(mouseX, mouseY);
  }

}
