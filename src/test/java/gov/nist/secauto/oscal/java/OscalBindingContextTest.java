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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.DeserializationFeature;
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.SystemSecurityPlan;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class OscalBindingContextTest {
  private static OscalBindingContext bindingContext;
  private static IBoundLoader loader;

  @BeforeAll
  private static void initialize() { // NOPMD - actually used by JUnit
    bindingContext = OscalBindingContext.instance();
    loader = bindingContext.newBoundLoader();
  }

  @Test
  void testLoadCatalogYaml(@TempDir Path tempDir) throws BindingException, IOException {
    // the YAML catalog is currently malformed, this will create a proper one for this test
    Catalog catalog
        = loader.load(new File("target/download/content/NIST_SP-800-53_rev5_catalog.yaml").getCanonicalFile());

    Path out = newPath(tempDir, "out-catalog.yaml");

    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.YAML, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));

    // out.delete();
  }

  @Test
  void testLoadCatalogJson(@TempDir Path tempDir) throws BindingException, IOException {
    Catalog catalog
        = loader.load(new File("target/download/content/NIST_SP-800-53_rev5_catalog.json").getCanonicalFile());
    assertNotNull(catalog);

    File out = new File(tempDir.toFile(), "out.json");
    // File out = new File("target/out.json");
    IBindingContext context = IBindingContext.newInstance();

    ISerializer<Catalog> serializer = context.newSerializer(Format.JSON, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));

    // out.delete();
  }

  @Test
  void testLoadCatalogXml(@TempDir Path tempDir) throws BindingException, IOException {
    Catalog catalog
        = loader.load(new File("target/download/content/NIST_SP-800-53_rev5_catalog.xml").getCanonicalFile());
    assertNotNull(catalog);

    // File out = new File(tempDir.toFile(), "out.xml");
    File out = new File("target/out.xml");
    IBindingContext context = IBindingContext.newInstance();

    ISerializer<Catalog> serializer = context.newSerializer(Format.XML, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));
    // out.delete();
  }

  @Test
  void testLoadProfileJson(@TempDir Path tempDir) throws BindingException, IOException {
    Profile profile
        = loader.load(
            new File("target/download/content/NIST_SP-800-53_rev5_MODERATE-baseline_profile.json").getCanonicalFile());
    assertNotNull(profile);

    File out = new File(tempDir.toFile(), "out.json");
    // File out = new File("target/out-profile.json");
    IBindingContext context = IBindingContext.newInstance();

    ISerializer<Profile> serializer = context.newSerializer(Format.JSON, Profile.class);
    serializer.serialize(profile, out);

    assertNotNull(loader.load(out));

    out = new File(tempDir.toFile(), "out.yaml");
    // out = new File("target/out-profile.yaml");

    serializer = context.newSerializer(Format.YAML, Profile.class);
    serializer.serialize(profile, out);

    // out.delete();
  }

  static Path newPath(@NotNull Path dir, @NotNull String filename) {
    return dir.resolve(filename);
    // return Path.of("target",filename);
  }

  @Test
  void testSerializeSspToOutputStream() throws IOException {
    SystemSecurityPlan ssp = new SystemSecurityPlan();

    IBindingContext context = IBindingContext.newInstance();
    ISerializer<SystemSecurityPlan> serializer = context.newSerializer(Format.JSON, SystemSecurityPlan.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize(ssp, out);
  }

  @Test
  void testCatalogXmlListItems(@TempDir Path tempDir) throws BindingException, IOException {
    Catalog catalog
        = loader.load(new File("src/test/resources/content/catalog-with-lists.xml").getCanonicalFile());
    assertNotNull(catalog);

    // File out = new File(tempDir.toFile(), "out.xml");
    Path out = Paths.get("target/generated-test-resources/catalog-with-lists.xml");
    Files.createDirectories(out.getParent());
    IBindingContext context = IBindingContext.newInstance();

    ISerializer<Catalog> serializer = context.newSerializer(Format.XML, Catalog.class);
    serializer.serialize(catalog, out);

    assertNotNull(bindingContext.loadCatalog(out));
    // out.delete();
  }
}
