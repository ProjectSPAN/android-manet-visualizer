//Imports
import apwidgets.*;
import java.util.TreeSet;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.adhoc.manet.ManetHelper;
import android.content.Context;
import android.app.Activity;
import android.widget.Toast;



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
void setup() { 

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

  //build graph
  makeGraph(null);
  redraw();
  mComm.getRoutingInfo();
} 

void onClickWidget(APWidget widget) {
  if (widget == btn_refresh) {
    mComm.getRoutingInfo();
  }
}

void draw() { 
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

void makeGraph(String data)
{
  nodes.clear();
  g = new Graph();

  if (data != null) {
    System.out.println(data+"\n-----------------------");
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
      Node srcN = new Node(src);
      Node dstN = new Node(dst);

      g.bidirectionalLink(srcN, dstN);
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
      Node srcN = new Node(src);
      Node dstN = new Node(dst);

      g.directedLink(srcN, dstN);
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
    Node thisN = new Node(myIP);
    g.focus(thisN);
    
    
  }
  //this is where we need to get the manet info
  // define a graph

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

