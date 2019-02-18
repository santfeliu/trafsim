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

import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.geom.Polygon;

/**
 *
 * @author realor
 */
public class GMLReader
{
  private String srsName = "EPSG:25831";

  public String getSrsName()
  {
    return srsName;
  }

  public void setSrsName(String srsName)
  {
    this.srsName = srsName;
  }

  public void readLayer(String wfsUrl, String layerName,
     String username, String password, Processor processor) throws Exception
  {
    URL url = new URL(wfsUrl +
      "?service=WFS&version=1.0.0&request=GetFeature&typeName=" + layerName +
      "&outputFormat=text/xml;%20subtype=gml/2.1.2&srsName=" + srsName);
    URLConnection conn = url.openConnection();
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(10000);
    if (username != null && password != null &&
        username.length() > 0 && password.length() > 0)
    {
      String userPassString = username + ":" + password;
      String autho = "Basic " +
        new String(Base64.getEncoder().encode(userPassString.getBytes()));
      conn.setRequestProperty("Authorization", autho);
    }
    // set username / password to conn
    readLayer(conn.getInputStream(), processor);
  }

  public void readLayer(File file, Processor processor) throws Exception
  {
    readLayer(new FileInputStream(file), processor);
  }

  public void readLayer(InputStream is, Processor processor) throws Exception
  {
    try
    {
      DocumentBuilderFactory factory =
      DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(is);
      Element collection = doc.getDocumentElement();
      NodeList features = collection.getElementsByTagName("gml:featureMember");
      HashMap attributes = new HashMap();
      for (int i = 0; i < features.getLength(); i++)
      {
        Element featureMember = (Element)features.item(i);
        NodeList nodeList = featureMember.getElementsByTagName("*");
        Element feature = (Element)nodeList.item(0);
        attributes.clear();
        Geometry geometry = null;
        NodeList featureList = feature.getElementsByTagName("*");
        for (int j = 0; j < featureList.getLength(); j++)
        {
          Element field = (Element)featureList.item(j);
          Node content = field.getFirstChild();
          if (content.getNodeType() == Node.TEXT_NODE)
          {
            String fieldName = field.getNodeName();
            int index = fieldName.indexOf(":");
            if (index != -1) fieldName = fieldName.substring(index + 1);
            attributes.put(fieldName, content.getTextContent());
          }
          else if (content.getNodeType() == Node.ELEMENT_NODE)
          {
            String geometryType = content.getNodeName();
            if ("gml:Point".equals(geometryType))
            {
              geometry = parsePoint(content.getTextContent());
            }
            else if ("gml:LineString".equals(geometryType))
            {
              geometry = parseLineString(content.getTextContent());
            }
            else if ("gml:Polygon".equals(geometryType))
            {
              geometry = parsePolygon(content.getTextContent());
            }
          }
        }
        processor.processEntity(geometry, attributes);
      }
    }
    finally
    {
      is.close();
    }
  }

  protected Point parsePoint(String text)
  {
    String parts[] = text.split(",");
    Point3d coords = new Point3d();
    coords.x = Double.parseDouble(parts[0]);
    coords.y = Double.parseDouble(parts[1]);
    if (parts.length >= 3)
    {
      coords.z = Double.parseDouble(parts[2]);
    }
    return new Point(coords);
  }

  protected LineString parseLineString(String text)
  {
    List<Point3d> points = new ArrayList<Point3d>();
    String vertices[] = text.split(" ");
    for (String vertex : vertices)
    {
      String[] parts = vertex.split(",");
      Point3d coords = new Point3d();
      coords.x = Double.parseDouble(parts[0]);
      coords.y = Double.parseDouble(parts[1]);
      if (parts.length >= 3)
      {
        coords.z = Double.parseDouble(parts[2]);
      }
      points.add(coords);
    }
    return new LineString(points);
  }

  protected Polygon parsePolygon(String text)
  {
    List<Point3d> points = new ArrayList<Point3d>();
    String vertices[] = text.split(" ");
    for (String vertex : vertices)
    {
      String[] parts = vertex.split(",");
      Point3d coords = new Point3d();
      coords.x = Double.parseDouble(parts[0]);
      coords.y = Double.parseDouble(parts[1]);
      if (parts.length >= 3)
      {
        coords.z = Double.parseDouble(parts[2]);
      }
      points.add(coords);
    }
    return new Polygon(points);
  }

  public interface Processor
  {
    public void processEntity(Geometry geometry, Map attributes);
  }
}
