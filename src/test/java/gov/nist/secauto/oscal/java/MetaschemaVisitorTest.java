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
import gov.nist.secauto.metaschema.binding.metapath.INodeContext;
import gov.nist.secauto.metaschema.binding.metapath.MetaschemaPathEvaluationVisitor;
import gov.nist.secauto.metaschema.binding.metapath.TerminalNodeContext;
import gov.nist.secauto.metaschema.binding.metapath.type.INodeItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IMetapathResult;
import gov.nist.secauto.metaschema.model.common.metapath.Metapath;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.oscal.lib.model.Catalog;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class MetaschemaVisitorTest {

  @Test
  void test() throws FileNotFoundException, IOException, BindingException {
    OscalLoader loader = new OscalLoader();
    loader.enableFeature(Feature.DESERIALIZE_VALIDATE);
    
    Catalog catalog
        = loader.load(new File("target/download/content/NIST_SP-800-53_rev5_catalog.xml").getCanonicalFile());

    // MetapathExpression path = Metapath.parseMetapathString("2 eq 1 + 1[/catalog]");
//    MetapathExpression path = Metapath.parseMetapathString("/catalog/back-matter/resource[rlink/@href='https://doi.org/10.6028/NIST.SP.800-53r5']");
//    MetapathExpression path = Metapath.parseMetapathString("/catalog//@id");
//    MetapathExpression path = Metapath.parseMetapathString("(/catalog//control[@id='ac-1'])");
//    System.out.println(path.toString());
//    MetaschemaPathEvaluationVisitor visitor = new MetaschemaPathEvaluationVisitor();
//    INodeContext context = new TerminalNodeContext(INodeItem.newRootNodeItem(catalog, loader.getBindingContext()),
//        loader.getBindingContext());
//
//    // catalog, loader.getBindingContext();
//    IMetapathResult result = visitor.visit(path.getASTNode(), context);
//    System.out.println("Result: ");
//    result.toSequence().asStream().map(x -> (INodeItem)x).forEach(x -> {
//      System.out.println("item: "+x.getMetapath());
//    });
  }

}
