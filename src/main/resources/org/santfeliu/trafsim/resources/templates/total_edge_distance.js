distance = 0;
iter = simulation.roadGraph.edges.iterator();
while (iter.hasNext())
{
  var edge = iter.next();
  distance += edge.lineString.getLength();
}
"Total edge distance: " + distance;


