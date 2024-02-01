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

import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
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
  private static final FilterNonSelectedVisitor SINGLETON = new FilterNonSelectedVisitor();

  public static FilterNonSelectedVisitor instance() {
    return SINGLETON;
  }

  @SuppressWarnings("null")
  protected FilterNonSelectedVisitor() {
    // all other entity types are handled in a special way by this visitor
    super(EnumSet.of(IEntityItem.ItemType.GROUP, IEntityItem.ItemType.CONTROL, IEntityItem.ItemType.PARAMETER));
  }

  public void visitCatalog(@NonNull IDocumentNodeItem catalogItem, @NonNull IIndexer indexer) {
    Context context = new Context(indexer);
    IResult result = visitCatalog(catalogItem, context);

    IRootAssemblyNodeItem root = catalogItem.getRootAssemblyNodeItem();

    Catalog catalog = (Catalog) catalogItem.getValue();
    result.applyTo(catalog);

    root.getModelItemsByName("metadata").forEach(child -> {
      assert child != null;
      visitMetadata(child, context);
    });

    root.getModelItemsByName("back-matter").forEach(child -> {
      assert child != null;
      visitBackMatter(child, context);
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

  protected void visitMetadata(@NonNull IRequiredValueModelNodeItem metadataItem, Context context) {
    Metadata metadata = (Metadata) metadataItem.getValue();

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
  private void visitBackMatter(@NonNull IRequiredValueModelNodeItem backMatterItem, Context context) {
    BackMatter backMatter = (BackMatter) backMatterItem.getValue();

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
      IRequiredValueModelNodeItem item,
      DefaultResult childResult,
      Context context) {
    CatalogGroup group = (CatalogGroup) item.getValue();

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
      IRequiredValueModelNodeItem item,
      DefaultResult childResult,
      Context context) {
    Control control = (Control) item.getValue();
    IIndexer index = context.getIndexer();
    // this control should always be found in the index
    IEntityItem entity = ObjectUtils.requireNonNull(
        index.getEntity(ItemType.CONTROL, ObjectUtils.requireNonNull(control.getId()), false));

    IRequiredValueModelNodeItem parent = ObjectUtils.notNull(item.getParentContentNodeItem());
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
      retval.removeControl(control);
      retval.appendPromoted(ObjectUtils.notNull(childResult));
      index.removeItem(entity);

      // remove any associated parts from the index
      removePartsFromIndex(item, index);
    }
    return retval;
  }

  protected static void removePartsFromIndex(@NonNull IRequiredValueModelNodeItem groupOrControlItem,
      @NonNull IIndexer index) {
    CHILD_PART_METAPATH.evaluate(groupOrControlItem).asStream()
        .map(item -> (IRequiredValueModelNodeItem) item)
        .forEachOrdered(partItem -> {
          ControlPart part = (ControlPart) partItem.getValue();
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
  protected DefaultResult visitParameter(IRequiredValueModelNodeItem item, IRequiredValueModelNodeItem parent,
      Context context) {
    Parameter param = (Parameter) item.getValue();
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
      super();
      this.indexer = indexer;
    }

    @NonNull
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "provides intentional access to index state")
    public IIndexer getIndexer() {
      return indexer;
    }
  }
}
