package com.dprince.apis.files;

import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.configuration.files.UploadVO;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.entities.Institution;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("files")
public class FilesController {
    private final LocalFileStorageService storageService;
    private final AppRepository appRepository;

    @Authenticated
    @PostMapping("/upload")
    public UploadVO uploadFile(@RequestParam("file")MultipartFile file){
        return this.storageService.store(file);
    }


    @Authenticated
    @PostMapping("/uploadMultipleFiles")
    public List<UploadVO> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::uploadFile)
                .collect(Collectors.toList());
    }

    @Authenticated
    @GetMapping("/get/{fileName:.+}")
    public ResponseEntity<StreamingResponseBody> getFile(@PathVariable("fileName") String fileName){
        return this.loadAndReturnFile(fileName, ApplicationFileType.FILE);
    }


    @GetMapping("/logo/{fileName:.+}")
    public ResponseEntity<StreamingResponseBody> getLogo(@PathVariable("fileName") String fileName){
        Institution institution = this.appRepository.getInstitutionRepository()
                .findByLogo(fileName);
        if(institution==null) return ResponseEntity.notFound().build();
        return this.loadAndReturnFile(fileName, ApplicationFileType.FILE);
    }

    @GetMapping("/public-image/{fileName:.+}")
    public ResponseEntity<StreamingResponseBody> getPublicImage(@PathVariable("fileName") String fileName){
        return this.loadAndReturnFile(fileName, ApplicationFileType.FILE);
    }



    public ResponseEntity<StreamingResponseBody> loadAndReturnFile(String fileName,
                                                                    ApplicationFileType fileType){
        Resource fileResource = this.storageService.loadFileAsResource(fileName,
                fileType);
        try {
            InputStream imageStream = fileResource.getInputStream();
            StreamingResponseBody streamingResponseBody = (outputStream -> {
                int numberOfBytesToWrite;
                byte[] data = new byte[1024];
                int dataLength = data.length;
                while ((numberOfBytesToWrite = imageStream.read(data, 0, dataLength)) != -1) {
                    outputStream.write(data, 0, numberOfBytesToWrite);
                }
            });

            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+fileName+"\"")
                    .header(HttpHeaders.ETAG, "file-"+fileName)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(streamingResponseBody);
        } catch (IOException exception){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to stream the file.");
        }
    }



    @GetMapping("/get-file/{token}/{fileName:.+}")
    public ResponseEntity<StreamingResponseBody> getProtectedFile(@PathVariable("token") String token,
                                                                  @PathVariable("fileName") String fileName){
        boolean tokenExists = this.appRepository.getFileTokensRepository()
                .existsByToken(token);
        if(tokenExists) return this.loadAndReturnFile(fileName, ApplicationFileType.BACKUP);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "File Not Found");
    }
}
