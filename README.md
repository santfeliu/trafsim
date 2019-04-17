# Traffic Simulator

Traffic Simulator is an open source Java desktop application to simulate the
vehicle traffic in a road network.

The road network is modeled as a directed graph formed by nodes and edges.
Each edge connects 2 nodes and represents a possible trajectory of a vehicle.

The simulation requires 3 layers to work:

* __Road graph__: contains the edges (trajectories) of the road network 
represented as LineString (polylines) geometries. An edge has 3 attributes:
the maximum speed (Km/h), the number of lanes of the road and a delay 
representing the number of seconds that a vehicle is stopped when crossing this edge.
* __Vehicles__: the vehicles that are going to be moved in the simulation 
represented as Point geometries. Each feature of this layer contains 2 
attributes: the number of vehicles and the group it belongs to.
* __Locations__: the destinations of the vehicles represented as Point 
geometries. A location has a name and a descriptive label.

For each group we must indicate what percentage of vehicles goes to each 
destination (location). This can be achived through the menu option: 
`Edit > groups`. The group destinations must be specified this way:

    --- <group_name1>
    <location_name1> <factor 0..1>
    <location_name2> <factor 0..1>
    ...
    <location_nameN> <factor 0..1>

    --- <group_name2>
    <location_name1> <factor 0..1>
    ...

Example:

    --- group_a
    st_joan   0.4
    st_feliu  0.4
    molins    0.2

    --- group_b
    st_joan   0.3
    st_feliu  0.4
    molins    0.3

The simulation moves the vehicles to their destinations through the fastest 
route. These destinations are selected according to the group of the vehicles.

Once the simulation is completed, the following indicators are provided:

* __Journeys__: the number of journeys to all destinations.
* __Routed journeys__: the number of journeys that have a route to the destination.
* __Unrouted journeys__: the number of journeys where destination is not reachable.
* __Total distance__: the total distance of all routed journeys.
* __Total time__: the total time of all routed journeys.
* __Journey avg. distance__: the average distance of a journey.
* __Journey avg. time__: the average time of a journey.

In addition, for each edge, these indicators are also provided:

* __Length__: the length of this edge.
* __Capacity__: the maximum capacity of this edge (vehicles per hour).
* __Vehicle count__: the number of vehicles that pass through this edge.
* __Required time__: the time it takes all the vehicles to cross that edge.

The time to cross an edge is calculated according to this formula:

    T = ED + (EL / EV)

where:

* ED = the delay of the edge
* EL = the length of the edge
* EV = the velocity of the edge

![Trafsim screenshot](https://www.santfeliu.cat/documents/2958858/screenshot.png "screenshot")

The application can import/export layers in GML format.




