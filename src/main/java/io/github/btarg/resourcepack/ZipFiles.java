package io.github.btarg.resourcepack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// https://www.spigotmc.org/threads/folder-zip-method-produces-broken-zip-file.329666/
public class ZipFiles {

    public static void zipFolder(String sourceDirPath, String zipFilePath) throws IOException {
        FileOutputStream fos;
        fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceDirPath);
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            assert children != null;
            for (File childFile : children) {
                zipChildFile(childFile, childFile.getName(), zipOut);
            }
            return;
        }
        writeZipFile(fileToZip, fileName, zipOut);
    }

    private static void zipChildFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            assert children != null;
            for (File childFile : children) {
                zipChildFile(childFile, fileName + File.separator + childFile.getName(), zipOut);
            }
            return;
        }
        writeZipFile(fileToZip, fileName, zipOut);
    }

    private static void writeZipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

}