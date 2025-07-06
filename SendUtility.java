/*
  *  @(#)SendUtility.java Jun 3, 2018 
  * 
  *  Copyright (c) Radiant Sage.
  *  SoftSol Building, Plot No.4,Hitech City, Hyderabad-500081,INDIA.
  *  All rights reserved.
  * 
  *  This software is the confidential and proprietary information of 
  *  Radiant Sage . ("Confidential Information").  You shall not
  *  disclose such Confidential Information and shall use it only in
  *  accordance with the terms of the license agreement you entered into
  *  with Radiant Sage.
 */
package com.rsv.rsvimport;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.dcm4che3.io.DicomInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Class description goes here.
 *
 * @version 	Jun 3, 2018
 * @author venky
 */
public class SendUtility {
    String icpticket=null;
/* A class implementation comment can go here. */
    public static void main(String[] args) throws IOException {
        Arguments argobj = Arguments.getInstance();
		argobj.parseArguments(args);
        File fl=new File("/tmp/input-dicom.dcm");
        SendUtility su=new SendUtility();
        su.sendFile(System.currentTimeMillis(),"/tmp/input-dicom.dcm");
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
		String urlstring = urlbuffer.toString();
		System.out.println(urlstring);
		
		File fl = new File(file);
		if(!fl.exists()) {
		System.out.println(fl.getAbsolutePath() + " does not exists");
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
					System.out.println( "Not a dicom file"+ex.getMessage());
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
				String propFileName = "/tmp/inputproperties.txt";
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
				System.out.println(uploadUrlString);

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
						System.out.println("Uploaded: "+(total/(1024*1024))+" mb");
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
									System.out.println("Uploaded: "+(total/(1024*1024))+" mb");
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
				System.out.println("Response Code: " + responseCode
						+ " Response Message: " + responseMessage + " for sending " + icpFileName);

			} else {
				System.out.println("Response Code: " + responseCode
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
					System.out.println("failed to import");
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
		System.out.println("URL is :: " + ticketurlString);
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

			 System.out.println("ResponseCode :: " + responsecode
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

			this.icpticket = result.getWholeText();
			// System.out.println("TICKET: " + this.icpTicket);
		} catch (java.net.SocketTimeoutException e) {
			e.printStackTrace();
			System.out.println("Connecton Timeout");
			System.exit(6);

		}
		 catch (IOException e) {
				e.printStackTrace();

			 System.out.println("Unable to Connect the Server");
				System.exit(7);
		}
		catch (Exception e) {
			System.out.println("Unable to read data from the Server");
			System.exit(8);
		}

		return icpticket;
	}
}
