/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.model.control.catalog;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.AbstractOscalInstance;
import gov.nist.secauto.oscal.lib.model.control.AbstractParameter;

import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractCatalog
    extends AbstractOscalInstance
    implements ICatalog {

  @NonNull
  @Override
  public Stream<String> getReferencedParameterIds() {
    // get parameters referenced by the control's parameters
    return ObjectUtils.notNull(
        CollectionUtil.listOrEmpty(getParams()).stream()
            .flatMap(ObjectUtils::filterNull)
            .flatMap(AbstractParameter::getParameterReferences)
            .distinct());
  }
}
