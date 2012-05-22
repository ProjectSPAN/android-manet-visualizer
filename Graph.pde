

// models a graph of nodes and bi-directional edges
class Graph
{
  ArrayList<Node> nodes = new ArrayList<Node>();

  void addNode(Node node) {
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  int size() { 
    return nodes.size();
  }

  boolean linkNodes(Node n1, Node n2) {
    if (nodes.contains(n1) && nodes.contains(n2)) {
      n1.addEdge(n2); 
      return true;
    }
    return false;
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
  void draw() {
    for (Node n: nodes) {
      n.drawEdges();
    }
    for (Node n: nodes) {
      n.draw();
    }
  }
}

