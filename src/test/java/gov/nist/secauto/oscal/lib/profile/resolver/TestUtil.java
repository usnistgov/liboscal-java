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
    public @NonNull String mapPartIdentifier(@NonNull String identifier) {
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
        ObjectUtils.notNull(Paths.get("").toUri()),
        importedCatalog);
  }
}
