package uk.ac.soton.group2seg.model;

/**
 * @author louistownsend
 */
public class LogicalRunway {
  private String name;
  private String designator;
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

  public LogicalRunway(){

  }

  public LogicalRunway(String designator, int bearing, int asda, int toda, int tora, int lda) {
    this.designator = designator;
    this.bearing = bearing;
    this.asda = asda;
    this.toda = toda;
    this.lda = lda;
    this.dispThreshold = tora - lda;

    initialise();
  }

  private void initialise() {
    currAsda = asda;
    currLda = lda;
    currToda = toda;
    currTora = tora;

    name = bearing + designator;
  }


  public String getName() {
    return name;
  }

  public String getDesignator() {
    return designator;
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
