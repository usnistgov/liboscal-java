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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Parameter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class DefaultResult implements IResult {
  private static final Logger LOGGER = LogManager.getLogger(DefaultResult.class);

  @NonNull
  private final Lazy<Set<Control>> promotedControls = ObjectUtils.notNull(Lazy.lazy(LinkedHashSet::new));
  @NonNull
  private final Lazy<Set<Parameter>> promotedParameters = ObjectUtils.notNull(Lazy.lazy(LinkedHashSet::new));
  @NonNull
  private final Lazy<Set<CatalogGroup>> removedGroups = ObjectUtils.notNull(Lazy.lazy(HashSet::new));
  @NonNull
  private final Lazy<Set<Control>> removedControls = ObjectUtils.notNull(Lazy.lazy(HashSet::new));
  @NonNull
  private final Lazy<Set<Parameter>> removedParameters = ObjectUtils.notNull(Lazy.lazy(HashSet::new));

  @SuppressWarnings("null")
  @NonNull
  protected Collection<Parameter> getPromotedParameters() {
    return promotedParameters.getIfAvailable().orElse(Collections.emptySet());
  }

  @SuppressWarnings("null")
  @NonNull
  protected Collection<Control> getPromotedControls() {
    return promotedControls.getIfAvailable().orElse(Collections.emptySet());
  }

  @SuppressWarnings("null")
  @NonNull
  protected Collection<CatalogGroup> getRemovedGroups() {
    return removedGroups.getIfAvailable().orElse(Collections.emptySet());
  }

  @SuppressWarnings("null")
  @NonNull
  protected Collection<Control> getRemovedControls() {
    return removedControls.getIfAvailable().orElse(Collections.emptySet());
  }

  @SuppressWarnings("null")
  @NonNull
  protected Collection<Parameter> getRemovedParameters() {
    return removedParameters.getIfAvailable().orElse(Collections.emptySet());
  }

  @Override
  public void promoteParameter(@NonNull Parameter param) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("promoting parameter '{}'", param.getId());
    }
    promotedParameters.get().add(param);
  }

  @Override
  public void promoteControl(@NonNull Control control) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("promoting control '{}'", control.getId());
    }
    promotedControls.get().add(control);
  }

  @Override
  public void applyTo(@NonNull Catalog parent) {
    applyRemovesTo(parent);
    getPromotedParameters().forEach(param -> parent.addParam(ObjectUtils.notNull(param)));
    getPromotedControls().forEach(control -> {
      assert control != null;
      parent.addControl(control);
      control.setParentControl(null);
    });
  }

  @Override
  public void applyTo(@NonNull CatalogGroup parent) {
    applyRemovesTo(parent);
    getPromotedControls().forEach(control -> {
      assert control != null;
      parent.addControl(control);
      control.setParentControl(null);
    });
    getPromotedParameters().forEach(param -> parent.addParam(ObjectUtils.notNull(param)));
  }

  @Override
  public void applyTo(@NonNull Control parent) {
    applyRemovesTo(parent);
    getPromotedControls().forEach(control -> {
      assert control != null;
      parent.addControl(control);
      control.setParentControl(null);
    });
    getPromotedParameters().forEach(param -> parent.addParam(ObjectUtils.notNull(param)));
  }

  public void applyRemovesTo(Catalog parent) {
    removeItems(parent.getGroups(), getRemovedGroups());
    removeItems(parent.getControls(), getRemovedControls());
    removeItems(parent.getParams(), getRemovedParameters());
  }

  public void applyRemovesTo(CatalogGroup parent) {
    removeItems(parent.getGroups(), getRemovedGroups());
    removeItems(parent.getControls(), getRemovedControls());
    removeItems(parent.getParams(), getRemovedParameters());
  }

  public void applyRemovesTo(Control parent) {
    removeItems(parent.getControls(), getRemovedControls());
    removeItems(parent.getParams(), getRemovedParameters());
  }

  public DefaultResult append(@NonNull DefaultResult that) {
    lazyAppend(promotedControls, that.promotedControls);
    lazyAppend(promotedParameters, that.promotedParameters);
    lazyAppend(removedGroups, that.removedGroups);
    lazyAppend(removedControls, that.removedControls);
    lazyAppend(removedParameters, that.removedParameters);
    return this;
  }

  public DefaultResult appendPromoted(@NonNull DefaultResult that) {
    lazyAppend(promotedControls, that.promotedControls);
    lazyAppend(promotedParameters, that.promotedParameters);
    return this;
  }

  protected static <T> void lazyAppend(@NonNull Lazy<Set<T>> self, @NonNull Lazy<Set<T>> other) {
    if (other.isAvailable()) {
      Set<T> otherSet = other.get();
      if (!otherSet.isEmpty()) {
        self.get().addAll(otherSet);
      }
    }
  }

  protected static <T> void removeItems(List<T> list, @NonNull Collection<T> itemsToDelete) {
    itemsToDelete.forEach(item -> {
      if (!list.remove(item)) {
        LOGGER.atError().log("item didn't exist in list");
      }
    });
  }

  public void removeGroup(CatalogGroup group) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Requesting removal of group '{}'.", group.getId());
    }
    removedGroups.get().add(group);
  }

  public void removeControl(Control control) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Requesting removal of control '{}'.", control.getId());
    }
    removedControls.get().add(control);
  }

  public void removeParameter(Parameter parameter) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Requesting removal of parameter '{}'.", parameter.getId());
    }
    removedParameters.get().add(parameter);
  }
}
