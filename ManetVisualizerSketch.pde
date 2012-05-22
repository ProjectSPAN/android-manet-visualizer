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
  //String s = Mcomm.getRoutingInfo();


  // frameRate(24);
  noLoop();

  //build graph
  makeGraph();
  redraw();
} 

void onClickWidget(APWidget widget) {
  if (widget == btn_refresh) {
    makeGraph(); //set the smaller size
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

void makeGraph()
{
  String s = mComm.getRoutingInfo();
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
  String info = "";

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
    });
  
  }

  public String getRoutingInfo() {
    if(connected){
      print("Get routing info...");
    Activity activity=(Activity) this.context;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText((Activity)ManetCommunicator.this.context, "Querying Service for Route Info", Toast.LENGTH_SHORT).show();
        helper.sendRoutingInfoQuery();
      }
    });
     while (info == "") {
     //wait
     }
    String retval = info;
    info = "";
    return retval;
    }
    return "";
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
    print("Recieved routing info!");
    info = arg0;
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

