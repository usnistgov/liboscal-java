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

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.profile.resolver.TestUtil;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.ControlSelectionState;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.ControlSelectionVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.IControlFilter;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.IControlSelectionFilter;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.IControlSelectionState;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIdentifierMapper;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;
import gov.nist.secauto.oscal.lib.profile.resolver.support.ReassignmentIndexer;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ReferenceCountingVisitorTest {
  @SuppressWarnings("null")
  @Test
  void test() throws IOException {
    // setup the imported catalog
    IDocumentNodeItem importedCatalogDocumentItem = TestUtil.newImportedCatalog();

    // setup the selection visitor
    IControlFilter filter = IControlFilter.newInstance(
        IControlSelectionFilter.ALL_MATCH,
        IControlSelectionFilter.matchIds("control2", "control5", "control7"));
    IIdentifierMapper mapper = TestUtil.UUID_CONCAT_ID_MAPPER;
    IIndexer indexer = new ReassignmentIndexer(mapper);
    IControlSelectionState state = new ControlSelectionState(indexer, filter);

    // process selections
    ControlSelectionVisitor.instance().visitCatalog(importedCatalogDocumentItem, state);

    IIndexer.logIndex(indexer, Level.DEBUG);

    // setup reference counting
    ReferenceCountingVisitor.instance()
        .visitCatalog(importedCatalogDocumentItem, indexer, importedCatalogDocumentItem.getBaseUri());

    IIndexer.logIndex(indexer, Level.DEBUG);

    OscalBindingContext.instance()
        .newSerializer(Format.YAML, Catalog.class)
        .serialize(
            (Catalog) INodeItem.toValue(importedCatalogDocumentItem),
            System.out);
  }

}
