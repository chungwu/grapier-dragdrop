package com.grapier.gwt.client.ui.dragdrop;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.grapier.gwt.client.ui.dragdrop.DragAwareWidget.DragListener;
import com.grapier.gwt.client.util.DomUtils;

/**
 * A DragListener that keeps track of whether a drag is hovering over
 * a widget from a Collection of widgets, and whether the drag ends on
 * any specific widget.  Useful when you need to drag something onto
 * a collection of things.
 * 
 * You specify a DragDropListener.WidgetHandler to handle events when
 * we hover over a widget or drop onto a widget.
 *  
 * @author chungwu
 */
public class DragDropListener implements DragListener {

  public interface WidgetHandler {
    /**
     * A drag is hovering over the argument widget; null if none.  Will only
     * be called if a drag is hovering over a different widget from the last
     * time it was called.
     */
    public void handleHoverWidget(Widget widget);
    
    /**
     * A drag is ended on the argument widget; null if none.
     */
    public void handleDropWidget(Widget widget);
  }

  // we don't want to check widget-hovering on every mouse event
  // because that's expensive.  Instead, we do it once every
  // INTERVAL_BETWEEN_CHECKS events.
  private static final int INTERVAL_BETWEEN_CHECKS = 10;
  
  /**
   * @gwt.typeArgs <com.google.gwt.user.client.ui.Widget>
   */
  private List dropTargets;
  private int timesSinceLastCheck;
  private WidgetHandler widgetHandler;
  private Widget activeTarget;
  
  /**
   * @param dropTargets a Collection of Widgets that you may drag
   *   over and drop onto 
   *   
   * @gwt.typeArgs dropTargets <com.google.gwt.user.client.ui.Widget>
   */
  public DragDropListener(List dropTargets, WidgetHandler widgetHandler) {
    this.dropTargets = dropTargets;
    this.widgetHandler = widgetHandler;
    this.timesSinceLastCheck = 0;
    this.activeTarget = null;
  }
  
  public void drag(int mouseX, int mouseY, int deltaX, int deltaY) {
    // we wait for INTERVAL_BETWEEN_CHECKS drag events, and then we check
    // whether we're hovering over a widget
    if (timesSinceLastCheck > INTERVAL_BETWEEN_CHECKS) {
      Widget target = findHoverWidget(mouseX, mouseY);
      if (activeTarget != target) {
        activeTarget = target;
        widgetHandler.handleHoverWidget(target);
      }
      timesSinceLastCheck = 0;
    } else {
      timesSinceLastCheck++;
    }
  }

  public void endDragging(int mouseX, int mouseY, int deltaX, int deltaY) {
    // fire a handleDrop event to the widgetHandler
    widgetHandler.handleDropWidget(findHoverWidget(mouseX, mouseY));
    activeTarget = null;
  }

  public void startDragging(int mouseX, int mouseY) {
    
  }
  
  /**
   * Finds the Widget, from dropTargets, that includes (mouseX, mouseY).
   * Returns null if none does.
   */
  private Widget findHoverWidget(int mouseX, int mouseY) {
    // we calculate the mouse coordinates with respect to the top-left
    // corner of the page, rather than top-left corner of the screen
    // (essentially, taking scroll left/top into account)

    int x = mouseX + DomUtils.getScrollLeft();
    int y = mouseY + DomUtils.getScrollTop();
    
    if (activeTarget != null && inWidget(x, y, activeTarget)) {
      return activeTarget;
    }
    
    for (int i=0; i<dropTargets.size(); i++) {
      Widget widget = (Widget) dropTargets.get(i);
      if (inWidget(x, y, widget)) {
        return widget;
      }
    }    
    return null;
  }
  
  /**
   * Returns true if (absX, absY), in absolute coordinates, lies within
   * the absolute boundaries of widget
   */
  private boolean inWidget(int absX, int absY, Widget widget) {
    return inRectangle(
        absX, absY, 
        widget.getAbsoluteLeft(), widget.getAbsoluteTop(), 
        widget.getOffsetWidth(), widget.getOffsetHeight());
  }
  
  /**
   * Returns true if (absX, absY) lies within the rectangle
   * (left, top)+(width, height).  All in absolute coordinates.
   */
  private boolean inRectangle(int absX, int absY, int left, int top, int width, int height) {
    return absX > left && absX < left+width && absY > top && absY < top+height;
  }
}
