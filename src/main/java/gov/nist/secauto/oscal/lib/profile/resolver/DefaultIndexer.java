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

import gov.nist.secauto.metaschema.model.common.metapath.item.IRequiredValueModelNodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.ControlPart;
import gov.nist.secauto.oscal.lib.model.Location;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Party;
import gov.nist.secauto.oscal.lib.model.Role;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import javax.annotation.Nonnull;

import java.util.UUID;

public class DefaultIndexer implements IIndexer {
  @Nonnull
  private final Index index;
  @Nonnull
  private final IIdentifierMapper mapper;

  public DefaultIndexer(@Nonnull IIdentifierMapper mapper) {
    this(new Index(), mapper);
  }

  public DefaultIndexer(@Nonnull Index index, @Nonnull IIdentifierMapper mapper) {
    this.index = index;
    this.mapper = mapper;
  }

  @Override
  @Nonnull
  public Index getIndex() {
    return index;
  }

  @Nonnull
  protected IIdentifierMapper getMapper() {
    return mapper;
  }

  @Override
  public EntityItem addRole(@Nonnull IRequiredValueModelNodeItem item) {
    Role role = (Role) item.getValue();
    String identifier = ObjectUtils.notNull(role.getId());
    String mappedIdentifier = getMapper().mapRoleIdentifier(identifier);

    // role.setId(mappedIdentifier);

    return addItem(
        ItemType.ROLE,
        item,
        identifier,
        mappedIdentifier);
  }

  @Override
  public EntityItem addLocation(@Nonnull IRequiredValueModelNodeItem item) {
    Location location = (Location) item.getValue();
    UUID identifier = ObjectUtils.notNull(location.getUuid());

    return addItem(
        ItemType.LOCATION,
        item,
        identifier);
  }

  @Override
  public EntityItem addParty(@Nonnull IRequiredValueModelNodeItem item) {
    Party party = (Party) item.getValue();
    UUID identifier = ObjectUtils.notNull(party.getUuid());

    return addItem(
        ItemType.PARTY,
        item,
        identifier);
  }

  @Override
  public EntityItem addGroup(@Nonnull IRequiredValueModelNodeItem item, boolean selected) {
    CatalogGroup group = (CatalogGroup) item.getValue();

    if (selected) {
      getIndex().markSelected(group);
    }

    String identifier = group.getId();

    EntityItem retval = null;
    if (identifier != null) {
      String mappedIdentifier = getMapper().mapGroupIdentifier(identifier);

      // group.setId(mappedIdentifier);

      retval = addItem(
          ItemType.GROUP,
          item,
          identifier,
          mappedIdentifier);
    }
    return retval;
  }

  @Override
  public EntityItem addControl(@Nonnull IRequiredValueModelNodeItem item, boolean selected) {
    Control control = (Control) item.getValue();
    String identifier = ObjectUtils.notNull(control.getId());
    String mappedIdentifier = getMapper().mapControlIdentifier(identifier);

    // control.setId(mappedIdentifier);

    if (selected) {
      getIndex().markSelected(control);
    }

    return addItem(
        ItemType.CONTROL,
        item,
        identifier,
        mappedIdentifier);
  }

  @Override
  public EntityItem addParameter(@Nonnull IRequiredValueModelNodeItem item) {
    Parameter parameter = (Parameter) item.getValue();
    String identifier = ObjectUtils.notNull(parameter.getId());
    String mappedIdentifier = getMapper().mapParameterIdentifier(identifier);

    // parameter.setId(mappedIdentifier);

    return addItem(
        ItemType.PARAMETER,
        item,
        identifier,
        mappedIdentifier);
  }

  @Override
  public EntityItem addPart(@Nonnull IRequiredValueModelNodeItem item) {
    ControlPart part = (ControlPart) item.getValue();
    String identifier = part.getId();

    EntityItem retval = null;
    if (identifier != null) {
      String mappedIdentifier = getMapper().mapPartIdentifier(identifier);

      // it's to update the part's id here
      part.setId(mappedIdentifier);

      retval = addItem(
          ItemType.PART,
          item,
          identifier,
          mappedIdentifier);
    }
    return retval;
  }

  @Override
  public EntityItem addResource(@Nonnull IRequiredValueModelNodeItem item) {
    Resource resource = (Resource) item.getValue();
    UUID identifier = ObjectUtils.notNull(resource.getUuid());

    return addItem(
        ItemType.RESOURCE,
        item,
        identifier);
  }

  protected <T> EntityItem addItem(@Nonnull ItemType type, @Nonnull IRequiredValueModelNodeItem item,
      @Nonnull UUID identifier) {
    EntityItem.Builder builder = EntityItem.builder()
        .instance(item, identifier)
        .source(ObjectUtils.requireNonNull(item.getBaseUri(), "item must have an associated URI"));
    return addItem(type, builder);
  }

  protected <T> EntityItem addItem(@Nonnull ItemType type, @Nonnull IRequiredValueModelNodeItem item,
      @Nonnull String identifier) {
    EntityItem.Builder builder = EntityItem.builder()
        .instance(item, identifier)
        .source(ObjectUtils.requireNonNull(item.getBaseUri(), "item must have an associated URI"));

    return addItem(type, builder);
  }

  protected EntityItem addItem(@Nonnull ItemType type, @Nonnull IRequiredValueModelNodeItem item,
      @Nonnull String identifier,
      @Nonnull String mappedIdentifier) {

    EntityItem.Builder builder = EntityItem.builder()
        .instance(item, mappedIdentifier)
        .originalIdentifier(identifier)
        .source(ObjectUtils.requireNonNull(item.getBaseUri(), "item must have an associated URI"));
    return addItem(type, builder);
  }

  protected <T> EntityItem addItem(@Nonnull ItemType type, @Nonnull EntityItem.Builder builder) {

    builder
        .itemType(type);

    return getIndex().addItem(builder.build());
  }
}
