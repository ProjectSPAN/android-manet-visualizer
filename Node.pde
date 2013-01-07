
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

  String getLabel() {
    return label;
  }
  
  synchronized void clearLinks(){
    incomingEdges.clear();
    outgoingEdges.clear();
  }

boolean isFocused(){
  return this.focus;
}

  void setFocus() {
    focus = true;
  }

  void unsetFocus() {
    focus=false;
  }

  synchronized void addIncomingEdge(Node n) {
    if (!incomingEdges.contains(n)) {
      incomingEdges.add(n);
    }
  }
  
   synchronized void addOutgoingEdge(Node n) {
    if (!outgoingEdges.contains(n)) {
      outgoingEdges.add(n);
    }
  }

  synchronized ArrayList<Node> getAllEdges() {
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



  boolean equals(Object other) {
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
  
  String toString(){
    return "Node: " + label;
  }

  synchronized void drawEdges() {
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
  void arrowLine(float x0, float y0, float x1, float y1, 
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
  void arrowhead(float x0, float y0, float r, float lineAngle, 
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

