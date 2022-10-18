package com.javarush.task.task31.task3110;

import com.javarush.task.task31.task3110.exception.PathIsNotFoundException;
import com.javarush.task.task31.task3110.exception.WrongZipFileException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFileManager {
    private Path zipFile;

    public ZipFileManager(Path zipFile) {
        this.zipFile = zipFile;
    }

    public void createZip(Path source) throws Exception{
        if (Files.notExists(zipFile.getParent())){
            Files.createDirectories(zipFile.getParent());
        }

        try(ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))){
            if (Files.isRegularFile(source)){
                addNewZipEntry(zipOutputStream, source.getParent() ,source.getFileName());
            }

            else if (Files.isDirectory(source)){
                FileManager fileManager = new FileManager(source);

                List<Path> list = fileManager.getFileList();
                for (Path files : list) {
                    addNewZipEntry(zipOutputStream, source, files);
                }
            }

            else {
                throw new PathIsNotFoundException();
            }
        }
    }

    public void extractAll(Path outPutFolder) throws Exception {
        if (!Files.isRegularFile(zipFile)){
            throw new WrongZipFileException();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            if (Files.notExists(outPutFolder)) {
                Files.createDirectories(outPutFolder);
            }

            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null){
                String name = zipEntry.getName();
                Path fullName = outPutFolder.resolve(name);

                if (Files.notExists(fullName.getParent())){
                    Files.createDirectories(fullName.getParent());
                }

                try (OutputStream outputStream = Files.newOutputStream(fullName)) {
                    copyData(zipInputStream, outputStream);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private void addNewZipEntry(ZipOutputStream zipOutputStream, Path filePath, Path fileName) throws Exception{
        Path fullPath = filePath.resolve(fileName);
        try (InputStream inputStream = Files.newInputStream(fullPath)){
            ZipEntry entry = new ZipEntry(fileName.toString());

            zipOutputStream.putNextEntry(entry);

            copyData(inputStream, zipOutputStream);

            zipOutputStream.closeEntry();
        }
    }

    private void copyData(InputStream in, OutputStream out) throws Exception{
        byte[] buffer = new byte[8 * 1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    public List<FileProperties> getFilesList() throws Exception{
        if (! Files.isRegularFile(zipFile)){
            throw new WrongZipFileException();
        }

        List<FileProperties> fileProperties = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))){

            ZipEntry entry = zipInputStream.getNextEntry();

            while (entry != null){
                FileProperties properties = new FileProperties(entry.getName(), entry.getSize(), entry.getCompressedSize(), entry.getMethod());

                fileProperties.add(properties);
                entry = zipInputStream.getNextEntry();
            }
        }
        return fileProperties;
    }
}