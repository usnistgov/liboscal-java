/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.java;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
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

    DynamicContext dynamicContext = new DynamicContext(nodeItem.getStaticContext());
    dynamicContext.setDocumentLoader(loader);
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    DefaultConstraintValidator validator = new DefaultConstraintValidator(handler);

    validator.validate(nodeItem, dynamicContext);
    validator.finalizeValidation(dynamicContext);

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
