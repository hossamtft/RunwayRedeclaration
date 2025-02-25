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

  public void LogicalRunway(char designator, int bearing, int asda, int toda, int tora, int lda, int dispThreshold) {
    this.designator = designator;
    this.bearing = bearing;
    this.asda = asda;
    this.toda = toda;
    this.lda = lda;
    //Add displaced threshold getter
  }


}
