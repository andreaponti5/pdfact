package icecite.utils.comparators;

import java.util.Comparator;

import icecite.models.HasBoundingBox;
import icecite.utils.geometric.Rectangle;

/**
 * A comparator that compares rectangle by their minX values.
 * 
 * @author Claudius Korzen
 */
public class MinXComparator implements Comparator<HasBoundingBox> {
  @Override
  public int compare(HasBoundingBox box1, HasBoundingBox box2) {
    if (box1 == null && box2 == null) {
      return 0;
    }
    if (box1 == null) {
      return 1;
    }
    if (box2 == null) {
      return -1;
    }

    Rectangle rect1 = box1.getBoundingBox();
    Rectangle rect2 = box2.getBoundingBox();
    if (rect1 == null && rect2 == null) {
      return 0;
    }
    if (rect1 == null) {
      return 1;
    }
    if (rect2 == null) {
      return -1;
    }

    return Float.compare(rect1.getMinX(), rect2.getMinX());
  }
}
