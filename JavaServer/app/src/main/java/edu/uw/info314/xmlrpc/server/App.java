package edu.uw.info314.xmlrpc.server;

import java.util.*;
import java.util.logging.*;

import static spark.Spark.*;

class Call {
    public String name;
    public List<Integer> args = new ArrayList<Integer>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());

    public static void main(String[] args) {
        LOG.info("Starting up on port 8080");
        port(8080);

        post("/*", (request, response) -> {
            response.header("Host", "localhost:" + port());
            response.type("text/xml");
            if (!request.pathInfo().equals("/RPC")) {
                response.status(405);
                return createErrorXMLResponse("Only /RPC request is supported", 3);
            }
            response.status(200);
            String xmlRequest = request.body();
            String xmlResponse = handleXMLRequest(xmlRequest);
            return xmlResponse;
        });
        get("/*", (request, response) -> {
            response.status(405);
            response.type("text/xml");
            response.header("Host", "localhost:" + port());
            return createErrorXMLResponse("Method Not Supported", 3);
        });

        put("/*", (request, response) -> {
            response.status(405);
            response.type("text/xml");
            response.header("Host", "localhost:" + port());
            return createErrorXMLResponse("Method Not Supported", 3);
        });

        delete("/*", (request, response) -> {
            response.status(405);
            response.type("text/xml");
            response.header("Host", "localhost:" + port());
            return createErrorXMLResponse("Method Not Supported", 3);
        });

        head("/*", (request, response) -> {
            response.status(405);
            response.type("text/xml");
            response.header("Host", "localhost:" + port());
            return createErrorXMLResponse("Method Not Supported", 3);
        });

        trace("/RPC", (request, response) -> {
            response.status(405);
            response.type("text/xml");
            response.header("Host", "localhost:" + port());
            return createErrorXMLResponse("Method Not Supported", 3);
        });

        connect("/*", (request, response) -> {
            response.status(405);
            response.type("text/xml");
            response.header("Host", "localhost:" + port());
            return createErrorXMLResponse("Method Not Supported", 3);
        });

        options("/*", (request, response) -> {
            response.status(405);
            response.type("text/xml");
            response.header("Host", "localhost:" + port());
            return createErrorXMLResponse("Method Not Supported", 3);
        });
    }

    private static String handleXMLRequest(String XMLRequest) {
        try {
            Call request = extractXMLRPCCall(XMLRequest);
            int result = executeCall(request);
            String xmlResponse = createXMLResponse(result);
            return xmlResponse;
        } catch (Exception ex) {
            return createErrorXMLResponse(ex.getMessage(), 3);
        }
    }

    public static Call extractXMLRPCCall(String XMLRequest) {
        String[] lines = XMLRequest.split("\\n");
        Call request = new Call();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("<methodName>")) {
                request.name = line.substring(line.indexOf("<methodName>") + "<methodName>".length(), line.indexOf("</methodName>"));
            } else if (line.contains("<i4>")) {
                String paramValue = line.substring(line.indexOf("<i4>") + "<i4>".length(), line.indexOf("</i4>"));
                request.args.add(Integer.parseInt(paramValue));
            } else if (line.contains("<value>")) {
                throw new IllegalArgumentException("illegal argument type");
            }
        }
        return request;
    }

    private static int executeCall(Call request) throws NoSuchMethodException {
        Calc c = new Calc();
        int result;
        if (request.name.equals("add")) {
            long totalResult = 0;
            for (int i = 0; i < request.args.size(); i++) {
                totalResult += request.args.get(i);
            }
            if (totalResult > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Add result caused integer overflow");
            }
            int[] argArray = convertArgsToIntArray(request.args);
            result = c.add(argArray);
        } else if (request.name.equals("subtract")) {
            result = c.subtract(request.args.get(0), request.args.get(1));
        } else if (request.name.equals("multiply")) {
            long totalResult = 1;
            for (int i = 0; i < request.args.size(); i++) {
                totalResult *= request.args.get(i);
            }
            if (totalResult > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Multiply result caused integer overflow");
            } 
            int[] argArray = convertArgsToIntArray(request.args);
            result = c.multiply(argArray);
        } else if (request.name.equals("divide")) {
            result = c.divide(request.args.get(0), request.args.get(1));
        }  else if (request.name.equals("modulo")) {
            result = c.modulo(request.args.get(0), request.args.get(1));
        } else {
            throw new NoSuchMethodException(request.name);
        }
        return result;
    }
    

    private static int[] convertArgsToIntArray(List<Integer> args) {
        int[] argArray = new int[args.size()];
        for (int i = 0; i < args.size(); i++) {
            argArray[i] = args.get(i);
        }
        return argArray;
    }

    private static String createXMLResponse(int result) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<methodResponse>\n");
        sb.append("<params>\n");
        sb.append("<param><value><i4>").append(result).append("</i4></value></param>\n");
        sb.append("</params>\n");
        sb.append("</methodResponse>\n");
        return sb.toString();
    }

    private static String createErrorXMLResponse(String ex, int faultCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<methodResponse>\n");
        sb.append("<fault>\n");
        sb.append("<value>\n<struct>\n");
        sb.append("<member>\n<name>faultCode</name>\n<value><i4>").append(faultCode).append("</i4></value>\n</member>\n");
        sb.append("<member>\n<name>faultString</name>\n<value><string>").append(ex).append("</string></value>\n</member>\n");
        sb.append("</struct>\n</value>\n");
        sb.append("</fault>\n");
        sb.append("</methodResponse>\n");
        return sb.toString();
    }

}
