import processing.core.*; 
import processing.xml.*; 

import apwidgets.*; 
import java.util.TreeSet; 
import android.adhoc.manet.ManetObserver; 
import android.adhoc.manet.service.ManetService.AdhocStateEnum; 
import android.adhoc.manet.system.ManetConfig; 
import android.adhoc.manet.ManetHelper; 
import android.content.Context; 
import android.app.Activity; 
import android.widget.Toast; 

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

  public Node addNode(Node node) {
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
    return nodes.get(nodes.indexOf(node));
  }

  public int size() { 
    return nodes.size();
  }

public boolean contains(Node n){
  return nodes.contains(n);
}


public String toString(){
  String s = "BEGIN GRAPH\n";
  for(int i=0; i<nodes.size(); i++){
    s+=nodes.get(i).getLabel();
    if(nodes.get(i).isFocused()){
      s+=" *****";
    }
    s+="\n";
  }
s+="END GRAPH\n";
return s;
}

//from n1 to n2
  public void directedLink(Node n1, Node n2){
    Node n_1 = addNode(n1);
    Node n_2 = addNode(n2);
    n_1.addOutgoingEdge(n_2);
    n_2.addIncomingEdge(n_1);
  }
  
  public void bidirectionalLink(Node n1, Node n2){
    directedLink(n1, n2);
    directedLink(n2, n1);
  }

  public Node getNode(int index) {
    return nodes.get(index);
  }

public Node copyNode(String label){
  int i = nodes.indexOf(new Node(label));
  if(i < 0){
    Node n = new Node(label, (int)random(0, width), (int)random(0,height) );
    return n;
  }
  else{
    return nodes.get(i);
  }
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
      ArrayList<Node> edges = n.getAllEdges();
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

//focus a node
  public void focus(Node n){
    if(nodes.contains(n)){
    nodes.get(nodes.indexOf(n)).setFocus();
    }
    else{
      n.setFocus();
      nodes.add(n);
    }
  }
  
  
  // draw nodes
  public void draw() {
    for (Node n: nodes) {
      n.draw();
    }
    for (Node n: nodes) {
      n.drawEdges();
    }

  }
}


//Imports












//Control Variables
APWidgetContainer widgetContainer; 
APButton btn_refresh;

//Data Variables
ManetCommunicator mComm;
Graph g=null;
ArrayList<Node> nodes;
Node focus;

//Other Variables
int padding=30;

//Set up initial state
public void setup() { 

  mComm = new ManetCommunicator(this);

  nodes = new ArrayList<Node>();
  size(screenWidth, screenHeight, P3D);
  //size(500, 300);
  //build controls
  //Mcomm = new ManetCommunicator(this);
  widgetContainer = new APWidgetContainer(this); //create new container for widgets
  btn_refresh = new APButton(10, 10, 100, 50, "Refresh"); 
  widgetContainer.addWidget(btn_refresh); //place button in container


  // frameRate(24);
  noLoop();
  g=new Graph();
  //build graph
  makeGraph(null);
  redraw();
  //create thread to get routing info on a timer
  
  Thread updateThread = new Thread(){
    public void run() {
    while(true) {
      System.out.println("Performing Auto-update");
      mComm.getRoutingInfo();
      try{
      Thread.sleep(10 * 1000);
      }catch(Exception e){
        e.printStackTrace();
      }
    }
   }
  };
  updateThread.start();
} 



public void onClickWidget(APWidget widget) {
  if (widget == btn_refresh) {
    mComm.getRoutingInfo();
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

public void makeGraph(String data)
{
  Graph g_temp = new Graph();
  if (data != null) {
    String lines[] = data.split("\\r?\\n");
    int linksIndex = -1;
    int topologyIndex=-1;
    int ipIndex = -1;


    for (int i=0; i<lines.length; i++) {
      if (lines[i].contains("Table: Links") ) {
        linksIndex = i+2;
      }
      if (lines[i].contains("Table: Topology") ) {
        topologyIndex = i+2;
      }
      if (lines[i].contains("Table: Interfaces") ) {
        ipIndex = i+2;
      }
    }



    //Add links
    System.out.println("\nAdding nodes from Links");
    int index = linksIndex;
    while ( lines[index].length ()> 16) {
      int wsIndex = lines[index].indexOf(' ');
      int dsIndex = lines[index].indexOf(' ', wsIndex+6);
      String src = lines[index].substring(0, wsIndex);
      String dst = lines[index].substring(wsIndex+5, dsIndex);

      //Add the edge to graph
      System.out.println("Adding node: " + src + " <-> " + dst);
      Node srcN = g.copyNode(src);
      srcN.clearLinks();
      Node dstN = g.copyNode(dst);
      dstN.clearLinks();

      g_temp.bidirectionalLink(srcN, dstN);
      index++;
    }
    System.out.println("\nAdding nodes from Topology");
    index = topologyIndex;
    while ( lines[index].length () > 16) {
      int wsIndex = lines[index].indexOf(' ');
      int dsIndex = lines[index].indexOf(' ', wsIndex+6);
      String dst = lines[index].substring(0, wsIndex);
      String src = lines[index].substring(wsIndex+5, dsIndex);

      //Add the edge to graph
      System.out.println("Adding node: " + src + " --> " + dst);
      Node srcN = g.copyNode(src);
      srcN.clearLinks();
      Node dstN = g.copyNode(dst);
      dstN.clearLinks();

      g_temp.directedLink(srcN, dstN);
      index++;
    }

    //Focus this node
    int wsIdx = lines[ipIndex].indexOf(' ');
    wsIdx = lines[ipIndex].indexOf(' ', wsIdx + 6);
    wsIdx = lines[ipIndex].indexOf(' ', wsIdx + 6);
    wsIdx = lines[ipIndex].indexOf(' ', wsIdx + 6);
    int dsIdx = lines[ipIndex].indexOf(' ', wsIdx+6);

    String myIP = lines[ipIndex].substring(wsIdx+5, dsIdx);  
    System.out.println("My IP: " + myIP);
    Node thisN = g.copyNode(myIP);
    g_temp.focus(thisN);      
    g = g_temp;
  }


  /*
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
   
   g.bidirectionalLink(n1, n2);
   g.bidirectionalLink(n2, n3);
   g.bidirectionalLink(n3, n4);
   g.bidirectionalLink(n4, n1);
   g.directedLink(n1, n3);
   g.directedLink(n2, n4);
   g.directedLink(n5, n6);
   g.directedLink(n1, n6);
   g.directedLink(n2, n5);
   */
} 



class ManetCommunicator implements ManetObserver {
  ManetHelper helper;
  Context context;
  boolean connected;
  String info = null;

  public ManetCommunicator(Context c) {
    this.context = c;
    connected = false;
    Activity activity=(Activity) this.context;
    helper = new ManetHelper(this.context);
    activity.runOnUiThread(new Runnable() {
      public void run() {
        helper.registerObserver(ManetCommunicator.this);
        helper.connectToService();
        Toast.makeText((Activity)ManetCommunicator.this.context, "Connected to Manet Service", Toast.LENGTH_LONG).show();
      }
    }
    );
  }

  public void getRoutingInfo() {
    if (connected) {
      print("Get routing info...");
      Activity activity=(Activity) this.context;
      activity.runOnUiThread(new Runnable() {
        public void run() {
          Toast.makeText((Activity)ManetCommunicator.this.context, "Querying Service for Route Info", Toast.LENGTH_SHORT).show();
          helper.sendRoutingInfoQuery();
        }
      }
      );
    }
  }


  public void onAdhocStateUpdated(AdhocStateEnum arg0, String arg1) {
    // TODO Auto-generated method stub
  }

  public void onConfigUpdated(ManetConfig arg0) {
    // TODO Auto-generated method stub
  }

  public void onError(String arg0) {
    // TODO Auto-generated method stub
  }

  public void onPeersUpdated(TreeSet<String> arg0) {
    // TODO Auto-generated method stub
  }

  public void onRoutingInfoUpdated(String arg0) {
    // TODO Auto-generated method stub

    if (arg0 == null) {
      Activity activity=(Activity) this.context;
      activity.runOnUiThread(new Runnable() {
        public void run() {
          Toast.makeText((Activity)ManetCommunicator.this.context, "Error: Device not in adhoc mode", Toast.LENGTH_SHORT).show();
        }
      }
      );
    }
    //print("Recieved routing info: " + arg0+"\n-------------------------");
    makeGraph(arg0);
  }

  public void onServiceConnected() {
    // TODO Auto-generated method stub
    connected = true;
    print("Connected!");
  }

  public void onServiceDisconnected() {
    connected = false;
    // TODO Auto-generated method stub
  }

  public void onServiceStarted() {
    // TODO Auto-generated method stub
  }

  public void onServiceStopped() {
    // TODO Auto-generated method stub
  }
}



class Node
{
  ArrayList<Node> incomingEdges = new ArrayList<Node>();
  ArrayList<Node> outgoingEdges = new ArrayList<Node>();
  String label;
  boolean focus = false;

  Node(String _label, int _x, int _y) {
    label=_label; 
    x=_x; 
    y=_y; 
    r1=10; 
    r2=10;
  }
  
  Node(String _label){
    label = _label;
    x = (int)random(0, width);
    y = (int)random(0, height);

    r1=10; 
    r2=10;
  }

  public String getLabel() {
    return label;
  }
  
  public void clearLinks(){
    incomingEdges.clear();
    outgoingEdges.clear();
  }

public boolean isFocused(){
  return this.focus;
}

  public void setFocus() {
    focus = true;
  }

  public void unsetFocus() {
    focus=false;
  }

  public void addIncomingEdge(Node n) {
    if (!incomingEdges.contains(n)) {
      incomingEdges.add(n);
    }
  }
  
    public void addOutgoingEdge(Node n) {
    if (!outgoingEdges.contains(n)) {
      outgoingEdges.add(n);
    }
  }

  public ArrayList<Node> getAllEdges() {
    ArrayList<Node> edges = new ArrayList<Node>();
    for(int i=0; i<incomingEdges.size(); i++){
      if(!edges.contains(incomingEdges.get(i))){
        edges.add(incomingEdges.get(i));
      }
    }
    for(int i=0; i<outgoingEdges.size(); i++){
      if(!edges.contains(outgoingEdges.get(i))){
        edges.add(outgoingEdges.get(i));
      }
    }
    
    return edges;
  }



  public boolean equals(Object other) {
    if(other == null) return false;
    Node on = (Node)other;
    if (this==on) return true;
    return label.equals(on.label);
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
  
  public String toString(){
    return "Node: " + label;
  }

  public void drawEdges() {
    stroke(0);
    fill(0);
    strokeWeight(4);
    for (Node e: incomingEdges) {
      arrowLine((float)x, (float)y, (float)e.x, (float)e.y, radians(20), 0, true);
    }
    for(Node e: outgoingEdges){
      arrowLine(e.x, e.y, x, y, radians(20), 0, true);  
    }
    strokeWeight(1);
  }
  
  
  
  
  
  /*
 *  "Arrows" by J David Eisenberg, licensed under Creative Commons Attribution-Share Alike 3.0 and GNU GPL license.
 *  Work: http://openprocessing.org/visuals/?visualID= 7029
 *  License:
 *  http://creativecommons.org/licenses/by-sa/3.0/
 *  http://creativecommons.org/licenses/GPL/2.0/
 *
 *  Edited 6/3/2012 By Nick Modly
 *
 */
 
  /*
   * Draws a lines with arrows of the given angles at the ends.
   * x0 - starting x-coordinate of line
   * y0 - starting y-coordinate of line
   * x1 - ending x-coordinate of line
   * y1 - ending y-coordinate of line
   * startAngle - angle of arrow at start of line (in radians)
   * endAngle - angle of arrow at end of line (in radians)
   * solid - true for a solid arrow; false for an "open" arrow
   */
  public void arrowLine(float x0, float y0, float x1, float y1, 
  float startAngle, float endAngle, boolean solid)
  {
    line(x0, y0, x1, y1);
    if (startAngle != 0)
    {
      arrowhead(x0, y0, atan2(y1 - y0, x1 - x0), startAngle, solid);
    }
    if (endAngle != 0)
    {
      arrowhead(x1, y1, atan2(y0 - y1, x0 - x1), endAngle, solid);
    }
  }

  /*
 * Draws an arrow head at given location
   * x0 - arrow vertex x-coordinate
   * y0 - arrow vertex y-coordinate
   * lineAngle - angle of line leading to vertex (radians)
   * arrowAngle - angle between arrow and line (radians)
   * solid - true for a solid arrow, false for an "open" arrow
   */
  public void arrowhead(float x0, float y0, float lineAngle, 
  float arrowAngle, boolean solid)
  {
    float phi;
    float x2;
    float y2;
    float x3;
    float y3;
    final float SIZE = 16;

    x2 = x0 + SIZE * cos(lineAngle + arrowAngle);
    y2 = y0 + SIZE * sin(lineAngle + arrowAngle);
    x3 = x0 + SIZE * cos(lineAngle - arrowAngle);
    y3 = y0 + SIZE * sin(lineAngle - arrowAngle);
    if (solid)
    {
      triangle(x0, y0, x2, y2, x3, y3);
    }
    else
    {
      line(x0, y0, x2, y2);
      line(x0, y0, x3, y3);
    }
  }
  
}


    static public void main(String args[]) {
        PApplet.main(new String[] { "--bgcolor=#ECE9D8", "ManetVisualizerSketch" });
    }
}
