/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.control.AbstractParameter;
import gov.nist.secauto.oscal.lib.model.control.AbstractPart;
import gov.nist.secauto.oscal.lib.model.control.catalog.AbstractCatalogGroup;
import gov.nist.secauto.oscal.lib.model.control.catalog.AbstractControl;
import gov.nist.secauto.oscal.lib.model.metadata.AbstractProperty;
import gov.nist.secauto.oscal.lib.model.metadata.IProperty;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IIdentifierMapper;

import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class TestUtil {

  @NonNull
  public static final IIdentifierMapper UUID_CONCAT_ID_MAPPER = new IIdentifierMapper() {
    private final Map<String, String> idToReassignmentMap = new ConcurrentHashMap<>();

    @NonNull
    private String reassign(@NonNull String identifier) {
      String retval = idToReassignmentMap.get(identifier);
      if (retval == null) {
        retval = identifier + "-" + UUID.randomUUID().toString();
        idToReassignmentMap.put(identifier, retval);
      }
      return retval;
    }

    @Override
    public String mapRoleIdentifier(@NonNull String identifier) {
      return reassign(identifier);
    }

    @Override
    public String mapControlIdentifier(@NonNull String identifier) {
      return reassign(identifier);
    }

    @Override
    public String mapGroupIdentifier(@NonNull String identifier) {
      return reassign(identifier);
    }

    @Override
    public String mapParameterIdentifier(@NonNull String identifier) {
      return reassign(identifier);
    }

    @Override
    public @NonNull
    String mapPartIdentifier(@NonNull String identifier) {
      return reassign(identifier);
    }
  };

  private TestUtil() {
    // disable construction
  }

  @NonNull
  public static IDocumentNodeItem newImportedCatalog() {

    // setup the imported catalog
    Catalog importedCatalog = new Catalog();
    importedCatalog.setUuid(UUID.randomUUID());

    importedCatalog.addParam(AbstractParameter.builder("param1")
        .build());

    importedCatalog.addGroup(AbstractCatalogGroup.builder("group1")
        .title("Group 1")
        .part(AbstractPart.builder("statement")
            .prose("group 1 part 1")
            .build())
        .param(AbstractParameter.builder("param2")
            .build())
        .control(AbstractControl.builder("control1")
            .title("Control 1")
            .param(AbstractParameter.builder("param3")
                .build())
            .part(AbstractPart.builder("statement")
                .prose("A {{ insert: param, param1}} reference.")
                .build())
            .part(AbstractPart.builder("statement")
                .prose("group 1 control 1 part 1")
                .part(AbstractPart.builder("statement")
                    .prose("group 1 control 1 part 1.a")
                    .build())
                .part(AbstractPart.builder("statement")
                    .prose("group 1 control 1 part 1.b")
                    .build())
                .build())
            .part(AbstractPart.builder("statement")
                .prose("group 1 control 1 part 2")
                .build())
            .build())
        // to be filtered
        .control(AbstractControl.builder("control2")
            .title("Control 2")
            .part(AbstractPart.builder("statement")
                .prose("A {{ insert: param, param2}} reference.")
                .build())
            .build())
        .build());
    importedCatalog.addGroup(AbstractCatalogGroup.builder("group2")
        .title("Group 2")
        .param(AbstractParameter.builder("param4")
            .prop(AbstractProperty.builder("aggregates")
                .namespace(IProperty.RMF_NAMESPACE)
                .value("param2")
                .build())
            .build())
        .control(AbstractControl.builder("control3")
            .title("Control 3")
            .build())
        .control(AbstractControl.builder("control4")
            .title("Control 4")
            .build())
        .group(AbstractCatalogGroup.builder("group3")
            .title("Group 3")
            // to be filtered
            .control(AbstractControl.builder("control5")
                .title("Control 5")
                .build())
            .build())
        .control(AbstractControl.builder("control6")
            .title("Control 6")
            .part(AbstractPart.builder("statement")
                .prose("A {{ insert: param, param4}} reference.")
                .build())
            .build())
        // to be filtered
        .control(AbstractControl.builder("control7")
            .title("Control 7")
            .param(AbstractParameter.builder("param5")
                .build())
            .control(AbstractControl.builder("control8")
                .title("Control 8")
                .part(AbstractPart.builder("statement")
                    .prose("A {{ insert: param, param5}} reference.")
                    .build())
                .build())
            .build())
        .build());

    return INodeItemFactory.instance().newDocumentNodeItem(
        ObjectUtils.requireNonNull(
            (IBoundDefinitionModelAssembly) OscalBindingContext.instance().getBoundDefinitionForClass(Catalog.class)),
        ObjectUtils.notNull(Paths.get(System.getProperty("user.dir")).toUri()),
        importedCatalog);
  }
}
