import java.io.*;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import org.w3c.dom.Node;

/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */
public class Client {
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    private static XPathFactory xpathFactory = XPathFactory.newInstance();
    
    private static URI serverURI;

    public static void main(String... args) throws Exception {
        serverURI = URI.create("http://" + args[0] + ":" + args[1]);
        System.out.println(add() == 0);
        System.out.println(add(1, 2, 3, 4, 5) == 15);
        System.out.println(add(2, 4) == 6);
        System.out.println(subtract(12, 6) == 6);
        System.out.println(multiply(3, 4) == 12);
        System.out.println(multiply(1, 2, 3, 4, 5) == 120);
        System.out.println(divide(10, 5) == 2);
        System.out.println(modulo(10, 5) == 0);

        try {
            System.out.println("Testing Add Max Ints");
            add(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } catch (Exception e) {
            System.out.println("Add Max Ints Error Caught: " + e.getMessage());
        }

        try {
            System.out.println("Testing Multiply Max Ints");
            multiply(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } catch (Exception e) {
            System.out.println("Multiply Max Ints Error Caught: " + e.getMessage());
        }

        try {
            System.out.println("Testing Subtract Strings");
            subtract("grddr", "drgdrg");
        } catch (Exception e) {
            System.out.println("Subtract Strings Error Caught: " + e.getMessage());
        }

        try {
            System.out.println("Testing Divide by Zero...");
            divide(1, 0);
        } catch (Exception e) {
            System.out.println("Divide by Zero Caught: " + e.getMessage());
        }
    }


    public static int add(int lhs, int rhs) throws Exception {
        return callServer("add", lhs, rhs);
    }
    public static int add(Integer... params) throws Exception {
        return callServer("add", (Object[]) params);
    }
    public static int subtract(int lhs, int rhs) throws Exception {
        return callServer("subtract", lhs, rhs);
    }
    public static int subtract(String lhs, String rhs) throws Exception {
        return callServer("subtract", lhs, rhs);
    }
    public static int multiply(int lhs, int rhs) throws Exception {
        return callServer("multiply", lhs, rhs);
    }
    public static int multiply(Integer... params) throws Exception {
        return callServer("multiply", (Object[]) params);
    }
    public static int divide(int lhs, int rhs) throws Exception {
        return callServer("divide", lhs, rhs);
    }
    public static int modulo(int lhs, int rhs) throws Exception {
        return callServer("modulo", lhs, rhs);
    }

    private static int callServer(String methodName, Object... params) throws Exception {
        String xmlRequest = createXmlRequest(methodName, params);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(serverURI.resolve("/RPC"))
                .header("User-Agent", "Ted's Terrifying Thinkers")
                .header("Content-Type", "text/xml")
                .POST(HttpRequest.BodyPublishers.ofString(xmlRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String xmlResponse = response.body();

        return handleXMLResponse(xmlResponse);
    }

    private static String createXmlRequest(String methodName, Object... params) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<methodCall>\n");
        sb.append("<methodName>").append(methodName).append("</methodName>\n");
        sb.append("<params>\n");
        for (Object param : params) {
            sb.append("<param><value><i4>").append(param).append("</i4></value></param>\n");
        }
        sb.append("</params>\n");
        sb.append("</methodCall>\n");
        return sb.toString();
    }

    private static int handleXMLResponse(String xmlResponse) throws Exception {
        Document doc = dbf.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlResponse)));
        Node faultNode = doc.getElementsByTagName("fault").item(0);
        XPath xpath = xpathFactory.newXPath();
        if (faultNode != null) {
            XPathExpression expr = xpath.compile("/methodResponse/fault/value/struct/member[name='faultString']/value/string/text()");
            throw new Exception((String) expr.evaluate(doc, XPathConstants.STRING));
        } else {
            XPathExpression expr = xpath.compile("/methodResponse/params/param/value/i4/text()");
            return Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING));
        }
    }

}
