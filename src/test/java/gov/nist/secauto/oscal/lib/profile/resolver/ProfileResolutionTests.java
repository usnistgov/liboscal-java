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

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.DefaultBoundLoader;
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Profile;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.xmlunit.assertj3.XmlAssert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.Stack;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

class ProfileResolutionTests {
  private static final String XSLT_PATH = "oscal/src/utils/util/resolver-pipeline/oscal-profile-test-helper.xsl";
  private static final String PROFILE_UNIT_TEST_PATH
      = "oscal/src/specifications/profile-resolution/profile-resolution-examples";
  private static final String JUNIT_TEST_PATH = "src/test/resources";
  private static final String PROFILE_EXPECTED_PATH = PROFILE_UNIT_TEST_PATH + "/output-expected";

  private static ProfileResolver profileResolver;
  private static Processor processor;
  private static XsltExecutable compairisonXslt;

  @BeforeAll
  static void setup() throws SaxonApiException {
    DynamicContext context = new StaticContext().newDynamicContext();
    context.setDocumentLoader(new DefaultBoundLoader(OscalBindingContext.instance()));
    profileResolver = new ProfileResolver(context);

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

  private Catalog resolveProfile(@NotNull Path profileFile)
      throws FileNotFoundException, BindingException, IOException {
    Profile profile = OscalBindingContext.instance().loadProfile(profileFile);
    ProfileResolver.ResolutionData data
        = new ProfileResolver.ResolutionData(profile, ObjectUtils.notNull(profileFile.toUri()), new Stack<>());
    getProfileResolver().resolve(data);
    return data.getCatalog();
  }

  private Catalog resolveProfile(@NotNull File profileFile)
      throws FileNotFoundException, BindingException, IOException {
    Profile profile = OscalBindingContext.instance().loadProfile(profileFile);
    ProfileResolver.ResolutionData data
        = new ProfileResolver.ResolutionData(profile, ObjectUtils.notNull(profileFile.toURI()), new Stack<>());
    getProfileResolver().resolve(data);
    return data.getCatalog();
  }

  private String transformXml(Source source) throws SaxonApiException {
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
  void test(String profileName) throws IllegalStateException, IOException, BindingException, SaxonApiException {
    String profileLocation = String.format("%s/%s_profile.xml", PROFILE_UNIT_TEST_PATH, profileName);

    File profileFile = new File(profileLocation);

    Catalog catalog = resolveProfile(profileFile);

    Assertions.assertThat(catalog.getUuid()).isNotNull();
    Assertions.assertThat(catalog.getMetadata()).isNotNull();
    Assertions.assertThat(catalog.getMetadata().getLastModified()).matches(a -> ZoneOffset.UTC.equals(a.getOffset()));
    Assertions.assertThat(catalog.getMetadata().getLinks()).filteredOn("rel", "source-profile").extracting("href")
        .hasSize(1);
    Assertions.assertThat(catalog.getMetadata().getProps()).filteredOn("name", "resolution-tool").extracting("value")
        .hasSize(1);

    ISerializer<@NotNull Catalog> serializer = OscalBindingContext.instance().newSerializer(Format.XML, Catalog.class);
    StringWriter writer = new StringWriter();
    serializer.serialize(catalog, writer);
    // serializer.serialize(catalog, System.out);

    // System.out.println("Pre scrub: " + writer.getBuffer().toString());

    String actual = transformXml(new StreamSource(new StringReader(writer.getBuffer().toString())));
    // System.out.println("Post scrub: "+actual);

    String expectedPath = String.format("%s/%s_profile_RESOLVED.xml", PROFILE_EXPECTED_PATH, profileName);
    String expected = transformXml(new StreamSource(new File(expectedPath)));

    XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().ignoreElementContentWhitespace().areIdentical();
  }

  @Test
  void testBrokenLink() throws IllegalStateException, IOException, BindingException {
    String profileLocation = String.format("%s/broken_profile.xml", PROFILE_UNIT_TEST_PATH);

    File profileFile = new File(profileLocation);

    assertThrows(FileNotFoundException.class, () -> {
      resolveProfile(profileFile);
    });
  }

  @Test
  void testCircularLink() throws IllegalStateException, IOException, BindingException {
    String profileLocation = String.format("%s/circular_profile.xml", PROFILE_UNIT_TEST_PATH);

    File profileFile = new File(profileLocation);

    @SuppressWarnings("null")
    IOException exceptionThrown = assertThrows(IOException.class, () -> {
      resolveProfile(profileFile);
    });

    MatcherAssert.assertThat(exceptionThrown.getCause(), CoreMatchers.instanceOf(ImportCycleException.class));
  }

  @Test
  void testOscalVersion() throws IllegalStateException, IOException, BindingException {
    Path profileFile = Path.of("src/test/resources/test-oscal-version-profile.xml");
    Catalog catalog = resolveProfile(profileFile);
    assertNotNull(catalog);
    assertEquals("1.0.4", catalog.getMetadata().getOscalVersion());
  }

  @Test
  void testImportResourceRelativeLink() throws IOException {
    Path profilePath = Paths.get(JUNIT_TEST_PATH, "content/profile-relative-links-resource.xml");
//    System.out.println(profilePath.normalize().toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
    Profile profile = OscalBindingContext.instance().loadProfile(profilePath);
    ProfileResolver.ResolutionData data
        = new ProfileResolver.ResolutionData(profile, ObjectUtils.notNull(profilePath.toUri()), new Stack<>());
    Catalog resolvedCatalog =  getProfileResolver().resolve(data);
    assertNotNull(resolvedCatalog);
  }
}
