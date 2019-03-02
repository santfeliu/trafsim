/*
 * Traffic Simulator
 *
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/
 *   and
 *   https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.trafsim.tool;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.Finder;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.Projector;
import org.santfeliu.trafsim.RoadGraph;
import org.santfeliu.trafsim.Simulation;
import org.santfeliu.trafsim.TrafficSimulator;

/**
 *
 * @author realor
 */
public abstract class Tool extends AbstractAction
{
  protected static final int SELECT_PIXELS = 8;
  protected static final int SNAP_PIXELS = 8;
  protected TrafficSimulator trafficSimulator;

  public Tool(TrafficSimulator trafficSimulator)
  {
    this.trafficSimulator = trafficSimulator;
    initValues();
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    trafficSimulator.start(this);
  }

  public abstract String getName();

  public abstract void start();

  public abstract void stop();

  public MapViewer getMapViewer()
  {
    return trafficSimulator.getMapViewer();
  }

  public Simulation getSimulation()
  {
    return trafficSimulator.getSimulation();
  }

  public Projector getProjector()
  {
    return trafficSimulator.getMapViewer().getProjector();
  }

  public void info(String message)
  {
    trafficSimulator.info(getName() + "Tool." + message);
  }

  protected void project(Point3d worldPoint, java.awt.Point dp)
  {
    MapViewer mapViewer = getMapViewer();
    Projector projector = mapViewer.getProjector();
    projector.project(worldPoint, dp);
  }

  protected void unproject(java.awt.Point dp, Point3d worldPoint)
  {
    MapViewer mapViewer = getMapViewer();
    Projector projector = mapViewer.getProjector();
    projector.unproject(dp, worldPoint);
  }

  protected boolean snapNode(java.awt.Point dp, Point3d snapPoint)
  {
    MapViewer mapViewer = getMapViewer();
    Projector projector = mapViewer.getProjector();
    Point3d selectPoint = new Point3d();
    projector.unproject(dp, selectPoint);

    double tolerance = SNAP_PIXELS / projector.getScaleX();
    RoadGraph roadGraph = getSimulation().getRoadGraph();
    return Finder.snapNode(roadGraph, selectPoint, snapPoint, tolerance);
  }

  private void initValues()
  {
    putValue(Action.NAME, trafficSimulator.getMessage(getName() + "Tool.name"));
  }
}
