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

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
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
    StaticContext staticContext = StaticContext.builder()
        .baseUri(baseUri)
        .build();
    DynamicContext dynamicContext = staticContext.newDynamicContext();
    dynamicContext.setDocumentLoader(loader);

    // File file = new
    // File("target/download/content/NIST_SP-800-53_rev5_LOW-baseline_profile.xml").getCanonicalFile();

    // IDocumentNodeItem nodeItem = loader.loadAsNodeItem(file);
    IDocumentNodeItem nodeItem = loader.loadAsNodeItem(new URL(
        "https://raw.githubusercontent.com/usnistgov/oscal-content/master/nist.gov/SP800-53/rev5/xml/NIST_SP-800-53_rev5_HIGH-baseline_profile.xml"));

    // @NonNull
    // Profile profile = nodeItem.toBoundObject();

    IDocumentNodeItem resolvedProfile = ResolveProfile.resolveProfile(nodeItem, dynamicContext);
    OscalBindingContext.instance().validate(resolvedProfile);

    // OscalBindingContext.instance().newSerializer(Format.XML,
    // Catalog.class).serialize(resolvedProfile.toBoundObject(), new FileWriter(new
    // File("resolved-catalog.xml")));

    // evaluatePath(MetapathExpression.compile("resolve-profile(doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile))))/(profile, catalog)//control/@id"), nodeItem,
    // dynamicContext);
    evaluatePath(MetapathExpression.compile("//control/@id"), resolvedProfile, dynamicContext);
    // evaluatePath(MetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified"), nodeItem,
    // dynamicContext);
    // evaluatePath(
    // MetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified -
    // /catalog/metadata/last-modified"),
    // nodeItem, dynamicContext);
    // evaluatePath(MetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified + duration('PT1H')"),
    // nodeItem,
    // dynamicContext);
    // evaluatePath(MetapathExpression.compile("doc(resolve-uri(/profile/import/@href,
    // document-uri(/profile)))/catalog/metadata/last-modified,/catalog/metadata/last-modified"),
    // nodeItem, dynamicContext);
    // evaluatePath(MetapathExpression.compile("doc('target/download/content/NIST_SP-800-53_rev5_catalog.xml')"),
    // nodeItem, dynamicContext);
    // evaluatePath(Metapath.parseMetapathString("2 eq 1 + 1[/catalog]"),
    // nodeContext, visitor);
    // evaluatePath(Metapath.parseMetapathString("/catalog/back-matter/resource[rlink/@href='https://doi.org/10.6028/NIST.SP.800-53r5']"),
    // nodeItem, dynamicContext);
    // evaluatePath(MetapathExpression.compile("/catalog//(@id,@uuid)"), nodeItem,
    // dynamicContext);
    // evaluatePath(MetapathExpression.compile("exists(/catalog//(@id,@uuid))"),
    // nodeItem,
    // dynamicContext);
    // evaluatePath(MetapathExpression.compile("/catalog//control//prop/@name"),
    // nodeItem,
    // dynamicContext);
    // evaluatePath(Metapath.parseMetapathString("(/catalog//control[@id='ac-1'])"),
    // nodeItem,
    // dynamicContext);
  }

  private static void evaluatePath(@NonNull MetapathExpression path, @NonNull IItem nodeContext,
      @NonNull DynamicContext dynamicContext) {
    // System.out.println("Path: " + path.getPath());
    // System.out.println("Compiled Path: " + path.toString());

    ISequence<?> result = path.evaluate(nodeContext, dynamicContext);
    // System.out.println("Result: ");
    AtomicInteger count = new AtomicInteger();
    result.asStream().forEachOrdered(x -> {
      count.incrementAndGet();
    });
    // System.out.println(String.format(" %d items", count.get()));
  }
}
