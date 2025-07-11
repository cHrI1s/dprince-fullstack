package com.dprince.configuration.database.services;

import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.entities.FileToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class DatabaseBackupService {
    private final DataSource dataSource;
    private final static String DATABASE_NAME = "dprince_db";

    private String path = null;
    public void setPath(LocalFileStorageService storageService) {
        this.path = storageService.getFileConfig().getProjectPath()
                + storageService.getFileConfig().getBackupDir();
    }

    public DatabaseBackupService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private String getBackupCode(String databaseName){
        return "BACKUP DATABASE ["+databaseName+"] " +
                "TO  DISK = '"+this.path+databaseName+".bak' WITH NOFORMAT," +
                "NOINIT, NAME = N'"+databaseName+"-Full Database Backup', " +
                "SKIP, NOREWIND, NOUNLOAD, STATS = 10;";
    }

    public FileToken backupDatabase(@NonNull LocalFileStorageService storageService,
                                    @Nullable String databaseName) {
        FileToken fileToken = null;
        if(StringUtils.isEmpty(databaseName)) databaseName = DATABASE_NAME;
        this.setPath(storageService);
        String sql = this.getBackupCode(databaseName);
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            Path path = storageService.getBackupPath().resolve(databaseName+".bak").normalize();
            Resource resource = new UrlResource(path.toUri());
            if(resource.exists()) resource.getFile().delete();
            statement.execute(sql);
            fileToken = new FileToken();
            fileToken.generateToken();

            fileToken.setFileName(databaseName+".bak");
            if(resource.exists()) fileToken.addAttachment(fileToken.getFileName(), resource.getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileToken;
    }


    public void backupInstitution(Long institutionId){

    }
}