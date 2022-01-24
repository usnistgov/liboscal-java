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

import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.oscal.lib.model.BackMatter;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Location;
import gov.nist.secauto.oscal.lib.model.Metadata;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Party;
import gov.nist.secauto.oscal.lib.model.Role;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Iterator;

public class ImportCatalogVisitor {
  private static final Logger log = LogManager.getLogger(ImportCatalogVisitor.class);

  @NotNull
  private final Index index;
  @NotNull
  private final URI source;

  public ImportCatalogVisitor(@NotNull Index index, @NotNull URI source) {
    this.index = index;
    this.source = source;
  }

  @NotNull
  public Index getIndex() {
    return index;
  }

  @NotNull
  public URI getSource() {
    return source;
  }

  public void visitCatalog(@NotNull Catalog catalog, @NotNull IControlFilter filter) {
    Result result = new Result();

    // process children
    Metadata metadata = catalog.getMetadata();
    if (metadata != null) {
      for (Role role : CollectionUtil.listOrEmpty(metadata.getRoles())) {
        visitRole(role);
      }

      for (Location location : CollectionUtil.listOrEmpty(metadata.getLocations())) {
        visitLocation(location);
      }

      for (Party party : CollectionUtil.listOrEmpty(metadata.getParties())) {
        visitParty(party);
      }

      BackMatter backMatter = catalog.getBackMatter();
      if (backMatter != null) {
        for (BackMatter.Resource resource : CollectionUtil.listOrEmpty(backMatter.getResources())) {
          visitResource(resource);
        }
      }
    }

    for (Iterator<CatalogGroup> iter = CollectionUtil.listOrEmpty(catalog.getGroups()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      CatalogGroup child = iter.next();
      Result childResult = visitGroup(child, filter);
      if (!childResult.isChildSelected()) {
        iter.remove();
      }

      // append result to current context
      result.append(childResult);
    }

    for (Iterator<Control> iter = CollectionUtil.listOrEmpty(catalog.getControls()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Control child = iter.next();
      Result childResult = visitControl(child, filter, false);
      if (!result.isSelected()) {
        iter.remove();
      }

      // append result to current context
      result.append(childResult);
    }

    for (Iterator<Parameter> parameterIter = CollectionUtil.listOrEmpty(catalog.getParams()).iterator(); parameterIter
        .hasNext();) {

      @SuppressWarnings("null")
      @NotNull
      Parameter childParam = parameterIter.next();

      visitParameter(childParam);
    }

    if (result.isChildSelected()) {
      result.apply(catalog);
    }
  }

  public void visitRole(@NotNull Role role) {
    EntityItem item = getIndex().addRole(role, getSource());
    if (item != null) {
      String entityType = item.getItemType().toString().toLowerCase();

      log.atWarn().log("The current {} '{}' in '{}' collides with the existing {} '{}' in '{}'. Using the current one.",
          entityType,
          role.getId(),
          getSource(),
          entityType,
          item.getIdentifier(),
          item.getSource(),
          entityType);
    }
  }

  private void visitLocation(@NotNull Location location) {
    EntityItem item = getIndex().addLocation(location, getSource());
    if (item != null) {
      String entityType = item.getItemType().toString().toLowerCase();

      log.atWarn().log("The current {} '{}' in '{}' collides with the existing {} '{}' in '{}'. Using the current one.",
          entityType,
          location.getUuid(),
          getSource(),
          entityType,
          item.getIdentifier(),
          item.getSource(),
          entityType);
    }
  }

  private void visitParty(@NotNull Party party) {
    EntityItem item = getIndex().addParty(party, getSource());
    if (item != null) {
      String entityType = item.getItemType().toString().toLowerCase();

      log.atWarn().log("The current {} '{}' in '{}' collides with the existing {} '{}' in '{}'. Using the current one.",
          entityType,
          party.getUuid(),
          getSource(),
          entityType,
          item.getIdentifier(),
          item.getSource(),
          entityType);
    }
  }

  private void visitResource(@NotNull Resource resource) {
    EntityItem item = getIndex().addResource(resource, getSource());
    if (item != null) {
      String entityType = item.getItemType().toString().toLowerCase();

      log.atWarn().log("The current {} '{}' in '{}' collides with the existing {} '{}' in '{}'. Using the current one.",
          entityType,
          resource.getUuid(),
          getSource(),
          entityType,
          item.getIdentifier(),
          item.getSource(),
          entityType);
    }
  }

  private void visitParameter(@NotNull Parameter parameter) {
    EntityItem item = getIndex().addParameter(parameter, getSource());
    if (item != null) {
      String entityType = item.getItemType().toString().toLowerCase();

      log.atWarn().log("The current {} '{}' in '{}' collides with the existing {} '{}' in '{}'. Using the current one.",
          entityType,
          parameter.getId(),
          getSource(),
          entityType,
          item.getIdentifier(),
          item.getSource(),
          entityType);
    }
  }

  public Result visitGroup(@NotNull CatalogGroup group, @NotNull IControlFilter filter) {
    Result result = new Result();

    // process children
    for (Iterator<CatalogGroup> iter = CollectionUtil.listOrEmpty(group.getGroups()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      CatalogGroup child = iter.next();
      Result childResult = visitGroup(child, filter);
      if (!childResult.isChildSelected()) {
        iter.remove();
      }

      // append result to current context
      result.append(childResult);
    }

    for (Iterator<Control> iter = CollectionUtil.listOrEmpty(group.getControls()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Control child = iter.next();
      Result childResult = visitControl(child, filter, false);
      if (!childResult.isSelected()) {
        iter.remove();
      }

      // apply result to current context
      result.append(childResult);
    }

    if (result.isChildSelected()) {
      // // manage referenced parameters
      // Set<@NotNull String> referencedParameterIds =
      // group.getReferencedParameterIds().collect(Collectors.toSet());
      //
      // // add them to the reference count index
      // referencedParameterIds.stream().forEach(pId -> getIndex().incrementParameterReferenceCount(pId));

      result.apply(group);

      result = new Result();
      result.setChildSelected(true);
      // } else {
      // // push up the result
      // for (Iterator<Parameter> parameterIter =
      // CollectionUtil.listOrEmpty(group.getParams()).iterator(); parameterIter
      // .hasNext();) {
      // Parameter childParam = parameterIter.next();
      //
      // String childParameterId = childParam.getId();
      //
      // if (getIndex().getParameterReferenceCount(childParameterId) == 0) {
      // // the parameter is unused
      // parameterIter.remove();
      // } else if (!result.isChildSelected()) {
      // // the parameter is used, but we will drop this group, so promote the parameter
      // result.promoteParameter(childParam);
      // }
      // }
    }

    for (Iterator<Parameter> parameterIter = CollectionUtil.listOrEmpty(group.getParams()).iterator(); parameterIter
        .hasNext();) {

      @SuppressWarnings("null")
      @NotNull
      Parameter childParam = parameterIter.next();

      visitParameter(childParam);
    }

    for (ControlPart partChild : CollectionUtil.listOrEmpty(group.getParts())) {
      visitPart(partChild);
    }

    // index the group
    getIndex().addGroup(group, getSource(), result.isChildSelected());

    return result;
  }

  public void visitPart(@NotNull ControlPart part) {
    EntityItem item = getIndex().addPart(part, getSource());
    if (item != null) {
      String entityType = item.getItemType().toString().toLowerCase();

      log.atWarn().log("The current {} '{}' in '{}' collides with the existing {} '{}' in '{}'. Using the current one.",
          entityType,
          part.getId(),
          getSource(),
          entityType,
          item.getIdentifier(),
          item.getSource(),
          entityType);
    }

    for (ControlPart partChild : CollectionUtil.listOrEmpty(part.getParts())) {
      visitPart(partChild);
    }
  }

  public Result visitControl(@NotNull Control control, @NotNull IControlFilter filter, boolean defaultMatch) {
    Pair<@NotNull Boolean, @NotNull Boolean> matchResult = filter.match(control, defaultMatch);
    @SuppressWarnings("null")
    boolean isMatch = matchResult.getLeft();
    @SuppressWarnings("null")
    boolean isWithChildren = matchResult.getRight();

    // index the control
    getIndex().addControl(control, getSource(), isMatch);

    Result result = new Result(isMatch);

    boolean defaultChildMatch = isMatch && isWithChildren;

    for (Iterator<Control> controlIter = CollectionUtil.listOrEmpty(control.getControls()).iterator(); controlIter
        .hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Control child = controlIter.next();
      Result childResult = visitControl(child, filter, defaultChildMatch);
      if (!childResult.isSelected()) {
        controlIter.remove();
      }

      if (isMatch) {
        // apply result to current context
        childResult.apply(control);
      } else {
        // push the result up
        result.append(childResult);
      }
    }

    for (Iterator<Parameter> parameterIter
        = CollectionUtil.listOrEmpty(control.getParams()).iterator(); parameterIter.hasNext();) {
      Parameter childParam = parameterIter.next();

      String childParameterId = childParam.getId();

      // if (getIndex().getParameterReferenceCount(childParameterId) == 0) {
      // // the parameter is unused
      // parameterIter.remove();
      // } else if (!isMatch) {
      if (!isMatch) {
        // the parameter is used, but we will drop this control, so promote the parameter
        log.atTrace().log("Promoting parameter '{}'", childParameterId);
        result.promoteParameter(childParam);
      } else {
        visitParameter(childParam);
      }
    }

    for (ControlPart partChild : CollectionUtil.listOrEmpty(control.getParts())) {
      visitPart(partChild);
    }

    return result;
    // // append in-scope parameters
    // List<Parameter> parameters = listOrEmpty(control.getParams());
    // parameters.stream().forEachOrdered(param -> inScopeParameters.put(param.getId(), param));
    //
    // controlStack.push(control);
    //
    // // determine matching controls
    // Result matchingChildren = listOrEmpty(control.getControls()).stream()
    // .filter(Objects::nonNull)
    // .map(child -> {
    // Result result = visitControl(child, filter);
    // return result;
    // }).reduce((resultA, resultB) -> {
    // return resultA.aggregate(resultB);
    // }).orElse(null);
    //
    // Result retval;
    // if (isMatch) {
    // // assign the resulting children to this control
    // control.setControls(matchingChildren.collect(Collectors.toList()));
    // control.getControls().forEach(child -> child.setParentControl(control));
    // // return this control as the child
    // retval = Stream.of(control);
    // } else {
    // // push the children up since this control is not a match
    // retval = matchingChildren;
    // }
    //
    // controlStack.pop();
    //
    // // remove in-scope parameters
    // parameters.stream().forEachOrdered(param -> inScopeParameters.remove(param.getId()));
    //
    // return retval;
  }

  public static class Result
      extends ControlResult {
    private boolean selected;
    private boolean childSelected;

    public Result() {
      this(false, false);
    }

    public Result(boolean selected) {
      this(selected, false);
    }

    public Result(boolean selected, boolean childSelected) {
      this.selected = selected;
      this.childSelected = childSelected;
    }

    public boolean isSelected() {
      return selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    public boolean isChildSelected() {
      return childSelected;
    }

    protected void setChildSelected(boolean childSelected) {
      this.childSelected = childSelected;
    }

    public void append(Result that) {
      super.append(that);

      if (that.isSelected() || that.isChildSelected()) {
        setChildSelected(true);
      }
    }
  }
}
