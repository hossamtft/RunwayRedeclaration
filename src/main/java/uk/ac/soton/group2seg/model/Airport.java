package uk.ac.soton.group2seg.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import java.util.ArrayList;

/**
 * @author louistownsend
 */
@XmlRootElement(name = "airport")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Airport {
  private String id;
  private String name;

  @XmlElement(name = "runway")
  private ArrayList<Runway> runways;

  public Airport(){

  }

  public Airport(@XmlElement(name = "id") String id, @XmlElement(name = "name") String name) {
    this.id = id;
    this.name = name;
    this.runways = new ArrayList<>();
  }

  public void addRunway(Runway runway){
    runways.add(runway);
  }

  @XmlElementWrapper(name = "runways")
  @XmlElement(name = "runway")
  public ArrayList<Runway> getRunways() {
    return runways;
  }

  @XmlElement(name = "id")
  public String getId() {
    return id;
  }

  @XmlElement(name = "name")
  public String getName() {
    return name;
  }

  protected void setName(String name) {
    this.name = name;
  }

  protected void setId(String id) {
    this.id = id;
  }

  protected void setRunways(ArrayList<Runway> runways) {
    this.runways = runways;
  }
}
