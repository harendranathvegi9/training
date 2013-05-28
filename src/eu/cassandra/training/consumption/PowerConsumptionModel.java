/*   
   Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eu.cassandra.training.consumption;

import java.util.ArrayList;
import java.util.Arrays;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * This class stores the variables of a consumption model. It has no
 * functionality except parsing a JSON string and return the values on demand.
 * 
 * @author kyrcha
 * 
 */
public class PowerConsumptionModel
{

  /** How many times the patterns repeat */
  private int outerN;

  /** The number of patterns */
  private int patternN;

  /** Total duration of the consumption model */
  private int totalDuration;

  /** How many times each pattern runs */
  private int[] n;

  /** Total duration per pattern */
  private int[] patternDuration;

  /** An array storing the consumption patterns */
  private ArrayList<TripletPower>[] patterns;

  private String model;

  public PowerConsumptionModel ()
  {
  }

  public PowerConsumptionModel (String amodel)
  {
    model = amodel;
    DBObject modelObj = (DBObject) JSON.parse(model);
    init(modelObj);
  }

  public void init (DBObject modelObj)
  {

    outerN = ((Integer) modelObj.get("n")).intValue();
    BasicDBList patternsObj = (BasicDBList) modelObj.get("params");
    patternN = patternsObj.size();
    patterns = new ArrayList[patternN];
    n = new int[patternN];
    patternDuration = new int[patternN];
    for (int i = 0; i < patternN; i++) {
      n[i] = ((Integer) ((DBObject) patternsObj.get(i)).get("n")).intValue();
      BasicDBList values =
        ((BasicDBList) ((DBObject) patternsObj.get(i)).get("values"));
      int tripplets = values.size();
      patterns[i] = new ArrayList<TripletPower>(tripplets);
      for (int j = 0; j < tripplets; j++) {
        TripletPower t = new TripletPower();
        try {
          t.p = ((Double) ((DBObject) values.get(j)).get("p")).doubleValue();
        }
        catch (ClassCastException e) {
          t.p =
            (double) ((Integer) ((DBObject) values.get(j)).get("p")).intValue();
        }
        t.d = ((Integer) ((DBObject) values.get(j)).get("d")).intValue();
        patternDuration[i] += t.d;
        totalDuration += (n[i] * t.d);
        try {
          t.s = ((Double) ((DBObject) values.get(j)).get("s")).doubleValue();
        }
        catch (ClassCastException e) {
          t.s =
            (double) ((Integer) ((DBObject) values.get(j)).get("s")).intValue();
        }
        patterns[i].add(t);
      }
    }

  }

  public int getTotalDuration ()
  {
    return totalDuration;
  }

  public int getOuterN ()
  {
    return outerN;
  }

  public int getPatternN ()
  {
    return patternN;
  }

  public int getN (int i)
  {
    return n[i];
  }

  public int getPatternDuration (int i)
  {
    return patternDuration[i];
  }

  public ArrayList<TripletPower> getPattern (int i)
  {
    return patterns[i];
  }

  public static void main (String[] args)
  {
    String s =
      "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"p\" : 140.0, \"d\" : 20, \"s\": 0.0}, {\"p\" : 117.0, \"d\" : 18, \"s\": 0.0}, {\"p\" : 0.0, \"d\" : 73, \"s\": 0.0}]},{ \"n\" : 1, \"values\" : [ {\"p\" : 14.0, \"d\" : 20, \"s\": 0.0}, {\"p\" : 11.0, \"d\" : 18, \"s\": 0.0}, {\"p\" : 5.0, \"d\" : 73, \"s\": 0.0}]}]}";
    PowerConsumptionModel cm = new PowerConsumptionModel(s);
    System.out.println(cm.toString());
  }

  public void status ()
  {

    System.out.println("Outer N:" + outerN);
    System.out.println("Total Duration:" + totalDuration);
    System.out.println("Number of Patterns:" + patternN);
    System.out.println("Pattern Times: " + Arrays.toString(n));
    System.out
            .println("Pattern Durations: " + Arrays.toString(patternDuration));

  }

}