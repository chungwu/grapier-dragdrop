package com.grapier.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.grapier.gwt.client.ui.dragdrop.ArrangeableTable;
import com.grapier.gwt.client.ui.dragdrop.ConstrainedDragMoveListener;
import com.grapier.gwt.client.ui.dragdrop.DragAwareWidget;
import com.grapier.gwt.client.ui.dragdrop.DragDropListener;
import com.grapier.gwt.client.ui.dragdrop.DragMoveListener;
import com.grapier.gwt.client.ui.dragdrop.DragPositionMoveListener;
import com.grapier.gwt.client.ui.dragdrop.ArrangeableTable.SqueezeStrategy;

public class DragDrop implements EntryPoint {

  private ArrangeableTable table;
  private int numWidgets = 0;

  public void onModuleLoad() {
    RootPanel.get().add(createImageDragPanel());
    RootPanel.get().add(createDragDropPanel());
    RootPanel.get().add(createConstrainedMovePanel());
    RootPanel.get().add(createArrangeablePanel());
  }
  
  private Panel createImageDragPanel() {
    VerticalPanel panel = new VerticalPanel();
    panel.add(new HTML("<strong>Drag this image anywhere in the screen!</strong>"));

    
    DragAwareWidget w = new DragAwareWidget(new Image("dragdrop.jpg"));
    DOM.setStyleAttribute(w.getElement(), "position", "absolute");
    DOM.setStyleAttribute(w.getElement(), "cursor", "pointer");
    DOM.setStyleAttribute(w.getElement(), "top", "30px");
    w.addDragListener(new DragMoveListener(w));
    panel.add(w);
    panel.setHeight("400px");
    
    return panel;
  }
  private Panel createDragDropPanel() {
    final HorizontalPanel panel = new HorizontalPanel();        
    panel.setSpacing(10);
    panel.add(new HTML("<strong>Drop text into a box!</strong>"));
    
    final DragAwareWidget w = new DragAwareWidget(new Label("Drag me into a box!"));
    
    final SimplePanel s1 = new SimplePanel();
    s1.setWidth("100px");
    s1.setHeight("100px");
    DOM.setStyleAttribute(s1.getElement(), "border", "1px solid black");
    
    final SimplePanel s2 = new SimplePanel();
    s2.setWidth("100px");
    s2.setHeight("100px");
    
    panel.add(s1);
    panel.add(s2);
    panel.add(w);
    DOM.setStyleAttribute(s2.getElement(), "border", "1px solid black");
    
    List targets = new ArrayList();
    targets.add(s1);
    targets.add(s2);
    w.addDragListener(new DragPositionMoveListener(w));
    w.addDragListener(new DragDropListener(targets, new DragDropListener.WidgetHandler() {
      public void handleDropWidget(Widget widget) {
        if (widget != null) {
          ((SimplePanel) widget).setWidget(w);
        } else if (widget == null && panel.getWidgetIndex(w) == -1){
          panel.add(w);
        }
        DOM.setStyleAttribute(s1.getElement(), "border", "1px solid black");
        DOM.setStyleAttribute(s2.getElement(), "border", "1px solid black");
      }

      public void handleHoverWidget(Widget widget) {
        DOM.setStyleAttribute(s1.getElement(), "border", "1px solid black");
        DOM.setStyleAttribute(s2.getElement(), "border", "1px solid black");
        
        if (widget != null) {
          DOM.setStyleAttribute(widget.getElement(), "border", "2px solid blue");
        }
      }
    }));
    
    
    return panel;
  }
  
  private Panel createArrangeablePanel() {
    VerticalPanel panel = new VerticalPanel();
    
    panel.add(new HTML("<strong>Arrange widgets in this table!  Set your options, and click 'Create new table'.  Click 'Add widget' to add widgets to the table.  Drag widgets around the table to reposition them!</strong>"));
    
    HorizontalPanel options = new HorizontalPanel();
    options.setSpacing(10);
    final TextBox minRowBox = new TextBox();
    minRowBox.setText("2");
    minRowBox.setWidth("20px");
    final TextBox minColBox = new TextBox();
    minColBox.setText("2");
    minColBox.setWidth("20");
    final CheckBox compactVertical = new CheckBox("Compact Vertically");
    compactVertical.setChecked(true);
    final CheckBox compactHorizontal = new CheckBox("Compact Horizontally");
    compactHorizontal.setChecked(true);
    final ListBox squeezeBox = new ListBox();
    squeezeBox.addItem("Swap");
    squeezeBox.addItem("Vertically");
    squeezeBox.addItem("Horizontally");
    squeezeBox.setSelectedIndex(0);
    
    final SimplePanel container = new SimplePanel();

    final Button add = new Button("Add widget");
    add.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        DragAwareWidget w = new DragAwareWidget(new HTML("Drag ME " + numWidgets + "!!"));
        table.addWidget(w, w);
        numWidgets++;
      }
    });
    Button create = new Button("Create new table");
    create.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        int index = squeezeBox.getSelectedIndex();
        ArrangeableTable.SqueezeStrategy strategy =
          (index == 0) ? ArrangeableTable.SqueezeStrategy.SWAP :
          (index == 1) ? ArrangeableTable.SqueezeStrategy.ONLY_VERTICAL :
          SqueezeStrategy.ONLY_HORIZONTAL;
        table = new ArrangeableTable(
            Integer.parseInt(minRowBox.getText()),
            Integer.parseInt(minColBox.getText()),
            compactVertical.isChecked(),
            compactHorizontal.isChecked(),
            strategy);
        numWidgets = 0;
        table.setCellHeight("50px");
        table.setCellWidth("50px");
        table.setBorderWidth(1);
        container.setWidget(table);
      }
    });
    options.add(new Label("Min Rows:"));
    options.add(minRowBox);
    options.add(new Label("Min Cols:"));
    options.add(minColBox);
    options.add(compactVertical);
    options.add(compactHorizontal);
    options.add(new Label("Squeeze Policy:"));
    options.add(squeezeBox);
    options.add(create);
    panel.add(options);
    panel.add(add);
    panel.add(container);
    
    return panel;
  }
  
  private Panel createConstrainedMovePanel() {
    AbsolutePanel panel = new AbsolutePanel();
    DragAwareWidget w = new DragAwareWidget(new HTML("Try dragging me out of this box!"));
    w.addDragListener(new ConstrainedDragMoveListener(w, panel));
    w.setWidth("100px");
    w.setHeight("100px");
    DOM.setStyleAttribute(w.getElement(), "position", "absolute");
    DOM.setStyleAttribute(w.getElement(), "borderWidth", "2px");
    DOM.setStyleAttribute(w.getElement(), "borderColor", "red");
    DOM.setStyleAttribute(w.getElement(), "borderStyle", "solid");
    
    panel.add(w);
    panel.setWidth("300px");
    panel.setHeight("200px");
    DOM.setStyleAttribute(panel.getElement(), "borderWidth", "2px");
    DOM.setStyleAttribute(panel.getElement(), "borderColor", "blue");
    DOM.setStyleAttribute(panel.getElement(), "borderStyle", "solid");
    DOM.setStyleAttribute(panel.getElement(), "position", "relative");
    return panel;
  }
}
