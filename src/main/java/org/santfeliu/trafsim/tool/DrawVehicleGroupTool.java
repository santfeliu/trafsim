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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.VehicleGroup;
import org.santfeliu.trafsim.VehicleGroupDialog;
import org.santfeliu.trafsim.geom.Point;

/**
 *
 * @author realor
 */
public class DrawVehicleGroupTool extends Tool implements MouseListener
{
  public DrawVehicleGroupTool(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public void start()
  {
    MapViewer mapViewer = getMapViewer();
    mapViewer.addMouseListener(this);
    mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }

  @Override
  public void stop()
  {
    MapViewer mapViewer = getMapViewer();
    mapViewer.removeMouseListener(this);
    mapViewer.setCursor(Cursor.getDefaultCursor());
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    MapViewer mapViewer = getMapViewer();
    java.awt.Point dp = e.getPoint();
    Point3d world = new Point3d();
    mapViewer.getProjector().unproject(dp, world);
    VehicleGroupDialog dialog = new VehicleGroupDialog(null, true);
    if (dialog.showDialog())
    {
      VehicleGroup vehicleGroup = new VehicleGroup(new Point(world),
        dialog.getCount(), dialog.getGroup());
      mapViewer.getSimulation().getVehicles().getFeatures().add(vehicleGroup);
      mapViewer.repaint();
      trafficSimulator.setModified(true);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
  }
}
