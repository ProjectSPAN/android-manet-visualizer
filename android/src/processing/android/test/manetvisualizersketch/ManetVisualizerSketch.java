package processing.android.test.manetvisualizersketch;

import processing.core.*; 

import apwidgets.*; 

import apwidgets.*; 

import android.view.MotionEvent; 
import android.view.KeyEvent; 
import android.graphics.Bitmap; 
import java.io.*; 
import java.util.*; 

public class ManetVisualizerSketch extends PApplet {

 //Imports




//Control Variables
APWidgetContainer widgetContainer; 
APButton btn_refresh;

//Data Variables
Graph g=null;
ArrayList<Node> nodes;
Node focus;

//Other Variables
int padding=30;

//Set up initial state
public void setup() { 
  nodes = new ArrayList<Node>();
 
  //size(500, 300);
  //build controls
  widgetContainer = new APWidgetContainer(this); //create new container for widgets
  btn_refresh = new APButton(10, 10, 100, 50, "Refresh"); 
  widgetContainer.addWidget(btn_refresh); //place button in container


  frameRate(24);
  noLoop();

  //build graph
  makeGraph();
  redraw();
} 

public void onClickWidget(APWidget widget) {
  if (widget == btn_refresh) {
    makeGraph(); //set the smaller size
  }
}

  public void draw() { 
    background(255);
    fill(150, 150, 150);
    stroke(0);
    rect(0, 0, 200, height);
    boolean done = g.reflow();
    g.draw();
    if (!done) { 
      loop();
    } 
    else { 
      noLoop();
    }
  }

  public void makeGraph()
  {
    //this is where we need to get the manet info
    // define a graph
    nodes.clear();
    g = new Graph();

    // define some nodes
    Node n1 = new Node("node1", width/2, height/2);
    Node n2 = new Node("node2", (int)random(padding, width-padding), (int)random(padding, height-padding));
    Node n3 = new Node("node3", (int)random(padding, width-padding), (int)random(padding, height-padding));
    Node n4 = new Node("node4", (int)random(padding, width-padding), (int)random(padding, height-padding));
    Node n5 = new Node("node5", (int)random(padding, width-padding), (int)random(padding, height-padding));
    Node n6 = new Node("node6", (int)random(padding, width-padding), (int)random(padding, height-padding));

    nodes.add(n1);
    nodes.add(n2);
    nodes.add(n3);
    nodes.add(n4);
    nodes.add(n5);
    nodes.add(n6);

    focus = n4;
    n4.setFocus();

    // add nodes to graph
    g.addNode(n1);
    g.addNode(n2);
    g.addNode(n3);
    g.addNode(n4);
    g.addNode(n5);
    g.addNode(n6);

    // link nodes
    g.linkNodes(n1, n2);
    g.linkNodes(n2, n3);
    g.linkNodes(n3, n4);
    g.linkNodes(n4, n1);
    g.linkNodes(n1, n3);
    g.linkNodes(n2, n4);
    g.linkNodes(n5, n6);
    g.linkNodes(n1, n6);
    g.linkNodes(n2, n5);
  } 





// models a graph of nodes and bi-directional edges
class Graph
{
  ArrayList<Node> nodes = new ArrayList<Node>();

  public void addNode(Node node) {
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  public int size() { 
    return nodes.size();
  }

  public boolean linkNodes(Node n1, Node n2) {
    if (nodes.contains(n1) && nodes.contains(n2)) {
      n1.addEdge(n2); 
      return true;
    }
    return false;
  }

  public Node getNode(int index) {
    return nodes.get(index);
  }

  public ArrayList<Node> getNodes() {
    return nodes;
  }


  public boolean reflow() {
    int buffer = 20;
    int control_width = 200;
    double elasticity = 200.0f;
    double repulsion = -1000;
    double tension = .1f;

    int reset = 0;
    for (Node n: nodes)
    {
      ArrayList<Node> edges = n.getEdges();
      // compute the total push force acting on this node
      //all nodes push on it
      double fx=0;
      double fy=0;
      for (Node n2: nodes) {
        if (!n.equals(n2)) {
          //Compute push force
          int deltaX = n2.x - n.x;
          int deltaY = n2.y - n.y;
          double R = sqrt(deltaX*deltaX + deltaY*deltaY);
          double F = repulsion / (R * R);
          fx += F * deltaX / R;
          fy += F * deltaY / R;
        }
      }

      //all edges pull on it
      for (Node ne:edges) {
        //compute pull force
        int deltaX = ne.x - n.x;
        int deltaY = ne.y - n.y;
        double R = sqrt(deltaX*deltaX + deltaY*deltaY);
        double F = tension;
        fx += F * deltaX / R;
        fy += F * deltaY / R;
      }
      //verify that motion follows constraints and move node
      n.x += fx;
      n.y += fy;
      if (n.x<control_width + buffer) { 
        n.x=control_width + buffer;
      } 
      else if (n.x>width-buffer) { 
        n.x=width-buffer;
      } // don't stray into menu or offscreen

      if (n.y<0+buffer) { 
        n.y=0+buffer;
      } 
      else if (n.y>height - buffer) { 
        n.y=height - buffer;
      } //don't stray offscreen
    }
    return false;
  }

  // draw nodes
  public void draw() {
    for (Node n: nodes) {
      n.drawEdges();
    }
    for (Node n: nodes) {
      n.draw();
    }
  }
}


class Node
{
  ArrayList<Node> edges = new ArrayList<Node>();
  String label;
  boolean focus = false;

  Node(String _label, int _x, int _y) {
    label=_label; 
    x=_x; 
    y=_y; 
    r1=5; 
    r2=5;
  }

  public String getLabel() {
    return label;
  }

  public void setFocus() {
    focus = true;
  }

  public void unsetFocus() {
    focus=false;
  }

  public void addEdge(Node n) {
    if (!edges.contains(n)) {
      edges.add(n);
      n.addEdge(this);
    }
  }

  public ArrayList<Node> getEdges() {
    return edges;
  }

  public int getEdgesCount() {
    return edges.size();
  }



  public boolean equals(Node other) {
    if (this==other) return true;
    return label.equals(other.label);
  }

  // visualisation-specific
  int x=0;
  int y=0;
  int r1=20;
  int r2=20;

  public void setPosition(int _x, int _y) {
    x=_x; 
    y=_y;
  }

  public void setRadii(int _r1, int _r2) {
    r1=_r1; 
    r2=_r2;
  }

  public void draw() {
    if (focus) {
      fill(0, 0, 255);
    }
    else {
      fill(255);
    }
    ellipse(x, y, r1*2, r2*2);
    fill(50, 50, 255);
    text(label, x+r1*2, y+r2*2);
  }

  public void drawEdges() {
    stroke(0);
    strokeWeight(4);
    for (Node e: edges) {
      line(x, y, e.x, e.y);
    }
    strokeWeight(1);
  }
  
}

/**
 * Simmple graph layout system
 * http://processingjs.nihongoresources.com/graphs
 * (c) Mike "Pomax" Kamermans 2011
 */

// =============================================
//      Some universal helper functions
// =============================================

// universal helper function: get the angle (in radians) for a particular dx/dy
public float getDirection(double dx, double dy) {
  // quadrant offsets
  double d1 = 0.0f;
  double d2 = PI/2.0f;
  double d3 = PI;
  double d4 = 3.0f*PI/2.0f;
  // compute angle basd on dx and dy values
  double angle = 0;
  float adx = abs((float)dx);
  float ady = abs((float)dy);
  // Vertical lines are one of two angles
  if(dx==0) { angle = (dy>=0? d2 : d4); }
  // Horizontal lines are also one of two angles
  else if(dy==0) { angle = (dx>=0? d1 : d3); }
  // The rest requires trigonometry (note: two use dx/dy and two use dy/dx!)
  else if(dx>0 && dy>0) { angle = d1 + atan(ady/adx); }		// direction: X+, Y+
  else if(dx<0 && dy>0) { angle = d2 + atan(adx/ady); }		// direction: X-, Y+
  else if(dx<0 && dy<0) { angle = d3 + atan(ady/adx); }		// direction: X-, Y-
  else if(dx>0 && dy<0) { angle = d4 + atan(adx/ady); }		// direction: X+, Y-
  // return directionality in positive radians
  return (float)(angle + 2*PI)%(2*PI); }

// universal helper function: rotate a coordinate over (0,0) by [angle] radians
public int[] rotateCoordinate(float x, float y, float angle) {
  int[] rc = {0,0};
  rc[0] = (int)(x*cos(angle) - y*sin(angle));
  rc[1] = (int)(x*sin(angle) + y*cos(angle));
  return rc; }

// universal helper function for Processing.js - 1.1 does not support ArrayList.addAll yet
public void addAll(ArrayList a, ArrayList b) { for(Object o: b) { a.add(o); }}

  public int sketchWidth() { return screenWidth; }
  public int sketchHeight() { return screenHeight; }
  public String sketchRenderer() { return P3D; }
}
