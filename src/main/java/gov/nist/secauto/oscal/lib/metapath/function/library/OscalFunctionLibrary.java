/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.metapath.function.library;

import gov.nist.secauto.metaschema.core.metapath.function.FunctionLibrary;

public class OscalFunctionLibrary
    extends FunctionLibrary {

  public OscalFunctionLibrary() {
    registerFunction(ResolveProfile.SIGNATURE_NO_ARG);
    registerFunction(ResolveProfile.SIGNATURE_ONE_ARG);
    registerFunction(HasOscalNamespace.SIGNATURE_ONE_ARG);
    registerFunction(HasOscalNamespace.SIGNATURE_TWO_ARGS);

    // for backwards compatibility with no function namespace
    registerFunction(ResolveProfile.SIGNATURE_NO_ARG_METAPATH);
    registerFunction(ResolveProfile.SIGNATURE_ONE_ARG_METAPATH);
    registerFunction(HasOscalNamespace.SIGNATURE_ONE_ARG_METAPATH);
    registerFunction(HasOscalNamespace.SIGNATURE_TWO_ARGS_METAPATH);
  }
}
