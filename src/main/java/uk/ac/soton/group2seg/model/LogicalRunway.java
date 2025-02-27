package uk.ac.soton.group2seg.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * @author louistownsend
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class LogicalRunway {

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "asda")
  private int asda;

  @XmlElement(name = "toda")
  private int toda;

  @XmlElement(name = "tora")
  private int tora;

  @XmlElement(name = "lda")
  private int lda;

  @XmlTransient
  private int dispThreshold;
  @XmlTransient
  private int currAsda;
  @XmlTransient
  private int currToda;
  @XmlTransient
  private int currTora;
  @XmlTransient
  private int currLda;
  @XmlTransient
  private Obstacle obstacle;

  public LogicalRunway(){
    initialise();

  }

  public LogicalRunway(String name, int asda, int toda, int tora, int lda) {
    this.name = name;
    this.asda = asda;
    this.toda = toda;
    this.tora = tora;
    this.lda = lda;
    this.dispThreshold = tora - lda;

    initialise();
  }

  public void initialise() {
    currAsda = asda;
    currLda = lda;
    currToda = toda;
    currTora = tora;

  }

  public String getDistances() {
    if(toda == 0) {
      initialise();
    }
    String distances = name + "\n"
        + "TORA: " + currTora + "m"
        + "\nTODA: " + currToda + "m"
        + "\nASDA: " + currAsda + "m"
        + "\nLDA: " + currLda + "m";

    return distances;
  }

  public String getName() {
    return name;
  }

  public int getCurrAsda() {
    return currAsda;
  }

  public int getCurrLda() {
    return currLda;
  }

  public int getCurrToda() {
    return currToda;
  }

  public int getCurrTora() {
    return currTora;
  }

  public int getDispThreshold() {
    return dispThreshold;
  }
  public int getAsda() {
    return asda;
  }
  public int getToda() {
    return toda;
  }
  public int getTora() {
    return tora;
  }
  public int getLda() {
    return lda;
  }

  public void setCurrAsda(int currAsda) {
    this.currAsda = currAsda;
  }
  public void setCurrLda(int currLda) {
    this.currLda = currLda;
  }
  public void setCurrToda(int currToda) {
    this.currToda = currToda;
  }
  public void setCurrTora(int currTora) {
    this.currTora = currTora;
  }


}
