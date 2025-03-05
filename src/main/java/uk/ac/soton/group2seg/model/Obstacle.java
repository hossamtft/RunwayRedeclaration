package uk.ac.soton.group2seg.model;

/**
 * @author louistownsend
 */
public class Obstacle {
  private int height;
  private int distLowerThreshold;
  private int distHigherThreshold;
  private int centreOffset;

  public Obstacle(){}

  public Obstacle(int height, int width, int distLowerThreshold, int distHigherThreshold, int centreOffset) {
    this.height = height;
    this.distLowerThreshold = distLowerThreshold;
    this.distHigherThreshold = distHigherThreshold;
    this.centreOffset = centreOffset;
  }
  public int getHeight() {
    return height;
  }
  public int getDistLowerThreshold() {
    return distLowerThreshold;
  }
  public int getCentreOffset() {
    return centreOffset;
  }
  public int getDistHigherThreshold() {
    return distHigherThreshold;
  }

}
