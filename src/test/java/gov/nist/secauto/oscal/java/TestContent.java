/**
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

/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.csrc.ns.oscal._1.Catalog;
import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.Format;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.io.MutableConfiguration;
import gov.nist.secauto.metaschema.binding.io.Serializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

class TestContent {
  private static final Logger logger = LogManager.getLogger(TestContent.class);

  private static <CLASS> CLASS measureDeserializer(String format, File file, Deserializer<CLASS> deserializer,
      int iterations) throws  BindingException, FileNotFoundException {
    CLASS retval = null;
    long totalTime = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      retval = deserializer.deserialize(file);
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1000000;
      logger.info(String.format("%s read in %d milliseconds from %s", format, timeElapsed, file));
      totalTime += timeElapsed;
    }
    long average = totalTime / iterations - 1;
    if (iterations > 1) {
      logger.info(String.format("%s read in %d milliseconds (on average) from %s", format, average, file));
    }
    return retval;
  }

  private static <CLASS> void measureSerializer(CLASS root, String format, File file, Serializer<CLASS> serializer,
      int iterations) throws BindingException, FileNotFoundException {
    long totalTime = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      serializer.serialize(root, file);
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1000000;
      logger.info(String.format("%s written in %d milliseconds to %s", format, timeElapsed, file));
      totalTime += timeElapsed;
    }
    long average = totalTime / iterations;
    if (iterations > 1) {
      logger.info(String.format("%s written in %d milliseconds (on average) to %s", format, average, file));
    }
  }

  private static <CLASS> void chainReadWrite(File xmlSource, Class<CLASS> clazz, Path tempDir, int iterations)
      throws BindingException, FileNotFoundException, IOException {
    BindingContext context = BindingContext.newInstance();
    MutableConfiguration config
        = new MutableConfiguration().enableFeature(Feature.SERIALIZE_ROOT).enableFeature(Feature.DESERIALIZE_ROOT);

    CLASS obj;

    // XML
    {
      Deserializer<CLASS> deserializer = context.newDeserializer(Format.XML, clazz, config);
      obj = measureDeserializer("XML", xmlSource, deserializer, iterations);

      File out = new File(tempDir.toFile(), "out.xml");
      Serializer<CLASS> serializer = context.newSerializer(Format.XML, clazz, config);
      measureSerializer(obj, "XML", out, serializer, iterations);
    }

    // JSON
    {
      File out = new File(tempDir.toFile(), "out.json");
      Serializer<CLASS> serializer = context.newSerializer(Format.JSON, clazz, config);
      measureSerializer(obj, "JSON", out, serializer, iterations);

      Deserializer<CLASS> deserializer = context.newDeserializer(Format.JSON, clazz, config);
      obj = measureDeserializer("JSON", out, deserializer, iterations);
    }

    // YAML
    {
      File out = new File(tempDir.toFile(), "out.yml");
      Serializer<CLASS> serializer = context.newSerializer(Format.YAML, clazz, config);
      measureSerializer(obj, "YAML", out, serializer, iterations);

      Deserializer<CLASS> deserializer = context.newDeserializer(Format.YAML, clazz, config);
      obj = measureDeserializer("YAML", out, deserializer, iterations);
    }
  }

  @Test
  public void testReadWriteOSCALCatalog(@TempDir Path tempDir) throws IOException, BindingException {

    File catalogSourceXml = new File("target/download/content/NIST_SP-800-53_rev4_catalog.xml");
    logger.info("Testing XML file: {}", catalogSourceXml.getName());
    assertTrue(catalogSourceXml.exists());

    File outDir = new File("target/test-content");
    outDir.mkdirs();
    tempDir = outDir.toPath();
    chainReadWrite(catalogSourceXml, Catalog.class, tempDir, 1);
  }

  @Test
  @Disabled
  public void testOSCALCatalogMetrics(@TempDir Path tempDir) throws IOException, BindingException {

    File catalogSourceXml = new File("target/download/content/NIST_SP-800-53_rev4_catalog.xml");
    logger.info("Testing XML file: {}", catalogSourceXml.getName());
    assertTrue(catalogSourceXml.exists());

    chainReadWrite(catalogSourceXml, Catalog.class, tempDir, 50);
  }
}
