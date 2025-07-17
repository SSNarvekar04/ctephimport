package com.rsv.rsvimport;

import org.dcm4che5.io.DicomInputStream;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SendUtility {
    private String icpTicket = null;

    public static void main(String[] args) throws IOException {
        Arguments argobj = Arguments.getInstance();
        argobj.parseArguments(args);
        SendUtility su = new SendUtility();
        su.sendFile(System.currentTimeMillis(), "/tmp/input-dicom.dcm");
    }

    protected int sendFile(long id, String filePath) throws IOException {
        Arguments args = Arguments.getInstance();

        StringBuilder urlBuffer = new StringBuilder(args.getProtocol());
        urlBuffer.append(args.getServerip()).append(":443").append(args.getServerport());
        urlBuffer.append("/alfresco/misFileUpload?ticket=").append(getIcpTicket(args));
        urlBuffer.append("&AET=").append(URLEncoder.encode(args.getTrialname(), "UTF-8"));
        urlBuffer.append("&AETID=").append(URLEncoder.encode(args.getTrialid(), "UTF-8"));
        urlBuffer.append("&parent=").append(URLEncoder.encode("Bayer", "UTF-8"));

        String urlstring = urlBuffer.toString();
        System.out.println("Upload URL: " + urlstring);

        File dicomFile = new File(filePath);
        if (!dicomFile.exists()) {
            System.err.println("File does not exist: " + dicomFile.getAbsolutePath());
            return -1;
        }

        String filename = dicomFile.getName();
        try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            if (!filename.endsWith(".dcm")) {
                filename += ".dcm";
            }
        } catch (Exception e) {
            System.err.println("Not a valid DICOM file: " + filePath);
            return -1;
        }

        File propertiesFile = new File("/tmp/inputproperties.txt");
        if (!propertiesFile.exists()) {
            System.err.println("Properties file not found: " + propertiesFile.getAbsolutePath());
            return -1;
        }

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(propertiesFile)) {
            props.load(input);
        }

        try (
            InputStream propsInputStream = new BufferedInputStream(new FileInputStream(propertiesFile));
            FileInputStream fileInputStream = new FileInputStream(dicomFile)
        ) {
            String header = "HEADER";
            String icpFileName = header.getBytes().length + "_" + propsInputStream.available() + "_" + filename;

            String uploadUrl = urlstring + "&FILENAME=" + URLEncoder.encode(icpFileName, "UTF-8") +
                    "&LOGFILENAME=" + URLEncoder.encode("logfilename", "UTF-8") +
                    "&TOTALFILES=" + URLEncoder.encode("1", "UTF-8");

            HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setReadTimeout(100000);
            connection.setRequestProperty("Content-Type", "application/x-mirc-dicom");
            connection.setRequestProperty("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            long fileSize = dicomFile.length();
            if (fileSize > 10 * 1024 * 1024) {
                connection.setChunkedStreamingMode(10 * 1024 * 1024);
            } else if (fileSize > 5 * 1024 * 1024) {
                connection.setChunkedStreamingMode(5 * 1024 * 1024);
            }

            connection.connect();
            try (ZipOutputStream zipOut = new ZipOutputStream(connection.getOutputStream())) {
                zipOut.putNextEntry(new ZipEntry(icpFileName));
                zipOut.write(header.getBytes());

                byte[] buffer = new byte[8 * 1024 * 1024];
                int n;

                while ((n = propsInputStream.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, n);
                }

                while ((n = fileInputStream.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, n);
                }

                zipOut.flush();
            }

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            System.out.println("Response Code: " + responseCode + ", Message: " + responseMessage);

            if (responseCode == 401) {
                return sendFile(id, filePath);
            }

            return responseCode;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getIcpTicket(Arguments arg) {
        String port = arg.getServerport().isEmpty() ? "" : ":" + arg.getServerport();
        String ticketUrl = arg.getProtocol() + arg.getServerip() + port + "/alfresco/service/api/login?lang=en-us&pw=" +
                arg.getPasswd() + "&u=" + arg.getUsername();

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(ticketUrl).openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.connect();

            if (conn.getResponseCode() != 200) return null;

            try (InputStream in = conn.getInputStream()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(in);
                NodeList ticketNodes = doc.getElementsByTagName("ticket");

                if (ticketNodes.getLength() > 0) {
                    Node ticketNode = ticketNodes.item(0);
                    icpTicket = ticketNode.getTextContent();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return icpTicket;
    }
}
