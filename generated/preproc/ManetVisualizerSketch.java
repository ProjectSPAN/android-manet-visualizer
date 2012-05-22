import processing.core.*; 
import processing.xml.*; 

import apwidgets.*; 
import android.adhoc.manet.system.*; 
import android.adhoc.manet.service.*; 
import android.adhoc.manet.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class ManetVisualizerSketch extends PApplet {



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
    double repulsion = -500;
    double tension = .01f;

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
    r1=10; 
    r2=10;
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


    static public void main(String args[]) {
        PApplet.main(new String[] { "--bgcolor=#ECE9D8", "ManetVisualizerSketch" });
    }
}
