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

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.oscal.lib.model.AssessmentPart;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Property;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;

public final class HasOscalNamespace {
  private HasOscalNamespace() {
    // disable construction
  }

  static final IFunction SIGNATURE_ONE_ARG = IFunction.newBuilder()
      .name("has-oscal-namespace")
      .argument(IArgument.newBuilder()
          .name("namespace")
          .type(IStringItem.class)
          .oneOrMore()
          .build())
      .returnType(IBooleanItem.class)
      .focusDependent()
      .contextIndependent()
      .deterministic()
      .returnOne()
      .functionHandler(HasOscalNamespace::executeOneArg)
      .build();

  static final IFunction SIGNATURE_TWO_ARGS = IFunction.newBuilder()
      .name("has-oscal-namespace")
      .argument(IArgument.newBuilder()
          .name("propOrPart")
          .type(INodeItem.class)
          .one()
          .build())
      .argument(IArgument.newBuilder()
          .name("namespace")
          .type(IStringItem.class)
          .oneOrMore()
          .build())
      .focusIndependent()
      .contextIndependent()
      .deterministic()
      .returnType(IBooleanItem.class)
      .returnOne()
      .functionHandler(HasOscalNamespace::executeTwoArg)
      .build();

  @NotNull
  public static ISequence<?> executeOneArg(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments, @NotNull DynamicContext dynamicContext,
      INodeItem focus) {
    INodeItem node = focus;
    if (node == null) {
      return ISequence.empty();
    }

    ISequence<? extends IStringItem> namespaceArgs = FunctionUtils.asType(arguments.get(0));
    if (namespaceArgs.isEmpty()) {
      return ISequence.empty();
    }

    return ISequence.of(hasNamespace(FunctionUtils.asType(node), namespaceArgs, dynamicContext));
  }

  @NotNull
  public static ISequence<?> executeTwoArg(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments, @NotNull DynamicContext dynamicContext,
      INodeItem focus) {
    ISequence<? extends INodeItem> nodeSequence = FunctionUtils.asType(arguments.get(0));

    IItem node = FunctionUtils.getFirstItem(nodeSequence, true);
    if (node == null) {
      return ISequence.empty();
    }

    ISequence<? extends IStringItem> namespaceArgs = FunctionUtils.asType(arguments.get(0));
    if (namespaceArgs.isEmpty()) {
      return ISequence.empty();
    }

    return ISequence.of(hasNamespace(FunctionUtils.asType(node), namespaceArgs, dynamicContext));
  }

  @NotNull
  public static IBooleanItem hasNamespace(@NotNull INodeItem propOrPart,
      @NotNull ISequence<? extends IStringItem> namespaces, @NotNull DynamicContext dynamicContext)
      throws MetapathException {
    Object propOrPartObject = propOrPart.toBoundObject();

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

    String nodeNamespaceString = Property.normalizeNamespace(nodeNamespace).toString();
    return IBooleanItem.valueOf(namespaces.asStream()
        .map(node -> nodeNamespaceString.equals(node.asString()))
        .anyMatch(bool -> bool));
  }
}
