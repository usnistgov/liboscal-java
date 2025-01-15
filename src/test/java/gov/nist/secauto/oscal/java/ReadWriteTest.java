/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.java;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.DefaultBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IDeserializer;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.oscal.lib.model.Catalog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.umd.cs.findbugs.annotations.NonNull;

class ReadWriteTest {
  private static final Logger LOGGER = LogManager.getLogger(ReadWriteTest.class);

  private static final int WARMUP_ITERATIONS = 3;
  private static final int ITERATIONS = WARMUP_ITERATIONS + 2;
  // private static final int ITERATIONS = 1;

  @NonNull
  private static <CLASS extends IBoundObject> CLASS measureDeserializer(
      @NonNull String format,
      @NonNull Path file,
      @NonNull IDeserializer<CLASS> deserializer,
      int iterations) throws IOException {

    if (iterations < 1) {
      throw new IllegalArgumentException(
          String.format("Illegal iteration value '%d'. The value must be greater than zero.",
              iterations));
    }

    CLASS retval = null;
    long totalTime = 0;
    int totalIterations = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      retval = deserializer.deserialize(file);
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1_000_000;
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(String.format("%s read in %d milliseconds from %s", format, timeElapsed, file));
      }

      // skip initial executions, if possible, to allow for JVM warmup
      if (i >= WARMUP_ITERATIONS) {
        totalTime += timeElapsed;
        ++totalIterations;
      }
    }
    if (totalIterations > 1 && LOGGER.isInfoEnabled()) {
      long average = totalTime / totalIterations;
      LOGGER.info(String.format("%s read in %d milliseconds (on average) from %s", format, average, file));
    }

    assert retval != null;

    return retval;
  }

  private static <CLASS extends IBoundObject> void measureSerializer(
      @NonNull CLASS root,
      @NonNull String format,
      @NonNull Path file,
      @NonNull ISerializer<CLASS> serializer,
      int iterations) throws IOException {

    long totalTime = 0;
    int totalIterations = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      serializer.serialize(root, file);
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1_000_000;
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(String.format("%s written in %d milliseconds to %s", format, timeElapsed, file));
      }

      // skip initial executions, if possible, to allow for JVM warmup
      if (i >= WARMUP_ITERATIONS) {
        totalTime += timeElapsed;
        ++totalIterations;
      }
    }

    if (totalIterations > 1 && LOGGER.isInfoEnabled()) {
      long average = totalTime / totalIterations;
      LOGGER.info(String.format("%s written in %d milliseconds (on average) to %s", format, average, file));
    }
  }

  private static <CLASS extends IBoundObject> void chainReadWrite(
      @NonNull Path xmlSource,
      @NonNull Class<CLASS> clazz,
      @NonNull Path tempDir,
      int iterations)
      throws IOException {

    CLASS obj;

    // XML
    {
      IBindingContext context = new DefaultBindingContext();
      IDeserializer<CLASS> deserializer = context.newDeserializer(Format.XML, clazz);
      deserializer.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);
      obj = measureDeserializer("XML", xmlSource, deserializer, iterations);
    }
    {
      IBindingContext context = new DefaultBindingContext();
      Path out = ObjectUtils.notNull(tempDir.resolve("out.xml"));
      ISerializer<CLASS> serializer = context.newSerializer(Format.XML, clazz);
      measureSerializer(obj, "XML", out, serializer, iterations);
    }

    // JSON
    {
      Path out = ObjectUtils.notNull(tempDir.resolve("out.json"));
      {
        IBindingContext context = new DefaultBindingContext();
        ISerializer<CLASS> serializer = context.newSerializer(Format.JSON, clazz);
        measureSerializer(obj, "JSON", out, serializer, iterations);
      }
      {
        IBindingContext context = new DefaultBindingContext();
        IDeserializer<CLASS> deserializer = context.newDeserializer(Format.JSON, clazz);
        deserializer.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);
        obj = measureDeserializer("JSON", out, deserializer, iterations);
      }
    }

    // YAML
    {
      Path out = ObjectUtils.notNull(tempDir.resolve("out.yaml"));
      {
        IBindingContext context = new DefaultBindingContext();
        ISerializer<CLASS> serializer = context.newSerializer(Format.YAML, clazz);
        measureSerializer(obj, "YAML", out, serializer, iterations);
      }
      {
        IBindingContext context = new DefaultBindingContext();
        IDeserializer<CLASS> deserializer = context.newDeserializer(Format.YAML, clazz);
        deserializer.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);
        measureDeserializer("YAML", out, deserializer, iterations);
      }
    }
  }

  @Test
  void testOscalCatalogMetrics(@NonNull @TempDir Path tempDir) throws IOException {

    Path catalogSourceXml = ObjectUtils.notNull(
        Path.of("target/download/content/NIST_SP-800-53_rev5_catalog.xml"));
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Testing XML file: {}", catalogSourceXml);
    }
    assertTrue(Files.exists(catalogSourceXml), "The source file does not exist");

    // File outDir = new File("target/test-content");
    // outDir.mkdirs();
    // Path outPath = outDir.toPath();
    Path outPath = tempDir;
    chainReadWrite(catalogSourceXml, Catalog.class, outPath, ITERATIONS);
  }
}
