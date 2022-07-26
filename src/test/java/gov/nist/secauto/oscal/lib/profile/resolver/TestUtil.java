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

import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.item.DefaultNodeItemFactory;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Property;
import javax.annotation.Nonnull;

import java.nio.file.Paths;
import java.util.UUID;

public final class TestUtil {

  @Nonnull
  public static final IIdentifierMapper UUID_CONCAT_ID_MAPPER = new IIdentifierMapper() {

    @Override
    public String mapRoleIdentifier(@Nonnull String identifier) {
      return identifier + "-" + UUID.randomUUID().toString();
    }

    @Override
    public String mapControlIdentifier(@Nonnull String identifier) {
      return identifier + "-" + UUID.randomUUID().toString();
    }

    @Override
    public String mapGroupIdentifier(@Nonnull String identifier) {
      return identifier + "-" + UUID.randomUUID().toString();
    }

    @Override
    public String mapParameterIdentifier(@Nonnull String identifier) {
      return identifier + "-" + UUID.randomUUID().toString();
    }

    @Override
    public @Nonnull String mapPartIdentifier(@Nonnull String identifier) {
      return identifier + "-" + UUID.randomUUID().toString();
    }
  };

  private TestUtil() {
    // disable construction
  }

  @Nonnull
  public static IDocumentNodeItem newImportedCatalog() {

    // setup the imported catalog
    Catalog importedCatalog = new Catalog();
    importedCatalog.setUuid(UUID.randomUUID());

    importedCatalog.addParam(Parameter.builder("param1")
        .build());

    importedCatalog.addGroup(CatalogGroup.builder("group1")
        .title("Group 1")
        .part(ControlPart.builder("statement") // NOPMD - no need to reduce literals
            .prose("group 1 part 1")
            .build())
        .param(Parameter.builder("param2")
            .build())
        .control(Control.builder("control1")
            .title("Control 1")
            .param(Parameter.builder("param3")
                .build())
            .part(ControlPart.builder("statement")
                .prose("A {{ insert: param, param1}} reference.")
                .build())
            .part(ControlPart.builder("statement")
                .prose("group 1 control 1 part 1")
                .part(ControlPart.builder("statement")
                    .prose("group 1 control 1 part 1.a")
                    .build())
                .part(ControlPart.builder("statement")
                    .prose("group 1 control 1 part 1.b")
                    .build())
                .build())
            .part(ControlPart.builder("statement")
                .prose("group 1 control 1 part 2")
                .build())
            .build())
        // to be filtered
        .control(Control.builder("control2")
            .title("Control 2")
            .part(ControlPart.builder("statement")
                .prose("A {{ insert: param, param2}} reference.")
                .build())
            .build())
        .build());
    importedCatalog.addGroup(CatalogGroup.builder("group2")
        .title("Group 2")
        .param(Parameter.builder("param4")
            .prop(Property.builder("aggregates")
                .namespace(Property.RMF_NAMESPACE)
                .value("param2")
                .build())
            .build())
        .control(Control.builder("control3")
            .title("Control 3")
            .build())
        .control(Control.builder("control4")
            .title("Control 4")
            .build())
        .group(CatalogGroup.builder("group3")
            .title("Group 3")
            // to be filtered
            .control(Control.builder("control5")
                .title("Control 5")
                .build())
            .build())
        .control(Control.builder("control6")
            .title("Control 6")
            .part(ControlPart.builder("statement")
                .prose("A {{ insert: param, param4}} reference.")
                .build())
            .build())
        // to be filtered
        .control(Control.builder("control7")
            .title("Control 7")
            .param(Parameter.builder("param5")
                .build())
            .control(Control.builder("control8")
                .title("Control 8")
                .part(ControlPart.builder("statement")
                    .prose("A {{ insert: param, param5}} reference.")
                    .build())
                .build())
            .build())
        .build());

    return DefaultNodeItemFactory.instance().newDocumentNodeItem(
        IRootAssemblyDefinition.toRootAssemblyDefinition(
            (IAssemblyClassBinding) OscalBindingContext.instance().getClassBinding(Catalog.class)),
        importedCatalog,
        ObjectUtils.notNull(Paths.get("").toUri()));
  }
}
