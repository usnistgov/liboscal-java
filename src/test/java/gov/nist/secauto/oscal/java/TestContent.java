/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.oscal.java;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.IDeserializer;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.oscal.lib.model.Catalog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class TestContent {
  private static final Logger LOGGER = LogManager.getLogger(TestContent.class);

  private enum OperationType {
    SERIALIZE,
    DESERIALIZE;
  }

  private static <CLASS> CLASS measureDeserializer(String format, File file, IDeserializer<CLASS> deserializer,
      int iterations) throws IOException {
    CLASS retval = null;
    long totalTime = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      retval = deserializer.deserialize(file);
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1_000_000;
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(String.format("%s read in %d milliseconds from %s", format, timeElapsed, file));
      }
      totalTime += timeElapsed;
    }
    long average = totalTime / iterations - 1;
    if (iterations > 1 && LOGGER.isInfoEnabled()) {
      LOGGER.info(String.format("%s read in %d milliseconds (on average) from %s", format, average, file));
    }
    return retval;
  }

  private static <CLASS> void measureSerializer(CLASS root, String format, File file, ISerializer<CLASS> serializer,
      int iterations) throws IOException {
    long totalTime = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      serializer.serialize(root, file);
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1_000_000;
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(String.format("%s written in %d milliseconds to %s", format, timeElapsed, file));
      }
      if (iterations == 1 || i > 0) {
        totalTime += timeElapsed;
      }
    }

    long average = totalTime / (iterations == 1 ? 1 : iterations - 1);
    if (iterations > 1 && LOGGER.isInfoEnabled()) {
      LOGGER.info(String.format("%s written in %d milliseconds (on average) to %s", format, average, file));
    }
  }

  private static <CLASS> void chainReadWrite(File xmlSource, Class<CLASS> clazz, Path tempDir, int iterations)
      throws IOException {
    IBindingContext context = IBindingContext.newInstance();

    CLASS obj;

    // XML
    {
      IDeserializer<CLASS> deserializer = context.newDeserializer(Format.XML, clazz);
      obj = measureDeserializer("XML", xmlSource, deserializer, iterations);

      File out = new File(tempDir.toFile(), "out.xml");
      ISerializer<CLASS> serializer = context.newSerializer(Format.XML, clazz);
      measureSerializer(obj, "XML", out, serializer, iterations);
    }

    // JSON
    {
      File out = new File(tempDir.toFile(), "out.json");
      ISerializer<CLASS> serializer = context.newSerializer(Format.JSON, clazz);
      measureSerializer(obj, "JSON", out, serializer, iterations);

      IDeserializer<CLASS> deserializer = context.newDeserializer(Format.JSON, clazz);
      obj = measureDeserializer("JSON", out, deserializer, iterations);
    }

    // YAML
    {
      File out = new File(tempDir.toFile(), "out.yml");
      ISerializer<CLASS> serializer = context.newSerializer(Format.YAML, clazz);
      measureSerializer(obj, "YAML", out, serializer, iterations);

      IDeserializer<CLASS> deserializer = context.newDeserializer(Format.YAML, clazz);
      measureDeserializer("YAML", out, deserializer, iterations);
    }
  }

  @Test
  public void testOscalCatalogMetrics(@TempDir Path tempDir) throws IOException, BindingException {

    File catalogSourceXml = new File("target/download/content/NIST_SP-800-53_rev5_catalog.xml");
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Testing XML file: {}", catalogSourceXml.getName());
    }
    assertTrue(catalogSourceXml.exists());

//    File outDir = new File("target/test-content");
//    outDir.mkdirs();
//    Path outPath = outDir.toPath();
    Path outPath = tempDir;
//    chainReadWrite(catalogSourceXml, Catalog.class, tempDir, 50);
    chainReadWrite(catalogSourceXml, Catalog.class, outPath, 1);
  }
}
