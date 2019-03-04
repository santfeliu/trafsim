selection.clear();
iter = simulation.roadGraph.edges.iterator();
while (iter.hasNext())
{
  var edge = iter.next();
  if (edge.speed === 50)
  {
    selection.add(edge);
  }
}
selection.size() + " selected.";


