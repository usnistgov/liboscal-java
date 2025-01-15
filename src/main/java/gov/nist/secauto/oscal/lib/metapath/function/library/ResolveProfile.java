/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.metapath.function.library;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.ISequence;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalModelConstants;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolver;

import java.io.IOException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class ResolveProfile {

  @NonNull
  static final IFunction SIGNATURE_NO_ARG = IFunction.builder()
      .name("resolve-profile")
      .namespace(OscalModelConstants.NS_OSCAL)
      .returnType(INodeItem.type())
      .focusDependent()
      .contextDependent()
      .deterministic()
      .returnOne()
      .functionHandler(ResolveProfile::executeNoArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("resolve-profile")
      .namespace(OscalModelConstants.NS_OSCAL)
      .argument(IArgument.builder()
          .name("profile")
          .type(INodeItem.type())
          .zeroOrOne()
          .build())
      .focusDependent()
      .contextDependent()
      .deterministic()
      .returnType(INodeItem.type())
      .returnOne()
      .functionHandler(ResolveProfile::executeOneArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_NO_ARG_METAPATH = IFunction.builder()
      .name("resolve-profile")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .returnType(INodeItem.type())
      .focusDependent()
      .contextDependent()
      .deterministic()
      .returnOne()
      .functionHandler(ResolveProfile::executeNoArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_ONE_ARG_METAPATH = IFunction.builder()
      .name("resolve-profile")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .argument(IArgument.builder()
          .name("profile")
          .type(INodeItem.type())
          .zeroOrOne()
          .build())
      .focusDependent()
      .contextDependent()
      .deterministic()
      .returnType(INodeItem.type())
      .returnOne()
      .functionHandler(ResolveProfile::executeOneArg)
      .build();

  private ResolveProfile() {
    // disable construction
  }

  @SuppressWarnings({ "unused",
      "PMD.OnlyOneReturn" // readability
  })
  @NonNull
  public static ISequence<?> executeNoArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    if (focus == null) {
      return ISequence.empty();
    }
    return ISequence.of(resolveProfile(FunctionUtils.asType(focus), dynamicContext));
  }

  @SuppressWarnings({ "unused",
      "PMD.OnlyOneReturn" // readability
  })
  @NonNull
  public static ISequence<?> executeOneArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {
    ISequence<? extends IDocumentNodeItem> arg = FunctionUtils.asType(
        ObjectUtils.notNull(arguments.get(0)));

    IItem item = arg.getFirstItem(true);
    if (item == null) {
      return ISequence.empty();
    }

    return ISequence.of(resolveProfile(FunctionUtils.asType(item), dynamicContext));
  }

  @NonNull
  public static IDocumentNodeItem resolveProfile(
      @NonNull IDocumentNodeItem profile,
      @NonNull DynamicContext dynamicContext) {
    Object profileObject = INodeItem.toValue(profile);

    IDocumentNodeItem retval;
    if (profileObject instanceof Catalog) {
      retval = profile;
    } else {
      // this is a profile
      ProfileResolver resolver
          = new ProfileResolver(dynamicContext, (uri, source) -> profile.getDocumentUri().resolve(uri));
      try {
        retval = resolver.resolve(profile);
      } catch (IOException | ProfileResolutionException ex) {
        throw new MetapathException(
            String.format("Unable to resolve profile '%s'. %s",
                profile.getBaseUri(),
                ex.getLocalizedMessage()),
            ex);
      }
    }
    return retval;
  }
}
