

// models a graph of nodes and bi-directional edges
class Graph
{
  volatile ArrayList<Node> nodes = new ArrayList<Node>();

 synchronized Node addNode(Node node) {
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
    return nodes.get(nodes.indexOf(node));
  }

  int size() { 
    return nodes.size();
  }

boolean contains(Node n){
  return nodes.contains(n);
}

boolean containsLabel(String s){
 return nodes.contains(new Node(s)); 
}

Node getNodeByLabel(String label){
  int i = nodes.indexOf(new Node(label));
  return nodes.get(i);
}

synchronized void clear(){
 nodes.clear(); 
}

synchronized String toString(){
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
  synchronized void directedLink(Node n_1, Node n_2){
    /*
    Node n_1 = addNode(n_1);
    Node n_2 = addNode(n_2);
    */
    n_1.addOutgoingEdge(n_2);
    n_2.addIncomingEdge(n_1);
  }
  
  void bidirectionalLink(Node n1, Node n2){
    directedLink(n1, n2);
    directedLink(n2, n1);
  }

  Node getNode(int index) {
    return nodes.get(index);
  }

Node copyNode(String label){
  int i = nodes.indexOf(new Node(label));
  if(i < 0){
    Node n = new Node(label, (int)random(0, width), (int)random(0,height) );
    return n;
  }
  else{
    return nodes.get(i);
  }
}

  ArrayList<Node> getNodes() {
    return nodes;
  }


synchronized Node getNearestNode(int x, int y){
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



  boolean reflow() {
    int buffer = 25;
    int buffer_right = 110;
    int control_width = 0;
    double elasticity = 200.0;
    double repulsion = -sqrt(width*width + height*height) * 4;
    double tension = .01;

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
  void focus(Node n){
    if(nodes.contains(n)){
    nodes.get(nodes.indexOf(n)).setFocus();
    }
    else{
      n.setFocus();
      nodes.add(n);
    }
  }
  
  
  // draw nodes
  synchronized void draw() {
    for (Node n: nodes) {
      n.draw();
    }
    for (Node n: nodes) {
      n.drawEdges();
    }

  }
}

