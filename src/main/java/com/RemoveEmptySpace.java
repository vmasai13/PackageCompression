package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * The reduce the amount of bytes for a physical file
 * This helps to reduce the size of packaging - WAR / EAR / JAR
 * This helps to provide more safety when the code is extracted out of a package for investigation
 * @author vijayganesan {v.masai13@gmail.com}
 */
public class RemoveEmptySpace {
    
    private static List<String> fileExtensions = new ArrayList<String>();
    private static List<String> excludeFolderList = new ArrayList<String>();
    private static String baseFolderLocation;
    private static String targetFolderLocation;
    
    public static void main(String args[]) {
        RemoveEmptySpace res = new RemoveEmptySpace();
        System.out.println("Source path :" + args[0]);
        System.out.println("Target path :" + args[1]);
        res.setUp(args);
        try {
            res.readFilesAndFolders(baseFolderLocation);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setUp(String arguments[]) {
        baseFolderLocation = arguments[0];
        targetFolderLocation = arguments[1];
        if (arguments.length > 2 && !StringUtils.isEmpty(arguments[2])) {
            String extensions[] = arguments[2].split(",");
            fileExtensions.addAll(Arrays.asList(extensions));
        }
        if (arguments.length > 3 && !StringUtils.isEmpty(arguments[3])) {
            String excludeFolders[] = arguments[3].split(",");
            excludeFolderList.addAll(Arrays.asList(excludeFolders));
        }
    }
    
    /**
     * To delete the white spaces and extra TAB components
     * @param file {@link File}
     * @throws IOException 
     */
    private void deleteSpace(File file, String targetLocation) throws IOException {
        File targetFolder = new File(targetLocation);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(targetLocation + "/" + file.getName())));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = br.readLine();
        while(null != line){
            line = line.trim();
            if (line.contains("//")) {
                if (line.startsWith("//")) {
                    line = br.readLine();
                    continue;
                } else {
                    line = line.replace(line.substring(line.indexOf("//"), line.length()), "");
                }
            }
            line = line.replaceAll("\t", "");
            bw.write(line);
            line = br.readLine();
        }
        br.close();
        bw.close();
    }
    
    /**
     * Parent method to process package compression
     * @param baseFolderLocation
     * @throws IOException
     */
    private void readFilesAndFolders(String baseFolderLocation) throws IOException {
        File folder = new File(baseFolderLocation);
        String excludeFolderName = null;
        if (!RemoveEmptySpace.baseFolderLocation.equals(baseFolderLocation)) {
            excludeFolderName = baseFolderLocation.substring(baseFolderLocation.lastIndexOf("/") + 1, baseFolderLocation.length());
        }
        if (folder.isDirectory() && !excludeFolderList.contains(excludeFolderName)) {
            File fileList[] = folder.listFiles();
            for (File file : fileList) {
                if (file.isFile()) {
                    String fileExtension = FilenameUtils.getExtension(file.getName());
                    String targetLocation = getTargetLocation(file.getAbsolutePath());
                    if (fileExtensions.contains(fileExtension)) {
                        deleteSpace(file, targetLocation);
                    } else {
                        justFileCopy(file, targetLocation);
                    }
                } else {
                    readFilesAndFolders(file.getAbsolutePath());
                }
            }
        } else {
            System.out.println("In correct file path");
            justFolderAndItsFileCopy(folder, getTargetLocation(folder.getAbsolutePath()));
        }
    }
    
    /**
     * Get the folder path for sub-folder's and its files
     * @param sourceLocation
     * @return 
     */
    private String getTargetLocation(String sourceLocation) {
        String targetPath;
        targetPath = sourceLocation.substring(baseFolderLocation.length(), sourceLocation.length());
        if (targetPath.contains("/")) {
            targetPath = targetFolderLocation + targetPath.substring(0, targetPath.lastIndexOf("/"));
        } else {
            targetPath = targetFolderLocation;
        }
        return targetPath; 
    }
    
    /**
     * To copy the file from source to destination
     * @param file {{@link File}
     * @throws IOException 
     */
    private void justFileCopy(File file, String targetLocation) throws IOException {
        File targetFolder = new File(targetLocation);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        FileUtils.copyFile(file, new File(targetLocation + "/" + file.getName()));
    }
    
    /**
     * Copy all the files and its sub-folders from source to destination folder
     * @param folder
     * @param targetLocation
     * @throws IOException
     */
    private void justFolderAndItsFileCopy(File folder, String targetLocation) throws IOException {
        File targetFolder = new File(targetLocation + "/" + folder.getName());
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        FileUtils.copyDirectory(folder, targetFolder);
    }
}
