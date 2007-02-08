package com.grapier.gwt.client.ui.dragdrop;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public class ConstrainedDragMoveListener extends DragMoveListener {

  private AbsolutePanel constrainingWidget;
  
  public ConstrainedDragMoveListener(Widget widget, AbsolutePanel constrainingWidget) {
    super(widget);
    this.constrainingWidget = constrainingWidget;
  }

  protected void setAbsolutePosition(int left, int top) {
    int minLeft = 0;
    int maxRight = minLeft + constrainingWidget.getOffsetWidth();
    int minTop = 0;
    int maxBottom = minTop + constrainingWidget.getOffsetHeight();
    
    int constrainedLeft = Math.min(Math.max(minLeft, left), maxRight - widget.getOffsetWidth());
    int constrainedTop = Math.min(Math.max(minTop, top), maxBottom - widget.getOffsetHeight());
    super.setAbsolutePosition(constrainedLeft, constrainedTop);
  }
}
