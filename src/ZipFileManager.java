package src;

import src.exception.PathIsNotFoundException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
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
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
        while (buffer.ready()){
            out.write(buffer.readLine().getBytes());
        }
        buffer.close();
    }

}
