/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public class LinkReferencePolicy
    extends AbstractMultiItemTypeReferencePolicy<Link> {
  private static final Logger LOGGER = LogManager.getLogger(LinkReferencePolicy.class);

  @SuppressWarnings("null")
  @NonNull
  public static LinkReferencePolicy create(@NonNull IEntityItem.ItemType itemType) {
    return create(List.of(itemType));
  }

  @NonNull
  public static LinkReferencePolicy create(@NonNull List<IEntityItem.ItemType> itemTypes) {
    return new LinkReferencePolicy(CollectionUtil.requireNonEmpty(itemTypes, "itemTypes"));
  }

  public LinkReferencePolicy(@NonNull List<IEntityItem.ItemType> itemTypes) {
    super(IIdentifierParser.FRAGMENT_PARSER, itemTypes);
  }

  @Override
  public String getReferenceText(@NonNull Link link) {
    return link.getHref().toString();
  }

  @Override
  public void setReferenceText(@NonNull Link link, @NonNull String newValue) {
    link.setHref(URI.create(newValue));
  }

  @Override
  protected void handleUnselected(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull Link link,
      @NonNull IEntityItem item,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    URI linkHref = link.getHref();
    URI sourceUri = item.getSource();

    URI resolved = visitorContext.getUriResolver().resolve(ObjectUtils.requireNonNull(linkHref), sourceUri);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.atTrace().log("At path '{}', remapping orphaned URI '{}' to '{}'",
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
          linkHref.toString(),
          resolved.toString());
    }
    link.setHref(resolved);
  }

  @Override
  protected boolean handleIndexMiss(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull Link link,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.atWarn().log(
          "The link at '{}' with rel '{}' should reference a {} identified by '{}'."
              + " The index did not contain the identifier.",
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
          link.getRel(),
          itemTypes.stream()
              .map(en -> en.name().toLowerCase(Locale.ROOT))
              .collect(CustomCollectors.joiningWithOxfordComma("or")),
          identifier);
    }
    return true;
  }

  @Override
  protected boolean handleIdentifierNonMatch(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull Link reference,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Ignoring URI '{}' at '{}'",
          reference.getHref().toString(),
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
    }

    return true;
  }
}
