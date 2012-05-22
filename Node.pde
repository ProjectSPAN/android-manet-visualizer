
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

  String getLabel() {
    return label;
  }

  void setFocus() {
    focus = true;
  }

  void unsetFocus() {
    focus=false;
  }

  void addEdge(Node n) {
    if (!edges.contains(n)) {
      edges.add(n);
      n.addEdge(this);
    }
  }

  ArrayList<Node> getEdges() {
    return edges;
  }

  int getEdgesCount() {
    return edges.size();
  }



  boolean equals(Node other) {
    if (this==other) return true;
    return label.equals(other.label);
  }

  // visualisation-specific
  int x=0;
  int y=0;
  int r1=20;
  int r2=20;

  void setPosition(int _x, int _y) {
    x=_x; 
    y=_y;
  }

  void setRadii(int _r1, int _r2) {
    r1=_r1; 
    r2=_r2;
  }

  void draw() {
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

  void drawEdges() {
    stroke(0);
    strokeWeight(4);
    for (Node e: edges) {
      line(x, y, e.x, e.y);
    }
    strokeWeight(1);
  }
  
}

