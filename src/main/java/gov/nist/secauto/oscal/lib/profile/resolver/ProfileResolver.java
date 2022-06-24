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

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.RootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.DefaultNodeItemFactory;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.OscalUtils;
import gov.nist.secauto.oscal.lib.model.BackMatter;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.model.Merge;
import gov.nist.secauto.oscal.lib.model.Metadata;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.ProfileImport;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.ReferenceCountingVisitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfileResolver {
  private static final Logger LOGGER = LogManager.getLogger(ProfileResolver.class);

  public enum StructuringDirective {
    FLAT,
    AS_IS,
    CUSTOM;
  }

  private IBoundLoader loader;
  private DynamicContext dynamicContext;

  @NotNull
  public IBoundLoader getBoundLoader() {
    synchronized (this) {
      if (loader == null) {
        loader = OscalBindingContext.instance().newBoundLoader();
      }
    }
    assert loader != null;
    return loader;
  }

  public void setBoundLoader(@NotNull IBoundLoader loader) {
    synchronized (this) {
      this.loader = loader;
    }
  }

  @NotNull
  public DynamicContext getDynamicContext() {
    synchronized (this) {
      if (dynamicContext == null) {
        dynamicContext = new StaticContext().newDynamicContext();
        dynamicContext.setDocumentLoader(getBoundLoader());
      }
    }
    assert dynamicContext != null;
    return dynamicContext;
  }

  public void setDynamicContext(@NotNull DynamicContext dynamicContext) {
    synchronized (this) {
      this.dynamicContext = dynamicContext;
    }
  }

  @NotNull
  protected EntityResolver getEntityResolver(@NotNull URI documentUri) {
    return new DocumentEntityResolver(documentUri);
  }

  public IDocumentNodeItem resolveProfile(@NotNull URL url) throws URISyntaxException, IOException {
    IBoundLoader loader = getBoundLoader();
    IDocumentNodeItem catalogOrProfile = loader.loadAsNodeItem(url);
    return resolve(catalogOrProfile);
  }

  public IDocumentNodeItem resolveProfile(@NotNull Path path) throws IOException {
    IBoundLoader loader = getBoundLoader();
    IDocumentNodeItem catalogOrProfile = loader.loadAsNodeItem(path);
    return resolve(catalogOrProfile);
  }

  public IDocumentNodeItem resolveProfile(@NotNull File file) throws IOException {
    return resolveProfile(ObjectUtils.notNull(file.toPath()));
  }

  @NotNull
  public IDocumentNodeItem resolve(@NotNull IDocumentNodeItem profileOrCatalog) throws IOException {
    return resolve(profileOrCatalog, new Stack<>());
  }

  @NotNull
  protected IDocumentNodeItem resolve(@NotNull IDocumentNodeItem profileOrCatalog,
      @NotNull Stack<@NotNull URI> importHistory)
      throws IOException {
    Object profileObject = profileOrCatalog.getValue();

    IDocumentNodeItem retval;
    if (profileObject instanceof Catalog) {
      // already a catalog
      retval = profileOrCatalog;
    } else {
      // must be a profile
      retval = resolveProfile(profileOrCatalog, importHistory);
    }
    return retval;
  }

  /**
   * Resolve the profile to a catalog.
   * 
   * @param profileDocument
   *          a {@link IDocumentNodeItem} containing the profile to resolve
   * @param importHistory
   *          the import stack for cycle detection
   * @return the resolved profile
   * @throws IOException
   *           if an error occurs while loading the profile or an import
   */
  @NotNull
  protected IDocumentNodeItem resolveProfile(
      @NotNull IDocumentNodeItem profileDocument,
      @NotNull Stack<@NotNull URI> importHistory) throws IOException {
    Catalog resolvedCatalog = new Catalog();

    generateMetadata(resolvedCatalog, profileDocument);

    resolveImports(resolvedCatalog, profileDocument, importHistory);
    handleMerge(resolvedCatalog, profileDocument);
    handleReferences(resolvedCatalog, profileDocument);

    return DefaultNodeItemFactory.instance().newDocumentNodeItem(
        new RootAssemblyDefinition(
            (IAssemblyClassBinding) OscalBindingContext.instance().getClassBinding(Catalog.class)),
        resolvedCatalog,
        profileDocument.getBaseUri());
  }

  private Profile toProfile(@NotNull IDocumentNodeItem profileDocument) {
    Object object = profileDocument.getValue();
    assert object != null;

    return (Profile) object;
  }

  @NotNull
  private static Profile toProfile(@NotNull IRootAssemblyNodeItem profileItem) {
    Object object = profileItem.getValue();
    assert object != null;

    return (Profile) object;
  }

  private void generateMetadata(@NotNull Catalog resolvedCatalog, @NotNull IDocumentNodeItem profileDocument) {
    resolvedCatalog.setUuid(UUID.randomUUID());

    Profile profile = toProfile(profileDocument);
    Metadata profileMetadata = profile.getMetadata();

    Metadata resolvedMetadata = new Metadata();
    resolvedMetadata.setTitle(profileMetadata.getTitle());

    if (profileMetadata.getVersion() != null) {
      resolvedMetadata.setVersion(profileMetadata.getVersion());
    }

    // metadata.setOscalVersion(OscalUtils.OSCAL_VERSION);
    resolvedMetadata.setOscalVersion(profileMetadata.getOscalVersion());

    resolvedMetadata.setLastModified(ZonedDateTime.now(ZoneOffset.UTC));

    resolvedMetadata.addProp(Property.builder("resolution-tool").value("libOSCAL-Java").build());

    URI profileUri = profileDocument.getDocumentUri();
    resolvedMetadata.addLink(Link.builder(profileUri).relation("source-profile").build());

    resolvedCatalog.setMetadata(resolvedMetadata);
  }

  private void resolveImports(@NotNull Catalog resolvedCatalog, @NotNull IDocumentNodeItem profileDocument,
      @NotNull Stack<@NotNull URI> importHistory)
      throws IOException {

    IRootAssemblyNodeItem profileItem = profileDocument.getRootAssemblyNodeItem();

    // first verify there is at least one import
    List<@NotNull ? extends IRequiredValueModelNodeItem> profileImports = profileItem.getModelItemsByName("import");
    if (profileImports.isEmpty()) {
      throw new IllegalStateException(String.format("Profile '%s' has no imports", profileItem.getBaseUri()));
    }

    // now process each import
    for (IRequiredValueModelNodeItem profileImportItem : profileImports) {
      ProfileImport profileImport = (ProfileImport) profileImportItem.getValue();

      URI importUri = profileImport.getHref();
      if (importUri == null) {
        throw new IllegalArgumentException("profileImport.getHref() must return a non-null URI");
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.atDebug().log("resolving profile import '{}'", importUri);
      }

      // Create an entity resolver to resolve relative references in the profile
      EntityResolver resolver = getEntityResolver(profileDocument.getDocumentUri());

      InputSource source;
      if (OscalUtils.isInternalReference(importUri)) {
        // handle internal reference
        String uuid = OscalUtils.internalReferenceFragmentToId(importUri);

        Profile profile = toProfile(profileItem);
        Resource resource = profile.getResourceByUuid(ObjectUtils.notNull(UUID.fromString(uuid)));
        if (resource == null) {
          throw new IllegalArgumentException(
              String.format("unable to find the resource identified by '%s' used in profile import", importUri));
        }

        source = OscalUtils.newInputSource(resource, resolver, null);
      } else {
        try {
          source = resolver.resolveEntity(null, importUri.toASCIIString());
        } catch (SAXException ex) {
          throw new IOException(ex);
        }
      }

      if (source == null || source.getSystemId() == null) {
        throw new IOException(String.format("Unable to resolve import '%s'.", importUri.toString()));
      }
      importUri = ObjectUtils.notNull(URI.create(source.getSystemId()));

      // check for import cycle
      try {
        requireNonCycle(importUri, importHistory);
      } catch (ImportCycleException ex) {
        throw new IOException(ex);
      }
      importHistory.push(importUri);

      IDocumentNodeItem document;
      document = (IDocumentNodeItem) getDynamicContext().getDocumentLoader().loadAsNodeItem(source);

      IDocumentNodeItem importedCatalog = resolve(document, importHistory);

      // Create a defensive deep copy of the document and associated values, since we will be making
      // changes to the data.
      try {
        importedCatalog = DefaultNodeItemFactory.instance().newDocumentNodeItem(
            importedCatalog.getRootAssemblyNodeItem().getDefinition(),
            OscalBindingContext.instance().copyBoundObject(importedCatalog.getValue(), null),
            importedCatalog.getDocumentUri());

        new Import(profileDocument, profileImportItem).resolve(importedCatalog, resolvedCatalog);
      } catch (BindingException ex) {
        throw new IOException(ex);
      }

      // pop the resolved catalog from the import history
      URI poppedUri = ObjectUtils.notNull(importHistory.pop());
      assert document.getDocumentUri().equals(poppedUri);
    }
  }

  @NotNull
  private void requireNonCycle(@NotNull URI uri, @NotNull Stack<@NotNull URI> importHistory)
      throws ImportCycleException {
    List<URI> cycle = checkCycle(uri, importHistory);
    if (!cycle.isEmpty()) {
      throw new ImportCycleException(String.format("Importing resource '%s' would result in the import cycle: %s", uri,
          cycle.stream().map(cycleUri -> cycleUri.toString()).collect(Collectors.joining(" -> ", " -> ", ""))));
    }
  }

  @SuppressWarnings("null")
  @NotNull
  private List<URI> checkCycle(@NotNull URI uri, @NotNull Stack<@NotNull URI> importHistory) {
    int index = importHistory.indexOf(uri);

    List<URI> retval;
    if (index == -1) {
      retval = Collections.emptyList();
    } else {
      retval = Collections.unmodifiableList(importHistory.subList(0, index + 1));
    }
    return retval;
  }

  private StructuringDirective getStructuringDirective(Profile profile) {
    Merge merge = profile.getMerge();

    StructuringDirective retval;
    if (merge == null) {
      retval = StructuringDirective.FLAT;
    } else if (merge.getAsIs() != null && merge.getAsIs()) {
      retval = StructuringDirective.AS_IS;
    } else if (merge.getCustom() != null) {
      retval = StructuringDirective.CUSTOM;
    } else {
      retval = StructuringDirective.FLAT;
    }
    return retval;
  }

  private void handleMerge(@NotNull Catalog resolvedCatalog, @NotNull IDocumentNodeItem profileDocument) {
    // handle combine

    // handle structuring
    switch (getStructuringDirective(toProfile(profileDocument))) {
    case AS_IS:
      // do nothing
      break;
    case CUSTOM:
      throw new UnsupportedOperationException("custom structuring");
    case FLAT:
    default:
      structureFlat(resolvedCatalog);
      break;
    }

  }

  private void structureFlat(@NotNull Catalog catalog) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("applying flat structuring directive");
    }
    new FlatStructureCatalogVisitor().visitCatalog(catalog);
  }

  private void handleReferences(@NotNull Catalog resolvedCatalog, @NotNull IDocumentNodeItem profileDocument) {
    IDocumentNodeItem resolvedCatalogItem = DefaultNodeItemFactory.instance().newDocumentNodeItem(
        new RootAssemblyDefinition(
            (IAssemblyClassBinding) OscalBindingContext.instance().getClassBinding(Catalog.class)),
        resolvedCatalog,
        profileDocument.getBaseUri());

    ControlSelectionVisitor selectionVisitor
        = new ControlSelectionVisitor(IControlFilter.ALWAYS_MATCH, IIdentifierMapper.IDENTITY);
    selectionVisitor.visitCatalog(resolvedCatalogItem);
    selectionVisitor.visitProfile(profileDocument);
    Index index = selectionVisitor.getIndex();

    // process references
    new ReferenceCountingVisitor(index, profileDocument.getBaseUri()).visitCatalog(resolvedCatalogItem);

    // filter based on selections
    FilterNonSelectedVisitor pruneVisitor = new FilterNonSelectedVisitor(index);
    pruneVisitor.visitCatalog(resolvedCatalogItem);

    // copy roles, parties, and locations with prop name:keep and any referenced
    Metadata resolvedMetadata = resolvedCatalog.getMetadata();
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

    // copy resources
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

  private class DocumentEntityResolver implements EntityResolver {
    @NotNull
    private final URI documentUri;

    public DocumentEntityResolver(@NotNull URI documentUri) {
      this.documentUri = documentUri;
    }

    @NotNull
    protected URI getDocumentUri() {
      return documentUri;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

      URI resolvedUri = getDocumentUri().resolve(systemId);

      EntityResolver resolver = getDynamicContext().getDocumentLoader().getEntityResolver();

      InputSource retval;
      if (resolver == null) {
        retval = new InputSource(resolvedUri.toASCIIString());
      } else {
        retval = resolver.resolveEntity(publicId, resolvedUri.toASCIIString());
      }
      return retval;
    }

  }
}
