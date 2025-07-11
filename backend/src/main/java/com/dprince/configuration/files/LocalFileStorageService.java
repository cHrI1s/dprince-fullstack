package com.dprince.configuration.files;


import com.dprince.apis.utils.AppStringUtils;
import com.dprince.configuration.files.config.LocalFileConfig;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.configuration.files.utils.FileUtils;
import com.dprince.entities.utils.AppRepository;
import lombok.Data;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Data
@Service
public class LocalFileStorageService {

    private final LocalFileConfig fileConfig;
    private final Path filesPath;
    private final Path backupPath;


    @Autowired
    public LocalFileStorageService(LocalFileConfig fileConfig){
        this.fileConfig = fileConfig;

        this.filesPath = Paths.get(this.fileConfig.getUploadDir())
                .toAbsolutePath()
                .normalize();

        this.backupPath = Paths.get(this.fileConfig.getBackupDir())
                .toAbsolutePath()
                .normalize();
        try {
            Files.createDirectories(this.filesPath);
        } catch (Exception ignored){}
    }


    public UploadVO store(MultipartFile file){
        UploadVO feedback = null;
        if(!AppStringUtils.isEmpty(file.getOriginalFilename())){
            String fileName = AppStringUtils.generateString()
                    + StringUtils.cleanPath(file.getOriginalFilename());
            if(!fileName.contains("..")) {
                try {
                    InputStream imageStream = file.getInputStream();

                    Path targetLocation = this.filesPath.resolve(fileName);
                    if (FileUtils.isImage(file.getContentType()) ||
                        FileUtils.isExcel(file.getContentType())
                    ) {
                        Files.copy(imageStream, targetLocation);
                        feedback = UploadVO.builder()
                                .fileName(fileName)
                                .build();
                    }
                    imageStream.close();
                } catch (IOException ignored) { }
            }
        }
        if(feedback==null) throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                "Failed to upload the file.");
        return feedback;
    }


    private Path getFilePath(@NotNull String fileName,
                             @NonNull ApplicationFileType fileType){
        Path path;
        switch(fileType){
            default:
            case FILE:
                path = this.filesPath.resolve(fileName).normalize();
                break;

            case BACKUP:
                path = this.backupPath.resolve(fileName).normalize();
                break;
        }
        return path;
    }

    public Resource loadFileAsResource(@NotNull String fileName,
                                       @NonNull ApplicationFileType fileType){
        try {
            Path path = this.getFilePath(fileName, fileType);
            Resource resource = new UrlResource(path.toUri());
            if(!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "File does not exist." +fileName);
            }
            return resource;
        } catch (MalformedURLException exception){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Filename is not valid.");
        }
    }

    public void delete(String fileName,
                       @NonNull ApplicationFileType fileType){
        try{
            Path path = this.getFilePath(fileName, fileType);
            Resource resource = new UrlResource(path.toUri());
            if(!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "File does not exist.");
            }
            resource.getFile().delete();
        }catch (Exception exception){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to delete the file");
        }
    }


    public void deleteUnusedFiles(@NonNull AppRepository appRepository){
        try {
            Stream<Path> filePaths = Files.walk(this.getFilesPath());
            filePaths.parallel()
                    .forEach(singlePath->{
                        try {
                            String fileType = Files.probeContentType(singlePath);
                            if(fileType.toLowerCase().startsWith("image/")){
                                // raba ko ataho ikoreshwa. Exampple kuri family cane kuri profile ntahandi canke kuri logo
                                // Member
                                // Family
                                // Institution
                            }
                        } catch (IOException e) {}
                    });
        } catch(Exception ignored){}
    }
}
