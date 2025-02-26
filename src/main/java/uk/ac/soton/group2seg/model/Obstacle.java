package uk.ac.soton.group2seg.model;

/**
 * @author louistownsend
 */
public class Obstacle {
  private int height;
  private int width;
  private int distLeftThreshold;
  private int centreOffset;

  public Obstacle(){

  }

  public Obstacle(int height, int width, int distLeftThreshold, int centreOffset) {
    this.height = height;
    this.width = width;
    this.distLeftThreshold = distLeftThreshold;
    this.centreOffset = centreOffset;
  }
}
