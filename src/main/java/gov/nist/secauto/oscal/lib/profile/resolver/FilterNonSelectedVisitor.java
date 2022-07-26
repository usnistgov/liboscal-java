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

package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.oscal.lib.model.BackMatter;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Location;
import gov.nist.secauto.oscal.lib.model.Metadata;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Party;
import gov.nist.secauto.oscal.lib.model.Role;
import gov.nist.secauto.oscal.lib.model.control.catalog.IControlContainer;
import gov.nist.secauto.oscal.lib.model.control.catalog.IGroupContainer;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nonnull;

public class FilterNonSelectedVisitor {
  private static final Logger LOGGER = LogManager.getLogger(FilterNonSelectedVisitor.class);

  @Nonnull
  private final Index index;

  public FilterNonSelectedVisitor(@Nonnull Index index) {
    this.index = index;
  }

  @Nonnull
  protected Index getIndex() {
    return index;
  }

  public void visitCatalog(@Nonnull IDocumentNodeItem catalogItem) {
    IRootAssemblyNodeItem root = catalogItem.getRootAssemblyNodeItem();

    Catalog catalog = (Catalog) catalogItem.getValue();

    root.getModelItemsByName("group").forEach(child -> {
      IResult result = visitGroup(child, catalog);
      result.applyTo(catalog);
    });

    root.getModelItemsByName("control").forEach(child -> {
      IResult result = visitControl(child, catalog);
      result.applyTo(catalog);
    });

    root.getModelItemsByName("param").forEach(child -> {
      IResult result = visitParam(child, catalog);
      result.applyTo(catalog);
    });

    root.getModelItemsByName("metadata").forEach(child -> {
      visitMetadata(child);
    });

    root.getModelItemsByName("back-matter").forEach(child -> {
      visitBackMatter(child);
    });
  }

  protected void visitMetadata(IRequiredValueModelNodeItem child) {
    Metadata metadata = (Metadata) child.getValue();

    // prune roles, parties, and locations
    // keep entries with prop name:keep and any referenced
    index.getEntitiesByItemType(ItemType.ROLE).stream()
        .filter(entity -> {
          return !(entity.getReferenceCount() > 0
              || (Boolean) Index.HAS_PROP_KEEP.evaluateAs(entity.getInstance(), ResultType.BOOLEAN));
        })
        .map(entity -> (Role) entity.getInstanceValue())
        .forEachOrdered(value -> {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.atDebug().log("Removing role '{}'", value.getId());
          }
          metadata.removeRole(value);
        });

    index.getEntitiesByItemType(ItemType.PARTY).stream()
        .filter(entity -> {
          return !(entity.getReferenceCount() > 0
              || (Boolean) Index.HAS_PROP_KEEP.evaluateAs(entity.getInstance(), ResultType.BOOLEAN));
        })
        .map(entity -> (Party) entity.getInstanceValue())
        .forEachOrdered(value -> {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.atDebug().log("Removing party '{}'", value.getUuid());
          }
          metadata.removeParty(value);
        });

    index.getEntitiesByItemType(ItemType.LOCATION).stream()
        .filter(entity -> {
          return !(entity.getReferenceCount() > 0
              || (Boolean) Index.HAS_PROP_KEEP.evaluateAs(entity.getInstance(), ResultType.BOOLEAN));
        })
        .map(entity -> (Location) entity.getInstanceValue())
        .forEachOrdered(value -> {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.atDebug().log("Removing location '{}'", value.getUuid());
          }
          metadata.removeLocation(value);
        });
  }

  private void visitBackMatter(IRequiredValueModelNodeItem child) {
    BackMatter backMatter = (BackMatter) child.getValue();

    index.getEntitiesByItemType(ItemType.RESOURCE).stream()
        .filter(entity -> {
          return !(entity.getReferenceCount() > 0
              || (Boolean) Index.HAS_PROP_KEEP.evaluateAs(entity.getInstance(), ResultType.BOOLEAN));
        })
        .map(entity -> (Resource) entity.getInstanceValue())
        .forEachOrdered(value -> {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.atDebug().log("Removing resource '{}'", value.getUuid());
          }
          backMatter.removeResource(value);
        });
  }

  @Nonnull
  protected IResult visitGroup(IRequiredValueModelNodeItem item, IGroupContainer container) {

    CatalogGroup group = (CatalogGroup) item.getValue();

    IResult retval = new DefaultResult();
    if (getIndex().isSelected(group)) {
      String groupId = group.getId();
      if (groupId != null) {
        // update the id
        EntityItem groupEntity = getIndex().getEntity(ItemType.GROUP, groupId);
        group.setId(groupEntity.getIdentifier());
      }

      item.getModelItemsByName("group").forEach(child -> {
        IResult result = visitGroup(child, group);
        result.applyTo(group);
      });

      item.getModelItemsByName("control").forEach(child -> {
        IResult result = visitControl(child, group);
        result.applyTo(group);
      });

      item.getModelItemsByName("param").forEach(child -> {
        IResult result = visitParam(child, group);
        result.applyTo(group);
      });
    } else {
      item.getModelItemsByName("group").forEach(child -> {
        IResult result = visitGroup(child, group);
        retval.append(result);
      });

      item.getModelItemsByName("control").forEach(child -> {
        IResult result = visitControl(child, group);
        retval.append(result);
      });

      item.getModelItemsByName("param").forEach(child -> {
        IResult result = visitParam(child, group);
        retval.append(result);
      });

      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Removing group '{}'", group.getId());
      }
      container.removeGroup(group);
    }
    return retval;
  }

  @Nonnull
  protected IResult visitControl(IRequiredValueModelNodeItem item, IControlContainer container) {
    Control control = (Control) item.getValue();

    IResult retval = new DefaultResult();
    if (getIndex().isSelected(control)) {
      // update the id
      control.setId(getIndex().getEntity(ItemType.CONTROL, control.getId()).getIdentifier());

      if (!getIndex().isSelected(container)) {
        // promote this control
        retval.promoteControl(control);
      }

      item.getModelItemsByName("control").forEach(child -> {
        IResult result = visitControl(child, control);
        result.applyTo(control);
      });

      item.getModelItemsByName("param").forEach(child -> {
        IResult result = visitParam(child, control);
        result.applyTo(control);
      });
    } else {
      item.getModelItemsByName("control").forEach(child -> {
        IResult result = visitControl(child, control);
        retval.append(result);
      });

      item.getModelItemsByName("param").forEach(child -> {
        IResult result = visitParam(child, control);
        retval.append(result);
      });

      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Removing control '{}'", control.getId());
      }
      container.removeControl(control);
    }
    return retval;
  }

  protected IResult visitParam(IRequiredValueModelNodeItem item, IControlContainer container) {

    Parameter param = (Parameter) item.getValue();
    EntityItem entity = getIndex().getEntity(ItemType.PARAMETER, param.getId());

    IResult retval = new DefaultResult();
    if (entity.getReferenceCount() > 0) {
      // update the id
      param.setId(entity.getIdentifier());

      if (!getIndex().isSelected(container)) {
        // promote this control
        retval.promoteParameter(param);
      }
    } else {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("Removing param '{}'", param.getId());
      }
      container.removeParam(param);
    }
    return retval;
  }
}
