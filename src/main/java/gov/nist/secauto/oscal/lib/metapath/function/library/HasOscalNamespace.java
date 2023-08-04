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

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidTypeFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.AssessmentPart;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Property;
import gov.nist.secauto.oscal.lib.model.metadata.AbstractProperty;

import java.net.URI;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class HasOscalNamespace {
  @NonNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("has-oscal-namespace")
      .namespace(OscalBindingContext.NS_OSCAL)
      .argument(IArgument.newBuilder()
          .name("namespace")
          .type(IStringItem.class)
          .oneOrMore()
          .build())
      .allowUnboundedArity(true)
      .returnType(IBooleanItem.class)
      .focusDependent()
      .contextIndependent()
      .deterministic()
      .returnOne()
      .functionHandler(HasOscalNamespace::executeOneArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_TWO_ARGS = IFunction.builder()
      .name("has-oscal-namespace")
      .namespace(OscalBindingContext.NS_OSCAL)
      .argument(IArgument.newBuilder()
          .name("propOrPart")
          .type(IAssemblyNodeItem.class)
          .one()
          .build())
      .argument(IArgument.newBuilder()
          .name("namespace")
          .type(IStringItem.class)
          .oneOrMore()
          .build())
      .allowUnboundedArity(true)
      .focusIndependent()
      .contextIndependent()
      .deterministic()
      .returnType(IBooleanItem.class)
      .returnOne()
      .functionHandler(HasOscalNamespace::executeTwoArg)
      .build();

  private HasOscalNamespace() {
    // disable construction
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
    assert arguments.size() == 1;
    ISequence<? extends IStringItem> namespaceArgs = FunctionUtils.asType(
        ObjectUtils.notNull(arguments.get(0)));

    if (namespaceArgs.isEmpty()) {
      return ISequence.empty();
    }

    IAssemblyNodeItem node = FunctionUtils.requireType(IAssemblyNodeItem.class, focus);
    return ISequence.of(hasNamespace(FunctionUtils.asType(node), namespaceArgs));
  }

  @SuppressWarnings({ "unused",
      "PMD.OnlyOneReturn" // readability
  })
  @NonNull
  public static ISequence<?> executeTwoArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {
    assert arguments.size() == 2;

    ISequence<? extends IStringItem> namespaceArgs = FunctionUtils.asType(
        ObjectUtils.notNull(arguments.get(1)));
    if (namespaceArgs.isEmpty()) {
      return ISequence.empty();
    }

    ISequence<? extends IAssemblyNodeItem> nodeSequence = FunctionUtils.asType(
        ObjectUtils.notNull(arguments.get(0)));

    // always not null, since the first item is required
    IAssemblyNodeItem node = FunctionUtils.requireFirstItem(nodeSequence, true);
    return ISequence.of(hasNamespace(node, namespaceArgs));
  }

  @SuppressWarnings("PMD.LinguisticNaming") // false positive
  @NonNull
  public static IBooleanItem hasNamespace(
      @NonNull IAssemblyNodeItem propOrPart,
      @NonNull ISequence<? extends IStringItem> namespaces)
      throws MetapathException {
    Object propOrPartObject = propOrPart.getValue();
    if (propOrPartObject == null) {
      throw new InvalidTypeFunctionException(InvalidTypeFunctionException.NODE_HAS_NO_TYPED_VALUE, propOrPart);
    }

    URI nodeNamespace;
    if (propOrPartObject instanceof Property) {
      nodeNamespace = ((Property) propOrPartObject).getNs();
    } else if (propOrPartObject instanceof ControlPart) {
      nodeNamespace = ((ControlPart) propOrPartObject).getNs();
    } else if (propOrPartObject instanceof AssessmentPart) {
      nodeNamespace = ((AssessmentPart) propOrPartObject).getNs();
    } else {
      throw new MetapathException(
          String.format("Node of definition type '%s' has no OSCAL namespace", propOrPart.getDefinition().getName()));
    }

    String nodeNamespaceString = AbstractProperty.normalizeNamespace(nodeNamespace).toString();
    return IBooleanItem.valueOf(namespaces.asStream()
        .map(node -> nodeNamespaceString.equals(node.asString()))
        .anyMatch(bool -> bool));
  }
}
