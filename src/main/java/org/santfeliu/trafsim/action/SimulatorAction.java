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
package org.santfeliu.trafsim.action;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.santfeliu.trafsim.Feature;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.MapViewer.Selection;
import org.santfeliu.trafsim.Projector;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.Simulation;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.Vehicles.VehicleGroup;

/**
 *
 * @author realor
 */
public abstract class SimulatorAction extends AbstractAction
{
  protected TrafficSimulator trafficSimulator;
  
  public SimulatorAction(TrafficSimulator trafficSimulator)
  {
    this.trafficSimulator = trafficSimulator;
    initValues();
  }

  public abstract String getName();

  protected MapViewer getMapViewer()
  {
    return trafficSimulator.getMapViewer();
  }

  protected Simulation getSimulation()
  {
    return trafficSimulator.getSimulation();
  }

  protected Projector getProjector()
  {
    return trafficSimulator.getMapViewer().getProjector();
  }

  protected UndoManager getUndoManager()
  {
    return trafficSimulator.getUndoManager();
  }
  
  protected String getMessage(String message)
  {
    return trafficSimulator.getMessage(message);
  }
  
  protected Selection getSelection()
  {
    return getMapViewer().getSelection();
  }
  
  protected List<Edge> getSelectedEdges()
  {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    MapViewer.Selection selection = getMapViewer().getSelection();
    for (Feature feature : selection)
    {
      if (feature instanceof Edge)
      {
        edges.add((Edge)feature);
      }
    }
    return edges;
  }

  protected List<VehicleGroup> getSelectedVehicleGroups()
  {
    ArrayList<VehicleGroup> vehicleGroups = new ArrayList<VehicleGroup>();
    MapViewer.Selection selection = getMapViewer().getSelection();
    for (Feature feature : selection)
    {
      if (feature instanceof VehicleGroup)
      {
        vehicleGroups.add((VehicleGroup)feature);
      }
    }
    return vehicleGroups;
  }
  
  private void initValues()
  {
    putValue(Action.NAME, trafficSimulator.getMessage(getName() + ".name"));
  }
  
  public abstract class BasicUndoableEdit implements UndoableEdit
  {
    @Override
    public boolean canUndo()
    {
      return true;
    }

    @Override
    public boolean canRedo()
    {
      return true;
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit)
    {
      return false;
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit)
    {
      return false;
    }

    @Override
    public boolean isSignificant()
    {
      return true;
    }

    @Override
    public String getPresentationName()
    {
      return null;
    }

    @Override
    public String getUndoPresentationName()
    {
      return getMessage(getName() + ".undo");
    }

    @Override
    public String getRedoPresentationName()
    {
      return getMessage(getName() + ".redo");
    }     

    @Override
    public void die()
    {      
    }
  }
}
