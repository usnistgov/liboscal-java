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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import gov.nist.csrc.ns.oscal._1.Catalog;
import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.Format;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.io.MutableConfiguration;
import gov.nist.secauto.metaschema.binding.io.Serializer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class OscalLoaderTest {
  private static OscalLoader loader;

  @BeforeAll
  private static void initializeLoader() {
    loader = new OscalLoader();
  }
  
  @Test
  void testLoadCatalogYaml(@TempDir Path tempDir) throws BindingException, IOException {
    // the YAML catalog is currently malformed, this will create a proper one for this test
    Catalog catalog = loader.loadCatalog(new File("target/download/content/NIST_SP-800-53_rev4_catalog.xml").getCanonicalFile());

    File out = new File(tempDir.toFile(), "out.yaml");
    BindingContext context = BindingContext.newInstance();
    MutableConfiguration config
        = new MutableConfiguration().enableFeature(Feature.SERIALIZE_ROOT).enableFeature(Feature.DESERIALIZE_ROOT);

    Serializer<Catalog> serializer = context.newSerializer(Format.YAML, Catalog.class, config);
    serializer.serialize(catalog, out);

    assertNotNull(loader.loadCatalog(out));

    out.delete();
  }
  
  @Test
  void testLoadCatalogJson() throws BindingException, IOException {
    assertNotNull(loader.loadCatalog(new File("target/download/content/NIST_SP-800-53_rev4_catalog.json").getCanonicalFile()));
  }
  
  @Test
  void testLoadCatalogXml() throws BindingException, IOException {
    assertNotNull(loader.loadCatalog(new File("target/download/content/NIST_SP-800-53_rev4_catalog.xml").getCanonicalFile()));
  }

}
