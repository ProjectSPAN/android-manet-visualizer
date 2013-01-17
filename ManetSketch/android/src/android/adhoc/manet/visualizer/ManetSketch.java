package android.adhoc.manet.visualizer;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import apwidgets.*; 
import java.util.TreeSet; 
import java.util.ArrayList; 
import java.util.HashSet; 
import java.net.DatagramPacket; 
import java.net.DatagramSocket; 
import java.net.InetAddress; 
import android.adhoc.manet.ManetObserver; 
import android.adhoc.manet.service.ManetService.AdhocStateEnum; 
import android.adhoc.manet.system.ManetConfig; 
import android.adhoc.manet.ManetHelper; 
import android.adhoc.manet.ManetParser; 
import android.adhoc.manet.routing.OlsrProtocol; 
import android.adhoc.manet.routing.SimpleProactiveProtocol; 
import android.adhoc.manet.routing.Node; 
import android.content.Context; 
import android.app.Activity; 
import android.view.Menu; 
import android.widget.Toast; 
import android.view.SubMenu; 
import android.view.MenuItem; 
import android.view.MotionEvent; 

import apwidgets.*; 
import android.adhoc.manet.system.*; 
import android.adhoc.manet.routing.*; 
import android.adhoc.manet.service.*; 
import android.adhoc.manet.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ManetSketch extends PApplet {

//Imports























//Control Variables
APWidgetContainer widgetContainer; 
APButton btn_refresh;

//Data Variables
ManetCommunicator mComm;
volatile Graph g=null;
ArrayList<Node> nodes;
Node focus;

//Other Variables
int padding=30;

//Handle Menu
@Override
public boolean onCreateOptionsMenu(Menu menu) {
  System.out.println("OnCreateOptionsMenu");
  boolean supRetVal = super.onCreateOptionsMenu(menu);
  SubMenu reset = menu.addSubMenu(0, 0, 0, "Reset");
  return supRetVal;
}

@Override
public boolean onOptionsItemSelected(MenuItem menuItem) {
  boolean supRetVal = super.onOptionsItemSelected(menuItem);
  switch (menuItem.getItemId()) {
  case 0 :
    g.clear();
    mComm.getRoutingInfo();
    break;
  }
  return supRetVal;
}    

//Set up initial state
public void setup() { 

  mComm = new ManetCommunicator(this);

  nodes = new ArrayList<Node>();
 
  //size(500, 300);
  //build controls
  //Mcomm = new ManetCommunicator(this);
  //widgetContainer = new APWidgetContainer(this); //create new container for widgets
  //btn_refresh = new APButton(10, 10, 100, 50, "Refresh"); 
  //widgetContainer.addWidget(btn_refresh); //place button in container


  // frameRate(24);
  noLoop();
  g=new Graph();
  //build graph
  redraw();
  //create thread to get routing info on a timer


  Thread updateThread = new Thread() {
    public void run() {
      while (true) {
        //System.out.println("Performing Auto-update");
        mComm.getRoutingInfo();
        try {
          Thread.sleep(3 * 1000);
        }
        catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
  };
  updateThread.start();
} 


/*
void onClickWidget(APWidget widget) {
 if (widget == btn_refresh) {
 g.clear();
 mComm.getRoutingInfo();
 }
 }
 */

public boolean surfaceTouchEvent(MotionEvent event) {
  print("SurfaceTouchEvent! "+mouseX +" " + mouseY);
  // your code here
  Node n = g.getNearestNode(mouseX, mouseY);
  if (n!=null) {
    int deltaX = mouseX - n.x;
    int deltaY = mouseY - n.y;
    double distance = sqrt(deltaX*deltaX + deltaY*deltaY);
    if (distance < 50) {
      //handle action on node

      //g.focus(n);
      //sendMessage(n.getLabel(), "Sent from Viz App!");
    }
  }
  return super.surfaceTouchEvent(event);
}

public void draw() { 
  background(255, 255, 255);
  fill(255);
  rect(0, 0, width, height);
  //fill(150, 150, 150);
  stroke(0);
  //rect(0, 0, 200, height);
  boolean done = g.reflow();
  g.draw();
  if (!done) { 
    loop();
  } 
  else { 
    noLoop();
  }
}

//modify this to pass in arrayList of Strings 192.168.11.xxx,192.168.11.yyy (directed edges)
public void makeGraph(ArrayList<String> data)
{
  if (data == null) return;


  Graph g_temp = new Graph();
  for (int i=0; i<data.size(); i++) {
    //System.out.println("Edge: " + data.get(i));
    int dsh = data.get(i).indexOf(">");
    String src = data.get(i).substring(0, dsh);
    String dst = data.get(i).substring(dsh+1, data.get(i).length());
    Node src_n;
    if (g_temp.containsLabel(src)) {
      src_n = g_temp.getNodeByLabel(src);
    }
    else if (g.containsLabel(src)) {
      src_n = g.getNodeByLabel(src);
      src_n.clearLinks();
      src_n = g_temp.addNode(src_n);
    }
    else {
      src_n = new Node(src);
      src_n = g_temp.addNode(src_n);
    }

    Node dst_n;
    if (g_temp.containsLabel(dst)) {
      dst_n = g_temp.getNodeByLabel(dst);
    }
    else if (g.containsLabel(dst)) {
      dst_n = g.getNodeByLabel(dst);
      dst_n.clearLinks();
      dst_n = g_temp.addNode(dst_n);
    }
    else {
      dst_n = new Node(dst);
      dst_n = g_temp.addNode(dst_n);
    }

    g_temp.directedLink(src_n, dst_n);
  }
  g = g_temp;

  String myIP = mComm.getConfig().getIpAddress();
  if (!g.containsLabel(myIP)) {
    g.addNode(new Node(myIP, width/2, height/2));
  }
  g.focus(g.getNodeByLabel(myIP));
} 

private void sendMessage(String address, String msg) {

  DatagramSocket socket = null;
  try {
    socket = new DatagramSocket();

    byte buff[] = msg.getBytes();
    int msgLen = buff.length;
    boolean truncated = false;
    if (msgLen > 256) {
      msgLen = 256;
      truncated = true;
    }
    DatagramPacket packet = new DatagramPacket(buff, msgLen, InetAddress.getByName(address), 9000);
    socket.send(packet);
    if (truncated) {
      print("Message truncated and sent.");
    } 
    else {
      print("Message sent: "+address+": " + msg);
    }
  } 
  catch (Exception e) {
    e.printStackTrace();
    //app.displayToastMessage("Error: " + e.getMessage());
  } 
  finally {
    if (socket != null) {
      socket.close();
    }
  }
}


class ManetCommunicator implements ManetObserver {
  ManetConfig manetcfg;
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

  public ManetConfig getConfig() {
    return manetcfg;
  }

  public void getRoutingInfo() {
    if (connected) {
      //getConfigObject
      Activity activity=(Activity) this.context;
      activity.runOnUiThread(new Runnable() {
        public void run() {
          //Toast.makeText((Activity)ManetCommunicator.this.context, "Querying Service for Manet Configuration", Toast.LENGTH_SHORT).show();
          helper.sendManetConfigQuery();
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
    //System.out.println("-Received ManetConfig");

    manetcfg = arg0;
    //set config object
    if (connected) {
      //getConfigObject
      Activity activity=(Activity) this.context;
      activity.runOnUiThread(new Runnable() {
        public void run() {
          //Toast.makeText((Activity)ManetCommunicator.this.context, "Querying Service for Manet Configuration", Toast.LENGTH_SHORT).show();
          helper.sendRoutingInfoQuery();
        }
      }
      );
    }
    // TODO Auto-generated method stub
  }

  @Override
    public void onError(String arg0) {
    // TODO Auto-generated method stub
  }


  @Override
    public void onRoutingInfoUpdated(String arg0) {
    // TODO Auto-generated method stub
    //System.out.println("-Receieved Routing Info");


    if (arg0 == null) {
      Activity activity=(Activity) this.context;
      activity.runOnUiThread(new Runnable() {
        public void run() {
          Toast.makeText((Activity)ManetCommunicator.this.context, "Error: Device not in adhoc mode", Toast.LENGTH_SHORT).show();
        }
      }
      );
    }

    ArrayList<String> edges;
    if (manetcfg.getRoutingProtocol().equals(OlsrProtocol.NAME)) {
      //System.out.println("--Using OLSR.");
      edges = ManetParser.parseOLSR(arg0);
    }
    else {
      //System.out.println("--Using Simple");
      edges = ManetParser.parseRoutingInfo(arg0);
    }

    makeGraph(edges);
  }

  @Override
    public void onServiceConnected() {
    // TODO Auto-generated method stub
    connected = true;
    System.out.println("Connected!");
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

  @Override
    public void onPeersUpdated(HashSet<android.adhoc.manet.routing.Node> peers) {
    // TODO Auto-generated method stub
  }
}



// models a graph of nodes and bi-directional edges
class Graph
{
  volatile ArrayList<Node> nodes = new ArrayList<Node>();

 public synchronized Node addNode(Node node) {
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

public boolean containsLabel(String s){
 return nodes.contains(new Node(s)); 
}

public Node getNodeByLabel(String label){
  int i = nodes.indexOf(new Node(label));
  return nodes.get(i);
}

public synchronized void clear(){
 nodes.clear(); 
}

public synchronized String toString(){
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
  public synchronized void directedLink(Node n_1, Node n_2){
    /*
    Node n_1 = addNode(n_1);
    Node n_2 = addNode(n_2);
    */
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


public synchronized Node getNearestNode(int x, int y){
  if(nodes.size() < 1) return null;
  Node nearest = nodes.get(0);
  double nearest_dist = sqrt(height*height + width*width);
  for(int i=0; i<nodes.size(); i++){
     int deltaX = x - nodes.get(i).x;
     int deltaY = y - nodes.get(i).y;
     double distance = sqrt(deltaX*deltaX + deltaY*deltaY);
     if (distance < nearest_dist){
       nearest = nodes.get(i);
       nearest_dist = distance; 
    }
  }
  return nearest;
}  



  public boolean reflow() {
    int buffer = 25;
    int buffer_right = 110;
    int control_width = 0;
    double elasticity = 200.0f;
    double repulsion = -sqrt(width*width + height*height) * 4;
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
      else if (n.x>width-buffer_right) { 
        n.x=width-buffer_right;
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
  public synchronized void draw() {
    for (Node n: nodes) {
      n.draw();
    }
    for (Node n: nodes) {
      n.drawEdges();
    }

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
  
  public synchronized void clearLinks(){
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

  public synchronized void addIncomingEdge(Node n) {
    if (!incomingEdges.contains(n)) {
      incomingEdges.add(n);
    }
  }
  
   public synchronized void addOutgoingEdge(Node n) {
    if (!outgoingEdges.contains(n)) {
      outgoingEdges.add(n);
    }
  }

  public synchronized ArrayList<Node> getAllEdges() {
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

  public synchronized void drawEdges() {
    stroke(0);
    fill(0);
    strokeWeight(2);
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
      arrowhead(x0, y0, r1, atan2(y1 - y0, x1 - x0), startAngle, solid);
    }
    if (endAngle != 0)
    {
      arrowhead(x1, y1, r1, atan2(y0 - y1, x0 - x1), endAngle, solid);
    }
  }

  /*
 * Draws an arrow head at given location
   * x0 - arrow vertex x-coordinate
   * y0 - arrow vertex y-coordinate
   * r  - distance from line endpoint to arrow vertex
   * lineAngle - angle of line leading to vertex (radians)
   * arrowAngle - angle between arrow and line (radians)
   * solid - true for a solid arrow, false for an "open" arrow
   */
  public void arrowhead(float x0, float y0, float r, float lineAngle, 
  float arrowAngle, boolean solid)
  {
    float phi;
    float x2;
    float y2;
    float x3;
    float y3;
    final float SIZE = 16;

    x0 = x0 + r * cos(lineAngle);
    y0 = y0 + r * sin(lineAngle);

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


  public int sketchWidth() { return displayWidth; }
  public int sketchHeight() { return displayHeight; }
  public String sketchRenderer() { return P3D; }
}
