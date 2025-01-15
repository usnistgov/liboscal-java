/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.selection;

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalModelConstants;
import gov.nist.secauto.oscal.lib.model.BackMatter;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Metadata;
import gov.nist.secauto.oscal.lib.model.Metadata.Location;
import gov.nist.secauto.oscal.lib.model.Metadata.Party;
import gov.nist.secauto.oscal.lib.model.Metadata.Role;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.profile.resolver.support.AbstractCatalogEntityVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer.SelectionStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class FilterNonSelectedVisitor
    extends AbstractCatalogEntityVisitor<FilterNonSelectedVisitor.Context, DefaultResult> {
  private static final Logger LOGGER = LogManager.getLogger(FilterNonSelectedVisitor.class);
  @NonNull
  private static final FilterNonSelectedVisitor SINGLETON = new FilterNonSelectedVisitor();

  @NonNull
  @SuppressFBWarnings(value = "SING_SINGLETON_GETTER_NOT_SYNCHRONIZED", justification = "class initialization")
  public static FilterNonSelectedVisitor instance() {
    return SINGLETON;
  }

  @SuppressWarnings("null")

  @SuppressFBWarnings(value = "SING_SINGLETON_HAS_NONPRIVATE_CONSTRUCTOR", justification = "allows for extension")
  protected FilterNonSelectedVisitor() {
    // all other entity types are handled in a special way by this visitor
    super(EnumSet.of(IEntityItem.ItemType.GROUP, IEntityItem.ItemType.CONTROL, IEntityItem.ItemType.PARAMETER));
  }

  public void visitCatalog(@NonNull IDocumentNodeItem catalogItem, @NonNull IIndexer indexer) {
    Context context = new Context(indexer);
    IResult result = visitCatalog(catalogItem, context);

    Catalog catalog = (Catalog) INodeItem.toValue(catalogItem);
    result.applyTo(catalog);

    catalogItem.modelItems().forEachOrdered(root -> {
      root.getModelItemsByName(OscalModelConstants.QNAME_METADATA).stream()
          .map(child -> (IAssemblyNodeItem) child)
          .forEachOrdered(child -> {
            assert child != null;
            visitMetadata(child, context);
          });

      root.getModelItemsByName(OscalModelConstants.QNAME_BACK_MATTER).stream()
          .map(child -> (IAssemblyNodeItem) child)
          .forEachOrdered(child -> {
            assert child != null;
            visitBackMatter(child, context);
          });
    });
  }

  @Override
  protected DefaultResult newDefaultResult(Context state) {
    return new DefaultResult();
  }

  @Override
  protected DefaultResult aggregateResults(DefaultResult first, DefaultResult second, Context state) {
    return first.append(ObjectUtils.notNull(second));
  }

  protected void visitMetadata(@NonNull IAssemblyNodeItem metadataItem, Context context) {
    Metadata metadata = ObjectUtils.requireNonNull((Metadata) metadataItem.getValue());

    IIndexer index = context.getIndexer();
    // prune roles, parties, and locations
    // keep entries with prop name:keep and any referenced
    for (IEntityItem entity : IIndexer.getUnreferencedEntitiesAsStream(index.getEntitiesByItemType(ItemType.ROLE))
        .collect(Collectors.toList())) {
      Role role = entity.getInstanceValue();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Removing role '{}'", role.getId());
      }
      metadata.removeRole(role);
      index.removeItem(entity);
    }

    for (IEntityItem entity : IIndexer.getUnreferencedEntitiesAsStream(index.getEntitiesByItemType(ItemType.PARTY))
        .collect(Collectors.toList())) {
      Party party = entity.getInstanceValue();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Removing party '{}'", party.getUuid());
      }
      metadata.removeParty(party);
      index.removeItem(entity);
    }

    for (IEntityItem entity : IIndexer.getUnreferencedEntitiesAsStream(index.getEntitiesByItemType(ItemType.LOCATION))
        .collect(Collectors.toList())) {
      Location location = entity.getInstanceValue();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Removing location '{}'", location.getUuid());
      }
      metadata.removeLocation(location);
      index.removeItem(entity);
    }
  }

  @SuppressWarnings("static-method")
  private void visitBackMatter(@NonNull IAssemblyNodeItem backMatterItem, Context context) {
    BackMatter backMatter = ObjectUtils.requireNonNull((BackMatter) backMatterItem.getValue());

    IIndexer index = context.getIndexer();
    for (IEntityItem entity : IIndexer.getUnreferencedEntitiesAsStream(index.getEntitiesByItemType(ItemType.RESOURCE))
        .collect(Collectors.toList())) {
      Resource resource = entity.getInstanceValue();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Removing resource '{}'", resource.getUuid());
      }
      backMatter.removeResource(resource);
      index.removeItem(entity);
    }
  }

  @Override
  public DefaultResult visitGroup(
      IAssemblyNodeItem item,
      DefaultResult childResult,
      Context context) {
    CatalogGroup group = ObjectUtils.requireNonNull((CatalogGroup) item.getValue());

    IIndexer index = context.getIndexer();
    String groupId = group.getId();
    DefaultResult retval = new DefaultResult();
    if (SelectionStatus.SELECTED.equals(index.getSelectionStatus(item))) {
      if (groupId != null) {
        // this group should always be found in the index
        IEntityItem entity = ObjectUtils.requireNonNull(index.getEntity(ItemType.GROUP, groupId, false));
        // update the id
        group.setId(entity.getIdentifier());
      }
      childResult.applyTo(group);
    } else {
      retval.removeGroup(group);
      retval.appendPromoted(ObjectUtils.notNull(childResult));

      if (groupId != null) {
        // this group should always be found in the index
        IEntityItem entity = ObjectUtils.requireNonNull(index.getEntity(ItemType.GROUP, groupId, false));
        index.removeItem(entity);
      }

      // remove any associated parts from the index
      removePartsFromIndex(item, index);
    }
    return retval;
  }

  @Override
  public DefaultResult visitControl(
      IAssemblyNodeItem item,
      DefaultResult childResult,
      Context context) {
    Control control = ObjectUtils.requireNonNull((Control) item.getValue());
    IIndexer index = context.getIndexer();
    // this control should always be found in the index
    IEntityItem entity = ObjectUtils.requireNonNull(
        index.getEntity(ItemType.CONTROL, ObjectUtils.requireNonNull(control.getId()), false));

    IAssemblyNodeItem parent = ObjectUtils.notNull(item.getParentContentNodeItem());
    DefaultResult retval = new DefaultResult();
    if (SelectionStatus.SELECTED.equals(index.getSelectionStatus(item))) {
      // keep this control
      // update the id
      control.setId(entity.getIdentifier());

      if (!SelectionStatus.SELECTED.equals(index.getSelectionStatus(parent))) {
        // promote this control
        retval.promoteControl(control);
      }
      childResult.applyTo(control);
    } else {
      // remove this control and promote any needed children

      if (SelectionStatus.SELECTED.equals(index.getSelectionStatus(parent))) {
        retval.removeControl(control);
      }
      retval.appendPromoted(ObjectUtils.notNull(childResult));
      index.removeItem(entity);

      // remove any associated parts from the index
      removePartsFromIndex(item, index);
    }
    return retval;
  }

  protected static void removePartsFromIndex(@NonNull IAssemblyNodeItem groupOrControlItem,
      @NonNull IIndexer index) {
    CHILD_PART_METAPATH.evaluate(groupOrControlItem).stream()
        .map(item -> (IAssemblyNodeItem) item)
        .forEachOrdered(partItem -> {
          ControlPart part = ObjectUtils.requireNonNull((ControlPart) partItem.getValue());
          String id = part.getId();
          if (id != null) {
            IEntityItem entity = index.getEntity(IEntityItem.ItemType.PART, id);
            if (entity != null) {
              index.removeItem(entity);
            }
          }
        });
  }

  @Override
  protected DefaultResult visitParameter(IAssemblyNodeItem item, IAssemblyNodeItem parent,
      Context context) {
    Parameter param = ObjectUtils.requireNonNull((Parameter) item.getValue());
    IIndexer index = context.getIndexer();
    // this parameter should always be found in the index
    IEntityItem entity = ObjectUtils.requireNonNull(
        index.getEntity(ItemType.PARAMETER, ObjectUtils.requireNonNull(param.getId()), false));

    DefaultResult retval = new DefaultResult();
    if (IIndexer.isReferencedEntity(entity)) {
      // keep the parameter
      // update the id
      param.setId(entity.getIdentifier());

      // a parameter is selected if it has a reference count greater than 0
      index.setSelectionStatus(item, SelectionStatus.SELECTED);

      // promote this parameter
      if (SelectionStatus.UNSELECTED.equals(index.getSelectionStatus(parent))) {
        retval.promoteParameter(param);
      }
    } else {
      // don't keep the parameter
      if (SelectionStatus.SELECTED.equals(index.getSelectionStatus(parent))) {
        retval.removeParameter(param);
      }
      index.removeItem(entity);
    }
    return retval;
  }

  protected static final class Context {

    @NonNull
    private final IIndexer indexer;

    private Context(@NonNull IIndexer indexer) {
      this.indexer = indexer;
    }

    @NonNull
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "provides intentional access to index state")
    public IIndexer getIndexer() {
      return indexer;
    }
  }
}
