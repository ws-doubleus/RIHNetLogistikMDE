package at.rihnet.rihnetlogistikmde;

import android.annotation.SuppressLint;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.BusinessPartnerDetails;
import at.rihnet.rihnetlogistikmde.models.SelectLine.CustomFields1;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentDetailAddress;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionStoreInformation;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorage;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorageCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorageUpdate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Token;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommunicationSelectLine {
    private static final String TAG = "RIHNet";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static String BASE_ADDRESS;
    private static String ACCESS_TOKEN;


    public static boolean login(String appKey, String baseAddress, String userName, String password) throws JSONException, NoSuchAlgorithmException, KeyManagementException {
        BASE_ADDRESS = baseAddress;
        @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCertificates[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();

        JSONObject credentialsJson = new JSONObject();
        credentialsJson.put("UserName", userName);
        credentialsJson.put("Password", password);
        credentialsJson.put("AppKey", appKey);

        RequestBody requestBody = RequestBody.create(credentialsJson.toString(), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(BASE_ADDRESS + "Login")
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            ObjectMapper objectMapper = new ObjectMapper();
            if (response.body() != null) {
                Token token = objectMapper.readValue(response.body().string(), Token.class);
                ACCESS_TOKEN = token.getAccessToken();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ManualStorageCreated createManualStorage(String addressNumber, String standort, String lager) {
        try {
            @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

            DocumentDetailAddress documentDetailAddress = new DocumentDetailAddress();
            documentDetailAddress.setNumber(addressNumber);
            BusinessPartnerDetails businessPartnerDetails = new BusinessPartnerDetails();
            businessPartnerDetails.setAddress(documentDetailAddress);
            ManualStorage manualStorage = new ManualStorage();
            manualStorage.setWarehouseNumber(lager);
            manualStorage.setWarehouseLocationNumber(standort);
            manualStorage.setBusinessPartnerType("Customer");
            manualStorage.setBusinessPartner(businessPartnerDetails);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(manualStorage);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "ManualStorages")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    String result = response.body().string();
                    Log.i(TAG, "ManualStorageNumber: " + result);
                    return objectMapper.readValue(result, ManualStorageCreated.class);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ManualStorageCreated createManualStorage(String standort) {
        try {
            @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            ManualStorage manualStorage = new ManualStorage();
            manualStorage.setWarehouseLocationNumber(standort);
            manualStorage.setBusinessPartnerType("Customer");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(manualStorage);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "ManualStorages")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    String result = response.body().string();
                    Log.i(TAG, "ManualStorageNumber: " + result);
                    return objectMapper.readValue(result, ManualStorageCreated.class);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, String zielLager, int zielLagerplatzId, int menge) {
        try {
            @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

//            Log.e(TAG, "menge: " + menge);
//            Log.e(TAG, "artikel.getArtikelnummer(): " + artikel.getArtikelnummer());
//            Log.e(TAG, "artikel.getLager(): " + artikel.getLager());
//            Log.e(TAG, "zielLager: " + zielLager);
//            Log.e(TAG, "artikel.getLagerplatzId(): " + artikel.getLagerplatzId());
//            Log.e(TAG, "zielLagerplatzId: " + zielLagerplatzId);

            List<DocumentPositionStoreInformation> documentPositionStoreInformations = new ArrayList<>();
            DocumentPositionStoreInformation documentPositionStoreInformation = new DocumentPositionStoreInformation();
            documentPositionStoreInformation.setQuantity(menge);
            documentPositionStoreInformation.setArticleNumber(artikel.getArtikelnummer());
            documentPositionStoreInformation.setWarehouse(artikel.getLager());
            documentPositionStoreInformation.setTargetWarehouse(zielLager);
            documentPositionStoreInformation.setStoragePlaceIdentifier(artikel.getLagerplatzId());
            documentPositionStoreInformation.setTargetStoragePlaceIdentifier(zielLagerplatzId);
            if (artikel.getSerieCharge().equals("S")) {
                documentPositionStoreInformation.setSerialNumber(artikel.getSeriennummer());
            } else if (artikel.getSerieCharge().equals("C")) {
                documentPositionStoreInformation.setSerialNumber(artikel.getCharge());
            }
            documentPositionStoreInformations.add(documentPositionStoreInformation);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(documentPositionStoreInformations);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "ManualStorages/" + manualStorageNumber + "/Positions/Store")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    return objectMapper.readValue(response.body().string(), ManualStorageCreated.class);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, int menge) {
        try {
            @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

            List<DocumentPositionStoreInformation> documentPositionStoreInformations = new ArrayList<>();
            DocumentPositionStoreInformation documentPositionStoreInformation = new DocumentPositionStoreInformation();
            documentPositionStoreInformation.setQuantity(menge);
            documentPositionStoreInformation.setArticleNumber(artikel.getArtikelnummer());
            documentPositionStoreInformation.setWarehouse(artikel.getLager());
            documentPositionStoreInformation.setStoragePlaceIdentifier(artikel.getLagerplatzId());
            if (artikel.getSerieCharge().equals("S")) {
                documentPositionStoreInformation.setSerialNumber(artikel.getSeriennummer());
            } else if (artikel.getSerieCharge().equals("C")) {
                documentPositionStoreInformation.setSerialNumber(artikel.getCharge());
            }
            documentPositionStoreInformations.add(documentPositionStoreInformation);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(documentPositionStoreInformations);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "ManualStorages/" + manualStorageNumber + "/Positions/Store")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    return objectMapper.readValue(response.body().string(), ManualStorageCreated.class);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateManualStorageAsync(String manualStorageNumber, String grund, String user) {
        try {
            @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

            ManualStorageUpdate manualStorageUpdate = new ManualStorageUpdate();
            CustomFields1 customFields1 = new CustomFields1();
            customFields1.setCustomText1(grund);
            customFields1.setCustomText2(user);
            manualStorageUpdate.setCustomFields(customFields1);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(manualStorageUpdate);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "ManualStorages/" + manualStorageNumber)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .put(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
