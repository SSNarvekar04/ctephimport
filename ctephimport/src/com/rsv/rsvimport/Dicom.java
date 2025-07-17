package com.rsv.rsvimport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che5.io.DicomInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Dicom {
    private Log logger = LogFactory.getLog(getClass());

    private String urlstring = null;
    private String icpTicket = null;

    static {
        disableSslVerification();
        System.setProperty("java.awt.headless", "true");
    }

    private static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    protected int sendFile(long id, String file) throws IOException {
        int responseCode = 0;
        Arguments args = Arguments.getInstance();

        StringBuilder urlbuffer = new StringBuilder(args.getProtocol());
        urlbuffer.append(args.getServerip()).append(":443").append(args.getServerport());
        urlbuffer.append("/alfresco/misFileUpload?ticket=").append(getIcpTicket(args));
        urlbuffer.append("&AET=").append(URLEncoder.encode(args.getTrialname(), "UTF-8"));
        urlbuffer.append("&AETID=").append(URLEncoder.encode(args.getTrialid(), "UTF-8"));
        urlbuffer.append("&parent=").append(URLEncoder.encode("Bayer", "UTF-8"));
        urlstring = urlbuffer.toString();

        File fl = new File(file);
        if (!fl.exists()) {
            logger.debug(fl.getAbsolutePath() + " does not exist");
            return -1;
        }

        String filename = fl.getName();
        String contentType = "application/x-mirc-dicom";
        HttpURLConnection connection = null;

        FileInputStream sendFileInputStream = null;
        InputStream propertiesFileInputStream = null;
        Properties prop;
        InputStream inputStream = null;

        ZipOutputStream svros;

        try {
            if (!filename.endsWith(".dcm")) {
                try (DicomInputStream in = new DicomInputStream(fl)) {
                    filename += ".dcm";
                } catch (Exception ex) {
                    logger.debug("Not a DICOM file");
                    return 0;
                }
            }

            StringBuilder headerStr = new StringBuilder("HEADER");
            StringBuilder icpFileName = new StringBuilder();

            prop = new Properties();
            String propFileName = "/home/chaitu/Desktop/test.properties";
            inputStream = new FileInputStream(propFileName);
            prop.load(inputStream);
            propertiesFileInputStream = new BufferedInputStream(inputStream);

            icpFileName.append(headerStr.toString().getBytes().length).append("_");
            icpFileName.append(propertiesFileInputStream.available()).append("_");
            icpFileName.append(filename);

            String uploadUrlString = urlstring + "&FILENAME=" + URLEncoder.encode(icpFileName.toString(), "UTF-8")
                    + "&LOGFILENAME=" + URLEncoder.encode("logfilename", "UTF-8")
                    + "&TOTALFILES=" + URLEncoder.encode("1", "UTF-8");
            logger.debug(uploadUrlString);

            URL serverAddress = new URL(uploadUrlString);
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setReadTimeout(100000);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            long fileLength = fl.length();
            if (fileLength > 10 * 1024 * 1024) {
                connection.setChunkedStreamingMode(10 * 1024 * 1024);
            } else if (fileLength > 5 * 1024 * 1024) {
                connection.setChunkedStreamingMode(5 * 1024 * 1024);
            }

            connection.connect();

            svros = new ZipOutputStream(connection.getOutputStream());
            svros.putNextEntry(new ZipEntry(icpFileName.toString()));

            byte[] buffer = new byte[8 * 1024 * 1024];
            long total = 0;

            svros.write(headerStr.toString().getBytes());

            int n;
            while ((n = propertiesFileInputStream.read(buffer)) > 0) {
                svros.write(buffer, 0, n);
                total += n;
                if (total > 100 * 1024 * 1024) {
                    logger.debug("Uploaded: " + (total / (1024 * 1024)) + " mb");
                }
            }

            sendFileInputStream = new FileInputStream(fl);
            while ((n = sendFileInputStream.read(buffer)) > 0) {
                svros.write(buffer, 0, n);
                total += n;
                if (total > 100 * 1024 * 1024) {
                    logger.debug("Uploaded: " + (total / (1024 * 1024)) + " mb");
                }
            }

            svros.flush();
            svros.close();
            propertiesFileInputStream.close();
            sendFileInputStream.close();

            responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            logger.debug("Response Code: " + responseCode + " Response Message: " + responseMessage + " for sending " + icpFileName);

            if (responseCode == 401) {
                return sendFile(id, file);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return responseCode;
    }

    public String getIcpTicket(Arguments arg) {
        String port = arg.getServerport().isEmpty() ? "" : ":" + arg.getServerport();
        String ticketurlString = arg.getProtocol() + arg.getServerip() + port + "/alfresco/service/api/login?lang=en-us&pw="
                + arg.getPasswd() + "&u=" + arg.getUsername();
        logger.debug("URL is :: " + ticketurlString);

        try {
            URL url = new URL(ticketurlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.connect();

            int responsecode = conn.getResponseCode();
            if (responsecode != 200) {
                return null;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(conn.getInputStream());
            conn.disconnect();

            NodeList ticketNode = doc.getElementsByTagName("ticket");
            if (ticketNode.getLength() > 0) {
                Node datum = ticketNode.item(0);
                Text result = (Text) datum.getFirstChild();
                this.icpTicket = result.getWholeText();
            }

        } catch (Exception e) {
            logger.debug("Error while retrieving ticket", e);
            System.exit(1);
        }

        return icpTicket;
    }

    public static void main(String[] args) {
        Arguments argobj = Arguments.getInstance();
        argobj.parseArguments(args);
        Dicom dicom = new Dicom();
        try {
            dicom.sendFile(1, "/home/chaitu/Desktop/AAA/1.3.12.2.1107.5.1.4.64149.30000013122700150110900002348.dcm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
