package com.grapier.gwt.client.ui.dragdrop;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.grapier.gwt.client.util.DomUtils;
import com.grapier.gwt.client.util.Position;

/**
 * A Grid where its elements can be arranged on the table via drag-and-drop.
 * You can configure the Grid with a minimum size.  As widgets are moved around
 * and pushed out of the way, the table may grow and shrink in size (but never
 * below the minimum).  You can further specify:
 * 
 * - whether all widgets in the table should be compacted vertically to the top
 * - whether all widgets in the table should be compacted horizontally to the left
 * - what to do when you try to move one widget to a spot with an existing widget
 * 
 * @author chungwu
 */
public class ArrangeableTable extends Grid {

  /**
   * strategy interface for deciding what to do when a widget in one position
   * is to replace another widget in a different position
   */
  public interface SqueezeStrategy {
    /**
     * @param oldPosition Position of the widget being moved from
     * @param newPosition Position of the widget you want to move to; that Position
     *   should already have an existing widget
     * @return Position that the existing, "squeezed" widget should move to to
     *   make way for this widget
     */
    public Position getSqueezedToPosition(ArrangeableTable table, Position oldPosition, Position newPosition);

    // always move the squeezed widget up if there's room, or down otherwise
    public static final SqueezeStrategy ONLY_VERTICAL = new SqueezeStrategy() {
      public Position getSqueezedToPosition(ArrangeableTable table, Position oldPosition, Position newPosition) {
        if (table.isRoomAbove(newPosition)) {
          return newPosition.subtractRow();
        } else {
          return newPosition.addRow();
        }
      }
    };
    
    // always move the squeezed widget left if there's room, or right otherwise
    public static final SqueezeStrategy ONLY_HORIZONTAL = new SqueezeStrategy() {
      public Position getSqueezedToPosition(ArrangeableTable table, Position oldPosition, Position newPosition) {
        if (table.isRoomToLeft(newPosition)) {
          return newPosition.subtractCol();
        } else {
          return newPosition.addCol();
        }
      }
    };
    
    // always swaps the squeezed widget with the moved widget
    public static final SqueezeStrategy SWAP = new SqueezeStrategy() {
      public Position getSqueezedToPosition(ArrangeableTable table, Position oldPosition, Position newPosition) {
        return oldPosition;
      }
    };
  }
  
  private boolean compactVertically;
  private boolean compactHorizontally;
  private int minRows;
  private int minCols;
  private SqueezeStrategy squeezeStrategy;
  private Map widgetPositionMap;
  private String shadowWidgetStyleName;
  private String cellHeight;
  private String cellWidth;

  private Widget draggedWidget;
  private SimplePanel shadowWidget;
  private String oldWidth;
  private String oldHeight;
  
  /**
   * @gwt.typeArgs <com.grapier.gwt.client.ui.dragdrop.ArrangeableTable.DropTarget>
   */
  private List dropTargets;

  /**
   * A DropTarget is a Widget wrapped around a cell "td" element.  It
   * remembers where it is in the grid.
   */
  private static class DropTarget extends Widget {
    private Position position;
    
    public DropTarget(int row, int col, Element tdElement, String width, String height) {
      this.setElement(tdElement);
      this.position = new Position(row, col);
      this.setWidth(width);
      this.setHeight(height);
    }
    
    public Position getPosition() {
      return position;
    }
  }
  
  /**
   * Constructs an ArrangeableTable
   * 
   * @param minRows minimum number of rows this grid will always at least have
   * @param minCols minimum number of columns this grid will always at least have
   * @param compactVertically if true, there will never be empty cells above widgets
   * @param compactHorizontally if true, there will never be empty cells to the left 
   *   of widgets
   * @param strategy a SqueezeStrategy; when you drag an widget to an existing
   *   widget, the strategy is used to determine where to "squeeze" the existing
   *   widget to.
   */
  public ArrangeableTable(int minRows, int minCols, boolean compactVertically, boolean compactHorizontally, SqueezeStrategy strategy) {
    super();
    this.minRows = minRows;
    this.minCols = minCols;
    this.compactVertically = compactVertically;
    this.compactHorizontally = compactHorizontally;
    this.squeezeStrategy = strategy;
    
    this.shadowWidgetStyleName = "drop-target-highlighted";
    this.cellHeight = "100%";
    this.cellWidth = "100%";
    
    this.dropTargets = DomUtils.newList();
    this.widgetPositionMap = DomUtils.newMap();
    
    this.resize(minRows, minCols);
  }
  
  public Widget getWidget(Position position) {
    return this.getWidget(position.getRow(), position.getCol());
  }
  
  /**
   * Returns the Position of the argument widget; null if the widget
   * cannot be found in the table
   */
  public Position findWidget(Widget widget) {
    return (Position) widgetPositionMap.get(widget);
  }

  /**
   * Returns the Position of the next empty cell; will return the
   * earliest empty cell encountered when iterating row by row,
   * column by column, in the obvious way.
   */
  public Position getNextEmptyCellPosition() {
    for (int r=0; r<getRowCount(); r++) {
      for (int c=0; c<getColumnCount(); c++) {
        if (isCellEmpty(r, c)) {
          return new Position(r, c);
        }
      }
    }
    return new Position(getRowCount(), 0);
  }
  
  /**
   * Adds a Widget to the next empty cell, which can be dragged around
   * by the argument handle.
   */
  public void addWidget(Widget widget, DragAwareWidget handle) {
    setWidget(widget, handle, getNextEmptyCellPosition());
  }

  /**
   * Adds a Widget to the specified position, which can be dragged around
   * by the argument handle.  Existing widgets at the same position will
   * be replaced.
   */
  public void setWidget(Widget widget, DragAwareWidget handle, Position position) {
    addDragListeners(widget, handle);
    setWidget(widget, position);
  }
  
  /**
   * Sets a Widget to the specified position.  Nothing will be done to make
   * it drag-movable.
   */
  public void setWidget(Widget widget, Position position) {
    ensurePositionExists(position);
    super.setWidget(position.getRow(), position.getCol(), widget);
    widgetPositionMap.put(widget, position);
  }

  /**
   * Sets a Widget to the specified position.  Nothing will be done to make
   * it drag-movable.
   */
  public void setWidget(int row, int col, Widget widget) {
    setWidget(widget, new Position(row, col));
  }
  
  /**
   * Removes the argument Widget.  Will also compact the table if you've
   * set compactHorizontally or compactVertically to be true.
   */
  public boolean remove(Widget widget) {
    boolean rc = this.removeWithoutCompacting(widget);
    if (rc) {
      compactWidgets();
      removeEmptyRowsAndColumnsFromFringes();
    }
    return rc;
  }
  
  /**
   * Sets the style of the shadow of the widget being dragged
   */
  public void setShadowStyleName(String styleName) {
    shadowWidgetStyleName = styleName;
  }
  
  /**
   * Sets the width of each cell
   */
  public void setCellWidth(String width) {
    cellWidth = width;
    fillDropTargets();
  }

  /**
   * Sets the height of each cell
   */
  public void setCellHeight(String height) {
    cellHeight = height;
    fillDropTargets();
  }
  
  /**
   * Installs the handle, by which we can drag the widget around, with the
   * necessary drag listeners
   */
  private void addDragListeners(final Widget widget, DragAwareWidget handle) {    
    
    // add a drag listener to listen for the start of a drag
    handle.addDragListener(new DragAwareWidget.DragListener() {
      public void startDragging(int mouseX, int mouseY) {
        startDraggingWidget(widget);
      }
      public void drag(int mouseX, int mouseY, int deltaX, int deltaY) {
      }
      public void endDragging(int mouseX, int mouseY, int deltaX, int deltaY) {
      }
    });

    // add a drag listener that moves the widget
    handle.addDragListener(new DragMoveListener(widget));
    
    // add a drag listener that listens for hovering and drops over 
    // the DropTargets
    handle.addDragListener(new DragDropListener(dropTargets, new DragDropListener.WidgetHandler() {
      public void handleDropWidget(Widget t) {
        dropDraggedWidget();
      }

      public void handleHoverWidget(Widget t) {
        hoverOverDropTarget((DropTarget) t);
      }
    }));
  }

  /**
   * Prepares the widget for drag.
   */
  private void startDraggingWidget(Widget widget) {
    // we're going to create a shadowWidget, which is going to be placed in the
    // cell that this widget will be dropped into.  As the widget is dragged
    // around, then, the shadowWidget is a "shadow" of where the widget
    // will be.  Basically, we need the shadowWidget there because the table
    // cell's border freaks out when its content becomes absolutely positioned.
    shadowWidget = new SimplePanel();
    shadowWidget.addStyleName(shadowWidgetStyleName);
    shadowWidget.setHeight(widget.getOffsetHeight() + "px");
    shadowWidget.setWidth(widget.getOffsetWidth() + "px");
    
    styleWidgetForDrag(widget);
    
    Position curPos = findWidget(widget);

    // put the shadow widget into the table
    setWidget(shadowWidget, curPos);
    
    RootPanel.get().add(widget);
    
    draggedWidget = widget;
  }

  /**
   * Drops the dragged widget
   */
  private void dropDraggedWidget() {
    // remove the shadowWidget from the table, and insert the widget back
    // where the shadowWidget currently is
    Position position = findWidget(shadowWidget);
    this.removeWithoutCompacting(shadowWidget);

    styleWidgetForDrop(draggedWidget);
    this.setWidget(position.getRow(), position.getCol(), draggedWidget);
    
    shadowWidget = null;
    draggedWidget = null;
    
    // remove empty rows at the bottom for the table
    removeEmptyRowsAndColumnsFromFringes();
    compactWidgets();
  }

  /**
   * Hovers the dragged widget over the argument DropTarget, which will 
   * move the shadowWidget into that position
   */
  private void hoverOverDropTarget(DropTarget target) {
    if (target != null) {
      // move the shadow widget into the argument table cell
      moveShadowWidgetAndCompact(target.getPosition());          
    }
  }
  
  /**
   * Moves the shadow widget into a position, and compacts the table
   */
  private void moveShadowWidgetAndCompact(Position newPosition) {
    Position oldPosition = findWidget(shadowWidget); 
    
    // if there's no move to be done, short circuit and quit
    if (oldPosition.equals(newPosition)) {
      return;
    }
    
    // first, move panel into the new position
    moveWidget(shadowWidget, oldPosition, newPosition);
    
    // compact the table
    compactWidgets();
  }
  
  /**
   * Compacts the table, vertically or horizontally, if 
   * compactVertically or compactHorizontally were set to true
   *
   */
  private void compactWidgets() {
    if (compactVertically) {
      compactVertically();
    }
    
    if (compactHorizontally) {
      compactHorizontally();
    }
  }
  
  private void compactVertically() {
    for (int r=0; r<getRowCount(); r++) {
      for (int c=0; c<getColumnCount(); c++) {
        if (!isCellEmpty(r, c)) {
          Position newPosition = findTopMostEmptyPositionAbove(r, c);
          if (newPosition != null) {
            Position oldPosition = new Position(r, c);
            Widget widget = getWidget(oldPosition);
            if (!widget.equals(shadowWidget)) {
              moveWidget(widget, oldPosition, newPosition);
            }
          }
        }
      }
    }
  }

  private Position findTopMostEmptyPositionAbove(int row, int column) {
    for (int r=0; r<row; r++) {
      if (isCellEmpty(r, column)) {
        return new Position(r, column);
      }
    }
    return null;
  }
  
  private void compactHorizontally() {
    for (int r=0; r<getRowCount(); r++) {
      for (int c=0; c<getColumnCount(); c++) {
        if (!isCellEmpty(r, c)) {
          Position newPosition = findLeftMostEmptyPositionToLeftOf(r, c);
          if (newPosition != null) {
            Position oldPosition = new Position(r, c);
            Widget widget = getWidget(oldPosition);
            if (!widget.equals(shadowWidget)) {
              moveWidget(widget, oldPosition, newPosition);
            }
          }
        }
      }
    }
  }
  
  private Position findLeftMostEmptyPositionToLeftOf(int row, int col) {
    for (int c=0; c<col; c++) {
      if (isCellEmpty(row, c)) {
        return new Position(row, c);
      }
    }
    return null;
  } 

  /**
   * Moves a widget into newPosition from oldPosition to newPosition.  If there's
   * already a widget in newPosition, that widget is pushed away according to
   * the SqueezeStrategy.
   */
  private void moveWidget(Widget widget, Position oldPosition, Position newPosition) {
    ensurePositionExists(newPosition);
    
    int newRow = newPosition.getRow();
    int newCol = newPosition.getCol();

    // the order is important!  First, remove the widget we want to move
    this.removeWithoutCompacting(widget);

    // next, save the exisiting widget in newPosition
    Widget widgetToPushAway = this.getWidget(newRow, newCol);
    
    // now we can go ahead and place the widget into the newPosition
    this.setWidget(newRow, newCol, widget);
    
    if (widgetToPushAway != null) {
      // if there was an existing widget, then ask the squeezeStrategy where to put
      // it and move it there
      Position squeezedPosition = squeezeStrategy.getSqueezedToPosition(this, oldPosition, newPosition);
      moveWidget(widgetToPushAway, newPosition, squeezedPosition);
    }
  }
  
  /**
   * Resizes the grid so that newPosition fits within its boundaries
   */
  private void ensurePositionExists(Position newPosition) {
    if (newPosition.getRow() >= getRowCount()) {
      resizeRows(newPosition.getRow() + 1);
    }
    
    if (newPosition.getCol() >= getColumnCount()) {
      resizeColumns(newPosition.getCol() + 1);
    }
  }

  /**
   * Removes the widget from the table without compacting the table
   */
  private boolean removeWithoutCompacting(Widget widget) {
    Position pos = findWidget(widget);
    boolean rc = super.remove(widget);
    if (rc) {
      widgetPositionMap.remove(widget);
      this.setHTML(pos.getRow(), pos.getCol(), "&nbsp;");
    }
    return rc;
  }
  
  /**
   * Returns true if there's an empty cell in the same column in a
   * row above (row, col)
   */
  private boolean isRoomAbove(Position position) {
    int row = position.getRow();
    int col = position.getCol();
    for (int r=row-1; r>=0; r--) {
      if (isCellEmpty(r, col)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns true if there's an empty cell in the same column in a
   * row above (row, col)
   */
  private boolean isRoomToLeft(Position position) {
    int row = position.getRow();
    int col = position.getCol();
    for (int c=col-1; c>=0; c--) {
      if (isCellEmpty(row, c)) {
        return true;
      }
    }
    return false;
  }
  
  private void removeEmptyRowsAndColumnsFromFringes() {
    removeEmptyRowsFromBottom();
    removeEmptyColumnsFromRight();
  }
  
  /**
   * Remove empty rows at bottom of the table
   */
  private void removeEmptyRowsFromBottom() {
    int rowsToRemove = 0;
    for (int r=getRowCount()-1; r>=minRows; r--) {
      if (!isRowEmpty(r)) {
        break;
      }
      
      rowsToRemove++;
    }
    
    if (rowsToRemove > 0) {
      resizeRows(getRowCount() - rowsToRemove);
    }
  }
  
  /**
   * Removes empty columns to the right of the table
   */
  private void removeEmptyColumnsFromRight() {
    int colsToRemove = 0;
    for (int c=getColumnCount()-1; c>=minCols; c--) {
      if (!isColEmpty(c)) {
        break;
      }
      
      colsToRemove++;
    }
    
    if (colsToRemove > 0) {
      resizeColumns(getColumnCount() - colsToRemove);
    }
  }
  
  /**
   * When we resize rows, we need to refresh our dropTargets list
   * to have all the table cells
   */
  public void resizeRows(int rows) {
    super.resizeRows(rows);
    fillDropTargets();
  }

  /**
   * When we resize cols, we need to refresh our dropTargets list
   * to have all the table cells
   */
  public void resizeColumns(int cols) {
    super.resizeColumns(cols);
    fillDropTargets();
  }
  
  /**
   * Clears and recreates the dropTargets list
   */
  private void fillDropTargets() {
    dropTargets.clear();
    for (int r=0; r<getRowCount(); r++) {
      for (int c=0; c<getColumnCount(); c++) {
        DropTarget target = 
          new DropTarget(
              r, c, getCellFormatter().getElement(r, c), cellWidth, cellHeight);
        dropTargets.add(target);
      }
    }
  }
  
  private boolean isRowEmpty(int row) {
    for (int c=0; c<getColumnCount(); c++) {
      if (!isCellEmpty(row, c)) {
        return false;
      }
    }    
    return true;
  }
  
  private boolean isColEmpty(int col) {
    for (int r=0; r<getRowCount(); r++) {
      if (!isCellEmpty(r, col)) {
        return false;
      }
    }    
    return true;
  }
  
  public boolean isCellPresent(int row, int col) {
    return row >= 0 && row < getRowCount() && col >= 0 && col < getColumnCount();
  }

  /**
   * Returns true if the cell at (row, col) doesn't have an existing widget
   */
  private boolean isCellEmpty(int row, int col) {
    if (!isCellPresent(row, col)) {
      return false;
    }
    
    return this.getWidget(row, col) == null;
  }
  
  private void styleWidgetForDrag(final Widget widget) {
    oldWidth = DOM.getStyleAttribute(widget.getElement(), "width");
    oldHeight = DOM.getStyleAttribute(widget.getElement(), "height");
    
    DOM.setStyleAttribute(widget.getElement(), "left", widget.getAbsoluteLeft() + "px");
    DOM.setStyleAttribute(widget.getElement(), "top", widget.getAbsoluteTop() + "px");
    DOM.setStyleAttribute(widget.getElement(), "width", widget.getOffsetWidth() + "px");
    DOM.setStyleAttribute(widget.getElement(), "height", widget.getOffsetHeight() + "px");
    DOM.setStyleAttribute(widget.getElement(), "position", "absolute");
  }

  private void styleWidgetForDrop(final Widget widget) {
    DOM.setStyleAttribute(widget.getElement(), "position", "relative");
    DOM.setStyleAttribute(widget.getElement(), "left", "0px");
    DOM.setStyleAttribute(widget.getElement(), "top", "0px");
    DOM.setStyleAttribute(widget.getElement(), "width", oldWidth);
    DOM.setStyleAttribute(widget.getElement(), "height", oldHeight);
  }
}
