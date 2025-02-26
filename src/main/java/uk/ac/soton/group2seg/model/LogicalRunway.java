package uk.ac.soton.group2seg.model;

/**
 * @author louistownsend
 */
public class LogicalRunway {

  private char designator;
  private int bearing;
  private int asda;
  private int toda;
  private int tora;
  private int lda;

  private int dispThreshold = 0;

  private int currAsda;
  private int currToda;
  private int currTora;
  private int currLda;

  private Obstacle obstacle;

  public LogicalRunway(char designator, int bearing, int asda, int toda, int tora, int lda, int dispThreshold) {
    this.designator = designator;
    this.bearing = bearing;
    this.asda = asda;
    this.toda = toda;
    this.lda = lda;
    //Add displaced threshold getter

    // At start Current values would be the Default Values
    this.currAsda = asda;
    this.currToda = toda;
    this.currTora = tora;
    this.currLda = lda;
  }

  public char getDesignator() {
    return designator;
  }
  public int getBearing() {
    return bearing;
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
  public int getDispThreshold() {
    return dispThreshold;
  }
  public int getCurrAsda() {
    return currAsda;
  }
  public int getCurrToda() {
    return currToda;
  }

  public int getCurrTora() {
    return currTora;
  }
  public int getCurrLda() {
    return currLda;
  }
  public Obstacle getObstacle() {
    return obstacle;
  }
  public void setAsda(int asda) {
    this.currAsda = asda;
  }
  public void setToda(int toda) {
    this.currToda = toda;
  }
  public void setTora(int tora) {
    this.currTora = tora;
  }
  public void setLda(int lda) {
    this.currLda = lda;
  }


}
