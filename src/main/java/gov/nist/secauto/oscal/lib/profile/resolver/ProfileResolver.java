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
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.OscalUtils;
import gov.nist.secauto.oscal.lib.model.BackMatter;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Location;
import gov.nist.secauto.oscal.lib.model.Merge;
import gov.nist.secauto.oscal.lib.model.Metadata;
import gov.nist.secauto.oscal.lib.model.Party;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.ProfileImport;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.Role;
import gov.nist.secauto.oscal.lib.model.builder.LinkBuilder;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.resource.Source;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfileResolver {
  private static final Logger log = LogManager.getLogger(ProfileResolver.class);

  public enum StructuringDirective {
    FLAT,
    AS_IS,
    CUSTOM;
  }

  private final DynamicContext dynamicContext;

  public ProfileResolver(@NotNull DynamicContext dynamicContext) {
    this.dynamicContext = dynamicContext;
  }

  protected DynamicContext getDynamicContext() {
    return dynamicContext;
  }

  public @NotNull Catalog resolve(@NotNull INodeItem profile) throws IOException {
    return resolve(profile, new Stack<>());
  }

  public @NotNull Catalog resolve(@NotNull INodeItem profile, @NotNull Stack<@NotNull URI> importHistory)
      throws IOException {
    Object profileObject = IBoundLoader.toBoundObject(profile);

    Catalog retval;
    if (profileObject instanceof Catalog) {
      retval = (Catalog) profileObject;
    } else {
      URI baseUri = profile.getBaseUri();
      if (baseUri == null) {
        throw new NullPointerException("profile.getBaseUri() must return a non-null URI");
      }

      Profile boundProfile = (Profile) profileObject;
      try {
        boundProfile = OscalBindingContext.instance().copyBoundObject(boundProfile, null);
      } catch (BindingException ex) {
        throw new IOException(ex);
      }

      ResolutionData data = new ResolutionData(boundProfile, baseUri, importHistory);
      resolve(data);
      retval = data.getCatalog();
    }
    return retval;
  }

  @NotNull
  public void resolve(@NotNull ResolutionData data) throws IOException, IllegalStateException {
    // final Profile profile, @NotNull final URI documentUri,
    // @NotNull Stack<@NotNull URI> importHistory

    Catalog resolvedCatalog = data.getCatalog();
    resolvedCatalog.setParams(new LinkedList<>());
    resolvedCatalog.setControls(new LinkedList<>());
    resolvedCatalog.setGroups(new LinkedList<>());

    resolvedCatalog.setUuid(UUID.randomUUID());

    Profile profile = data.getProfile();

    Metadata metadata = new Metadata();
    metadata.setTitle(profile.getMetadata().getTitle());
    metadata.setVersion(profile.getMetadata().getVersion());
    metadata.setOscalVersion(OscalUtils.OSCAL_VERSION);
    metadata.setLastModified(ZonedDateTime.now(ZoneOffset.UTC));

    metadata.setProps(new LinkedList<>());
    metadata.getProps().add(Property.builder("resolution-tool").value("libOSCAL-Java").build());
    metadata.setLinks(new LinkedList<>());
    URI profileUri = data.getProfileUri();
    metadata.getLinks().add(new LinkBuilder(profileUri).relation("source-profile").build());

    resolvedCatalog.setMetadata(metadata);

    Version version;
    if (profile.getMetadata().getVersion() != null) {
      version = VersionUtil.parseVersion(profile.getMetadata().getVersion(), null, null);
    } else {
      version = Version.unknownVersion();
    }
    metadata.setOscalVersion(version.toString());

    resolveImports(data);
    handleMerge(data);
    handleReferences(data);
  }

  private void resolveImports(@NotNull ResolutionData data) throws IOException {
    Profile profile = data.getProfile();

    List<ProfileImport> imports = profile.getImports();
    if (imports == null || imports.isEmpty()) {
      throw new IllegalStateException(String.format("profile '%s' has no imports", data.getProfileUri()));
    }

    for (ProfileImport profileImport : imports) {
      if (profileImport == null)
        continue;

      resolveImport(profileImport, data);
    }
  }

  @NotNull
  private void resolveImport(@NotNull ProfileImport profileImport, @NotNull ResolutionData data)
      throws IOException {
    URI importUri = profileImport.getHref();
    if (importUri == null) {
      throw new NullPointerException("profileImport.getHref() must return a non-null URI");
    }

    log.debug("resolving profile import '{}'", importUri);

    URI profileUri = data.getProfileUri();
    Stack<URI> importHistory = data.getImportHistory();

    IDocumentNodeItem document;
    if (OscalUtils.isInternalReference(importUri)) {
      // handle internal reference
      String uuid = OscalUtils.internalReferenceFragmentToId(importUri);

      Profile profile = data.getProfile();
      Resource resource = profile.getResourceByUuid(ObjectUtils.notNull(UUID.fromString(uuid)));

      @SuppressWarnings("null")
      @NotNull
      URI resolvedUri = importUri.resolve(profileUri);
      if (resource == null) {
        throw new NullPointerException(
            String.format("unable to find the resource identified by '%s' used in profile import", resolvedUri));
      }

      // check for import cycle
      try {
        requireNonCycle(resolvedUri, importHistory);
      } catch (ImportCycleException ex) {
        throw new IOException(ex);
      }
      importHistory.push(resolvedUri);

      Source source = OscalUtils.newSource(resource, resolvedUri, null);
      document = (IDocumentNodeItem) getDynamicContext().getDocumentLoader().loadAsNodeItem(source.newInputStream(),
          source.getSystemId());

      @SuppressWarnings("null")
      URI poppedUri = importHistory.pop();
      assert resolvedUri.equals(poppedUri);
    } else {
      // handle external reference
      @SuppressWarnings("null")
      @NotNull
      URI source = profileUri.resolve(importUri);

      // check for import cycle
      try {
        requireNonCycle(source, importHistory);
      } catch (ImportCycleException ex) {
        throw new IOException(ex);
      }

      document = (IDocumentNodeItem) getDynamicContext().getDocumentLoader()
          .loadAsNodeItem(ObjectUtils.notNull(source.toURL()));
    }

    importHistory.push(document.getDocumentUri());

    Catalog importedCatalog = resolve(document, importHistory);

    // make a defensive copy, since we will be modifying the catalog
    try {
      importedCatalog = OscalBindingContext.instance().copyBoundObject(importedCatalog, null);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }

    // filter controls based on selections
    IControlFilter filter = IControlFilter.newInstance(profileImport);
    new ImportCatalogVisitor(data.getIndex(), importUri).visitCatalog(importedCatalog, filter);

    // pop the resolved catalog from the import history
    URI poppedUri = importHistory.pop();
    assert document.getDocumentUri().equals(poppedUri);

    Version catalogVersion = VersionUtil.parseVersion(importedCatalog.getMetadata().getOscalVersion(), null, null);

    Catalog resolvingCatalog = data.getCatalog();
    Version resolvingCatalogVersion
        = VersionUtil.parseVersion(resolvingCatalog.getMetadata().getOscalVersion(), null, null);

    if (catalogVersion.compareTo(resolvingCatalogVersion) > 0) {
      resolvingCatalog.getMetadata().setOscalVersion(catalogVersion.toString());
    }

    resolvingCatalog.getParams().addAll(CollectionUtil.listOrEmpty(importedCatalog.getParams()));
    resolvingCatalog.getControls().addAll(CollectionUtil.listOrEmpty(importedCatalog.getControls()));
    resolvingCatalog.getGroups().addAll(CollectionUtil.listOrEmpty(importedCatalog.getGroups()));

    // TODO: copy roles, parties, and locations with prop name:keep and any referenced
    // TODO: handle resources properly
    BackMatter backMatter = importedCatalog.getBackMatter();
    if (backMatter != null) {
      BackMatter resolvingBackMatter = resolvingCatalog.getBackMatter();
      if (resolvingBackMatter == null) {
        resolvingBackMatter = new BackMatter();
        resolvingBackMatter.setResources(new LinkedList<>());
        resolvingCatalog.setBackMatter(resolvingBackMatter);
      }

      resolvingBackMatter.getResources().addAll(backMatter.getResources());
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

  private void handleMerge(@NotNull ResolutionData data) {
    // handle combine

    // handle structuring
    switch (getStructuringDirective(data.getProfile())) {
    case AS_IS:
      // do nothing
      break;
    case CUSTOM:
      throw new UnsupportedOperationException("custom structuring");
    case FLAT:
    default:
      structureFlat(data.getCatalog());
      break;
    }

  }

  private void structureFlat(@NotNull Catalog catalog) {
    log.debug("applying flat structuring directive");
    new FlatStructureCatalogVisitor().visitCatalog(catalog);
  }

  private void handleReferences(@NotNull ResolutionData data) {
    ReferenceCountingVisitor visitor = new ReferenceCountingVisitor(data);
    
    visitor.visitCatalog(data.getCatalog());
    visitor.visitProfile(data.getProfile());

    Catalog catalog = data.getCatalog();
    Metadata metadata = catalog.getMetadata();

    Index index = data.getIndex();

    metadata.setRoles(index.getEntitiesByItemType(ItemType.ROLE).stream()
        .filter(item -> {
          boolean retval = item.getReferenceCount() > 0;
          if (!retval) {
            Role instance = (Role) item.getInstance();
            retval = Property.find(instance.getProps(), Property.qname("keep"))
                .map(prop -> "always".equals(prop.getValue()))
                .or(() -> Optional.of(false))
                .get();

          }
          return retval;
        })
        .map(item -> (Role)item.getInstance())
        .collect(Collectors.toCollection(LinkedList::new)));
    metadata.setLocations(index.getEntitiesByItemType(ItemType.LOCATION).stream()
        .filter(item -> {
          boolean retval = item.getReferenceCount() > 0;
          if (!retval) {
            Location instance = (Location) item.getInstance();
            retval = Property.find(instance.getProps(), Property.qname("keep"))
                .map(prop -> "always".equals(prop.getValue()))
                .or(() -> Optional.of(false))
                .get();

          }
          return retval;
        })
        .map(item -> (Location)item.getInstance())
        .collect(Collectors.toCollection(LinkedList::new)));
    metadata.setParties(index.getEntitiesByItemType(ItemType.PARTY).stream()
        .filter(item -> {
          boolean retval = item.getReferenceCount() > 0;
          if (!retval) {
            Party instance = (Party) item.getInstance();
            retval = Property.find(instance.getProps(), Property.qname("keep"))
                .map(prop -> "always".equals(prop.getValue()))
                .or(() -> Optional.of(false))
                .get();

          }
          return retval;
        })
        .map(item -> (Party)item.getInstance())
        .collect(Collectors.toCollection(LinkedList::new)));

    List<BackMatter.Resource> resources = index.getEntitiesByItemType(ItemType.RESOURCE).stream()
    .filter(item -> {
      boolean retval = item.getReferenceCount() > 0;
      if (!retval) {
        BackMatter.Resource instance = (BackMatter.Resource) item.getInstance();
        retval = Property.find(instance.getProps(), Property.qname("keep"))
            .map(prop -> "always".equals(prop.getValue()))
            .or(() -> Optional.of(false))
            .get();

      }
      return retval;
    })
    .map(item -> (BackMatter.Resource)item.getInstance())
    .collect(Collectors.toCollection(LinkedList::new));
    
    if (!resources.isEmpty()) {
      BackMatter backMatter = catalog.getBackMatter();
      if (backMatter == null) {
        backMatter = new BackMatter();
        catalog.setBackMatter(backMatter);
      }
  
      backMatter.setResources(resources);
    }
  }

  public static class ResolutionData {
    @NotNull
    private final Profile profile;
    @NotNull
    private final URI documentUri;
    @NotNull
    private final Stack<@NotNull URI> importHistory;
    @NotNull
    private final Index index = new Index();
    @NotNull
    private final Catalog catalog = new Catalog();

    public ResolutionData(@NotNull Profile profile, @NotNull URI documentUri,
        @NotNull Stack<@NotNull URI> importHistory) {
      this.profile = profile;
      this.documentUri = documentUri;
      this.importHistory = importHistory;
    }

    @NotNull
    public Profile getProfile() {
      return profile;
    }

    @NotNull
    public URI getProfileUri() {
      return documentUri;
    }

    @NotNull
    public Stack<@NotNull URI> getImportHistory() {
      return importHistory;
    }

    @NotNull
    public Index getIndex() {
      return index;
    }

    @NotNull
    public Catalog getCatalog() {
      return catalog;
    }
  }
}
