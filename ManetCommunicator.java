//package android.adhoc.manet.logger;

import java.util.TreeSet;

import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.adhoc.manet.ManetHelper;
import android.content.Context;


public class ManetCommunicator implements ManetObserver {
  ManetHelper helper;
  Context context;
  String info = "";

  public ManetCommunicator(Context c) {
    this.context = c;
    helper = new ManetHelper(this.context);
    helper.registerObserver(this);
    helper.connectToService();
  }

  public String getRoutingInfo() {
    /*Activty a = (Activity) context;
    a.runOnUiThread(new Runnable() {
      public void run() {
        helper.sendRoutingInfoQuery();
      }
    });
    while (info == "") {
      //wait
      try {
        wait(10);
      } 
      catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }*/
    String retval = info;
    info = "";
    return retval;
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
    info = arg0;
  }

  @Override
    public void onServiceConnected() {
    // TODO Auto-generated method stub
  }

  @Override
    public void onServiceDisconnected() {
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

