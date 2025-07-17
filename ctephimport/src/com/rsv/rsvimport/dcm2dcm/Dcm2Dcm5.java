package com.rsv.rsvimport.dcm2dcm;

import org.apache.commons.cli.*;
import org.dcm4che5.data.*;
import org.dcm4che5.io.*;
import org.dcm4che5.tool.transcode.DcmTranscoder;
import org.dcm4che5.util.SafeClose;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Dcm2Dcm5 {

    private String tsuid = UID.ExplicitVRLittleEndian;
    private boolean retainFMI = false;
    private boolean noFMI = false;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    public void setTransferSyntax(String uid) {
        this.tsuid = uid;
    }

    public void setRetainFileMetaInformation(boolean retainFMI) {
        this.retainFMI = retainFMI;
    }

    public void setWithoutFileMetaInformation(boolean noFMI) {
        this.noFMI = noFMI;
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    public void transcode(File src, File dest) throws IOException {
        DicomInputStream dis = null;
        DicomOutputStream dos = null;
        try {
            dis = new DicomInputStream(src);
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);

            Attributes fmi = dis.readFileMetaInformation();
            Attributes dataset = dis.readDataset(-1, -1);

            String inputTSUID = dis.getTransferSyntax();
            String outputTSUID = tsuid;

            if (noFMI) {
                fmi = null;
            } else if (retainFMI && fmi != null) {
                fmi.setString(Tag.TransferSyntaxUID, VR.UI, outputTSUID);
            } else {
                fmi = dataset.createFileMetaInformation(outputTSUID);
            }

            dos = new DicomOutputStream(dest);
            dos.setEncodingOptions(encOpts);

            DcmTranscoder transcoder = new DcmTranscoder();
            transcoder.setSourceTransferSyntax(inputTSUID);
            transcoder.setDestinationTransferSyntax(outputTSUID);
            transcoder.transcode(dataset);

            dos.writeDataset(fmi, dataset);
        } finally {
            SafeClose.close(dis);
            SafeClose.close(dos);
        }
    }

    private static void process(File src, File destDir, Dcm2Dcm5 converter) {
        if (src.isDirectory()) {
            destDir.mkdirs();
            for (File file : Objects.requireNonNull(src.listFiles())) {
                process(file, new File(destDir, file.getName()), converter);
            }
        } else {
            try {
                converter.transcode(src, destDir);
                System.out.println("Transcoded: " + src.getPath());
            } catch (IOException e) {
                System.err.println("Failed: " + src.getPath() + " -> " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("t", "transfer-syntax", true, "Transfer Syntax UID");
        options.addOption("f", "retain-fmi", false, "Retain File Meta Information");
        options.addOption("F", "no-fmi", false, "No File Meta Information");
        options.addOption("h", "help", false, "Print help");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h") || cmd.getArgList().size() < 2) {
                formatter.printHelp("Dcm2Dcm5 [options] <src> <dest>", options);
                System.exit(0);
            }

            File src = new File(cmd.getArgList().get(0));
            File dest = new File(cmd.getArgList().get(1));

            Dcm2Dcm5 converter = new Dcm2Dcm5();

            if (cmd.hasOption("t"))
                converter.setTransferSyntax(cmd.getOptionValue("t"));

            if (cmd.hasOption("f"))
                converter.setRetainFileMetaInformation(true);

            if (cmd.hasOption("F"))
                converter.setWithoutFileMetaInformation(true);

            process(src, dest, converter);

        } catch (ParseException e) {
            System.err.println("Parsing failed: " + e.getMessage());
            formatter.printHelp("Dcm2Dcm5", options);
        }
    }
}
