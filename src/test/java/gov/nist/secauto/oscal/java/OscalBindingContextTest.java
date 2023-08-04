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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.SystemSecurityPlan;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

class OscalBindingContextTest {
  private static OscalBindingContext bindingContext;
  private static IBoundLoader loader;

  @BeforeAll
  static void initialize() { // NOPMD actually used
    bindingContext = OscalBindingContext.instance();
    loader = bindingContext.newBoundLoader();
  }

  @Test
  void testLoadCatalogYaml(@TempDir Path tempDir) throws IOException {
    // the YAML catalog is currently malformed, this will create a proper one for
    // this test
    Catalog catalog
        = loader.load(ObjectUtils.notNull(
            new File("target/download/content/NIST_SP-800-53_rev5_catalog.yaml").getCanonicalFile()));

    Path out = newPath(ObjectUtils.notNull(tempDir), "out-catalog.yaml");

    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.YAML, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));
  }

  @Test
  void testLoadCatalogJson(@TempDir Path tempDir) throws IOException {
    Catalog catalog
        = loader.load(ObjectUtils.notNull(
            new File("target/download/content/NIST_SP-800-53_rev5_catalog.json").getCanonicalFile()));
    assertNotNull(catalog);

    Path out = newPath(ObjectUtils.notNull(tempDir), "out.json");

    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.JSON, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));
  }

  @Test
  void testLoadCatalogXml(@TempDir Path tempDir) throws IOException {
    Catalog catalog
        = loader.load(ObjectUtils.notNull(
            new File("target/download/content/NIST_SP-800-53_rev5_catalog.xml").getCanonicalFile()));
    assertNotNull(catalog);

    Path out = newPath(ObjectUtils.notNull(tempDir), "out.xml");

    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.XML, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));
    // out.delete();
  }

  @Test
  void testLoadProfileJson(@TempDir Path tempDir) throws IOException {
    Profile profile
        = loader.load(ObjectUtils.notNull(
            new File("target/download/content/NIST_SP-800-53_rev5_MODERATE-baseline_profile.json").getCanonicalFile()));
    assertNotNull(profile);

    Path out = newPath(ObjectUtils.notNull(tempDir), "out.json");

    ISerializer<Profile> serializer = bindingContext.newSerializer(Format.JSON, Profile.class);
    serializer.serialize(profile, out);

    assertNotNull(loader.load(out));

    out = newPath(ObjectUtils.notNull(tempDir), "out.yaml");

    serializer = bindingContext.newSerializer(Format.YAML, Profile.class);
    serializer.serialize(profile, out);
  }

  @SuppressWarnings("null")
  @NonNull
  static Path newPath(@NonNull Path dir, @NonNull String filename) {
    return dir.resolve(filename);
  }

  @Test
  void testSerializeSspToOutputStream() throws IOException {
    SystemSecurityPlan ssp = new SystemSecurityPlan();

    ISerializer<SystemSecurityPlan> serializer = bindingContext.newSerializer(Format.JSON, SystemSecurityPlan.class);
    try (StringWriter writer = new StringWriter()) {
      serializer.serialize(ssp, writer);
    }
  }

  @Test
  void testCatalogXmlListItems() throws IOException {
    Catalog catalog
        = loader.load(ObjectUtils.notNull(
            new File("src/test/resources/content/catalog-with-lists.xml").getCanonicalFile()));
    assertNotNull(catalog);

    Path out = Paths.get("target/generated-test-resources/catalog-with-lists.xml");

    Path parent = out.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.XML, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));
    // out.delete();
  }

  @Test
  void testLoadCatalogTightLists() throws IOException, URISyntaxException {
    // test for usnistgov/liboscal-java#18
    Catalog catalog = loader.load(ObjectUtils.requireNonNull(
        OscalBindingContext.class.getResource("/content/issue13-catalog.xml")));

    assertNotNull(catalog);

    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.XML, Catalog.class);

    String output;
    try (StringWriter writer = new StringWriter()) {
      serializer.serialize(catalog, writer);
      output = writer.toString();
    }

    Pattern listItemPattern = Pattern.compile("<li>Item");
    Matcher matcher = listItemPattern.matcher(output);
    int count = 0;
    while (matcher.find()) {
      count++;
    }

    assertEquals(3, count);
  }

  @Test
  void testLoadCatalogIssue5(@TempDir Path tempDir) throws IOException, URISyntaxException {
    Catalog catalog = loader.load(
        ObjectUtils.requireNonNull(
            OscalBindingContext.class.getResource("/content/issue5-catalog.xml")));
    assertNotNull(catalog);

    File out = new File(tempDir.toFile(), "issue13-out.xml");
    // File out = new File("target/out.xml");

    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.XML, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));
    // out.delete();
  }
}
