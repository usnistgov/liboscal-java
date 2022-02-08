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

package gov.nist.secauto.oscal.lib.profile.resolver.policy;

import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.oscal.lib.model.Link;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem;
import gov.nist.secauto.oscal.lib.profile.resolver.EntityItem.ItemType;
import gov.nist.secauto.oscal.lib.profile.resolver.Index;
import gov.nist.secauto.oscal.lib.profile.resolver.policy.IIdentifierParser.Match;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LinkReferencePolicy
    extends AbstractStaticItemTypesReferencePolicy<Link> {
  private static final Logger LOGGER = LogManager.getLogger(LinkReferencePolicy.class);
  @NotNull
  private static final IReferencePolicyHandler<Link> INDEX_MISS_HANDLER = new IndexMissHandler();
  @NotNull
  private static final IReferencePolicyHandler<Link> INDEX_HIT_UNSELECTED_HANDLER = new IndexHitUnselectedHandler();
  @NotNull
  private static final IReferencePolicyHandler<Link> INDEX_HIT_INCREMENT_HANDLER
      = IReferencePolicyHandler.incrementCountIndexHitPolicy();

  @SuppressWarnings("null")
  @NotNull
  public static LinkReferencePolicy create(@NotNull ItemType itemType) {
    return create(Set.of(itemType));
  }

  @NotNull
  public static LinkReferencePolicy create(Set<ItemType> itemTypes) {
    return new LinkReferencePolicy(
        CollectionUtil.requireNonEmpty(Objects.requireNonNull(itemTypes, "itemTypes"), "itemTypes"));
  }

  @SuppressWarnings("null")
  public LinkReferencePolicy(@NotNull Set<ItemType> itemTypes) {
    super(IIdentifierParser.FRAGMENT_PARSER,
        List.of(INDEX_MISS_HANDLER,
            INDEX_HIT_UNSELECTED_HANDLER,
            INDEX_HIT_INCREMENT_HANDLER),
        itemTypes);
  }

  @Override
  protected String getReference(@NotNull Link link) {
    return link.getHref().toString();
  }

  private static class IndexHitUnselectedHandler
      extends AbstractIndexHitUnselectedPolicyHandler<Link> {
    @Override
    protected boolean handleUnselected(EntityItem item, @NotNull Link link) {
      URI linkHref = link.getHref();
      URI sourceUri = item.getSource();

      URI resolved = sourceUri.resolve(linkHref);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.atTrace().log("remapping orphaned URI '{}' to '{}'", linkHref.toString(), resolved.toString());
      }
      link.setHref(resolved);
      return true;
    }
  }

  private static class IndexMissHandler
      extends AbstractIndexMissPolicyHandler<Link> {

    @Override
    public boolean handleIndexMiss(@NotNull Link link, @NotNull Set<ItemType> itemTypes,
        @NotNull Match match, @NotNull Index index) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.atWarn().log(
            "link with rel '{}' should reference a {} identified by '{}'. The index did not contain the identifier.",
            link.getRel(),
            itemTypes.stream().map(en -> en.name().toLowerCase())
                .collect(CustomCollectors.joiningWithOxfordComma("or")),
            match.getIdentifier());
      }
      return true;
    }

  }
}
