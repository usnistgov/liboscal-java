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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.databind.io.DefaultBoundLoader;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.profile.resolver.selection.ImportCycleException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.xmlunit.assertj3.XmlAssert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import edu.umd.cs.findbugs.annotations.NonNull;

class ProfileResolutionTests {
  private static final String XSLT_PATH = "src/test/resources/profile-test-helper.xsl";
  private static final String PROFILE_UNIT_TEST_PATH
      = "oscal/src/specifications/profile-resolution/profile-resolution-examples";
  private static final String JUNIT_TEST_PATH = "src/test/resources";
  private static final String PROFILE_EXPECTED_PATH = PROFILE_UNIT_TEST_PATH + "/output-expected";

  private static ProfileResolver profileResolver;
  private static Processor processor;
  private static XsltExecutable compairisonXslt;

  @BeforeAll
  static void setup() throws SaxonApiException {
    DynamicContext context = new DynamicContext(OscalBindingContext.OSCAL_STATIC_METAPATH_CONTEXT);
    context.setDocumentLoader(new DefaultBoundLoader(OscalBindingContext.instance()));
    profileResolver = new ProfileResolver();
    profileResolver.setDynamicContext(context);

    processor = new Processor(false);
    XsltCompiler comp = processor.newXsltCompiler();
    compairisonXslt = comp.compile(new StreamSource(new File(XSLT_PATH)));
  }

  public static Processor getProcessor() {
    return processor;
  }

  public static XsltExecutable getCompairisonXslt() {
    return compairisonXslt;
  }

  public static ProfileResolver getProfileResolver() {
    return profileResolver;
  }

  private static Catalog resolveProfile(@NonNull Path profileFile)
      throws FileNotFoundException, IOException, ProfileResolutionException {
    return (Catalog) INodeItem.toValue(getProfileResolver().resolve(profileFile));
  }

  private static Catalog resolveProfile(@NonNull File profileFile)
      throws FileNotFoundException, IOException, ProfileResolutionException {
    return (Catalog) INodeItem.toValue(getProfileResolver().resolve(profileFile));
  }

  private static Catalog resolveProfile(@NonNull URL profileUrl)
      throws IOException, ProfileResolutionException, URISyntaxException {
    return (Catalog) INodeItem.toValue(getProfileResolver().resolve(profileUrl));
  }

  /**
   * Transform the source to normalize content for test comparison.
   *
   * @param source
   *          the source to normalize
   * @return the transformed content
   * @throws SaxonApiException
   *           if an error occurs while performing the transformation
   */
  private static String transformXml(Source source) throws SaxonApiException {
    net.sf.saxon.s9api.Serializer out = getProcessor().newSerializer();
    out.setOutputProperty(net.sf.saxon.s9api.Serializer.Property.METHOD, "xml");
    // out.setOutputProperty(net.sf.saxon.s9api.Serializer.Property.INDENT, "yes");
    StringWriter writer = new StringWriter();
    out.setOutputWriter(writer);
    XsltTransformer trans = compairisonXslt.load();
    trans.setSource(source);
    trans.setDestination(out);
    trans.transform();

    return writer.toString();
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/profile-tests.csv", numLinesToSkip = 1)
  void test(String profileName) throws IOException, SaxonApiException {
    performTest(profileName);
  }

  @Test
  void testSingle() throws IOException, SaxonApiException {
    performTest("modify-adds");
  }

  void performTest(String profileName) throws IOException, SaxonApiException {
    String profileLocation = String.format("%s/%s_profile.xml", PROFILE_UNIT_TEST_PATH, profileName);

    File profileFile = new File(profileLocation);

    Catalog catalog = null;
    try {
      catalog = resolveProfile(profileFile);
    } catch (ProfileResolutionException ex) {
      fail(String.format("Resolution of profile '%s' failed. %s", profileFile.getAbsolutePath(),
          ex.getLocalizedMessage()));
    }
    assert catalog != null;

    Assertions.assertThat(catalog.getUuid()).isNotNull();
    Assertions.assertThat(catalog.getMetadata()).isNotNull();
    Assertions.assertThat(catalog.getMetadata().getLastModified()).matches(a -> ZoneOffset.UTC.equals(a.getOffset()));
    Assertions.assertThat(catalog.getMetadata().getLinks()).filteredOn("rel", "source-profile").extracting("href")
        .hasSize(1);
    Assertions.assertThat(catalog.getMetadata().getProps()).filteredOn("name", "resolution-tool").extracting("value")
        .hasSize(1);

    ISerializer<Catalog> serializer = OscalBindingContext.instance().newSerializer(Format.XML, Catalog.class);
    StringWriter writer = new StringWriter();
    serializer.serialize(catalog, writer);

    // OscalBindingContext.instance().newSerializer(Format.YAML,
    // Catalog.class).serialize(catalog,
    // System.out);

    // System.out.println("Pre scrub: " + writer.getBuffer().toString());

    String actual = transformXml(new StreamSource(new StringReader(writer.getBuffer().toString())));
    // System.out.println("Post scrub: "+actual);

    String expectedPath = String.format("%s/%s_profile_RESOLVED.xml", PROFILE_EXPECTED_PATH, profileName);
    String expected = transformXml(new StreamSource(new File(expectedPath)));

    XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().ignoreElementContentWhitespace().areIdentical();
  }

  @Test
  void testBrokenLink() {
    String profileLocation = String.format("%s/broken_profile.xml", PROFILE_UNIT_TEST_PATH);

    File profileFile = new File(profileLocation);

    assertThrows(FileNotFoundException.class, () -> {
      resolveProfile(profileFile);
    });
  }

  @Test
  void testCircularLink() {
    String profileLocation = String.format("%s/circular_profile.xml", PROFILE_UNIT_TEST_PATH);

    File profileFile = new File(profileLocation);

    IOException exceptionThrown = assertThrows(IOException.class, () -> {
      resolveProfile(profileFile);
    });

    MatcherAssert.assertThat(exceptionThrown.getCause(), CoreMatchers.instanceOf(ImportCycleException.class));
  }

  @Test
  void testOscalVersion() throws IOException, ProfileResolutionException {
    Path profileFile = Paths.get(JUNIT_TEST_PATH, "content/test-oscal-version-profile.xml");
    assert profileFile != null;
    Catalog catalog = resolveProfile(profileFile);
    assertNotNull(catalog);
    assertEquals("1.0.4", catalog.getMetadata().getOscalVersion());
  }

  @Test
  void testImportResourceRelativeLink() throws IOException, ProfileResolutionException {
    Path profilePath = Paths.get(JUNIT_TEST_PATH, "content/profile-relative-links-resource.xml");
    assert profilePath != null;
    Catalog resolvedCatalog = resolveProfile(profilePath);
    assertNotNull(resolvedCatalog);
  }

  @Test
  @Disabled
  void testRemove() throws IOException, ProfileResolutionException, URISyntaxException {
    URL url = new URL(
        "https://raw.githubusercontent.com/GSA/fedramp-automation/2229f10cc0b143410522026b793f4947eebb0872/dist/content/baselines/rev4/xml/FedRAMP_rev4_LI-SaaS-baseline_profile.xml");

    Catalog resolvedCatalog = resolveProfile(url);
    assertNotNull(resolvedCatalog);
  }

  @Test
  void testArsModerateProfile() throws IOException, ProfileResolutionException, URISyntaxException {
    URL url = new URL(
        "https://raw.githubusercontent.com/CMSgov/ars-machine-readable/4850049d550772672be855c5551a727aa57eb1bd/ODP-extract/ars-5.0-moderate-profile.xml");

    Catalog resolvedCatalog = resolveProfile(url);

    assertNotNull(resolvedCatalog);
  }
}
