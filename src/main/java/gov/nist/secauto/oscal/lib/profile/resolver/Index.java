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

import gov.nist.secauto.oscal.lib.model.BackMatter.Resource;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Location;
import gov.nist.secauto.oscal.lib.model.Parameter;
import gov.nist.secauto.oscal.lib.model.Party;
import gov.nist.secauto.oscal.lib.model.Role;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Index {
  @NotNull
  private final Map<ItemType, ItemGroup> entityMap;
  
  
  @SuppressWarnings("null")
  public Index() {
    this.entityMap = new EnumMap<>(ItemType.class);
  }


  @NotNull
  protected ItemGroup getItemGroup(@NotNull ItemType itemType) {
    ItemGroup retval = entityMap.get(itemType);
    if (retval == null) {
      retval = new ItemGroup(itemType);
      entityMap.put(itemType, retval);
    }
    return retval;
  }

  @NotNull
  public Collection<EntityItem> getEntitiesByItemType(@NotNull ItemType itemType) {
    ItemGroup group = getItemGroup(itemType);
    return group.getEntities();
  }

  @SuppressWarnings("null")
  public EntityItem getEntity(@NotNull ItemType itemType, @NotNull UUID identifier) {
    return getEntity(itemType, identifier.toString());
  }

  public EntityItem getEntity(@NotNull ItemType itemType, @NotNull String identifier) {
    ItemGroup group = getItemGroup(itemType);
    return group.getEntity(identifier);
  }

  @SuppressWarnings("null")
  public EntityItem addRole(@NotNull Role role, @NotNull URI source) {
    ItemGroup group = getItemGroup(ItemType.ROLE);
    return group.add(EntityItem.builder()
        .itemType(ItemType.ROLE)
        .instance(role, role.getId())
        .source(source)
        .build());
  }

  @SuppressWarnings("null")
  public EntityItem addLocation(@NotNull Location location, @NotNull URI source) {
    ItemGroup group = getItemGroup(ItemType.LOCATION);
    return group.add(EntityItem.builder()
        .itemType(ItemType.LOCATION)
        .instance(location, location.getUuid())
        .source(source)
        .build());
  }

  @SuppressWarnings("null")
  public EntityItem addParty(@NotNull Party party, @NotNull URI source) {
    ItemGroup group = getItemGroup(ItemType.PARTY);
    return group.add(EntityItem.builder()
        .itemType(ItemType.PARTY)
        .instance(party, party.getUuid())
        .source(source)
        .build());
  }

  @SuppressWarnings("null")
  public EntityItem addControl(@NotNull Control control, @NotNull URI source) {
    ItemGroup group = getItemGroup(ItemType.CONTROL);
    return group.add(EntityItem.builder()
        .itemType(ItemType.CONTROL)
        .instance(control, control.getId())
        .source(source)
        .build());
  }

  @SuppressWarnings("null")
  public EntityItem addParameter(@NotNull Parameter parameter, @NotNull URI source) {
    ItemGroup group = getItemGroup(ItemType.PARAMETER);
    return group.add(EntityItem.builder()
        .itemType(ItemType.PARAMETER)
        .instance(parameter, parameter.getId())
        .source(source)
        .build());
  }

  @SuppressWarnings("null")
  public EntityItem addResource(@NotNull Resource resource, @NotNull URI source) {
    ItemGroup group = getItemGroup(ItemType.RESOURCE);
    return group.add(EntityItem.builder()
        .itemType(ItemType.RESOURCE)
        .instance(resource, resource.getUuid())
        .source(source)
        .build());
  }

//
//  public int getParameterReferenceCount(String parameterId) {
//    Integer count = parameterReferenceCountMap.get(parameterId);
//    if (count == null) {
//      count = 0;
//    }
//    return count;
//  }
//
//  public void incrementParameterReferenceCount(@NotNull String parameterId) {
//    int count = getParameterReferenceCount(parameterId);
//    
//    parameterReferenceCountMap.put(parameterId, ++count);
//  }
//
//  public void decrementParameterReferenceCount(@NotNull String parameterId) {
//    int count = getParameterReferenceCount(parameterId);
//    
//    if (count == 0) {
//      throw new IllegalStateException(String.format("reference count for parameter '%s' is already 0", parameterId));
//    } else {
//      count -= 1;
//      if (count > 0) {
//        parameterReferenceCountMap.put(parameterId, count);
//      } else {
//        parameterReferenceCountMap.remove(parameterId);
//      }
//    }
//  }

  public static class ItemGroup {
    @NotNull
    private final ItemType itemType;
    Map<String, EntityItem> idToEntityMap;

    public ItemGroup(@NotNull ItemType itemType) {
      this.itemType = itemType;
      this.idToEntityMap = new LinkedHashMap<>();
    }

    public EntityItem getEntity(@NotNull String identifier) {
      return idToEntityMap.get(identifier);
    }

    @SuppressWarnings("null")
    @NotNull
    public Collection<EntityItem> getEntities() {
      return idToEntityMap.values();
    }

    public EntityItem add(@NotNull EntityItem entity) {
      assert itemType.equals(entity.getItemType());
      return idToEntityMap.put(entity.getIdentifier(), entity);
    }
  }
}
