package verso.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private List <String> fileList;
    private String outputFile;
    private String sourceFolder;

    public ZipUtils(String source, String output) {
        fileList = new ArrayList < String > ();
      
        this.sourceFolder = source;
        this.outputFile = output;
        this.sourceFolder = this.sourceFolder.replaceAll("\\\\", "/");
        this.outputFile = this.outputFile.replaceAll("\\\\", "/");
        System.out.println(sourceFolder + " - "+outputFile);
        this.generateFileList(new File(this.sourceFolder));
    }


    public void zipIt() {
        byte[] buffer = new byte[1024];
        String source = new File(this.sourceFolder).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(this.outputFile);
            zos = new ZipOutputStream(fos);

            System.out.println("Output to Zip : " + this.outputFile);
            FileInputStream in = null;

            for (String file: this.fileList) {
                System.out.println("File Added : " + file);
                ZipEntry ze = new ZipEntry(source + "/" + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(this.sourceFolder + File.separator + file);
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }
            zos.closeEntry();
            System.out.println("Folder successfully compressed");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateFileList(File node) {
        // add file only
    	System.out.println("("+node.getPath()+")");
        if (node.isFile()) {
        	String n = generateZipEntry(node.getPath());
        	System.out.println("---"+n);
            fileList.add(n);
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename: subNote) {
                generateFileList(new File(node, filename));
            }
        }
    }

    private String generateZipEntry(String file) {
        return file.substring(this.sourceFolder.length(), file.length());
    }
}