package com.example.dicomreader;

import org.dcm4che5.data.Attributes;
import org.dcm4che5.data.Tag;
import org.dcm4che5.io.DicomInputStream;
import me.tongfei.progressbar.*;

import java.io.File;
import java.io.IOException;

public class DicomProgressReader5 {

    public static void main(String[] args) {
        File dicomDir = new File("D:/dicoms"); // ← इथे तुमचं DICOM फोल्डर पथ द्या
        File[] dicomFiles = dicomDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".dcm"));

        if (dicomFiles == null || dicomFiles.length == 0) {
            System.out.println("No DICOM files found in " + dicomDir.getAbsolutePath());
            return;
        }

        // Progress bar build
        try (ProgressBar pb = new ProgressBarBuilder()
                .setTaskName("Reading DICOMs")
                .setInitialMax(dicomFiles.length)
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setUpdateIntervalMillis(100)
                .setUnit("files", 1)
                .build()) {

            for (File dicomFile : dicomFiles) {
                readDicomFile(dicomFile);
                pb.step(); // Progress bar update
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readDicomFile(File file) {
        try (DicomInputStream dis = new DicomInputStream(file)) {
            Attributes dataset = dis.readDataset(-1, -1);
            String patientName = dataset.getString(Tag.PatientName);
            String studyDate = dataset.getString(Tag.StudyDate);

            System.out.printf("File: %s | Patient: %s | Date: %s\n",
                    file.getName(),
                    patientName != null ? patientName : "Unknown",
                    studyDate != null ? studyDate : "Unknown");

        } catch (IOException e) {
            System.err.println("Failed to read DICOM file: " + file.getName());
        }
    }
}
