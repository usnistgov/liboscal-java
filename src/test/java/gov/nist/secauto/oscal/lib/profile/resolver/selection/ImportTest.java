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

import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.item.DefaultNodeItemFactory;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.IncludeAll;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.model.ProfileImport;
import gov.nist.secauto.oscal.lib.model.control.catalog.AbstractControl;
import gov.nist.secauto.oscal.lib.model.control.profile.AbstractProfileSelectControlById;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;

import edu.umd.cs.findbugs.annotations.NonNull;

class ImportTest {

  @NonNull
  private static IDocumentNodeItem newImportedCatalog() {

    // setup the imported catalog
    Catalog importedCatalog = new Catalog();

    importedCatalog.addControl(AbstractControl.builder("control1")
        .title("Control 1")
        .build());
    importedCatalog.addControl(AbstractControl.builder("control2")
        .title("Control 2")
        .build());

    return DefaultNodeItemFactory.instance().newDocumentNodeItem(
        IRootAssemblyDefinition.toRootAssemblyDefinition(
            ObjectUtils.notNull(
                (IAssemblyClassBinding) OscalBindingContext.instance().getClassBinding(Catalog.class))),
        importedCatalog,
        ObjectUtils.notNull(Paths.get("").toUri()));
  }

  @SuppressWarnings("null")
  @Test
  void test() throws ProfileResolutionException {
    URI cwd = Paths.get("").toUri();

    // setup the imported catalog
    IDocumentNodeItem importedCatalogDocumentItem = newImportedCatalog();

    // setup the profile
    Profile profile = new Profile();

    ProfileImport profileImport = new ProfileImport();
    profileImport.setIncludeAll(new IncludeAll());
    profileImport.setExcludeControls(Collections.singletonList(
        AbstractProfileSelectControlById.builder()
            .withId("control1")
            .build()));
    profileImport.setHref(cwd);
    profile.addImport(profileImport);

    IDocumentNodeItem profileDocumentItem = DefaultNodeItemFactory.instance().newDocumentNodeItem(
        IRootAssemblyDefinition.toRootAssemblyDefinition(
            ObjectUtils.notNull(
                (IAssemblyClassBinding) OscalBindingContext.instance().getClassBinding(Profile.class))),
        profile,
        cwd);

    // setup the resolved catalog
    Catalog resolvedCatalog = new Catalog();

    for (IRequiredValueModelNodeItem importItem : CollectionUtil.toIterable(
        profileDocumentItem.getModelItemsByName("profile").stream()
            .flatMap(root -> root.getModelItemsByName("import").stream()))) {

      Import catalogImport = new Import(profileDocumentItem, importItem);
      catalogImport.resolve(importedCatalogDocumentItem, resolvedCatalog);
    }
  }

}
