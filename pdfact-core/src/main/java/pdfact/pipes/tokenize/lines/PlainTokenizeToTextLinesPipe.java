package pdfact.pipes.tokenize.lines;

import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import pdfact.model.Character;
import pdfact.model.CharacterStatistic;
import pdfact.model.Line;
import pdfact.model.Line.LineFactory;
import pdfact.model.Page;
import pdfact.model.PdfDocument;
import pdfact.model.Position;
import pdfact.model.Position.PositionFactory;
import pdfact.model.Rectangle;
import pdfact.model.Rectangle.RectangleFactory;
import pdfact.model.TextArea;
import pdfact.model.TextLine;
import pdfact.model.TextLine.TextLineFactory;
import pdfact.util.comparator.MinXComparator;
import pdfact.util.counter.FloatCounter;
import pdfact.util.exception.PdfActException;
import pdfact.util.lexicon.CharacterLexicon;
import pdfact.util.list.CharacterList;
import pdfact.util.list.TextLineList;
import pdfact.util.list.TextLineList.TextLineListFactory;
import pdfact.util.statistician.CharacterStatistician;
import pdfact.util.statistician.TextLineStatistician;
import pdfact.util.xycut.XYCut;

/**
 * A plain implementation of {@link TokenizeToTextLinesPipe}.
 * 
 * @author Claudius Korzen
 */
public class PlainTokenizeToTextLinesPipe extends XYCut
    implements TokenizeToTextLinesPipe {
  /**
   * The factory to create instances of {@link TextLineList}.
   */
  protected TextLineListFactory textLineListFactory;

  /**
   * The factory to create instances of {@link TextLine}.
   */
  protected TextLineFactory textLineFactory;

  /**
   * The factory to create instances of {@link Position}.
   */
  protected PositionFactory positionFactory;

  /**
   * The factory to create instances of {@link Rectangle}.
   */
  protected RectangleFactory rectangleFactory;

  /**
   * The factory to create instances of {@link Line}.
   */
  protected LineFactory lineFactory;

  /**
   * The statistician to compute statistics about characters.
   */
  protected CharacterStatistician characterStatistician;

  /**
   * The statistician to compute the statistics about text lines.
   */
  protected TextLineStatistician textLineStatistician;

  /**
   * Creates a new text line tokenizer.
   * 
   * @param textLineListFactory
   *        The factory to create instances of {@link TextLineList}.
   * @param textLineFactory
   *        The factory to create instances of {@link TextLine}.
   * @param positionFactory
   *        The factory to create instances of {@link Position}.
   * @param rectangleFactory
   *        The factory to create instances of {@link Rectangle}.
   * @param lineFactory
   *        The factory to create instances of {@link Line}.
   * @param characterStatistician
   *        The statistician to compute statistics about characters.
   * @param textLineStatistician
   *        The statistician to compute statistics about text lines.
   */
  @Inject
  public PlainTokenizeToTextLinesPipe(
      TextLineListFactory textLineListFactory,
      TextLineFactory textLineFactory,
      PositionFactory positionFactory,
      RectangleFactory rectangleFactory,
      LineFactory lineFactory,
      CharacterStatistician characterStatistician,
      TextLineStatistician textLineStatistician) {
    this.textLineListFactory = textLineListFactory;
    this.textLineFactory = textLineFactory;
    this.positionFactory = positionFactory;
    this.rectangleFactory = rectangleFactory;
    this.lineFactory = lineFactory;
    this.characterStatistician = characterStatistician;
    this.textLineStatistician = textLineStatistician;
  }

  // ==========================================================================

  @Override
  public PdfDocument execute(PdfDocument pdf) throws PdfActException {
    tokenizeToTextLines(pdf);
    return pdf;
  }

  // ==========================================================================

  /**
   * Tokenizes the text areas in the pages of the given PDF document into text
   * lines.
   * 
   * @param pdf
   *        The PDF document to process.
   * 
   * @throws PdfActException
   *         If something went wrong while tokenization.
   */
  protected void tokenizeToTextLines(PdfDocument pdf) throws PdfActException {
    if (pdf == null) {
      return;
    }

    List<Page> pages = pdf.getPages();
    if (pages == null) {
      return;
    }

    for (Page page : pages) {
      if (page == null) {
        continue;
      }

      TextLineList textLines = tokenizeToTextLines(pdf, page);
      page.setTextLineStatistic(this.textLineStatistician.compute(textLines));
      page.setTextLines(textLines);
    }
    pdf.setTextLineStatistic(this.textLineStatistician.aggregate(pages));
  }

  /**
   * Tokenizes the text areas in the given page into text lines.
   * 
   * @param pdf
   *        The PDF document to which the given page belongs to.
   * @param page
   *        The PDF page to process.
   * 
   * @return The list of text lines.
   * 
   * @throws PdfActException
   *         If something went wrong while tokenization.
   */
  protected TextLineList tokenizeToTextLines(PdfDocument pdf, Page page)
      throws PdfActException {
    TextLineList result = this.textLineListFactory.create();

    for (TextArea area : page.getTextAreas()) {
      List<CharacterList> charLists = cut(pdf, page, area.getCharacters());

      for (CharacterList charList : charLists) {
        // Create a PdfTextLine object.
        TextLine textLine = this.textLineFactory.create();
        textLine.setCharacters(charList);
        textLine.setBaseline(computeBaseline(charList));
        textLine.setCharacterStatistic(computeCharacterStatistic(charList));
        textLine.setPosition(computePosition(page, charList));
        result.add(textLine);
      }
    }

    return result;
  }

  // ==========================================================================
  
  /**
   * Computes the baseline from the given characters of a line.
   * 
   * @param characters
   *        The list of characters to process.
   * @return The computed baseline.
   */
  protected Line computeBaseline(CharacterList characters) {
    Line baseLine = null;
    // TODO: Inject!
    FloatCounter minYCounter = new FloatCounter();

    if (characters != null && !characters.isEmpty()) {
      Collections.sort(characters, new MinXComparator());

      float minX = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE;
      for (Character character : characters) {
        if (CharacterLexicon.isBaselineCharacter(character)) {
          minYCounter.add(character.getPosition().getRectangle().getMinY());
        }

        minX = Math.min(minX, character.getPosition().getRectangle().getMinX());
        maxX = Math.max(maxX, character.getPosition().getRectangle().getMaxX());
      }

      if (!minYCounter.isEmpty()) {
        float minY = minYCounter.getMostCommonFloat();
        baseLine = this.lineFactory.create(minX, minY, maxX, minY);
      }
    }

    return baseLine;
  }
  
  /**
   * Computes the character statistics for the given text line.
   * 
   * @param chars
   *        The characters to process.
   * @return The character statistics for the given text line.
   */
  protected CharacterStatistic computeCharacterStatistic(CharacterList chars) {
    return this.characterStatistician.compute(chars);
  }

  /**
   * Computes the position for the given text line.
   * 
   * @param page
   *        The PDF page in which the line is located.
   * @param chars
   *        The characters of the text line.
   * @return The position for the given text line.
   */
  protected Position computePosition(Page page, CharacterList chars) {
    Rectangle rect = this.rectangleFactory.fromHasPositionElements(chars);
    return this.positionFactory.create(page, rect);
  }
  
  // ==========================================================================

  @Override
  public float assessVerticalCut(PdfDocument pdf, Page page,
      List<CharacterList> halves) {
    return -1;
  }

  // ==========================================================================

  @Override
  public float assessHorizontalCut(PdfDocument pdf, Page page,
      List<CharacterList> halves) {
    CharacterList upper = halves.get(0);
    CharacterStatistic upperStats = this.characterStatistician.compute(upper);
    float upperMinY = upperStats.getSmallestMinY();

    CharacterList lower = halves.get(1);
    CharacterStatistic lowerStats = this.characterStatistician.compute(lower);
    float lowerMaxY = lowerStats.getLargestMaxY();

    return upperMinY - lowerMaxY;
  }
}