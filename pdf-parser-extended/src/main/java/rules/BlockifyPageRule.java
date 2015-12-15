package rules;

import static model.SweepDirection.HorizontalSweepDirection.TOP_TO_BOTTOM;
import static model.SweepDirection.VerticalSweepDirection.LEFT_TO_RIGHT;

import de.freiburg.iif.model.Rectangle;
import model.PdfArea;
import model.PdfDocument;
import model.SweepDirection.HorizontalSweepDirection;
import model.SweepDirection.VerticalSweepDirection;

/**
 * The rules to blockify a page into several "blocks".
 *
 * @author Claudius Korzen
 */
public class BlockifyPageRule implements BlockifyRule {
  /** The sweep direction for the horizontal lane. */
  protected HorizontalSweepDirection horizontalLaneSweepDirection;
  /** The sweep direction for the vertical lane. */
  protected VerticalSweepDirection verticalLaneSweepDirection;

  /** 
   * The default constructor. 
   */
  public BlockifyPageRule() {
    this.horizontalLaneSweepDirection = TOP_TO_BOTTOM;
    this.verticalLaneSweepDirection = LEFT_TO_RIGHT;
  }
  
  /**
   * The default constructor.
   */
  public BlockifyPageRule(HorizontalSweepDirection horizontalSweepDirection, 
      VerticalSweepDirection verticalSweepDirection) {
    this.horizontalLaneSweepDirection = horizontalSweepDirection;
    this.verticalLaneSweepDirection = verticalSweepDirection;
  }
  
  @Override
  public VerticalSweepDirection getVerticalLaneSweepDirection() {
    return this.verticalLaneSweepDirection;
  }

  @Override
  public float getVerticalLaneWidth(PdfArea area) {
    // Ideally, we should use most common values here. But doing so fails
    // for grotoap-20902190.pdf - because of so many dots on page 3 and 4.
    PdfDocument doc = area.getPdfDocument();
    
    float docWidths = doc.getDimensionStatistics().getAverageWidth();
    float pageWidths = area.getDimensionStatistics().getAverageWidth();
    
    return 2.5f * Math.max(docWidths, pageWidths);
  }

  @Override
  public boolean isValidVerticalLane(PdfArea area, Rectangle lane) {    
    return area.getElementsOverlappedBy(lane).isEmpty();
  }

  @Override
  public HorizontalSweepDirection getHorizontalLaneSweepDirection() {
    return this.horizontalLaneSweepDirection;
  }

  @Override
  public float getHorizontalLaneHeight(PdfArea area) {
    // Ideally, we should use most common values here. But doing so fails
    // for grotoap-20902190.pdf - because of so many dots on page 3 and 4.
    PdfDocument doc = area.getPdfDocument();
    float docWidths = doc.getDimensionStatistics().getAverageHeight();
    float pageWidths = area.getDimensionStatistics().getAverageHeight();
        
    return 1.2f * Math.max(docWidths, pageWidths);
  }

  @Override
  public boolean isValidHorizontalLane(PdfArea area, Rectangle lane) {
    return isValidVerticalLane(area, lane);
  }
}
