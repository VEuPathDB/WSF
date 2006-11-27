/**
 * 
 */
package org.gusdb.wsf.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Jerric
 * @created Jan 10, 2006
 */
public class WsfDeployHelper {

    private static Logger logger = Logger.getLogger(WsfDeployHelper.class);

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // check arguments
        if (args.length == 0) {
            System.err.println("Usage: java WSFDeployHelper command [options...]");
            System.exit(-1);
        }

        String command = args[0];
        if (command.equalsIgnoreCase("ChangeClass")) {
            // change the service class defined in wsdd file
            if (args.length < 3) {
                System.err.println("Usage: java WSFDeployHelper ChangeClass "
                        + "<deploy.wsdd> <newClassName>");
                System.exit(-1);
            }
            try {
                changeClass(args[1], args[2]);
            } catch (Exception ex) {
                logger.error(ex);
                throw ex;
            }
        }
    }

    public static void changeClass(String wsddFile, String newClass)
            throws SAXException, IOException, ParserConfigurationException,
            TransformerException {
        File file = new File(wsddFile);
        // read the wsdd file into a document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        logger.info("Parsing configuration file: " + file.getAbsolutePath());
        Document document = factory.newDocumentBuilder().parse(file);

        // get parameters and replace to a new value
        NodeList nodeList = document.getElementsByTagName("parameter");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = node.getAttributes();
                Node name = attributes.getNamedItem("name");
                if (name == null) continue;
                if (name.getNodeValue().equalsIgnoreCase("className")) {
                    Node value = attributes.getNamedItem("value");
                    value.setNodeValue(newClass);
                    break;
                }
            }
        }
        // write the xml file back
        Source source = new DOMSource(document);
        Result result = new StreamResult(wsddFile);
        Transformer transform = TransformerFactory.newInstance().newTransformer();
        transform.transform(source, result);
    }
}
