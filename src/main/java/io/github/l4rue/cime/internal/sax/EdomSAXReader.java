package io.github.l4rue.cime.internal.sax;

import io.github.l4rue.cime.internal.document.DocumentException;
import io.github.l4rue.cime.internal.io.EfileUtils;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.net.URL;

/**
 * Reads E-language sources into a W3C DOM document using the custom XML reader.
 *
 * @author dingyh
 */
public class EdomSAXReader {

    private XMLReader xmlReader;
    private String encoding;

    /**
     * Creates a reader that lazily instantiates the default XML reader.
     */
    public EdomSAXReader() {
    }

    /**
     * Creates a reader backed by the supplied XML reader.
     *
     * @param xmlReader XML reader used to produce SAX events
     */
    public EdomSAXReader(XMLReader xmlReader) {
        this.xmlReader = xmlReader;
    }

    /**
     * Creates a reader backed by the supplied XML reader implementation class.
     *
     * @param xmlReaderClassName fully qualified XML reader class name
     * @throws SAXException when the XML reader cannot be created
     */
    public EdomSAXReader(String xmlReaderClassName) throws SAXException {
        if (xmlReaderClassName != null) {
            this.xmlReader = XMLReaderFactory
                    .createXMLReader(xmlReaderClassName);
        }
    }

    /**
     * <p>
     * Reads a Document from the given <code>File</code>
     * </p>
     *
     * @param file is the <code>File</code> to read from.
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(File file) throws DocumentException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            InputSource source = new InputSource(fileInputStream);
            if (this.encoding == null) {
                this.encoding = EfileUtils.getCharset(file);
            }

            if (this.encoding != null) {
                source.setEncoding(this.encoding);
            }
            String path = file.getAbsolutePath();

            if (path != null) {
                // Code taken from Ant FileUtils
                StringBuffer sb = new StringBuffer("file://");

                // add an extra slash for filesystems with drive-specifiers
                if (!path.startsWith(File.separator)) {
                    sb.append("/");
                }

                path = path.replace('\\', '/');
                sb.append(path);
                source.setSystemId(sb.toString());
            }

            return read(source);
        } catch (FileNotFoundException e) {
            throw new DocumentException(e.getMessage(), e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * <p>
     * Reads a Document from the given <code>URL</code> using SAX
     * </p>
     *
     * @param url <code>URL</code> to read from.
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(URL url) throws DocumentException {
        String systemID = url.toExternalForm();

        InputSource source = new InputSource(systemID);
        if (this.encoding != null) {
            source.setEncoding(this.encoding);
        }

        return read(source);
    }

    /**
     * <p>
     * Reads a Document from the given URL or filename using SAX.
     * </p>
     *
     * <p>
     * If the systemId contains a <code>':'</code> character then it is
     * assumed to be a URL otherwise its assumed to be a file name. If you want
     * finer grained control over this mechansim then please explicitly pass in
     * either a {@link URL}or a {@link File}instance instead of a {@link
     * String} to denote the source of the document.
     * </p>
     *
     * @param systemId is a URL for a document or a file name.
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(String systemId) throws DocumentException {
        InputSource source = new InputSource(systemId);
        if (this.encoding != null) {
            source.setEncoding(this.encoding);
        }

        return read(source);
    }

    /**
     * <p>
     * Reads a Document from the given stream using SAX
     * </p>
     *
     * @param in <code>InputStream</code> to read from.
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(InputStream in) throws DocumentException {
        InputSource source = new InputSource(in);
        if (this.encoding != null) {
            source.setEncoding(this.encoding);
        }

        return read(source);
    }

    /**
     * <p>
     * Reads a Document from the given <code>Reader</code> using SAX
     * </p>
     *
     * @param reader is the reader for the input
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(Reader reader) throws DocumentException {
        InputSource source = new InputSource(reader);
        if (this.encoding != null) {
            source.setEncoding(this.encoding);
        }

        return read(source);
    }

    /**
     * <p>
     * Reads a Document from the given stream using SAX
     * </p>
     *
     * @param in       <code>InputStream</code> to read from.
     * @param systemId is the URI for the input
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(InputStream in, String systemId)
            throws DocumentException {
        InputSource source = new InputSource(in);
        source.setSystemId(systemId);
        if (this.encoding != null) {
            source.setEncoding(this.encoding);
        }

        return read(source);
    }

    /**
     * <p>
     * Reads a Document from the given <code>Reader</code> using SAX
     * </p>
     *
     * @param reader   is the reader for the input
     * @param systemId is the URI for the input
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(Reader reader, String systemId)
            throws DocumentException {
        InputSource source = new InputSource(reader);
        source.setSystemId(systemId);
        if (this.encoding != null) {
            source.setEncoding(this.encoding);
        }

        return read(source);
    }

    /**
     * <p>
     * Reads a Document from the given <code>InputSource</code> using SAX
     * </p>
     *
     * @param in <code>InputSource</code> to read from.
     * @return the newly created Document instance
     * @throws DocumentException if an error occurs during parsing.
     */
    public Document read(InputSource in) throws DocumentException {
        try {
            XMLReader reader = getXMLReader();

            EdomSAXContentHandler contentHandler = createContentHandler();
            reader.setContentHandler(contentHandler);

            reader.parse(in);
            return contentHandler.getDocument();
        } catch (Exception e) {
            if (e instanceof SAXParseException) {
                // e.printStackTrace();
                SAXParseException parseException = (SAXParseException) e;
                String systemId = parseException.getSystemId();

                if (systemId == null) {
                    systemId = "";
                }

                String message = "Error on line "
                        + parseException.getLineNumber() + " of document "
                        + systemId + " : " + parseException.getMessage();

                throw new DocumentException(message, e);
            } else {
                throw new DocumentException(e.getMessage(), e);
            }
        }
    }

    private EdomSAXContentHandler createContentHandler() {
        return new EdomSAXContentHandler();
    }

    /**
     * DOCUMENT ME!
     *
     * @return the <code>XMLReader</code> used to parse SAX events
     * @throws SAXException DOCUMENT ME!
     */
    public XMLReader getXMLReader() throws SAXException {
        if (xmlReader == null) {
            xmlReader = createXMLReader();
        }

        return xmlReader;
    }

    /**
     * Sets the <code>XMLReader</code> used to parse SAX events
     *
     * @param reader is the <code>XMLReader</code> to parse SAX events
     */
    public void setXMLReader(XMLReader reader) {
        this.xmlReader = reader;
    }

    private XMLReader createXMLReader() throws SAXException {
        return new DdomSAXParsers().getXMLReader();
    }

    /**
     * Returns encoding used for InputSource (null means system default
     * encoding)
     *
     * @return encoding used for InputSource
     *
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets encoding used for InputSource (null means system default encoding)
     *
     * @param encoding is encoding used for InputSource
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the class name of the <code>XMLReader</code> to be used to parse
     * SAX events.
     *
     * @param xmlReaderClassName is the class name of the <code>XMLReader</code> to parse SAX
     *                           events
     * @throws SAXException DOCUMENT ME!
     */
    public void setXMLReaderClassName(String xmlReaderClassName)
            throws SAXException {
        setXMLReader(XMLReaderFactory.createXMLReader(xmlReaderClassName));
    }

    protected static class SAXEntityResolver implements EntityResolver,
            Serializable {
        protected String uriPrefix;

        public SAXEntityResolver(String uriPrefix) {
            this.uriPrefix = uriPrefix;
        }

        public InputSource resolveEntity(String publicId, String systemId) {
            // try create a relative URI reader...
            if ((systemId != null) && (systemId.length() > 0)) {
                if ((uriPrefix != null) && (systemId.indexOf(':') <= 0)) {
                    systemId = uriPrefix + systemId;
                }
            }

            return new InputSource(systemId);
        }
    }
}

 
