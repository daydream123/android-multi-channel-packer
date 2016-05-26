package com.zf.android.packer;

import com.zf.android.packer.utils.FileUtils;
import com.zf.android.packer.utils.IOUtils;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

/**
 *
 * @author zhangfei
 */
public class Builder {
    
    public static void build(File srcFile, String outputPath, String[] channels, ProgressListener listener) throws Exception{
        File channelFile = new File(outputPath + "/channel.txt");
        if (!channelFile.exists()) {
            try {
                FileUtils.createFile(channelFile);
            } catch (IOException ex) {
                Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "创建临时渠道号文件失败", "错误提示", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        if (channels != null && channels.length > 0) {
            int count = channels.length;
            int index = 0;
            for (String channel : channels) {
                index++;
                ZipOutputStream outputStream = null;
                try {
                    String copyedApkPath = outputPath + "/"
                            + FileUtils.getFileNameWitoutSuffix(srcFile)
                            + "_" + channel + ".zip";
                    File copied = new File(copyedApkPath);

                    FileUtils.copyFile(srcFile, copied);
                    overrideWriteChannel(channelFile, channel);
                    appendChannel(copied, channelFile.getAbsolutePath());
                    
                    // update progress
                    int percentage = (int) (index / (count * 1f) * 100);
                    listener.onProgress(channel, percentage);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            }
            
            TVFS.umount();
            renameApks(outputPath);
            
            // delete channel file
            channelFile.delete();
        }
    }
    
    private static void appendChannel(File fileToAppend, String channelFile) throws IOException, Exception{
        String targetFile = fileToAppend.getAbsolutePath() + "/META-INF";
        TFile src = new TFile(channelFile);
        TFile dst = new TFile(targetFile);
        if (dst.isArchive() || dst.isDirectory()) {
            dst = new TFile(dst, src.getName());
        }
        src.cp_rp(dst);        
    }

    private static void overrideWriteChannel(File channelDescFile, String channel) throws FileNotFoundException, IOException {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(channelDescFile, false);
            outputStream.write(channel.getBytes());
            outputStream.flush();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }
    
    private static void renameApks(String outputDir){
        File[] files = new File(outputDir).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String newName = file.getAbsolutePath().replace("zip", "apk");
                file.renameTo(new File(newName));
            }
        }
    }
    
    private static void copyAndAppend() throws IOException{
         // read srcFile.zip and write to append.zip
        ZipFile srcFile = new ZipFile("C:/Carbada_android.apk");
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream("C:/Carbada_android_cpp.zip"));
        append.setMethod(ZipOutputStream.DEFLATED);
        
        // first, copy contents from existing srcFile
        Enumeration<? extends ZipEntry> entries = srcFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry itemFile = new ZipEntry(entries.nextElement().getName());
            System.out.println("copy: " + itemFile.getName());
            append.putNextEntry(itemFile);
            if (!itemFile.isDirectory()) {
                IOUtils.copy(srcFile.getInputStream(itemFile), append);
            }
            append.closeEntry();
        }
        
        ZipEntry channelEntry = new ZipEntry("/assets/channel");
        append.putNextEntry(channelEntry);
        append.write("434342".getBytes());
        append.closeEntry();
        
        // close
        srcFile.close();
        append.close();
    }

}
