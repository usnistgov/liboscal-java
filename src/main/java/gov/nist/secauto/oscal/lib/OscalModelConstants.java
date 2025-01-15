/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.lib;

import gov.nist.secauto.metaschema.core.qname.IEnhancedQName;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class OscalModelConstants {

  @NonNull
  public static final String NS_OSCAL = "http://csrc.nist.gov/ns/oscal/1.0";
  @NonNull
  public static final IEnhancedQName QNAME_METADATA = IEnhancedQName.of(NS_OSCAL, "metadata");
  @NonNull
  public static final IEnhancedQName QNAME_BACK_MATTER = IEnhancedQName.of(NS_OSCAL, "back-matter");
  @NonNull
  public static final IEnhancedQName QNAME_PROFILE = IEnhancedQName.of(NS_OSCAL, "profile");
  @NonNull
  public static final IEnhancedQName QNAME_IMPORT = IEnhancedQName.of(NS_OSCAL, "import");
  @NonNull
  public static final IEnhancedQName QNAME_TITLE = IEnhancedQName.of(NS_OSCAL, "title");
  @NonNull
  public static final IEnhancedQName QNAME_PROP = IEnhancedQName.of(NS_OSCAL, "prop");
  @NonNull
  public static final IEnhancedQName QNAME_LINK = IEnhancedQName.of(NS_OSCAL, "link");
  @NonNull
  public static final IEnhancedQName QNAME_CITATION = IEnhancedQName.of(NS_OSCAL, "citation");
  @NonNull
  public static final IEnhancedQName QNAME_TEXT = IEnhancedQName.of(NS_OSCAL, "text");
  @NonNull
  public static final IEnhancedQName QNAME_PROSE = IEnhancedQName.of(NS_OSCAL, "prose");
  @NonNull
  public static final IEnhancedQName QNAME_PARAM = IEnhancedQName.of(NS_OSCAL, "param");
  @NonNull
  public static final IEnhancedQName QNAME_ROLE = IEnhancedQName.of(NS_OSCAL, "role");
  @NonNull
  public static final IEnhancedQName QNAME_LOCATION = IEnhancedQName.of(NS_OSCAL, "location");
  @NonNull
  public static final IEnhancedQName QNAME_PARTY = IEnhancedQName.of(NS_OSCAL, "party");
  @NonNull
  public static final IEnhancedQName QNAME_GROUP = IEnhancedQName.of(NS_OSCAL, "group");
  @NonNull
  public static final IEnhancedQName QNAME_CONTROL = IEnhancedQName.of(NS_OSCAL, "control");

  private OscalModelConstants() {
    // disable construction
  }
}
