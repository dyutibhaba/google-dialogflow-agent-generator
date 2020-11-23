/**
 * 
 */
package com.dlgflw.agent.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author SII068
 *
 */
public class ZipUnzipUtility {
	
	private static Logger logger = LoggerFactory.getLogger(ZipUnzipUtility.class);
	

    public void unzipAgentFile(String zipFileDir, String zipFileName, String unzipDir) {
    	boolean isIntentsDirCreated = false;
    	boolean isEntitiesDirCreated = false;
        String zipFilePath = zipFileDir + File.separator + zipFileName;
        try{
            logger.info("zipFilePath = {}", zipFilePath);
            try(ZipFile zipFile = new ZipFile(zipFilePath)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while(entries.hasMoreElements()){
                    ZipEntry entry = entries.nextElement();
                    if(!isIntentsDirCreated && entry.getName().startsWith("intents")) {
                    	new File(AgentUtility.getIntentDir(unzipDir)).mkdir();
                    	isIntentsDirCreated = true;
                    }
                    if(!isEntitiesDirCreated && entry.getName().startsWith("entities")) {
                    	new File(AgentUtility.getEntityDir(unzipDir)).mkdir();
                    	isEntitiesDirCreated = true;
                    }
                    if(entry.isDirectory()){
                        logger.info("dir  : {}", entry.getName());
                        String destPath = unzipDir + File.separator + entry.getName();
                        logger.info("destPath => {}", destPath);

                        //todo check destPath for Zip Slip problem - see further down this page.


                        File file = new File(destPath);
                        file.mkdirs();
                    } else {
                        String destPath = unzipDir + File.separator + entry.getName();

                        //todo check destPath for Zip Slip problem - see further down this page.

                        try(InputStream inputStream = zipFile.getInputStream(entry);
                            FileOutputStream outputStream = new FileOutputStream(destPath);
                        ){
                            int data = inputStream.read();
                            while(data != -1){
                                outputStream.write(data);
                                data = inputStream.read();
                            }
                        }
                        logger.info("file : {} => {}",  entry.getName(), destPath);
                    }
                }
            }


        } catch(IOException e){
            throw new RuntimeException("Error unzipping file " + zipFilePath, e);
        }
    }

    @SuppressWarnings("unused")
	private boolean isValidDestPath(String destPath) {
		return false;
        // validate the destination path of a ZipFile entry,
        // and return true or false telling if it's valid or not.
    }
    
    public void zipAgentFolder(String unzipDir, String zipFilePath) throws IOException {
    	Path zipFile = Files.createFile(Paths.get(zipFilePath));

        Path sourceDirPath = Paths.get(unzipDir);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile));
             Stream<Path> paths = Files.walk(sourceDirPath)) {
            paths
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                        try {
                            zipOutputStream.putNextEntry(zipEntry);
                            Files.copy(path, zipOutputStream);
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }

        System.out.println("Zip is created at : "+zipFile);
    }

}
