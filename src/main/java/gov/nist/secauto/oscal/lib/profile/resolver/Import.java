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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;

import gov.nist.secauto.metaschema.binding.io.BindingException;
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
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.ReferenceCountingVisitor;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Import {

  @NotNull
  private final IDocumentNodeItem profileDocument;
  @NotNull
  private final IModelNodeItem profileImportItem;

  public Import(
      @NotNull IDocumentNodeItem profileDocument,
      @NotNull IModelNodeItem profileImportItem) throws BindingException {

    this.profileDocument = profileDocument;
    this.profileImportItem = profileImportItem;
  }

  protected IDocumentNodeItem getProfileItem() {
    return profileDocument;
  }

  protected IModelNodeItem getProfileImportItem() {
    return profileImportItem;
  }

  @SuppressWarnings("null")
  @NotNull
  protected ProfileImport getProfileImport() {
    return (@NotNull ProfileImport) profileImportItem.getValue();
  }

  private Catalog toCatalog(IDocumentNodeItem catalogDocument) {
    return (Catalog) catalogDocument.getValue();
  }

  @NotNull
  protected IControlFilter newControlFilter() {
    return IControlFilter.newInstance(getProfileImport());
  }

  @NotNull
  protected IIdentifierMapper newIdentifierMapper() {
    return IIdentifierMapper.IDENTITY;
  }

  public Index resolve(@NotNull IDocumentNodeItem importedCatalogDocument, @NotNull Catalog resolvedCatalog) {
    ProfileImport profileImport = getProfileImport();
    URI uri = ObjectUtils.requireNonNull(profileImport.getHref(), "profile import href is null");

    // determine which controls and groups to keep
    IControlFilter filter = newControlFilter();
    IIdentifierMapper mapper = newIdentifierMapper();
    ControlSelectionVisitor selectionVisitor = new ControlSelectionVisitor(filter, mapper);
    selectionVisitor.visitCatalog(importedCatalogDocument);
    Index index = selectionVisitor.getIndex();

    // process references
    new ReferenceCountingVisitor(index, uri).visitCatalog(importedCatalogDocument);

    // filter based on selections
    FilterNonSelectedVisitor pruneVisitor = new FilterNonSelectedVisitor(index);
    pruneVisitor.visitCatalog(importedCatalogDocument);

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

    generateMetadata(importedCatalogDocument, resolvedCatalog, index);
    generateBackMatter(importedCatalogDocument, resolvedCatalog, index);
    return index;
  }

  private void generateMetadata(@NotNull IDocumentNodeItem importedCatalogDocument, @NotNull Catalog resolvedCatalog,
      @NotNull Index index) {
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
          Index.merge(
              ObjectUtils.notNull(CollectionUtil.listOrEmpty(resolvedMetadata.getRoles()).stream()),
              index,
              ItemType.ROLE,
              item -> item.getId())
              .collect(Collectors.toCollection(LinkedList::new)));
      resolvedMetadata.setParties(
          Index.merge(
              ObjectUtils.notNull(CollectionUtil.listOrEmpty(resolvedMetadata.getParties()).stream()),
              index,
              ItemType.PARTY,
              item -> item.getUuid())
              .collect(Collectors.toCollection(LinkedList::new)));
      resolvedMetadata.setLocations(
          Index.merge(
              ObjectUtils.notNull(CollectionUtil.listOrEmpty(resolvedMetadata.getLocations()).stream()),
              index,
              ItemType.LOCATION,
              item -> item.getUuid())
              .collect(Collectors.toCollection(LinkedList::new)));
    }
  }

  private void generateBackMatter(@NotNull IDocumentNodeItem importedCatalogDocument, @NotNull Catalog resolvedCatalog,
      Index index) {
    BackMatter importedBackMatter = toCatalog(importedCatalogDocument).getBackMatter();

    if (importedBackMatter != null) {
      BackMatter resolvedBackMatter = resolvedCatalog.getBackMatter();

      List<Resource> resolvedResources = resolvedBackMatter == null ? CollectionUtil.emptyList()
          : CollectionUtil.listOrEmpty(resolvedBackMatter.getResources());

      List<Resource> resources = Index.merge(
          ObjectUtils.notNull(resolvedResources.stream()),
          index,
          ItemType.RESOURCE,
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
