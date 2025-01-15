/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
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
    // promote
    promotedControls.get().add(control);
  }

  @Override
  public void applyTo(@NonNull Catalog parent) {
    applyRemovesTo(parent);
    getPromotedParameters().forEach(param -> parent.addParam(ObjectUtils.notNull(param)));
    getPromotedControls().forEach(control -> {
      assert control != null;
      // add promoted control
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
