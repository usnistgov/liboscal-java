/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.CharSubSequence;

import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.profile.resolver.support.IEntityItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public class AnchorReferencePolicy
    extends AbstractCustomReferencePolicy<InlineLinkNode> {
  private static final Logger LOGGER = LogManager.getLogger(AnchorReferencePolicy.class);

  public AnchorReferencePolicy() {
    super(IIdentifierParser.FRAGMENT_PARSER);
  }

  @SuppressWarnings("null")
  @Override
  protected List<IEntityItem.ItemType> getEntityItemTypes(@NonNull InlineLinkNode link) {
    return List.of(
        IEntityItem.ItemType.RESOURCE,
        IEntityItem.ItemType.CONTROL,
        IEntityItem.ItemType.GROUP,
        IEntityItem.ItemType.PART);
  }

  @Override
  public String getReferenceText(@NonNull InlineLinkNode link) {
    return link.getUrl().toString();
  }

  @Override
  public void setReferenceText(@NonNull InlineLinkNode link, @NonNull String newValue) {
    link.setUrl(BasedSequence.of(newValue));
  }

  @Override
  protected void handleUnselected(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull InlineLinkNode link,
      @NonNull IEntityItem item,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    URI linkHref = ObjectUtils.notNull(URI.create(link.getUrl().toString()));
    URI sourceUri = item.getSource();

    URI resolved = visitorContext.getUriResolver().resolve(linkHref, sourceUri);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.atTrace().log("At path '{}', remapping orphaned URI '{}' to '{}'",
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
          linkHref.toString(),
          resolved.toString());
    }
    link.setUrl(CharSubSequence.of(resolved.toString()));
  }

  @Override
  protected boolean handleIndexMiss(
      @NonNull IModelNodeItem<?, ?> contextItem,
      @NonNull InlineLinkNode reference,
      @NonNull List<IEntityItem.ItemType> itemTypes,
      @NonNull String identifier,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.atError().log(
          "The anchor at '{}' should reference a {} identified by '{}', but the identifier was not found in the index.",
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER),
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
      @NonNull InlineLinkNode reference,
      @NonNull ReferenceCountingVisitor.Context visitorContext) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.atDebug().log("Ignoring URI '{}' at '{}'",
          reference.getUrl().toStringOrNull(),
          contextItem.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
    }

    return true;
  }
}
