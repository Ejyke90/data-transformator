package org.translator.service;

import com.prowidesoftware.swift.model.mx.dic.Pacs00800101;
import com.prowidesoftware.swift.model.mx.dic.Pacs00900101;
import org.translator.mapper.Pacs008ToPacs009Mapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class TransformHttpServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignore) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/transform-payment", new TransformHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("Starting TransformHttpServer on port " + port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down TransformHttpServer");
            server.stop(1);
        }));
    }

    static class TransformHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                // Read request body
                InputStream is = exchange.getRequestBody();

                // Unmarshal incoming pacs.008
                JAXBContext jaxbCtx = JAXBContext.newInstance(Pacs00800101.class);
                Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
                Object unmarshalled = unmarshaller.unmarshal(is);

                Pacs00800101 src;
                if (unmarshalled instanceof JAXBElement) {
                    JAXBElement<?> el = (JAXBElement<?>) unmarshalled;
                    src = (Pacs00800101) el.getValue();
                } else {
                    src = (Pacs00800101) unmarshalled;
                }

                // Map to pacs.009 using existing mapper
                Pacs00900101 mapped = Pacs008ToPacs009Mapper.INSTANCE.mapProwide(src);

                // Marshal response
                JAXBContext outCtx = JAXBContext.newInstance(Pacs00900101.class);
                Marshaller marshaller = outCtx.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                QName rootName = new QName("urn:iso:std:iso:20022:tech:xsd:pacs.009.001.01", "Document");
                JAXBElement<Pacs00900101> root = new JAXBElement<>(rootName, Pacs00900101.class, mapped);

                byte[] responseBytes;
                try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                    marshaller.marshal(root, baos);
                    responseBytes = baos.toByteArray();
                }

                exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=utf-8");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

            } catch (jakarta.xml.bind.JAXBException jb) {
                try {
                    String msg = "Invalid XML: " + jb.getMessage();
                    byte[] mb = msg.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                    exchange.sendResponseHeaders(400, mb.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(mb); }
                } catch (Exception ignore) {}
            } catch (Exception e) {
                try {
                    String msg = "Internal server error: " + e.getMessage();
                    byte[] mb = msg.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                    exchange.sendResponseHeaders(500, mb.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(mb); }
                } catch (Exception ignore) {}
            }
        }
    }
}
