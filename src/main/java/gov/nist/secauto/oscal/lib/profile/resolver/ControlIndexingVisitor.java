
package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;

import javax.annotation.Nonnull;

public class ControlIndexingVisitor
    extends AbstractCatalogControlItemVisitor<Void, Void> {
  @Nonnull
  private final IIndexer indexer;

  public ControlIndexingVisitor(@Nonnull IIdentifierMapper mapper) {
    this.indexer = new DefaultIndexer(mapper);
  }

  @Nonnull
  protected IIndexer getIndexer() {
    return indexer;
  }

  @Nonnull
  public Index getIndex() {
    return indexer.getIndex();
  }

  @Override
  protected Void visitCatalog(@Nonnull IDocumentNodeItem catalogItem, Void context) {
    return super.visitCatalog(catalogItem, null);
  }

  @Override
  protected Void visitControl(@Nonnull IRequiredValueModelNodeItem controlItem, Void context) {
    getIndexer().addControl(controlItem, true);
    return super.visitControl(controlItem, context);
  }
  @Override
  protected Void visitControlContainer(@Nonnull IRequiredValueModelNodeItem catalogOrGroupOrControl,
      Void context) {
    // handle parameters
    catalogOrGroupOrControl.getModelItemsByName("param").forEach(paramItem -> {
      getIndexer().addParameter(paramItem);
    });
    return super.visitControlContainer(catalogOrGroupOrControl, null);
  }

  @Override
  protected Void newDefaultResult(Void context) {
    return null;
  }

  @Override
  protected Void aggregateResults(Void first, Void second, Void context) {
    return null;
  }
}
