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

package gov.nist.secauto.oscal.lib;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.net.URI;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class OscalModelConstants {

  @NonNull
  public static final String NS_OSCAL = "http://csrc.nist.gov/ns/oscal/1.0";
  @NonNull
  public static final URI NS_URI_OSCAL = ObjectUtils.notNull(URI.create(NS_OSCAL));
  @NonNull
  public static final QName QNAME_METADATA = new QName(NS_OSCAL, "metadata");
  @NonNull
  public static final QName QNAME_BACK_MATTER = new QName(NS_OSCAL, "back-matter");
  @NonNull
  public static final QName QNAME_PROFILE = new QName(NS_OSCAL, "profile");
  @NonNull
  public static final QName QNAME_IMPORT = new QName(NS_OSCAL, "import");
  @NonNull
  public static final QName QNAME_TITLE = new QName(NS_OSCAL, "title");
  @NonNull
  public static final QName QNAME_PROP = new QName(NS_OSCAL, "prop");
  @NonNull
  public static final QName QNAME_LINK = new QName(NS_OSCAL, "link");
  @NonNull
  public static final QName QNAME_CITATION = new QName(NS_OSCAL, "citation");
  @NonNull
  public static final QName QNAME_TEXT = new QName(NS_OSCAL, "text");
  @NonNull
  public static final QName QNAME_PROSE = new QName(NS_OSCAL, "prose");
  @NonNull
  public static final QName QNAME_PARAM = new QName(NS_OSCAL, "param");
  @NonNull
  public static final QName QNAME_ROLE = new QName(NS_OSCAL, "role");
  @NonNull
  public static final QName QNAME_LOCATION = new QName(NS_OSCAL, "location");
  @NonNull
  public static final QName QNAME_PARTY = new QName(NS_OSCAL, "party");
  @NonNull
  public static final QName QNAME_GROUP = new QName(NS_OSCAL, "group");
  @NonNull
  public static final QName QNAME_CONTROL = new QName(NS_OSCAL, "control");

  private OscalModelConstants() {
    // disable construction
  }
}
