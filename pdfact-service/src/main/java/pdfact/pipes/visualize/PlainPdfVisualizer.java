package pdfact.pipes.visualize;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import pdfact.exception.PdfActVisualizeException;
import pdfact.model.Character;
import pdfact.model.Element;
import pdfact.model.Figure;
import pdfact.model.HasPosition;
import pdfact.model.HasPositions;
import pdfact.model.HasSemanticRole;
import pdfact.model.Page;
import pdfact.model.Paragraph;
import pdfact.model.PdfDocument;
import pdfact.model.Position;
import pdfact.model.Rectangle;
import pdfact.model.SemanticRole;
import pdfact.model.Shape;
import pdfact.model.TextBlock;
import pdfact.model.TextLine;
import pdfact.model.TextUnit;
import pdfact.model.Word;
import pdfact.pipes.visualize.PdfDrawer.PdfDrawerFactory;

/**
 * A plain implementation of {@link PdfVisualizer}.
 *
 * @author Claudius Korzen
 */
public class PlainPdfVisualizer implements PdfVisualizer {
  /**
   * The factory to create instance of {@link PdfDrawer}.
   */
  protected PdfDrawerFactory pdfDrawerFactory;

  /**
   * The text unit.
   */
  protected TextUnit textUnit;

  /**
   * The semantic roles to consider on visualizing.
   */
  protected Set<SemanticRole> rolesFilter;

  // ==========================================================================
  // Constructors.

  /**
   * Creates a new PDF visualizer.
   * 
   * @param pdfDrawerFactory
   *        The factory to create instances of {@link PdfDrawer}.
   */
  @AssistedInject
  public PlainPdfVisualizer(PdfDrawerFactory pdfDrawerFactory) {
    this.pdfDrawerFactory = pdfDrawerFactory;
  }

  /**
   * Creates a new PDF visualizer.
   * 
   * @param pdfDrawerFactory
   *        The factory to create instances of {@link PdfDrawer}.
   * @param textUnit
   *        The text unit.
   * @param rolesFilter
   *        The semantic roles filter.
   */
  @AssistedInject
  public PlainPdfVisualizer(PdfDrawerFactory pdfDrawerFactory,
      @Assisted TextUnit textUnit,
      @Assisted Set<SemanticRole> rolesFilter) {
    this(pdfDrawerFactory);
    this.textUnit = textUnit;
    this.rolesFilter = rolesFilter;
  }

  // ==========================================================================

  @Override
  public byte[] visualize(PdfDocument pdf) throws PdfActVisualizeException {
    if (pdf != null) {
      PdfDrawer drawer = this.pdfDrawerFactory.create(pdf.getFile());

      switch (this.textUnit) {
        case CHARACTER:
          visualizeCharacters(pdf, drawer);
          break;
        case WORD:
          visualizeWords(pdf, drawer);
          break;
        case PARAGRAPH:
        default:
          visualizeParagraphs(pdf, drawer);
          break;
      }
      
      try {
        return drawer.toByteArray();
      } catch (IOException e) {
        throw new PdfActVisualizeException("Error on visualization.", e);
      }
    }
    return null;
  }

  // ==========================================================================

  /**
   * Visualizes the paragraphs of the given PDF document.
   * 
   * @param pdf
   *        The PDF document to process.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeParagraphs(PdfDocument pdf, PdfDrawer drawer)
      throws PdfActVisualizeException {
    // Visualize the textual elements.
    for (Paragraph paragraph : pdf.getParagraphs()) {
      // Ignore the paragraph if its role doesn't match the roles filter.
      if (!hasRelevantRole(paragraph)) {
        continue;
      }

      visualizeParagraph(paragraph, drawer);
    }
  }

  /**
   * Visualizes the given paragraph.
   * 
   * @param paragraph
   *        The paragraph to visualize.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeParagraph(Paragraph paragraph, PdfDrawer drawer)
      throws PdfActVisualizeException {
    visualizePdfElement(paragraph, drawer, Color.RED);
  }

  // ==========================================================================

  /**
   * Visualizes the words of the given PDF document.
   * 
   * @param pdf
   *        The PDF document to process.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeWords(PdfDocument pdf, PdfDrawer drawer)
      throws PdfActVisualizeException {
    // Visualize the textual elements.
    for (Paragraph paragraph : pdf.getParagraphs()) {
      // Ignore the paragraph if its role doesn't match the roles filter.
      if (!hasRelevantRole(paragraph)) {
        continue;
      }

      for (Word word : paragraph.getWords()) {
        visualizeWord(word, drawer);
      }
    }
  }

  /**
   * Visualizes the given word.
   * 
   * @param word
   *        The word to visualize.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeWord(Word word, PdfDrawer drawer)
      throws PdfActVisualizeException {
    visualizePdfElement(word, drawer, Color.ORANGE);
  }

  // ==========================================================================

  /**
   * Visualizes the characters of the given PDF document.
   * 
   * @param pdf
   *        The PDF document to process.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeCharacters(PdfDocument pdf, PdfDrawer drawer)
      throws PdfActVisualizeException {
    // Visualize the textual elements.
    for (Paragraph paragraph : pdf.getParagraphs()) {
      // Ignore the paragraph if its role doesn't match the roles filter.
      if (!hasRelevantRole(paragraph)) {
        continue;
      }

      for (Word word : paragraph.getWords()) {
        if (word == null) {
          continue;
        }
        
        for (Character character : word.getCharacters()) {
          visualizeCharacter(character, drawer);
        }
      }
    }
  }

  /**
   * Visualizes the given character.
   * 
   * @param character
   *        The character to visualize.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeCharacter(Character character, PdfDrawer drawer)
      throws PdfActVisualizeException {
    visualizePdfElement(character, drawer, Color.BLACK);
  }

  // ==========================================================================

  /**
   * Visualizes the given figure.
   * 
   * @param figure
   *        The figure to visualize.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeFigure(Figure figure, PdfDrawer drawer)
      throws PdfActVisualizeException {
    visualizePdfElement(figure, drawer, Color.CYAN);
  }

  /**
   * Visualizes the given shape.
   * 
   * @param shape
   *        The shape to visualize.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeShape(Shape shape, PdfDrawer drawer)
      throws PdfActVisualizeException {
    visualizePdfElement(shape, drawer, Color.MAGENTA);
  }

  // ==========================================================================

  /**
   * Visualizes the given PDF element using the given drawer.
   * 
   * @param element
   *        The element to visualize.
   * @param drawer
   *        The drawer to use.
   * @param color
   *        The color to use.
   * @throws PdfActVisualizeException
   *         If something went wrong on visualization.
   */
  protected void visualizePdfElement(Element element, PdfDrawer drawer,
      Color color) throws PdfActVisualizeException {
    if (element instanceof HasPositions) {
      HasPositions hasPositions = (HasPositions) element;
      List<Position> positions = hasPositions.getPositions();

      visualizePositions(drawer, color, positions);
    }

    if (element instanceof HasPosition) {
      HasPosition hasPosition = (HasPosition) element;
      Position position = hasPosition.getPosition();

      visualizePositions(drawer, color, position);
    }
  }

  /**
   * Visualizes the given PDF positions with the given color.
   * 
   * @param drawer
   *        The drawer to use.
   * @param color
   *        The color to use.
   * @param positions
   *        The positions to visualize.
   * @throws PdfActVisualizeException
   *         If something went wrong on visualization.
   */
  protected void visualizePositions(PdfDrawer drawer, Color color,
      Position... positions) throws PdfActVisualizeException {
    visualizePositions(drawer, color, Arrays.asList(positions));
  }

  /**
   * Visualizes the given PDF positions with the given color.
   * 
   * @param drawer
   *        The drawer to use.
   * @param color
   *        The color to use.
   * @param positions
   *        The positions to visualize.
   * @throws PdfActVisualizeException
   *         If something went wrong on visualization.
   */
  protected void visualizePositions(PdfDrawer drawer, Color color,
      List<Position> positions) throws PdfActVisualizeException {
    if (drawer == null) {
      return;
    }

    if (positions == null) {
      return;
    }

    for (Position position : positions) {
      visualizePosition(position, drawer, color);
    }
  }

  /**
   * Visualizes the given PDF position.
   * 
   * @param position
   *        The position to visualize.
   * @param drawer
   *        The drawer to use.
   * @param color
   *        The color to use.
   * @throws PdfActVisualizeException
   *         If something went wrong on visualization.
   */
  private void visualizePosition(Position position, PdfDrawer drawer,
      Color color) throws PdfActVisualizeException {
    if (position != null) {
      Page page = position.getPage();
      Rectangle rect = position.getRectangle();

      if (page != null && rect != null) {
        int pageNum = page.getPageNumber();
        try {
          drawer.drawBoundingBox(rect, pageNum, color, null, 1f);
        } catch (IOException e) {
          throw new PdfActVisualizeException(
              "Couldn't visualize the PDF document", e);
        }
      }
    }
  }

  // ==========================================================================

  @Override
  public TextUnit getTextUnit() {
    return this.textUnit;
  }

  @Override
  public void setTextUnit(TextUnit textUnit) {
    this.textUnit = textUnit;
  }

  // ==========================================================================

  @Override
  public Set<SemanticRole> getSemanticRolesFilter() {
    return this.rolesFilter;
  }

  @Override
  public void setSemanticRolesFilter(Set<SemanticRole> roles) {
    this.rolesFilter = roles;
  }

  // ==========================================================================

  /**
   * Checks if the semantic role of the given element matches the semantic roles
   * filter of this serializer.
   * 
   * @param element
   *        The element to check.
   * 
   * @return True, if the role of the given element matches the semantic roles
   *         filter of this serializer, false otherwise.
   */
  protected boolean hasRelevantRole(HasSemanticRole element) {
    if (element == null) {
      return false;
    }

    if (this.rolesFilter == null || this.rolesFilter.isEmpty()) {
      // No filter is given -> The paragraph is relevant.
      return true;
    }

    SemanticRole role = element.getSemanticRole();
    if (role == null) {
      return false;
    }

    return this.rolesFilter.contains(role);
  }
  
  // ==========================================================================
  
  /**
   * Visualizes the given text block.
   * 
   * @param block
   *        The text block to visualize.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeTextBlock(TextBlock block, PdfDrawer drawer)
      throws PdfActVisualizeException {
    visualizePdfElement(block, drawer, Color.BLUE);
  }
  
  /**
   * Visualizes the given text line.
   * 
   * @param line
   *        The text line to visualize.
   * @param drawer
   *        The drawer to use.
   * 
   * @throws PdfActVisualizeException
   *         If the drawing failed.
   */
  protected void visualizeTextLine(TextLine line, PdfDrawer drawer)
      throws PdfActVisualizeException {
    visualizePdfElement(line, drawer, Color.GREEN);
  }
}