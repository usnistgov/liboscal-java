/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.java;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.IMetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.ISequence;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.metapath.function.library.ResolveProfile;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.findbugs.annotations.NonNull;

class MetaschemaVisitorTest {

  @Disabled
  @Test
  void test() throws FileNotFoundException, IOException, URISyntaxException {
    OscalBindingContext bindingContext = OscalBindingContext.instance();
    IBoundLoader loader = bindingContext.newBoundLoader();

    URI baseUri = ObjectUtils.notNull(new File("").getAbsoluteFile().toURI());
    StaticContext staticContext = OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT.buildFrom()
        .baseUri(baseUri)
        .build();
    DynamicContext dynamicContext = new DynamicContext(staticContext);
    dynamicContext.setDocumentLoader(loader);

    // File file = new
    // File("target/download/content/NIST_SP-800-53_rev5_LOW-baseline_profile.xml").getCanonicalFile();

    // IDocumentNodeItem nodeItem = loader.loadAsNodeItem(file);
    IDocumentNodeItem nodeItem = loader.loadAsNodeItem(new URL(
        "https://raw.githubusercontent.com/usnistgov/oscal-content/master/nist.gov/SP800-53/rev5/xml/NIST_SP-800-53_rev5_HIGH-baseline_profile.xml"));

    // @NonNull
    // Profile profile = nodeItem.toBoundObject();

    IDocumentNodeItem resolvedProfile = ResolveProfile.resolveProfile(nodeItem, dynamicContext);
    OscalBindingContext.instance().validate(resolvedProfile, loader, null);

    // OscalBindingContext.instance().newSerializer(Format.XML,
    // Catalog.class).serialize(resolvedProfile.toBoundObject(), new FileWriter(new
    // File("resolved-catalog.xml")));

    // evaluatePath(IMetapathExpression.compile("resolve-profile(doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile))))/(profile, catalog)//control/@id"), nodeItem,
    // dynamicContext);
    evaluatePath(IMetapathExpression.compile("//control/@id"), resolvedProfile, dynamicContext);
    // evaluatePath(IMetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified"), nodeItem,
    // dynamicContext);
    // evaluatePath(
    // IMetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified -
    // /catalog/metadata/last-modified"),
    // nodeItem, dynamicContext);
    // evaluatePath(IMetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified + duration('PT1H')"),
    // nodeItem,
    // dynamicContext);
    // evaluatePath(IMetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified,/catalog/metadata/last-modified"),
    // nodeItem, dynamicContext);
    // evaluatePath(IMetapathExpression.compile("doc('target/download/content/NIST_SP-800-53_rev5_catalog.xml')"),
    // nodeItem, dynamicContext);
    // evaluatePath(Metapath.parseMetapathString("2 eq 1 + 1[/catalog]"),
    // nodeContext, visitor);
    // evaluatePath(Metapath.parseMetapathString("/catalog/back-matter/resource[rlink/@href='https://doi.org/10.6028/NIST.SP.800-53r5']"),
    // nodeItem, dynamicContext);
    // evaluatePath(IMetapathExpression.compile("/catalog//(@id,@uuid)"), nodeItem,
    // dynamicContext);
    // evaluatePath(IMetapathExpression.compile("exists(/catalog//(@id,@uuid))"),
    // nodeItem,
    // dynamicContext);
    // evaluatePath(IMetapathExpression.compile("/catalog//control//prop/@name"),
    // nodeItem,
    // dynamicContext);
    // evaluatePath(Metapath.parseMetapathString("(/catalog//control[@id='ac-1'])"),
    // nodeItem,
    // dynamicContext);
  }

  private static void evaluatePath(@NonNull IMetapathExpression path, @NonNull IItem nodeContext,
      @NonNull DynamicContext dynamicContext) {
    // System.out.println("Path: " + path.getPath());
    // System.out.println("Compiled Path: " + path.toString());

    ISequence<?> result = path.evaluate(nodeContext, dynamicContext);
    // System.out.println("Result: ");
    AtomicInteger count = new AtomicInteger();
    result.stream().forEachOrdered(x -> {
      count.incrementAndGet();
    });
    // System.out.println(String.format(" %d items", count.get()));
  }
}
