/* Copyright 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */
package com.predic8.membrane.core.interceptor.xmlcontentfilter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;

import com.predic8.membrane.annot.MCInterceptor;
import com.predic8.membrane.core.exchange.Exchange;
import com.predic8.membrane.core.http.Message;
import com.predic8.membrane.core.http.Response;
import com.predic8.membrane.core.interceptor.AbstractInterceptor;
import com.predic8.membrane.core.interceptor.Outcome;

@MCInterceptor(xsd="" +
		"	<xsd:element name=\"xmlContentFilter\">\r\n" + 
		"		<xsd:complexType>\r\n" + 
		"			<xsd:complexContent>\r\n" + 
		"				<xsd:extension base=\"beans:identifiedType\">\r\n" + 
		"					<xsd:sequence />\r\n" + 
		"					<xsd:attribute name=\"xPath\" type=\"xsd:string\" use=\"required\" />\r\n" + 
		"				</xsd:extension>\r\n" + 
		"			</xsd:complexContent>\r\n" + 
		"		</xsd:complexType>\r\n" + 
		"	</xsd:element>\r\n" + 
		"")
public class XMLContentFilterInterceptor extends AbstractInterceptor {
	
	private static final Logger LOG = Logger.getLogger(XMLContentFilterInterceptor.class);
	
	private String xPath;
	private XMLContentFilter xmlContentFilter;
	
	public XMLContentFilterInterceptor() {
		setFlow(Flow.REQUEST_RESPONSE);
	}
	
	public String getXPath() {
		return xPath;
	}
	
	public void setXPath(String xPath) {
		this.xPath = xPath;
		try {
			this.xmlContentFilter = new XMLContentFilter(xPath);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Outcome handleRequest(Exchange exc) throws Exception {
		return handleMessage(exc, exc.getRequest());
	}
	
	@Override
	public Outcome handleResponse(Exchange exc) throws Exception {
		return handleMessage(exc, exc.getResponse());
	}
	
	@Override
	protected void parseAttributes(XMLStreamReader token) throws Exception {
		setXPath(token.getAttributeValue("", "xPath"));
	}

	@Override
	protected void writeInterceptor(XMLStreamWriter out)
			throws XMLStreamException {
		out.writeStartElement("xmlContentFilter");
		out.writeAttribute("xPath", xPath);
		out.writeEndElement();
	}


	private Outcome handleMessage(Exchange exc, Message message) {
		try {
			xmlContentFilter.removeMatchingElements(message);
			return Outcome.CONTINUE;
		} catch (Exception e) {
			LOG.error("xmlContentFilter error", e);
			exc.setResponse(Response.interalServerError("xmlContentFilter error. See log for details.").build());
			return Outcome.ABORT;
		}
	}
	
	@Override
	public String getHelpId() {
		return "xml-content-filter";
	}

	
}
