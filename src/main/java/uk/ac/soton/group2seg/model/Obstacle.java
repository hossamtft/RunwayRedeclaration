package uk.ac.soton.group2seg.model;

/**
 * @author louistownsend
 */
public class Obstacle {
  private int height;
  private int width;
  private int distLeftThreshold;
  private int distRightThreshold;
  private int centreOffset;

  public Obstacle(){

  }

  public Obstacle(int height, int width, int distLeftThreshold, int centreOffset) {
    this.height = height;
    this.width = width;
    this.distLeftThreshold = distLeftThreshold;
    this.centreOffset = centreOffset;
  }
  public int getHeight() {
    return height;
  }
  public void setHeight(int height) {
    this.height = height;
  }
  public int getWidth() {
    return width;
  }
  public void setWidth(int width) {
    this.width = width;
  }
  public int getDistLeftThreshold() {
    return distLeftThreshold;
  }
  public int getCentreOffset() {
    return centreOffset;
  }
  public int getDistRightThreshold() {
    return distRightThreshold;
  }

}
