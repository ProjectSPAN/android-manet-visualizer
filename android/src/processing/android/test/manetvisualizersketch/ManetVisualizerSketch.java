package processing.android.test.manetvisualizersketch;

import processing.core.*; 

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
 
  //size(500, 300);
  //build controls
  //Mcomm = new ManetCommunicator(this);
  widgetContainer = new APWidgetContainer(this); //create new container for widgets
  btn_refresh = new APButton(10, 10, 100, 50, "Refresh"); 
  widgetContainer.addWidget(btn_refresh); //place button in container


  // frameRate(24);
  noLoop();

  //build graph
  makeGraph(null);
  redraw();
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
  if (data != null) {
    System.out.println(data+"\n-----------------------");
    String lines[] = data.split("\\r?\\n");
    int index = 5;
    while (!lines[index].contains ("Table:")) {
      
      System.out.println( lines[index] );
      int wsIndex = lines[index].indexOf(' ');
      int dsIndex = lines[index].indexOf(' ', wsIndex+6);
      String src = lines[index].substring(0, wsIndex);
      String dst = lines[index].substring(wsIndex+6, dsIndex);
      System.out.println(src + " -> " + dst);
      index++;
    }
  }
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


  @Override
    public void onAdhocStateUpdated(AdhocStateEnum arg0, String arg1) {
    // TODO Auto-generated method stub
  }

  @Override
    public void onConfigUpdated(ManetConfig arg0) {
    // TODO Auto-generated method stub
  }

  @Override
    public void onError(String arg0) {
    // TODO Auto-generated method stub
  }

  @Override
    public void onPeersUpdated(TreeSet<String> arg0) {
    // TODO Auto-generated method stub
  }

  @Override
    public void onRoutingInfoUpdated(String arg0) {
    // TODO Auto-generated method stub
    
    if(arg0 == null){
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

  @Override
    public void onServiceConnected() {
    // TODO Auto-generated method stub
    connected = true;
    print("Connected!");
  }

  @Override
    public void onServiceDisconnected() {
    connected = false;
    // TODO Auto-generated method stub
  }

  @Override
    public void onServiceStarted() {
    // TODO Auto-generated method stub
  }

  @Override
    public void onServiceStopped() {
    // TODO Auto-generated method stub
  }
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


  public int sketchWidth() { return screenWidth; }
  public int sketchHeight() { return screenHeight; }
  public String sketchRenderer() { return P3D; }
}
