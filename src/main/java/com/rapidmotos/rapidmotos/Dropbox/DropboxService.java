package com.rapidmotos.rapidmotos.Dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.rapidmotos.rapidmotos.Dropbox.Dto.Response.TokenRefreshResponse;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DropboxService {

    private DbxClientV2 dropboxClient;

    private String accessToken=System.getenv("access_token");

    private String refreshToken=System.getenv("refresh_token");

    private String appKey=System.getenv("app_key");

    private String appSecret=System.getenv("app_secret");

    private static final String TOKEN_URL = "https://api.dropboxapi.com/oauth2/token";

    public DropboxService() {
        initializeDropboxClient();
    }

    private void initializeDropboxClient() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/spring-boot-app").build();
        this.dropboxClient = new DbxClientV2(config, accessToken);
    }

    private void refreshAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("client_id", appKey);
        body.add("client_secret", appSecret);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenRefreshResponse> responseEntity = restTemplate.postForEntity(TOKEN_URL, requestEntity, TokenRefreshResponse.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                TokenRefreshResponse response = responseEntity.getBody();

                if (response != null && response.access_token() != null) {
                    accessToken = response.access_token();
                    initializeDropboxClient();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {

        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String folderPath = "/" + authentication.getName() + "/" + currentDate.getYear() + "/" + currentDate.format(monthFormatter) + "/" + currentDate.getDayOfMonth() + "/";
        String fileName = currentDate.format(DateTimeFormatter.ISO_DATE_TIME) + ".jpg";

        try (InputStream in = file.getInputStream()) {
            FileMetadata metadata = dropboxClient.files().uploadBuilder(folderPath + fileName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(in);

            return dropboxClient.sharing().createSharedLinkWithSettings(metadata.getPathLower()).getUrl();

        } catch (CreateSharedLinkWithSettingsErrorException e) {
            throw new RuntimeException(e);
        } catch (UploadErrorException e) {
            throw new RuntimeException(e);
        } catch (InvalidAccessTokenException e) {
            refreshAccessToken();
            uploadFile(file);
        }
        catch (DbxException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}