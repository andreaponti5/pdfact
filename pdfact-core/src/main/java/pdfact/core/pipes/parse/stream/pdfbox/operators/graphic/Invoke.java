package pdfact.core.pipes.parse.stream.pdfbox.operators.graphic;

import static pdfact.core.PdfActCoreSettings.FLOATING_NUMBER_PRECISION;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import pdfact.core.model.Color;
import pdfact.core.model.Figure;
import pdfact.core.model.Page;
import pdfact.core.model.PdfDocument;
import pdfact.core.model.Point;
import pdfact.core.model.Position;
import pdfact.core.model.Shape;
import pdfact.core.pipes.parse.stream.pdfbox.operators.OperatorProcessor;
import pdfact.core.pipes.parse.stream.pdfbox.utils.ColorUtils;
import pdfact.core.util.PdfActUtils;

/**
 * Do: Invoke a named xobject.
 *
 * @author Claudius Korzen
 */
public class Invoke extends OperatorProcessor {
  /**
   * The logger.
   */
  protected static Logger log = LogManager.getLogger(Invoke.class);

  // ==============================================================================================

  @Override
  public void process(PdfDocument pdf, Page page, Operator op,
      List<COSBase> args) throws IOException {
    // Get the name of the PDXOject.
    COSName name = (COSName) args.get(0);

    // Get the PDXObject.
    PDXObject xobject = this.engine.getResources().getXObject(name);

    // if (xobject instanceof PDFormXObject) {
    // PDFormXObject form = (PDFormXObject) xobject;
    //
    // // if there is an optional form matrix, we have to map the form space to
    // // the user space
    // Matrix matrix = form.getMatrix();
    // if (matrix != null) {
    // Matrix xCTM = matrix.multiply(context.getCurrentTransformationMatrix());
    // context.getGraphicsState().setCurrentTransformationMatrix(xCTM);
    //
    // // Transform PDRectangle => SimpleRectangle.
    // PDRectangle rectangle = form.getBBox();
    // Rectangle boundingBox = new SimpleRectangle();
    //
    // boundingBox.setMinX(rectangle.getLowerLeftX());
    // boundingBox.setMinY(rectangle.getLowerLeftY());
    // boundingBox.setMaxX(rectangle.getUpperRightX());
    // boundingBox.setMaxY(rectangle.getUpperRightY());
    //
    // context.showForm(boundingBox);
    // }
    // // find some optional resources, instead of using the current resources
    // context.processStream(form);
    // } else

    if (xobject instanceof PDFormXObject) {
      PDFormXObject form = (PDFormXObject) xobject;

      // if there is an (optional) form matrix, we have to map the form space to the user space
      Matrix matrix = form.getMatrix();

      if (matrix != null) {
        this.engine.getGraphicsState().getCurrentTransformationMatrix().concatenate(matrix);
      }

      float formWidth = form.getBBox().getWidth();
      float formHeight = form.getBBox().getHeight();

      Matrix ctm = this.engine.getGraphicsState().getCurrentTransformationMatrix().clone();

      // TODO: Check if ur and ll are indeed ur and ll.
      float minX = ctm.getTranslateX();
      float minY = ctm.getTranslateY();
      float maxX = minX + ctm.getScaleX() * formWidth;
      float maxY = minY + ctm.getScaleY() * formHeight;

      // Round the values.
      minX = PdfActUtils.round(minX, FLOATING_NUMBER_PRECISION);
      minY = PdfActUtils.round(minY, FLOATING_NUMBER_PRECISION);
      maxX = PdfActUtils.round(maxX, FLOATING_NUMBER_PRECISION);
      maxY = PdfActUtils.round(maxY, FLOATING_NUMBER_PRECISION);

      Point ll = new Point(minX, minY);
      Point ur = new Point(maxX, maxY);
      Position position = new Position(page, ll, ur);

      // TODO: A PDFormXObject isn't necessarily a figure (but can be).
      Figure figure = new Figure();
      figure.setPosition(position);
      this.engine.handlePdfFigure(pdf, page, figure);
    } else if (xobject instanceof PDImageXObject) {
      PDImageXObject image = (PDImageXObject) xobject;

      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();

      Matrix ctm = this.engine.getCurrentTransformationMatrix().clone();
      AffineTransform ctmAT = ctm.createAffineTransform();
      ctmAT.scale(1f / imageWidth, 1f / imageHeight);
      Matrix at = new Matrix(ctmAT);

      // TODO: Check if ur and ll are indeeed ur and ll.
      float minX = ctm.getTranslateX();
      float minY = ctm.getTranslateY();
      float maxX = minX + at.getScaleX() * imageWidth;
      float maxY = minY + at.getScaleY() * imageHeight;

      // Round the values.
      minX = PdfActUtils.round(minX, FLOATING_NUMBER_PRECISION);
      minY = PdfActUtils.round(minY, FLOATING_NUMBER_PRECISION);
      maxX = PdfActUtils.round(maxX, FLOATING_NUMBER_PRECISION);
      maxY = PdfActUtils.round(maxY, FLOATING_NUMBER_PRECISION);

      Point ll = new Point(minX, minY);
      Point ur = new Point(maxX, maxY);
      Position position = new Position(page, ll, ur);

      // If the image consists of only one color, consider it as a shape.
      // TODO: Manage the colors.
      float[] exclusiveColor = ColorUtils.getExclusiveColor(image.getImage());

      if (exclusiveColor != null) {
        Color color = new Color();
        color.setRGB(exclusiveColor);

        log.debug("The xobject consists only of the color " + color + ". "
            + "Considering it as a shape.");

        Shape shape = new Shape();
        shape.setPosition(position);
        shape.setColor(color);
        this.engine.handlePdfShape(pdf, page, shape);
      } else {
        log.debug("Considering the xobject as a figure.");

        Figure figure = new Figure();
        figure.setPosition(position);
        this.engine.handlePdfFigure(pdf, page, figure);
      }
    }
  }

  @Override
  public String getName() {
    return "Do";
  }
}
