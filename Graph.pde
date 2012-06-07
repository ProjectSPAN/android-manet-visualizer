

// models a graph of nodes and bi-directional edges
class Graph
{
  ArrayList<Node> nodes = new ArrayList<Node>();

  Node addNode(Node node) {
    if (!nodes.contains(node)) {
      node.setPosition((int)random(0, width), (int)random(0,height));
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

//from n1 to n2
  void directedLink(Node n1, Node n2){
    Node n_1 = addNode(n1);
    Node n_2 = addNode(n2);
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

  ArrayList<Node> getNodes() {
    return nodes;
  }




  boolean reflow() {
    int buffer = 20;
    int control_width = 200;
    double elasticity = 200.0;
    double repulsion = -500;
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
  void focus(Node n){
    nodes.get(nodes.indexOf(n)).setFocus();  
  }
  
  
  // draw nodes
  void draw() {
    for (Node n: nodes) {
      n.draw();
    }
    for (Node n: nodes) {
      n.drawEdges();
    }

  }
}

