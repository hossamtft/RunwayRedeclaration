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

  private void initialise() {
    currAsda = asda;
    currLda = lda;
    currToda = toda;
    currTora = tora;

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


}
