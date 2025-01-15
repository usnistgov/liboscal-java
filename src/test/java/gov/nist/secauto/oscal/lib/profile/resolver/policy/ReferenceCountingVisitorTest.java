/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
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
        .visitCatalog(
            importedCatalogDocumentItem,
            indexer,
            (uri, src) -> importedCatalogDocumentItem.getBaseUri().resolve(uri));

    IIndexer.logIndex(indexer, Level.DEBUG);

    OscalBindingContext.instance()
        .newSerializer(Format.YAML, Catalog.class)
        .serialize(
            (Catalog) INodeItem.toValue(importedCatalogDocumentItem),
            System.out);
  }

}
