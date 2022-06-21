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

import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ControlSelectionVisitor
    extends AbstractCatalogControlItemVisitor<Boolean, @NotNull Boolean> {
  private static final Logger LOGGER = LogManager.getLogger(ControlSelectionVisitor.class);

  @NotNull
  private static final MetapathExpression BACK_MATTER_RESOURCES_METAPATH
      = MetapathExpression.compile("back-matter/resource");
  @NotNull
  private static final MetapathExpression PART_METAPATH
      = MetapathExpression.compile("part|part//part");

  @NotNull
  private final IControlFilter filter;
  @NotNull
  private final IIndexer indexer;

  public ControlSelectionVisitor(@NotNull IControlFilter filter, @NotNull IIdentifierMapper mapper) {
    this.filter = filter;
    this.indexer = new DefaultIndexer(mapper);
  }

  @NotNull
  protected IIndexer getIndexer() {
    return indexer;
  }

  @NotNull
  public Index getIndex() {
    return indexer.getIndex();
  }

  @Override
  protected Boolean newDefaultResult(Boolean defaultMatch) {
    return false;
  }

  @Override
  protected Boolean aggregateResults(Boolean first, Boolean second, Boolean defaultMatch) {
    return first || second;
  }

  public void visitProfile(@NotNull IDocumentNodeItem profileItem) {
    IRootAssemblyNodeItem root = profileItem.getRootAssemblyNodeItem();
    indexMetadata(root);
    indexBackMatter(root);
  }

  public void visitCatalog(@NotNull IDocumentNodeItem catalogItem) {
    visitCatalog(catalogItem, false);

    IRootAssemblyNodeItem root = catalogItem.getRootAssemblyNodeItem();
    indexMetadata(root);
    indexBackMatter(root);
  }

  @Override
  protected Boolean visitGroup(@NotNull IRequiredValueModelNodeItem groupItem, Boolean defaultMatch) {
    boolean result = super.visitGroup(groupItem, defaultMatch);

    CatalogGroup group = (CatalogGroup) groupItem.getValue();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Selecting group '{}'. match={}", group.getId(), result);
    }

    getIndexer().addGroup(groupItem, result);
    return result;
  }

  @Override
  protected Boolean visitControl(@NotNull IRequiredValueModelNodeItem controlItem, Boolean defaultMatch) {
    Control control = (Control) controlItem.getValue();

    // determine if the control is a match
    Pair<@NotNull Boolean, @NotNull Boolean> matchResult = filter.match(control, defaultMatch);
    @SuppressWarnings("null")
    boolean isMatch = matchResult.getLeft();
    @SuppressWarnings("null")
    boolean isWithChildren = matchResult.getRight();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Selecting control '{}'. match={}", control.getId(), isMatch);
    }

    getIndexer().addControl(controlItem, isMatch);

    boolean defaultChildMatch = isMatch && isWithChildren;

    return aggregateResults(isMatch, super.visitControl(controlItem, defaultChildMatch), null);
  }

  @Override
  protected Boolean visitControlContainer(@NotNull IRequiredValueModelNodeItem catalogOrGroupOrControl,
      Boolean defaultMatch) {
    boolean retval = super.visitControlContainer(catalogOrGroupOrControl, defaultMatch);

    // handle parameters
    catalogOrGroupOrControl.getModelItemsByName("param").forEach(paramItem -> {
      indexer.addParameter(paramItem);
    });

    // handle parts
    PART_METAPATH.evaluate(catalogOrGroupOrControl).asStream()
        .map(item -> (@NotNull IRequiredValueModelNodeItem) item)
        .forEachOrdered(partItem -> {
          indexer.addPart(partItem);
        });
    return retval;
  }

  protected void indexMetadata(@NotNull IRootAssemblyNodeItem rootItem) {
    IIndexer indexer = getIndexer();

    rootItem.getModelItemsByName("metadata").forEach(metadataItem -> {
      metadataItem.getModelItemsByName("role").forEach(roleItem -> {
        indexer.addRole(roleItem);
      });

      metadataItem.getModelItemsByName("location").forEach(locationItem -> {
        indexer.addLocation(locationItem);
      });

      metadataItem.getModelItemsByName("party").forEach(partyItem -> {
        indexer.addParty(partyItem);
      });
    });
  }

  protected void indexBackMatter(@NotNull IRootAssemblyNodeItem rootItem) {
    IIndexer indexer = getIndexer();
    BACK_MATTER_RESOURCES_METAPATH.evaluate(rootItem).asStream()
        .map(item -> (@NotNull IRequiredValueModelNodeItem) item)
        .forEachOrdered(resourceItem -> {
          indexer.addResource(resourceItem);
        });
  }
}
