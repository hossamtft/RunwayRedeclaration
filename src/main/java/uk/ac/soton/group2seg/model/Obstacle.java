package uk.ac.soton.group2seg.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author louistownsend
 */
public class Obstacle {
  private final Logger logger = LogManager.getLogger(this.getClass());
  private int height;
  private int distLowerThreshold;
  private int distHigherThreshold;
  private int centreOffset;

  public Obstacle(){}

  public Obstacle(int height, int distLowerThreshold, int distHigherThreshold, int centreOffset) {
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
