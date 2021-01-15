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

package gov.nist.secauto.oscal.java;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.format.DataFormatMatcher;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.Format;
import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.io.MutableConfiguration;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.oscal.java.objects.Catalog;
import gov.nist.secauto.oscal.java.objects.ComponentDefinition;
import gov.nist.secauto.oscal.java.objects.Profile;
import gov.nist.secauto.oscal.java.objects.SystemSecurityPlan;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class OscalLoader {
  private final BindingContext bindingContext;

  public OscalLoader() {
    this(BindingContext.newInstance());
  }

  public OscalLoader(BindingContext bindingContext) {
    this.bindingContext = bindingContext;
  }

  protected BindingContext getBindingContext() {
    return bindingContext;
  }

  private Class<?> detectModelXml(File file) throws BindingException {
    Class<?> retval = null;
    try {
      XMLInputFactory2 xmlInputFactory = (XMLInputFactory2) WstxInputFactory.newInstance();
      xmlInputFactory.configureForXmlConformance();
      xmlInputFactory.setProperty(XMLInputFactory2.IS_COALESCING, false);

      try (Reader reader = new FileReader(file, Charset.forName("UTF8"))) {
        XMLEventReader2 eventReader = (XMLEventReader2) xmlInputFactory.createXMLEventReader(reader);
        if (eventReader.peek().isStartDocument()) {
          while (eventReader.hasNext() && !eventReader.peek().isStartElement()) {
            eventReader.nextEvent();
          }
        }

        if (!eventReader.peek().isStartElement()) {
          throw new UnsupportedOperationException("Unable to detect a start element");
        }

        StartElement start = eventReader.nextEvent().asStartElement();
        QName qname = start.getName();

        if ("http://csrc.nist.gov/ns/oscal/1.0".equals(qname.getNamespaceURI())) {
          switch (qname.getLocalPart()) {
          case "catalog":
            retval = Catalog.class;
            break;
          case "profile":
            retval = Profile.class;
            break;
          case "system-security-plan":
            retval = SystemSecurityPlan.class;
            break;
          case "component-definition":
            retval = ComponentDefinition.class;
            break;
          default:
            throw new UnsupportedOperationException("Unrecognized element name: " + qname.toString());
          }
        }

        if (retval == null) {
          throw new UnsupportedOperationException("Unrecognized element name: " + qname.toString());
        }
        reader.close();
      }
    } catch (IOException | XMLStreamException ex) {
      throw new BindingException(ex);
    }
    return retval;
  }

  private Class<?> detectModelJson(JsonParser parser) throws BindingException {
    Class<?> retval = null;
    try {
      JsonUtil.consumeAndAssert(parser, JsonToken.START_OBJECT);
      outer: while (JsonToken.FIELD_NAME.equals(parser.nextToken())) {
        String name = parser.getCurrentName();
        switch (name) {
        case "catalog":
          retval = Catalog.class;
          break outer;
        case "profile":
          retval = Profile.class;
          break outer;
        case "system-security-plan":
          retval = SystemSecurityPlan.class;
          break outer;
        case "component-definition":
          retval = ComponentDefinition.class;
          break outer;
        case "$schema":
          JsonUtil.skipValue(parser);
          break;
        default:
          throw new UnsupportedOperationException("Unrecognized field name: " + name);
        }
      }
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
    return retval;
  }

  private Deserializer getDeserializer(Class<?> clazz, Format format, Configuration config)
      throws BindingException {
    Deserializer retval = getBindingContext().newDeserializer(format, clazz, config);
    return retval;
  }

  public <CLASS> CLASS load(Class<CLASS> clazz, File file) throws BindingException {
    try (InputStream is = new FileInputStream(file)) {

      DataFormatMatcher matcher = ContentUtil.detectFormat(is);
      switch (matcher.getMatchStrength()) {
      case FULL_MATCH:
      case SOLID_MATCH:
      case WEAK_MATCH:
        Class<?> modelClass;
        if ("XML".equals(matcher.getMatchedFormatName())) {
          is.close();
          modelClass = detectModelXml(file);
        } else {
          modelClass = detectModelJson(matcher.createParserWithMatch());
          is.close();
        }
        if (!clazz.isAssignableFrom(modelClass)) {
          throw new UnsupportedOperationException(String.format(
              "The detected model class '%s' is not assignable to '%s'", modelClass.getName(), clazz.getName()));
        }
        Format format = Format.valueOf(matcher.getMatchedFormatName());
        if (format == null) {
          is.close();
          throw new UnsupportedOperationException("Unsupported source format: " + matcher.getMatchedFormatName());
        }

        MutableConfiguration config = new MutableConfiguration().enableFeature(Feature.DESERIALIZE_ROOT);
        Deserializer deserializer = getDeserializer(clazz, format, config);
        CLASS retval = deserializer.deserialize(file);
        return retval;
      case INCONCLUSIVE:
      case NO_MATCH:
      default:
        is.close();
        throw new UnsupportedOperationException("Unable to identify format for file: " + file.getPath());
      }
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
  }

  public Catalog loadCatalog(File file) throws BindingException {
    return load(Catalog.class, file);
  }

  public Profile loadProfile(File file) throws BindingException {
    return load(Profile.class, file);
  }

  public SystemSecurityPlan loadSystemSecurityPlan(File file) throws BindingException {
    return load(SystemSecurityPlan.class, file);
  }

  public ComponentDefinition loadComponentDefinition(File file) throws BindingException {
    return load(ComponentDefinition.class, file);
  }
}
