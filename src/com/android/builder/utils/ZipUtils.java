package com.android.builder.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private static final int BUFFER_SIZE = 4096;

    public ZipUtils() {
    }

    public static boolean unzip(File zipFile, String destDirectory) {
        ZipInputStream zipIn = null;

        boolean filePath;
        try {
            zipIn = new ZipInputStream(new FileInputStream(zipFile));

            for(ZipEntry e = zipIn.getNextEntry(); e != null; e = zipIn.getNextEntry()) {
                String filePath1 = destDirectory + File.separator + e.getName();
                if(!e.isDirectory()) {
                    extractFile(zipIn, filePath1);
                } else {
                    File e1 = new File(filePath1);
                    e1.mkdir();
                }

                zipIn.closeEntry();
            }

            filePath = true;
            return filePath;
        } catch (IOException var14) {
            var14.printStackTrace();
            filePath = false;
        } finally {
            if(zipIn != null) {
                try {
                    zipIn.close();
                } catch (IOException var13) {
                    var13.printStackTrace();
                }
            }

        }

        return filePath;
    }

    public static boolean unzip(InputStream inputStream, String destDirectory) {
        File destDir = new File(destDirectory);
        if(!destDir.exists()) {
            destDir.mkdir();
        }

        ZipInputStream zipIn = null;

        boolean filePath;
        try {
            zipIn = new ZipInputStream(inputStream);

            for(ZipEntry e = zipIn.getNextEntry(); e != null; e = zipIn.getNextEntry()) {
                String filePath1 = destDirectory + File.separator + e.getName();
                if(!e.isDirectory()) {
                    extractFile(zipIn, filePath1);
                } else {
                    File e1 = new File(filePath1);
                    e1.mkdir();
                }

                zipIn.closeEntry();
            }

            filePath = true;
            return filePath;
        } catch (IOException var15) {
            var15.printStackTrace();
            filePath = false;
        } finally {
            if(zipIn != null) {
                try {
                    zipIn.close();
                } catch (IOException var14) {
                    var14.printStackTrace();
                }
            }

        }

        return filePath;
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        FileUtils.createFile(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        boolean read = false;

        int read1;
        while((read1 = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read1);
        }

        bos.close();
    }

    public static void zip(List<File> listFiles, String destZipFilePath) throws FileNotFoundException, IOException {
        ZipOutputStream zos = null;

        try {
            zos = new ZipOutputStream(new FileOutputStream(destZipFilePath));
            Iterator var3 = listFiles.iterator();

            while(var3.hasNext()) {
                File file = (File)var3.next();
                if(file.isDirectory()) {
                    zipDirectory(file, file.getName(), zos);
                } else {
                    zipFile(file, zos);
                }
            }
        } finally {
            if(zos != null) {
                zos.flush();
                zos.close();
            }

        }

    }

    public static void zip(String[] files, String destZipFilePath) throws FileNotFoundException, IOException {
        ArrayList listFiles = new ArrayList();

        for(int i = 0; i < files.length; ++i) {
            listFiles.add(new File(files[i]));
        }

        zip((List)listFiles, destZipFilePath);
    }

    private static void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws FileNotFoundException, IOException {
        File[] var3 = folder.listFiles();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            if(file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
            } else {
                zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
                BufferedInputStream bis = null;

                try {
                    bis = new BufferedInputStream(new FileInputStream(file));
                    long bytesRead = 0L;
                    byte[] bytesIn = new byte[4096];

                    int var15;
                    for(boolean read = false; (var15 = bis.read(bytesIn)) != -1; bytesRead += (long)var15) {
                        zos.write(bytesIn, 0, var15);
                    }

                    zos.closeEntry();
                } finally {
                    if(bis != null) {
                        bis.close();
                    }

                }
            }
        }

    }

    private static void zipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            long bytesRead = 0L;
            byte[] bytesIn = new byte[4096];

            int read1;
            for(boolean read = false; (read1 = bis.read(bytesIn)) != -1; bytesRead += (long)read1) {
                zos.write(bytesIn, 0, read1);
            }

            zos.closeEntry();
        } finally {
            if(bis != null) {
                bis.close();
            }

        }
    }
}
