package visualizer;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import drawer.PdfDrawer;
import drawer.pdfbox.PdfBoxDrawer;
import model.PdfArea;
import model.PdfDocument;
import model.PdfElement;
import model.PdfFeature;
import model.PdfPage;

/**
 * The default implmentation of a PdfVisualizer.
 *
 * @author Claudius Korzen
 *
 */
public class PlainPdfVisualizer implements PdfVisualizer {

  @Override
  public void visualize(PdfDocument document, OutputStream stream)
    throws IOException {
    visualize(document, null, stream);
  }

  @Override
  public void visualize(PdfDocument document, List<PdfFeature> features,
      OutputStream stream) throws IOException {
    PdfDrawer drawer = new PdfBoxDrawer(document.getPdfFile());

    // If the list of features is empty, take all features.
    if (features == null || features.isEmpty()) {
      features = Arrays.asList(PdfFeature.values());
    }
    
    for (PdfPage page : document.getPages()) {
      visualizePage(page, features, drawer);  
    }

    drawer.writeTo(stream);
  }

  // ___________________________________________________________________________

  /**
   * Visualizes the given features of the given document using the given drawer.
   */
  protected void visualizePage(PdfPage page, List<PdfFeature> features,
      PdfDrawer drawer) throws IOException {

    if (features == null) {
      return;
    }
    
    for (PdfFeature feature : features) {
      visualizeFeature(page, feature, drawer);
    }
    
//    for (PdfArea area : page.getBlocks()) {
//      visualizeElement(area, page, Color.BLACK, drawer);
//      drawer.drawText("" + area.getRectangle(), page.getPageNumber(), area.getRectangle().getLowerRight(), Color.BLACK, 2f);
//      drawer.drawRectangle(new SimpleRectangle(125.76056f,203.03093f,484.50726f,203.13094f), 4);
//    }
  }

  // ___________________________________________________________________________

  /**
   * Visualizes the paragraphs of the given document using the given drawer.
   */
  protected void visualizeFeature(PdfPage page, PdfFeature feature, 
      PdfDrawer drawer) throws IOException {
    if (feature == null) {
      return;
    }
    
    if (page == null) {
      return;
    }
    
    for (PdfElement element : page.getElementsByFeature(feature)) {
      visualizeElement(element, feature.getColor(), drawer);  
    }
  }

  // ___________________________________________________________________________

  /**
   * Visualizes the given list of rectangles using the given drawer.
   */
  protected void visualizeElement(PdfElement element,
      Color color, PdfDrawer drawer) throws IOException {
    visualizeElement(element, element.getPage(), color, drawer);
  }
  
  /**
   * Visualizes the given list of rectangles using the given drawer.
   */
  protected void visualizeElement(HasRectangle element, PdfPage page,
      Color color, PdfDrawer drawer) throws IOException {
    if (element == null) {
      return;
    }
    
    if (page != null) {
      int pageNumber = page.getPageNumber();
      drawer.drawRectangle(element.getRectangle(), pageNumber, color);
    }
  }
}
