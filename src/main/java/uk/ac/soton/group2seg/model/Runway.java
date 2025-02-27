package uk.ac.soton.group2seg.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.wagon.observers.Debug;

/**
 * @author louistownsend
 */

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Runway {

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "logicalRunway")
  private ArrayList<LogicalRunway> logicalRunways;

  public Runway() {
    this.logicalRunways = new ArrayList<>();
  }

  public Runway(LogicalRunway leftRunway, LogicalRunway rightRunway) {
    this.logicalRunways = new ArrayList<>();
    this.name = leftRunway.getName() + "/" + rightRunway.getName();
    this.logicalRunways.add(leftRunway);
    this.logicalRunways.add(rightRunway);
  }

  protected void setName(String name) {
    this.name = name;
  }

  public List<LogicalRunway> getLogicalRunways() {
    return logicalRunways;
  }

  protected void setLogicalRunways(ArrayList<LogicalRunway> logicalRunways) {
    this.logicalRunways = logicalRunways;
  }

  public LogicalRunway getLeftRunway() {
    for (LogicalRunway lr : logicalRunways) {
      int heading = parseHeading(lr.getName());
      if (heading >= 0 && heading <= 17) {
        return lr;
      }
    }
    return null;
  }

  public LogicalRunway getRightRunway() {
    for (LogicalRunway lr : logicalRunways) {
      int heading = parseHeading(lr.getName());
      if (heading >= 18 && heading <= 35) {
        return lr;
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }

  private int parseHeading(String runwayName) {
    try {
      return Integer.parseInt(runwayName.replaceAll("[^0-9]", ""));
    } catch (NumberFormatException e) {
      System.err.println("Could not parse heading from runway: " + runwayName);
      return -1;
    }
  }
}

