package pdfact.cli.pipes.serialize;

import static pdfact.cli.PdfActCliSettings.DEFAULT_EXTRACTION_UNITS;
import static pdfact.cli.PdfActCliSettings.DEFAULT_SEMANTIC_ROLES;
import static pdfact.cli.PdfActCliSettings.DEFAULT_SERIALIZE_FORMAT;
import static pdfact.cli.PdfActCliSettings.DEFAULT_WITH_CONTROL_CHARACTERS;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pdfact.cli.model.ExtractionUnit;
import pdfact.cli.model.SerializationFormat;
import pdfact.cli.util.exception.PdfActSerializeException;
import pdfact.core.model.Document;
import pdfact.core.model.SemanticRole;
import pdfact.core.util.exception.PdfActException;

/**
 * A plain implementation of {@link SerializePdfPipe}.
 *
 * @author Claudius Korzen
 */
public class PlainSerializePdfPipe implements SerializePdfPipe {
  /**
   * The logger.
   */
  protected static Logger log = LogManager.getLogger(PlainSerializePdfPipe.class);

  /**
   * The serialization format.
   */
  protected SerializationFormat format;

  /**
   * The serialization target, given as a file.
   */
  protected Path targetPath;

  /**
   * The serialization target, given as a stream.
   */
  protected OutputStream targetStream;

  /**
   * The units to extract.
   */
  protected Set<ExtractionUnit> extractionUnits;

  /**
   * The semantic roles to include.
   */
  protected Set<SemanticRole> semanticRolesToInclude;

  /**
   * The boolean flag indicating whether or not this serializer should insert control
   * characters, i.e.: "^L" between two PDF elements in case a page break between the two elements
   * occurs in the PDF and "^A" in front of headings.
   */
  protected boolean withControlCharacters;

  /**
   * The boolean flag indicating whether or not the pdf.js mode is enabled.
   */
  protected boolean isPdfJsMode;

  // ==============================================================================================

  /**
   * The default constructor.
   */
  public PlainSerializePdfPipe() {
    this.format = DEFAULT_SERIALIZE_FORMAT;
    this.extractionUnits = DEFAULT_EXTRACTION_UNITS;
    this.semanticRolesToInclude = DEFAULT_SEMANTIC_ROLES;
    this.withControlCharacters = DEFAULT_WITH_CONTROL_CHARACTERS;
  }

  // ==============================================================================================

  @Override
  public Document execute(Document pdf) throws PdfActException {
    log.debug("Start of pipe: " + getClass().getSimpleName() + ".");

    log.debug("Process: Serializing the PDF document.");
    serialize(pdf);

    log.debug("Serializing the PDF document done.");
    log.debug("serialization format: " + this.format);
    log.debug("extraction units: " + this.extractionUnits);
    log.debug("semantic roles: " + this.semanticRolesToInclude);

    log.debug("End of pipe: " + getClass().getSimpleName() + ".");

    return pdf;
  }

  /**
   * Serializes the given PDF document.
   *
   * @param pdf The PDf document to serialize.
   * @throws PdfActException If something went wrong while serializing the PDF document.
   */
  protected void serialize(Document pdf) throws PdfActException {
    // Instantiate a serializer.
    PdfSerializer serializer;

    switch (this.format) {
      case XML:
        serializer = new PdfXmlSerializer(this.extractionUnits, this.semanticRolesToInclude);
        break;
      case JSON:
        serializer = new PdfJsonSerializer(this.extractionUnits, this.semanticRolesToInclude);
        break;
      case TXT:
        serializer = new PdfTxtSerializer(this.withControlCharacters, this.extractionUnits,
                this.semanticRolesToInclude);
        break;
      default:
        throw new PdfActSerializeException(
                "Couldn't find a serializer for the format '" + this.format + "'.");
    }

    // Use a specific serializer when the pdf.js mode is enabled.
    if (this.isPdfJsMode()) {
      serializer = new PdfJsSerializer();
    }

    // Serialize the PDF document.
    byte[] serialization = serializer.serialize(pdf);

    // If the target is given as a stream, write the serialization it.
    if (this.targetStream != null) {
      writeToStream(serialization, this.targetStream);
    }

    // If the target is given as a file, open it and write the serialization.
    if (this.targetPath != null) {
      writeToPath(serialization, this.targetPath);
    }
  }

  /**
   * Writes the given bytes to the given output stream.
   *
   * @param bytes  The bytes to write.
   * @param stream The stream to write to.
   * @throws PdfActSerializeException If something went wrong while writing the bytes to the stream.
   */
  protected void writeToStream(byte[] bytes, OutputStream stream) throws PdfActSerializeException {
    try {
      stream.write(bytes);
    } catch (IOException e) {
      throw new PdfActSerializeException("Couldn't write to output stream.", e);
    }
  }

  /**
   * Writes the given bytes to the given file.
   *
   * @param bytes The bytes to write.
   * @param path  The file to write to.
   * @throws PdfActSerializeException If something went wrong while writing the bytes to the stream.
   */
  protected void writeToPath(byte[] bytes, Path path) throws PdfActSerializeException {
    try (OutputStream os = Files.newOutputStream(path)) {
      os.write(bytes);
    } catch (IOException e) {
      throw new PdfActSerializeException("Couldn't write to file.");
    }
  }

  // ==============================================================================================

  @Override
  public Set<ExtractionUnit> getExtractionUnits() {
    return this.extractionUnits;
  }

  @Override
  public void setExtractionUnits(Set<ExtractionUnit> units) {
    this.extractionUnits = units;
  }

  // ==============================================================================================

  @Override
  public Set<SemanticRole> getSemanticRolesToInclude() {
    return this.semanticRolesToInclude;
  }

  @Override
  public void setSemanticRolesToInclude(Set<SemanticRole> roles) {
    this.semanticRolesToInclude = roles;
  }

  // ==============================================================================================

  @Override
  public SerializationFormat getSerializationFormat() {
    return this.format;
  }

  @Override
  public void setSerializationFormat(SerializationFormat format) {
    this.format = format;
  }

  // ==============================================================================================

  @Override
  public OutputStream getTargetStream() {
    return this.targetStream;
  }

  @Override
  public void setTargetStream(OutputStream stream) {
    this.targetStream = stream;
  }

  // ==============================================================================================

  @Override
  public Path getTargetPath() {
    return this.targetPath;
  }

  @Override
  public void setTargetPath(Path path) {
    this.targetPath = path;
  }

  // ==============================================================================================

  @Override
  public boolean isWithControlCharacters() {
    return this.withControlCharacters;
  }

  @Override
  public void setWithControlCharacters(boolean withControlCharacters) {
    this.withControlCharacters = withControlCharacters;
  }

  // ==============================================================================================

  @Override
  public boolean isPdfJsMode() {
    return this.isPdfJsMode;
  }

  @Override
  public void setIsPdfJsMode(boolean isPdfJsMode) {
    this.isPdfJsMode = isPdfJsMode;
  }
}
