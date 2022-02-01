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

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathFactory;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.IExpressionEvaluationVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.MetaschemaPathEvaluationVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IValuedItem;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.metapath.function.library.ResolveProfile;
import gov.nist.secauto.oscal.lib.model.Profile;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

class MetaschemaVisitorTest {

  @Test
  void test() throws FileNotFoundException, IOException, BindingException {
    OscalBindingContext bindingContext = OscalBindingContext.instance();
    IBoundLoader loader = bindingContext.newBoundLoader();
    loader.enableFeature(Feature.DESERIALIZE_VALIDATE);

    StaticContext staticContext = new StaticContext();
    @SuppressWarnings("null")
    @NotNull
    URI baseUri = new File("").getAbsoluteFile().toURI();
    staticContext.setBaseUri(baseUri);
    DynamicContext dynamicContext = staticContext.newDynamicContext();
    dynamicContext.setDocumentLoader(loader);

    MetaschemaPathEvaluationVisitor visitor = new MetaschemaPathEvaluationVisitor(dynamicContext);

    File file = new File("target/download/content/NIST_SP-800-53_rev5_LOW-baseline_profile.xml").getCanonicalFile();
    IDocumentNodeItem nodeItem = loader.loadAsNodeItem(file);

    @NotNull
    Profile profile = IBoundLoader.toBoundObject(nodeItem);
    
    IDocumentNodeItem resolvedProfile = ResolveProfile.resolveProfile(nodeItem, dynamicContext);

//    evaluatePath(MetapathFactory.parseMetapathString("resolve-profile(doc(resolve-uri(/profile/import/@href, document-uri(/profile))))/(profile, catalog)//control/@id"), nodeItem, visitor);
    evaluatePath(MetapathFactory.parseMetapathString("//control/@id"), resolvedProfile, visitor);
//    evaluatePath(MetapathFactory.parseMetapathString("doc(resolve-uri(/profile/import/@href, document-uri(/profile)))/catalog/metadata/last-modified"), nodeItem, visitor);
//    evaluatePath(
//        MetapathFactory.parseMetapathString("doc(resolve-uri(/profile/import/@href, document-uri(/profile)))/catalog/metadata/last-modified - /catalog/metadata/last-modified"),
//        nodeItem, visitor);
//    evaluatePath(MetapathFactory.parseMetapathString("doc(resolve-uri(/profile/import/@href, document-uri(/profile)))/catalog/metadata/last-modified + duration('PT1H')"), nodeItem,
//        visitor);
//    evaluatePath(MetapathFactory.parseMetapathString("doc(resolve-uri(/profile/import/@href, document-uri(/profile)))/catalog/metadata/last-modified,/catalog/metadata/last-modified"),
//        nodeItem, visitor);
//    evaluatePath(MetapathFactory.parseMetapathString("doc('target/download/content/NIST_SP-800-53_rev5_catalog.xml')"),
//        nodeItem, visitor);
    // evaluatePath(Metapath.parseMetapathString("2 eq 1 + 1[/catalog]"), nodeContext, visitor);
    // evaluatePath(Metapath.parseMetapathString("/catalog/back-matter/resource[rlink/@href='https://doi.org/10.6028/NIST.SP.800-53r5']"),
    // nodeItem, visitor);
    // evaluatePath(MetapathFactory.parseMetapathString("/catalog//(@id,@uuid)"), nodeItem, visitor);
    // evaluatePath(MetapathFactory.parseMetapathString("exists(/catalog//(@id,@uuid))"), nodeItem,
    // visitor);
    // evaluatePath(MetapathFactory.parseMetapathString("/catalog//control//prop/@name"), nodeItem,
    // visitor);
    // evaluatePath(Metapath.parseMetapathString("(/catalog//control[@id='ac-1'])"), nodeItem,
    // visitor);
  }

  private void evaluatePath(@NotNull MetapathExpression path, @NotNull INodeContext context,
      @NotNull IExpressionEvaluationVisitor visitor) {
    System.out.println("Path: " + path.getPath());
    System.out.println("Compiled Path: " + path.toString());

    ISequence<?> result = visitor.visit(path.getASTNode(), context);
    System.out.println("Result: ");
    result.asStream().forEachOrdered(x -> {
      if (x instanceof IValuedItem) {
        Object value = ((IValuedItem) x).getValue();
        System.out.println(String.format("  %s: %s", x.getItemName(), value));
      }
    });
  }
}
