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
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.core.model.constraint.FindingCollectingConstraintValidationHandler;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class ExamplesTest {

  @Test
  void simpleLoadAndSave() throws IOException {
    // Initialize the Module framework
    OscalBindingContext bindingContext = OscalBindingContext.instance(); // manages the Module model
    IBoundLoader loader = bindingContext.newBoundLoader(); // supports loading OSCAL documents

    // load an OSCAL catalog
    Catalog catalog = loader.load(
        ObjectUtils.requireNonNull(Paths.get("src/test/resources/content/test-catalog.xml"))); // load the catalog
    assertNotNull(catalog);

    // Create a serializer which can be used to write multiple catalogs
    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.YAML, Catalog.class);

    // create the output directory
    Path outDir = Paths.get("target/generated-test-resources");
    Files.createDirectories(outDir);

    // serialize the catalog as yaml
    serializer.serialize(catalog, ObjectUtils.notNull(outDir.resolve("test-catalog.yaml")));
  }

  @Disabled
  @Test
  void testConstraintValidation()
      throws MalformedURLException, IOException, URISyntaxException, ProfileResolutionException {
    // Initialize the Module framework
    OscalBindingContext bindingContext = OscalBindingContext.instance(); // manages the Module model
    IBoundLoader loader = bindingContext.newBoundLoader(); // supports loading OSCAL documents
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);

    IDocumentNodeItem nodeItem = loader.loadAsNodeItem(new URL(
        "https://raw.githubusercontent.com/Rene2mt/fedramp-automation/a692b9385d8fbcacbb1d3e3d0b0d7e3c45a205d0/src/content/baselines/rev5/xml/FedRAMP_rev5_HIGH-baseline_profile.xml"));

    DynamicContext dynamicContext = StaticContext.instance().dynamicContext();
    dynamicContext.setDocumentLoader(loader);
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext, handler);

    validator.validate(nodeItem);
    validator.finalizeValidation();

    assertTrue(handler.isPassing());

    IDocumentNodeItem resolvedCatalog = new ProfileResolver().resolve(nodeItem);

    // Create a serializer which can be used to write multiple catalogs
    ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.YAML, Catalog.class);
    // serialize the catalog as yaml
    @SuppressWarnings("resource") // not owned
    OutputStream os = ObjectUtils.notNull(System.out);

    serializer.serialize((Catalog) INodeItem.toValue(resolvedCatalog), os);
  }
}
