/* Copyright 2009, 2021 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */
package com.predic8.membrane.annot.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

import com.predic8.membrane.annot.AnnotUtils;
import com.predic8.membrane.annot.model.AttributeInfo;
import com.predic8.membrane.annot.model.ChildElementDeclarationInfo;
import com.predic8.membrane.annot.model.ChildElementInfo;
import com.predic8.membrane.annot.model.ElementInfo;
import com.predic8.membrane.annot.model.MainInfo;
import com.predic8.membrane.annot.model.Model;

public class Parsers {

	private final ProcessingEnvironment processingEnv;

	public Parsers(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	public void writeParserDefinitior(Model m) throws IOException {

		for (MainInfo main : m.getMains()) {
			List<Element> sources = new ArrayList<Element>();
			sources.addAll(main.getInterceptorElements());
			sources.add(main.getElement());

			try {
				FileObject o = processingEnv.getFiler().createSourceFile(
						main.getAnnotation().outputPackage() + ".NamespaceHandlerAutoGenerated",
						sources.toArray(new Element[0]));
				BufferedWriter bw = new BufferedWriter(o.openWriter());
				try {
					bw.write("/* Copyright 2012,2013 predic8 GmbH, www.predic8.com\r\n" +
							"\r\n" +
							"   Licensed under the Apache License, Version 2.0 (the \"License\");\r\n" +
							"   you may not use this file except in compliance with the License.\r\n" +
							"   You may obtain a copy of the License at\r\n" +
							"\r\n" +
							"   http://www.apache.org/licenses/LICENSE-2.0\r\n" +
							"\r\n" +
							"   Unless required by applicable law or agreed to in writing, software\r\n" +
							"   distributed under the License is distributed on an \"AS IS\" BASIS,\r\n" +
							"   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\r\n" +
							"   See the License for the specific language governing permissions and\r\n" +
							"   limitations under the License. */\r\n" +
							"\r\n" +
							"package " + main.getAnnotation().outputPackage() + ";\r\n" +
							"\r\n" +
							"/**\r\n" +
							"  * Automatically generated by " + Parsers.class.getName() + ".\r\n" +
							"  */\r\n" +
							"public class NamespaceHandlerAutoGenerated {\r\n" +
							"\r\n" +
							"	public static void registerBeanDefinitionParsers(NamespaceHandler nh) {\r\n");
					for (ElementInfo i : main.getIis()) {
						if (i.getAnnotation().topLevel()) {
							bw.write("		nh.registerGlobalBeanDefinitionParser(\"" + i.getAnnotation().name() + "\", new " + i.getParserClassSimpleName() + "());\r\n");
						} else {
							for (ChildElementDeclarationInfo cedi : i.getUsedBy()) {
								for (ChildElementInfo cei : cedi.getUsedBy()) {
									TypeElement element = cei.getEi().getElement();
									String clazz = AnnotUtils.getRuntimeClassName(element);
									bw.write("		nh.registerLocalBeanDefinitionParser(\"" + clazz + "\", \"" + i.getAnnotation().name() + "\", new " + i.getParserClassSimpleName() + "());\r\n");
								}
							}
						}
					}
					bw.write(
							"	}\r\n" +
									"}\r\n" +
							"");
				} finally {
					bw.close();
				}
			} catch (FilerException e) {
				if (e.getMessage().contains("Source file already created"))
					return;
				throw e;
			}
		}
	}

	public void writeParsers(Model m) throws IOException {
		for (MainInfo main : m.getMains()) {
			for (ElementInfo ii : main.getIis()) {
				List<Element> sources = new ArrayList<Element>();
				sources.add(main.getElement());
				sources.add(ii.getElement());

				String interceptorClassName = ii.getElement().getQualifiedName().toString();

				try {
					FileObject o = processingEnv.getFiler().createSourceFile(main.getAnnotation().outputPackage() + "." + ii.getParserClassSimpleName(),
							sources.toArray(new Element[0]));
					BufferedWriter bw = new BufferedWriter(o.openWriter());
					try {
						bw.write("/* Copyright 2012 predic8 GmbH, www.predic8.com\r\n" +
								"\r\n" +
								"   Licensed under the Apache License, Version 2.0 (the \"License\");\r\n" +
								"   you may not use this file except in compliance with the License.\r\n" +
								"   You may obtain a copy of the License at\r\n" +
								"\r\n" +
								"   http://www.apache.org/licenses/LICENSE-2.0\r\n" +
								"\r\n" +
								"   Unless required by applicable law or agreed to in writing, software\r\n" +
								"   distributed under the License is distributed on an \"AS IS\" BASIS,\r\n" +
								"   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\r\n" +
								"   See the License for the specific language governing permissions and\r\n" +
								"   limitations under the License. */\r\n" +
								"\r\n" +
								"package " + main.getAnnotation().outputPackage() + ";\r\n" +
								"\r\n" +
								"import org.w3c.dom.Element;\r\n" +
								"import org.springframework.beans.factory.xml.ParserContext;\r\n" +
								"import org.springframework.beans.factory.support.BeanDefinitionBuilder;\r\n");
						if (!main.getAnnotation().outputPackage().equals("com.predic8.membrane.core.config.spring"))
							bw.write("import com.predic8.membrane.core.config.spring.*;\r\n");
						bw.write(
								"\r\n" +
										"/**\r\n" +
										"  * Automatically generated by " + Parsers.class.getName() + ".\r\n" +
										"  */\r\n" +
										"public class " + ii.getParserClassSimpleName() + " extends AbstractParser {\r\n" +
										"\r\n" +
										"	protected Class<?> getBeanClass(org.w3c.dom.Element element) {\r\n" +
										"		return " + interceptorClassName + ".class;\r\n" +
								"	}\r\n");
						bw.write("	@Override\r\n" +
								"	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {\r\n");
						if (ii.isHasIdField())
							bw.write("		setPropertyIfSet(\"id\", element, builder);\r\n");
						bw.write(
								"		setIdIfNeeded(element, parserContext, \"" + ii.getAnnotation().name() + "\");\r\n");
						for (AttributeInfo ai : ii.getAis()) {
							if (ai.getXMLName().equals("id"))
								continue;
							if (ai.isBeanReference(processingEnv.getTypeUtils())) {
								if (!ai.isRequired())
									bw.write("		if (element.hasAttribute(\"" + ai.getXMLName() + "\"))\r\n");
								bw.write("		builder.addPropertyReference(\"" + ai.getSpringName() + "\", element.getAttribute(\"" + ai.getXMLName() + "\"));\r\n");
							} else {
								bw.write("		setProperty" + (ai.isRequired() ? "" : "IfSet") + "(\"" + ai.getXMLName() + "\", \"" + ai.getSpringName() + "\", element, builder" + (ai.isEnum(processingEnv.getTypeUtils()) ? ", true" : "") + ");\r\n");
							}
							if (ai.getXMLName().equals("name"))
								bw.write("		element.removeAttribute(\"name\");\r\n");
						}
						if (ii.getOai() != null) {
							bw.write("		setProperties(\"" + ii.getOai().getSpringName() + "\", element, builder);\r\n");
						}
						for (ChildElementInfo cei : ii.getCeis())
							if (cei.isList())
								bw.write("		builder.addPropertyValue(\"" + cei.getPropertyName() + "\", new java.util.ArrayList<Object>());\r\n");
						if (ii.getTci() != null)
							bw.write("		builder.addPropertyValue(\"" + ii.getTci().getPropertyName() + "\", element.getTextContent());\r\n");
						else
							bw.write("		parseChildren(element, parserContext, builder);\r\n");
						for (ChildElementInfo cei : ii.getCeis())
							if (cei.isList() && cei.isRequired()) {
								bw.write("		if (builder.getBeanDefinition().getPropertyValues().getPropertyValue(\"" + cei.getPropertyName() + "[0]\") == null)\r\n");
								bw.write("			throw new RuntimeException(\"Property '" + cei.getPropertyName() + "' is required, but none was defined (empty list).\");\r\n");
							}

						bw.write(
								"	}\r\n" +
								"");

						bw.write(
								"@Override\r\n" +
								"protected void handleChildObject(Element ele, ParserContext parserContext, BeanDefinitionBuilder builder, Class<?> clazz, Object child) {\r\n");
						for (ChildElementInfo cei : ii.getCeis()) {
							bw.write(
									"	if (" + cei.getTypeDeclaration().getQualifiedName() + ".class.isAssignableFrom(clazz)) {\r\n" +
											"		setProperty(builder, \"" + cei.getPropertyName() + "\"" + (cei.isList() ? "+\"[\"+ incrementCounter(builder, \"" + cei.getPropertyName() + "\") + \"]\" " : "") + ", child);\r\n" +
									"	} else \r\n");
						}
						bw.write(
								"	{\r\n" +
										"		throw new RuntimeException(\"Unknown child class \\\"\" + clazz + \"\\\".\");\r\n" +
								"	}\r\n");
						bw.write(
								"}\r\n");

						bw.write(
								"}\r\n" +
								"");
					} finally {
						bw.close();
					}
				} catch (FilerException e) {
					if (e.getMessage().contains("Source file already created"))
						return;
					throw e;
				}

			}
		}

	}


}
