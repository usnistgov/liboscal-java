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

package gov.nist.secauto.oscal.lib.metapath.function.library;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IXdmFactory;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnDocFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Stack;

public class ResolveProfile {
  private static final Logger logger = LogManager.getLogger(FnDocFunction.class);

  static final IFunction SIGNATURE_NO_ARG = IFunction.newBuilder()
      .name("resolve-profile")
      .returnType(INodeItem.class)
      .returnOne()
      .functionHandler(ResolveProfile::executeNoArg)
      .build();

  static final IFunction SIGNATURE_ONE_ARG = IFunction.newBuilder()
      .name("resolve-profile")
      .argument(IArgument.newBuilder()
          .name("profile")
          .type(INodeItem.class)
          .zeroOrOne()
          .build())
      .returnType(INodeItem.class)
      .returnOne()
      .functionHandler(ResolveProfile::executeOneArg)
      .build();

  @NotNull
  public static ISequence<?> executeNoArg(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments, @NotNull DynamicContext dynamicContext,
      INodeItem focus) {

    INodeItem item = focus;
    if (item == null) {
      return ISequence.empty();
    }
    return ISequence.of(resolveProfile(FunctionUtils.asType(item), dynamicContext));
  }

  @NotNull
  public static ISequence<?> executeOneArg(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments, @NotNull DynamicContext dynamicContext,
      INodeItem focus) {
    ISequence<? extends IDocumentNodeItem> arg = FunctionUtils.asType(arguments.get(0));

    IItem item = FunctionUtils.getFirstItem(arg, true);
    if (item == null) {
      return ISequence.empty();
    }

    return ISequence.of(resolveProfile(FunctionUtils.asType(item), dynamicContext));
  }
  
  @NotNull
  public static IDocumentNodeItem resolveProfile(@NotNull IDocumentNodeItem profile, @NotNull DynamicContext dynamicContext) {
    Object profileObject = IBoundLoader.toBoundObject(profile);

    IDocumentNodeItem retval;
    if (profileObject instanceof Catalog) {
      retval = profile;
    } else {
      // this is a profile
      URI baseUri = profile.getBaseUri();
      ProfileResolver resolver = new ProfileResolver(dynamicContext);
      @NotNull Catalog resolvedCatalog;
      try {
        ProfileResolver.ResolutionData data = new ProfileResolver.ResolutionData((Profile)profileObject, baseUri, new Stack<>());
        resolver.resolve(data);
        resolvedCatalog = data.getCatalog();
      } catch (IOException ex) {
        throw new MetapathException(String.format("Unable to resolve profile '%s'", profile.getBaseUri()), ex);
      }
      BindingContext bindingContext = OscalBindingContext.instance();
      retval = IXdmFactory.INSTANCE.newDocumentNodeItem(resolvedCatalog, bindingContext, profile.getBaseUri());
    }
    return retval;
  }
}
