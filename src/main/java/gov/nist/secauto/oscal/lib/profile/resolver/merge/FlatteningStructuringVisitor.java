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

package gov.nist.secauto.oscal.lib.profile.resolver.merge;

import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Metadata.Location;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Metadata.Party;
import gov.nist.secauto.oscal.lib.model.Metadata.Role;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.ReferenceCountingVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.DefaultResult;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.FilterNonSelectedVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.support.AbstractCatalogEntityVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer.SelectionStatus;

import java.util.EnumSet;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FlatteningStructuringVisitor
    extends AbstractCatalogEntityVisitor<IIndexer, Void> {
  private static final FlatteningStructuringVisitor SINGLETON = new FlatteningStructuringVisitor();

  public static FlatteningStructuringVisitor instance() {
    return SINGLETON;
  }

  public FlatteningStructuringVisitor() {
    super(ObjectUtils.notNull(EnumSet.of(ItemType.GROUP, ItemType.CONTROL)));
  }

  @Override
  protected Void newDefaultResult(IIndexer state) {
    // do nothing
    return null;
  }

  @Override
  protected Void aggregateResults(Void first, Void second, IIndexer state) {
    // do nothing
    return null;
  }

  @Override
  public Void visitCatalog(@NonNull IDocumentNodeItem catalogItem, IIndexer index) {
    index.resetSelectionStatus();

    index.setSelectionStatus(catalogItem, SelectionStatus.SELECTED);
    super.visitCatalog(catalogItem, index);

    for (ItemType itemType : ItemType.values()) {
      assert itemType != null;
      for (IEntityItem item : index.getEntitiesByItemType(itemType)) {
        item.resetReferenceCount();
      }
    }

    // process references, looking for orphaned links to groups
    ReferenceCountingVisitor.instance().visitCatalog(catalogItem, index, catalogItem.getDocumentUri());

    FlatteningFilterNonSelectedVisitor.instance().visitCatalog(catalogItem, index);
    return null;
  }

  @Override
  public Void visitGroup(IRequiredValueModelNodeItem item, Void childResult, IIndexer index) {
    CatalogGroup group = (CatalogGroup) item.getValue();
    String id = group.getId();
    if (id != null) {
      IEntityItem entity = index.getEntity(ItemType.GROUP, id);
      assert entity != null;
      // refresh the instance
      entity.setInstance(item);
    }

    index.setSelectionStatus(item, SelectionStatus.UNSELECTED);
    handlePartSelection(item, index, SelectionStatus.UNSELECTED);
    return super.visitGroup(item, childResult, index);
  }

  @Override
  public Void visitControl(IRequiredValueModelNodeItem item, Void childResult, IIndexer index) {
    Control control = (Control) item.getValue();
    String id = ObjectUtils.requireNonNull(control.getId());
    IEntityItem entity = index.getEntity(ItemType.CONTROL, id);
    assert entity != null;
    // refresh the instance
    entity.setInstance(item);

    index.setSelectionStatus(item, SelectionStatus.SELECTED);
    handlePartSelection(item, index, SelectionStatus.SELECTED);
    return null;
  }

  @Override
  protected Void visitParameter(IRequiredValueModelNodeItem item,
      IRequiredValueModelNodeItem catalogOrGroupOrControl, IIndexer index) {
    Parameter parameter = (Parameter) item.getValue();
    String id = ObjectUtils.requireNonNull(parameter.getId());
    IEntityItem entity = index.getEntity(ItemType.PARAMETER, id);
    assert entity != null;
    // refresh the instance
    entity.setInstance(item);

    return null;
  }

  @Override
  protected void visitRole(IRequiredValueModelNodeItem item, IRequiredValueModelNodeItem metadataItem,
      IIndexer index) {
    Role role = (Role) item.getValue();
    String id = ObjectUtils.requireNonNull(role.getId());
    IEntityItem entity = index.getEntity(ItemType.ROLE, id);
    assert entity != null;
    // refresh the instance
    entity.setInstance(item);
  }

  @Override
  protected void visitLocation(IRequiredValueModelNodeItem item, IRequiredValueModelNodeItem metadataItem,
      IIndexer index) {
    Location location = (Location) item.getValue();
    UUID uuid = ObjectUtils.requireNonNull(location.getUuid());
    IEntityItem entity = index.getEntity(ItemType.LOCATION, uuid);
    assert entity != null;
    // refresh the instance
    entity.setInstance(item);
  }

  @Override
  protected void visitParty(IRequiredValueModelNodeItem item, IRequiredValueModelNodeItem metadataItem,
      IIndexer index) {
    Party location = (Party) item.getValue();
    UUID uuid = ObjectUtils.requireNonNull(location.getUuid());
    IEntityItem entity = index.getEntity(ItemType.PARTY, uuid);
    assert entity != null;
    // refresh the instance
    entity.setInstance(item);
  }

  @Override
  protected void visitResource(IRequiredValueModelNodeItem item, IRootAssemblyNodeItem rootItem,
      IIndexer index) {
    Resource location = (Resource) item.getValue();
    UUID uuid = ObjectUtils.requireNonNull(location.getUuid());
    IEntityItem entity = index.getEntity(ItemType.RESOURCE, uuid);
    assert entity != null;
    // refresh the instance
    entity.setInstance(item);
  }

  private static void handlePartSelection(
      @NonNull IRequiredValueModelNodeItem groupOrControlItem,
      @NonNull IIndexer index,
      @NonNull SelectionStatus selectionStatus) {
    CHILD_PART_METAPATH.evaluate(groupOrControlItem).asStream()
        .map(item -> (IRequiredValueModelNodeItem) item)
        .forEachOrdered(partItem -> {
          index.setSelectionStatus(ObjectUtils.requireNonNull(partItem), selectionStatus);

          ControlPart part = (ControlPart) partItem.getValue();
          String id = part.getId();
          if (id != null) {
            IEntityItem entity = index.getEntity(ItemType.PART, id);
            assert entity != null;
            // refresh the instance
            entity.setInstance(partItem);
          }
        });
  }

  private static final class FlatteningFilterNonSelectedVisitor
      extends FilterNonSelectedVisitor {
    private static final FlatteningFilterNonSelectedVisitor SINGLETON = new FlatteningFilterNonSelectedVisitor();

    public static FlatteningFilterNonSelectedVisitor instance() {
      return SINGLETON;
    }

    @Override
    public DefaultResult visitControl(IRequiredValueModelNodeItem item, DefaultResult childResult,
        Context context) {
      assert childResult != null;

      Control control = (Control) item.getValue();
      IIndexer index = context.getIndexer();
      // this control should always be found in the index
      IEntityItem entity = ObjectUtils.requireNonNull(
          index.getEntity(ItemType.CONTROL, ObjectUtils.requireNonNull(control.getId()), false));

      IRequiredValueModelNodeItem parent = ObjectUtils.notNull(item.getParentContentNodeItem());
      DefaultResult retval = new DefaultResult();
      if (SelectionStatus.SELECTED.equals(index.getSelectionStatus(item))) {
        // keep this control

        // always promote the control and any children
        retval.promoteControl(control);

        retval.appendPromoted(childResult);
        childResult.applyRemovesTo(control);

        if (parent.getValue() instanceof Control && SelectionStatus.SELECTED.equals(index.getSelectionStatus(parent))) {
          retval.removeControl(control);
        }
      } else {
        // remove this control and promote any needed children

        if (SelectionStatus.SELECTED.equals(index.getSelectionStatus(parent))) {
          retval.removeControl(control);
        }
        retval.appendPromoted(ObjectUtils.notNull(childResult));
        index.remove(entity);

        // remove any associated parts from the index
        removePartsFromIndex(item, index);
      }
      return retval;
    }
  }
}
