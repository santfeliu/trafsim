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
package org.santfeliu.trafsim.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.Box;
import org.santfeliu.trafsim.Feature;
import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;
import org.santfeliu.trafsim.geom.Polygon;

/**
 *
 * @author realor
 */
public class GMLWriter extends XMLWriter
{
  private String layerName = "layer";
  private String srsName = "EPSG:25831";

  public GMLWriter(OutputStream os) throws IOException
  {
    super(os);
  }

  public String getLayerName()
  {
    return layerName;
  }

  public void setLayerName(String layerName)
  {
    this.layerName = layerName;
  }

  public String getSrsName()
  {
    return srsName;
  }

  public void setSrsName(String srsName)
  {
    this.srsName = srsName;
  }

  public void write(Collection<Feature> features) throws IOException
  {
    try
    {
      writePreambule();
      startTag("ogr:FeatureCollection");
      writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      writeAttribute("xmlns:ogr", "http://ogr.maptools.org/");
      writeAttribute("xmlns:gml", "http://www.opengis.net/gml");
      writeAttribute("xsi:schemaLocation", "http://ogr.maptools.org/ " +
        layerName + ".xsd");
      writeBoundedBy(features);
      int fid = 0;
      for (Feature feature : features)
      {
        writeEntity(feature, fid++);
      }
      endTag("ogr:FeatureCollection");
    }
    finally
    {
      close();
    }
  }

  private void writeBoundedBy(Collection<Feature> features)
  {
    Box box = new Box();
    for (Feature feature : features)
    {
      box.extend(feature.getGeometry().getBoundingBox());
    }
    startTag("gml:boundedBy");

    startTag("gml:Box");

    startTag("gml:coord");
    startTag("gml:X");
    writeText(box.xmin);
    endTag("gml:X");
    startTag("gml:Y");
    writeText(box.ymin);
    endTag("gml:Y");
    startTag("gml:Z");
    writeText(box.zmin);
    endTag("gml:Z");
    endTag("gml:coord");

    startTag("gml:coord");
    startTag("gml:X");
    writeText(box.xmax);
    endTag("gml:X");
    startTag("gml:Y");
    writeText(box.ymax);
    endTag("gml:Y");
    startTag("gml:Z");
    writeText(box.zmax);
    endTag("gml:Z");
    endTag("gml:coord");

    endTag("gml:Box");
    endTag("gml:boundedBy");
  }

  private void writeEntity(Feature feature, int fid)
  {
    startTag("gml:featureMember");
    startTag("ogr:" + layerName);
    writeAttribute("fid", layerName + "." + fid);

    startTag("ogr:geometry");
    writeGeometry(feature.getGeometry());
    endTag("ogr:geometry");

    startTag("ogr:gml_id");
    writeText(fid);
    endTag("ogr:gml_id");
    HashMap<String, Object> attributes = new LinkedHashMap<String, Object>();
    feature.loadAttributes(attributes);
    for (String name : attributes.keySet())
    {
      startTag("ogr:" + name);
      writeText(attributes.get(name));
      endTag("ogr:" + name);
    }
    endTag("ogr:" + layerName);
    endTag("gml:featureMember");
  }

  private void writeGeometry(Geometry geometry)
  {
    if (geometry instanceof Point)
    {
      startTag("gml:Point");
      writeAttribute("srsName", srsName);
      startTag("gml:coordinates");
      writePoint3d(((Point)geometry).getPosition());
      endTag("gml:coordinates");
      endTag("gml:Point");
    }
    else if (geometry instanceof LineString)
    {
      startTag("gml:LineString");
      writeAttribute("srsName", srsName);
      startTag("gml:coordinates");
      List<Point3d> points = ((LineString)geometry).getVertices();
      for (int i = 0; i < points.size(); i++)
      {
        if (i > 0) writeText(" ");
        writePoint3d(points.get(i));
      }
      endTag("gml:coordinates");
      endTag("gml:LineString");
    }
    else if (geometry instanceof Polygon)
    {
      // TODO:
    }
  }

}
