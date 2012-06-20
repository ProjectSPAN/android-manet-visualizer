//Imports
import apwidgets.*;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.HashSet;
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
public boolean onCreateOptionsMenu(Menu menu){
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
void setup() { 

  mComm = new ManetCommunicator(this);

  nodes = new ArrayList<Node>();
  size(screenWidth, screenHeight, P3D);
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
        System.out.println("Performing Auto-update");
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

void draw() { 
  background(255);
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
void makeGraph(ArrayList<String> data)
{
  if (data == null) return;


  Graph g_temp = new Graph();
  for (int i=0; i<data.size(); i++) {
    System.out.println("Edge: " + data.get(i));
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
  g.focus(g.getNodeByLabel(myIP));
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
    System.out.println("-Received ManetConfig");

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
    System.out.println("-Receieved Routing Info");


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
      System.out.println("--Using OLSR.");
      edges = ManetParser.parseOLSR(arg0);
    }
    else {
      System.out.println("--Using Simple");
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

