package model;

/**
 * The interface for a single pdf color.
 *
 * @author Claudius Korzen
 */
public interface PdfColor extends HasPdfDocument, HasId, Serializable {  
  /**
   * Returns true, if this color is equal to white.
   */
  public boolean isWhite();

  /**
   * Returns true, if this color is equal to white, with respect to the given 
   * tolerance.
   */
  public boolean isWhite(float tolerance);
}