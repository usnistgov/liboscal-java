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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;

import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.BackMatter;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Metadata;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.ProfileImport;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionEvaluationException;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.ReferenceCountingVisitor;
import gov.nist.secauto.oscal.lib.profile.resolver.support.BasicIndexer;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIndexer;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class Import {

  @NonNull
  private final IDocumentNodeItem profileDocument;
  @NonNull
  private final IModelNodeItem profileImportItem;

  public Import(
      @NonNull IDocumentNodeItem profileDocument,
      @NonNull IModelNodeItem profileImportItem) {

    this.profileDocument = profileDocument;
    this.profileImportItem = profileImportItem;
  }

  protected IDocumentNodeItem getProfileItem() {
    return profileDocument;
  }

  protected IModelNodeItem getProfileImportItem() {
    return profileImportItem;
  }

  @NonNull
  protected ProfileImport getProfileImport() {
    return ObjectUtils.requireNonNull((ProfileImport) profileImportItem.getValue());
  }

  private static Catalog toCatalog(IDocumentNodeItem catalogDocument) {
    return (Catalog) catalogDocument.getValue();
  }

  @NonNull
  protected IControlFilter newControlFilter() {
    return IControlFilter.newInstance(getProfileImport());
  }

  @NonNull
  protected IIndexer newIndexer() {
    // TODO: add support for reassignment
    // IIdentifierMapper mapper = IIdentifierMapper.IDENTITY;
    // IIndexer indexer = new ReassignmentIndexer(mapper);
    return new BasicIndexer();
  }

  @NonNull
  public IIndexer resolve(@NonNull IDocumentNodeItem importedCatalogDocument, @NonNull Catalog resolvedCatalog)
      throws ProfileResolutionException {
    ProfileImport profileImport = getProfileImport();
    URI uri = ObjectUtils.requireNonNull(profileImport.getHref(), "profile import href is null");

    // determine which controls and groups to keep
    IControlFilter filter = newControlFilter();
    IIndexer indexer = newIndexer();
    IControlSelectionState state = new ControlSelectionState(indexer, filter);

    try {
      ControlSelectionVisitor.instance().visitCatalog(importedCatalogDocument, state);

      // process references
      ReferenceCountingVisitor.instance().visitCatalog(importedCatalogDocument, indexer, uri);

      // filter based on selections
      FilterNonSelectedVisitor.instance().visitCatalog(importedCatalogDocument, indexer);
    } catch (ProfileResolutionEvaluationException ex) {
      throw new ProfileResolutionException(
          String.format("Unable to resolve profile import '%s'. %s", uri.toString(), ex.getMessage()), ex);
    }

    Catalog importedCatalog = toCatalog(importedCatalogDocument);
    for (Parameter param : CollectionUtil.listOrEmpty(importedCatalog.getParams())) {
      if (param != null) {
        resolvedCatalog.addParam(param);
      }
    }
    for (Control control : CollectionUtil.listOrEmpty(importedCatalog.getControls())) {
      if (control != null) {
        resolvedCatalog.addControl(control);
      }
    }
    for (CatalogGroup group : CollectionUtil.listOrEmpty(importedCatalog.getGroups())) {
      if (group != null) {
        resolvedCatalog.addGroup(group);
      }
    }

    generateMetadata(importedCatalogDocument, resolvedCatalog, indexer);
    generateBackMatter(importedCatalogDocument, resolvedCatalog, indexer);
    return indexer;
  }

  private static void generateMetadata(
      @NonNull IDocumentNodeItem importedCatalogDocument,
      @NonNull Catalog resolvedCatalog,
      @NonNull IIndexer indexer) {
    Metadata importedMetadata = toCatalog(importedCatalogDocument).getMetadata();

    if (importedMetadata != null) {
      Metadata resolvedMetadata = resolvedCatalog.getMetadata();
      if (resolvedMetadata == null) {
        resolvedMetadata = new Metadata();
        resolvedCatalog.setMetadata(resolvedMetadata);
      }

      String importedVersion = importedMetadata.getOscalVersion();
      if (importedVersion != null) {
        Version importOscalVersion = VersionUtil.parseVersion(importedVersion, null, null);

        Version resolvedCatalogVersion
            = VersionUtil.parseVersion(resolvedMetadata.getOscalVersion(), null, null);

        if (importOscalVersion.compareTo(resolvedCatalogVersion) > 0) {
          resolvedMetadata.setOscalVersion(importOscalVersion.toString());
        }
      }

      // copy roles, parties, and locations with prop name:keep and any referenced
      resolvedMetadata.setRoles(
          IIndexer.filterDistinct(
              ObjectUtils.notNull(CollectionUtil.listOrEmpty(resolvedMetadata.getRoles()).stream()),
              indexer.getEntitiesByItemType(IEntityItem.ItemType.ROLE),
              item -> item.getId())
              .collect(Collectors.toCollection(LinkedList::new)));
      resolvedMetadata.setParties(
          IIndexer.filterDistinct(
              ObjectUtils.notNull(CollectionUtil.listOrEmpty(resolvedMetadata.getParties()).stream()),
              indexer.getEntitiesByItemType(IEntityItem.ItemType.PARTY),
              item -> item.getUuid())
              .collect(Collectors.toCollection(LinkedList::new)));
      resolvedMetadata.setLocations(
          IIndexer.filterDistinct(
              ObjectUtils.notNull(CollectionUtil.listOrEmpty(resolvedMetadata.getLocations()).stream()),
              indexer.getEntitiesByItemType(IEntityItem.ItemType.LOCATION),
              item -> item.getUuid())
              .collect(Collectors.toCollection(LinkedList::new)));
    }
  }

  private static void generateBackMatter(
      @NonNull IDocumentNodeItem importedCatalogDocument,
      @NonNull Catalog resolvedCatalog,
      @NonNull IIndexer indexer) {
    BackMatter importedBackMatter = toCatalog(importedCatalogDocument).getBackMatter();

    if (importedBackMatter != null) {
      BackMatter resolvedBackMatter = resolvedCatalog.getBackMatter();

      List<Resource> resolvedResources = resolvedBackMatter == null ? CollectionUtil.emptyList()
          : CollectionUtil.listOrEmpty(resolvedBackMatter.getResources());

      List<Resource> resources = IIndexer.filterDistinct(
          ObjectUtils.notNull(resolvedResources.stream()),
          indexer.getEntitiesByItemType(IEntityItem.ItemType.RESOURCE),
          item -> item.getUuid())
          .collect(Collectors.toCollection(LinkedList::new));

      if (!resources.isEmpty()) {
        if (resolvedBackMatter == null) {
          resolvedBackMatter = new BackMatter();
          resolvedCatalog.setBackMatter(resolvedBackMatter);
        }

        resolvedBackMatter.setResources(resources);
      }
    }
  }
}
