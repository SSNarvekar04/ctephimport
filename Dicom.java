package com.rsv.rsvimport;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che3.io.DicomInputStream;

public class Dicom {
    	private Log logger = LogFactory.getLog(getClass());

	private String urlstring = null;
	private String icpTicket=null;

	static {
		disableSslVerification();
		System.setProperty("java.awt.headless", "true");

	}

	private static void disableSslVerification() {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}


	
	protected int sendFile(long id,String file) throws IOException {
		int responseCode = 0;
		
		Arguments args=Arguments.getInstance();

		StringBuffer urlbuffer = new StringBuffer(args.getProtocol());
		urlbuffer.append(args.getServerip() + ":443" + args.getServerport());
		urlbuffer.append("/alfresco/misFileUpload");
		urlbuffer.append("?ticket="
				+ getIcpTicket(args));

		urlbuffer.append("&AET="
				+ URLEncoder.encode(args.getTrialname(), "UTF-8"));
		urlbuffer.append("&AETID="
				+ URLEncoder.encode(args.getTrialid(), "UTF-8"));
		urlbuffer.append("&parent="
				+ URLEncoder.encode("Bayer", "UTF-8"));
		urlstring = urlbuffer.toString();
		//logger.debug(urlstring);
		
		File fl = new File(file);
		if(!fl.exists()) {
		logger.debug(fl.getAbsolutePath() + " does not exists");
		}
		String filename = fl.getName();
		String contentType = "application/x-mirc-dicom";
		HttpURLConnection connection = null;

		URL serverAddress = null;
		FileInputStream sendFileInputStream = null;
		InputStream propertiesFileInputStream = null;
		Properties prop = null;
		InputStream inputStream =null;

		ZipOutputStream svros;

		try {

			if (!filename.endsWith(".dcm")) {
				DicomInputStream in = null;
				try {
					in = new DicomInputStream(fl);					
					if (in != null) {
						filename = filename + ".dcm";
					}
				} catch(Exception ex) {
					logger.debug( "Not a dicom file");
					return 0;
				} finally {
					try {
						in.close();
					} catch (IOException ex) {
					}
				}
			}

			StringBuilder headerStr = new StringBuilder();
			StringBuilder icpFileName = new StringBuilder();
			try {
				String uploadUrlString = null;


			     prop = new Properties();
				String propFileName = "/home/chaitu/Desktop/test.properties";
				try {

				 inputStream = new FileInputStream(new File(
							propFileName));

					prop.load(inputStream);
					//inputStream.close();

				} catch (IOException e) {
					e.printStackTrace();
				} finally {

				}

				propertiesFileInputStream = new BufferedInputStream(inputStream);
				// Build the header for sending files.
				headerStr.append("HEADER");
				
				// Building file name and reading supplemental data file's
				// content
				icpFileName
						.append(headerStr.toString().getBytes().length + "_");
				icpFileName.append(propertiesFileInputStream.available() + "_");
					icpFileName.append(filename);
				
				uploadUrlString = urlstring + "&FILENAME="
						+ URLEncoder.encode(icpFileName.toString(), "UTF-8");
			//	log.info("LogFileName: " + logfilename);
				uploadUrlString = uploadUrlString + "&LOGFILENAME="
						+ URLEncoder.encode("logfilename", "UTF-8");

				uploadUrlString = uploadUrlString + "&TOTALFILES="
						+ URLEncoder.encode(Long.toString(1), "UTF-8");
				logger.debug(uploadUrlString);

				serverAddress = new URL(uploadUrlString);
				connection = (HttpURLConnection) serverAddress.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setReadTimeout(100000);
				connection.setRequestProperty("Content-Type", contentType);
				connection.setRequestProperty("Content-Disposition",
						"attachment; filename=\"" + filename + "\"");
				//Instead of buffering entire data send it in chunks of specified mb. 
				//This will work only with the server which supports http1.1 protocol
				if(fl.length()>1024*1024*10) {
					connection.setChunkedStreamingMode(1024*1024*10);
				} else if(fl.length()>1024*1024*5) {
					connection.setChunkedStreamingMode(1024*1024*5);
				}
				connection.connect();

				svros = new ZipOutputStream(connection.getOutputStream());

				// svros.putNextEntry(new ZipEntry(propertiesFileInputStream.
				// available() + "_" +(amlfileinputstream==null?
				// 0:amlfileinputstream.available())+"_"+ filename));
				svros.putNextEntry(new ZipEntry(icpFileName.toString()));

				// System.out.print("JPS::: File name is ::: "+propertiesFileInputStream.
				// available() + "_" + filename);

				/*
				 * get the output stream writer and write the output to the
				 * server
				 */
				int n;
				byte[] bbuf = new byte[1024*1024*8];
				long total=0;
			
				// Writing Header String to output
				svros.write(headerStr.toString().getBytes());

				// writing properties file content to output
				while ((n = propertiesFileInputStream
						.read(bbuf, 0, bbuf.length)) > 0) {
					total=total+n;
					if(total>100*1024*1024)
					{
						logger.debug("Uploaded: "+(total/(1024*1024))+" mb");
					}
					svros.write(bbuf, 0, n);
				}		
		
						try {
							sendFileInputStream = new FileInputStream(fl);
							while ((n = sendFileInputStream.read(bbuf, 0, bbuf.length)) > 0) {
								svros.write(bbuf, 0, n);
								total=total+n;
								if(total>100*1024*1024)
								{
									logger.debug("Uploaded: "+(total/(1024*1024))+" mb");
								}
							}
						} catch (FileNotFoundException ex) {
							ex.printStackTrace();
							return -1;
						} finally {
							if(sendFileInputStream != null) {
								sendFileInputStream.close();
							}
						}
				svros.flush();
				svros.close();
				propertiesFileInputStream.close();
				
			} catch (IOException ex) {
				ex.printStackTrace();
				return -1;
			}

			responseCode = connection.getResponseCode();
			String responseMessage = connection.getResponseMessage();

			if (responseCode == 200) {
				logger.debug("Response Code: " + responseCode
						+ " Response Message: " + responseMessage + " for sending " + icpFileName);

			} else {
				logger.debug("Response Code: " + responseCode
						+ " Response Message: " + responseMessage + " for sending " + icpFileName);
				if (responseCode == 404) {
					// Display Option dialog box so that the user can abort or
					// continue;
				}

				if (responseCode == 401) {
					//statusbar.setProgress(--progress);
					return sendFile(id, file);
				}

				if (responseCode == 500) {
					logger.debug("failed to import");
				}
				
			}

		} catch (MalformedURLException ex) {
			ex.printStackTrace();


		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			// close the connection, set all objects to null
			if (connection != null) {
				connection.disconnect();
			}

			connection = null;
		}

		return responseCode;

	}


	public String getIcpTicket(Arguments arg) {
		String port = "";
		if (!arg.getServerport().equals("")) {
			port = ":" + arg.getServerport();
		}
		String ticketurlString = arg.getProtocol() + arg.getServerip() + port
				+ "/alfresco/service/api/login?" + "lang=en-us&pw="
				+ arg.getPasswd() + "&u=" + arg.getUsername();
		logger.debug("URL is :: " + ticketurlString);
		URL url;
		try {
			url = new URL(ticketurlString);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setConnectTimeout(5000);
			// conn.setRequestMethod("POST");
			conn.connect();

			int responsecode = conn.getResponseCode();
			String responsemesg = conn.getResponseMessage();

			 logger.debug("ResponseCode :: " + responsecode
			 + "ResponseMesg :: " + responsemesg);

			if (responsecode == -1) {
				return null;
			}
			if (responsecode == 403) {
				return null;
			}
			if (responsecode == 401) {
				return null;
			}
			if (responsecode != 200) {
				return null;
			}
			// Read the response
			DOMParser parser = new DOMParser();
			InputStream in = conn.getInputStream();
			InputSource source = new InputSource(in);
			parser.parse(source);
			in.close();
			conn.disconnect();

			Document doc = parser.getDocument();
			NodeList ticketnode = doc.getElementsByTagName("ticket");
			Node datum = ticketnode.item(0);
			Text result = (Text) datum.getFirstChild();

			this.icpTicket = result.getWholeText();
			// logger.debug("TICKET: " + this.icpTicket);
		} catch (java.net.SocketTimeoutException e) {
			e.printStackTrace();
			logger.debug("Connecton Timeout");
			System.exit(6);

		}
		 catch (IOException e) {
				e.printStackTrace();

			 logger.debug("Unable to Connect the Server");
				System.exit(7);
		}
		catch (Exception e) {
			logger.debug("Unable to read data from the Server");
			System.exit(8);
		}

		return icpTicket;
	}
	
	public static void main(String[] args) {
		Arguments argobj = Arguments.getInstance();
		argobj.parseArguments(args);
		Dicom dicom=new Dicom();
		try {
			dicom.sendFile(1,"/home/chaitu/Desktop/AAA/1.3.12.2.1107.5.1.4.64149.30000013122700150110900002348.dcm");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
