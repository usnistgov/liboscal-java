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

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.profile.resolver.support.AbstractIndexingVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer.SelectionStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Walks a {@link Catalog} indexing all nodes that can be referenced.
 * <p>
 * For each {@link CatalogGroup}, {@link Control}, and {@link ControlPart},
 * determine if that object is {@link SelectionStatus#SELECTED} or
 * {@link SelectionStatus#UNSELECTED}.
 * <p>
 * A {@link Control} is {@link SelectionStatus#SELECTED} if it matches the
 * configured {@link IControlFilter}, otherwise it is
 * {@link SelectionStatus#UNSELECTED}.
 * <p>
 * A {@link CatalogGroup} is {@link SelectionStatus#SELECTED} if it contains a
 * {@link SelectionStatus#SELECTED} descendant {@link Control}, otherwise it is
 * {@link SelectionStatus#UNSELECTED}.
 * <p>
 * A {@link ControlPart} is {@link SelectionStatus#SELECTED} if its containing
 * control is {@link SelectionStatus#SELECTED}.
 * <p>
 * All other indexed nodes will have the {@link SelectionStatus#UNKNOWN}, since
 * these nodes require reference counting to determine if they are to be kept or
 * not.
 */
public final class ControlSelectionVisitor
    extends AbstractIndexingVisitor<IControlSelectionState, Boolean> {
  private static final Logger LOGGER = LogManager.getLogger(ControlSelectionVisitor.class);

  private static final ControlSelectionVisitor SINGLETON = new ControlSelectionVisitor();

  @SuppressFBWarnings(value = "SING_SINGLETON_GETTER_NOT_SYNCHRONIZED", justification = "class initialization")
  public static ControlSelectionVisitor instance() {
    return SINGLETON;
  }

  private ControlSelectionVisitor() {
    // disable construction
  }

  @Override
  protected IIndexer getIndexer(IControlSelectionState state) {
    return state.getIndex();
  }

  @Override
  protected Boolean newDefaultResult(IControlSelectionState state) {
    return false;
  }

  @Override
  protected Boolean aggregateResults(Boolean first, Boolean second, IControlSelectionState state) {
    return first || second;
  }

  public void visit(@NonNull IDocumentNodeItem catalogDocument, @NonNull IControlSelectionState state) {
    visitCatalog(catalogDocument, state);
  }

  public void visitProfile(
      @NonNull IDocumentNodeItem catalogDocument,
      @NonNull IDocumentNodeItem profileDocument,
      @NonNull IControlSelectionState state) {
    visit(catalogDocument, state);

    profileDocument.modelItems().forEachOrdered(item -> {
      IRootAssemblyNodeItem root = ObjectUtils.requireNonNull((IRootAssemblyNodeItem) item);

      visitMetadata(root, state);
      visitBackMatter(root, state);
    });
  }

  @Override
  public Boolean visitCatalog(IDocumentNodeItem catalogDocument, IControlSelectionState state) {
    getIndexer(state).setSelectionStatus(catalogDocument, SelectionStatus.SELECTED);
    return super.visitCatalog(catalogDocument, state);
  }

  @Override
  public Boolean visitGroup(IAssemblyNodeItem groupItem, Boolean childSelected,
      IControlSelectionState state) {
    super.visitGroup(groupItem, childSelected, state);
    if (LOGGER.isTraceEnabled()) {
      CatalogGroup group = ObjectUtils.requireNonNull((CatalogGroup) groupItem.getValue());
      LOGGER.atTrace().log("Selecting group '{}'. match={}", group.getId(), childSelected);
    }

    // these should agree
    assert state.isSelected(groupItem) == childSelected;

    if (childSelected) {
      getIndexer(state).setSelectionStatus(groupItem, SelectionStatus.SELECTED);
    } else {
      getIndexer(state).setSelectionStatus(groupItem, SelectionStatus.UNSELECTED);
    }

    handlePartSelection(groupItem, childSelected, state);
    return childSelected;
  }

  private void handlePartSelection(
      @NonNull IAssemblyNodeItem groupOrControlItem,
      boolean selected,
      IControlSelectionState state) {
    if (isVisitedItemType(IEntityItem.ItemType.PART)) {
      SelectionStatus selectionStatus = selected ? SelectionStatus.SELECTED : SelectionStatus.UNSELECTED;

      IIndexer index = getIndexer(state);
      CHILD_PART_METAPATH.evaluate(groupOrControlItem).stream()
          .map(item -> (IAssemblyNodeItem) item)
          .forEachOrdered(partItem -> {
            index.setSelectionStatus(ObjectUtils.requireNonNull(partItem), selectionStatus);
          });
    }
  }

  @Override
  public Boolean visitControl(
      IAssemblyNodeItem controlItem,
      Boolean childResult,
      IControlSelectionState state) {
    super.visitControl(controlItem, childResult, state);

    boolean selected = state.isSelected(controlItem);
    if (selected) {
      getIndexer(state).setSelectionStatus(controlItem, SelectionStatus.SELECTED);
    } else {
      getIndexer(state).setSelectionStatus(controlItem, SelectionStatus.UNSELECTED);
    }

    handlePartSelection(controlItem, selected, state);
    return selected;
  }
}
