package at.rihnet.rihnetlogistikmde;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ArticlePositionItem;
import at.rihnet.rihnetlogistikmde.models.SelectLine.BusinessPartnerDetails;
import at.rihnet.rihnetlogistikmde.models.SelectLine.CustomFields1;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreateModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentDetailAddress;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentJournalModelCreate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionReadModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionStoreInformation;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionUpdateModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPrintInformation;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;
import at.rihnet.rihnetlogistikmde.models.SelectLine.InventoryArticleEdit;
import at.rihnet.rihnetlogistikmde.models.SelectLine.JournalAttachmentCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.JournalCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorage;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorageCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorageUpdate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.PredecessorDocumentData;
import at.rihnet.rihnetlogistikmde.models.SelectLine.SuccessorsDocumentData;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Token;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
public class CommunicationSelectLine {
    private static final String TAG = "RIHNet";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static String BASE_ADDRESS;
    private static String ACCESS_TOKEN;
    private static final OkHttpClient CLIENT;

    static {
        try {
            CLIENT = buildTrustAllClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean login(String appKey, String baseAddress, String userName, String password) throws JSONException, NoSuchAlgorithmException, KeyManagementException {
        try {
            BASE_ADDRESS = baseAddress;
            OkHttpClient client = buildTrustAllClient();

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

                    //Token token = objectMapper.readValue(response.body().string(), Token.class);
                    //ACCESS_TOKEN = token.getAccessToken();

                    String responseBody = response.body().string();
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    if (jsonNode.has("AccessToken")) {
                        Token token = objectMapper.treeToValue(jsonNode, Token.class);
                        ACCESS_TOKEN = token.getAccessToken();
                        return true;
                    } else {
                        android.util.Log.e(TAG, "Response ist nicht vom Typ 'Token'!");
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ManualStorageCreated createManualStorage(String addressNumber, String standort, String lager) {
        try {
            OkHttpClient client = buildTrustAllClient();

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
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ManualStorageCreated createManualStorage(String standort) {
        try {
            OkHttpClient client = buildTrustAllClient();

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
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, String zielLager, int zielLagerplatzId, int menge) {
        return storePosition(manualStorageNumber, artikel, zielLager, zielLagerplatzId, (double) menge);
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, String zielLager, int zielLagerplatzId, double menge) {
        try {
            OkHttpClient client = buildTrustAllClient();

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
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, int menge) {
        return storePosition(manualStorageNumber, artikel, (double) menge);
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, double menge) {
        try {
            OkHttpClient client = buildTrustAllClient();

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
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateManualStorageAsync(String manualStorageNumber, String grund, String user) {
        try {
            OkHttpClient client = buildTrustAllClient();

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
            try (Response ignored = client.newCall(request).execute()) {

            } catch (Exception ex) {
                Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DocumentCreated createDocument(DocumentCreateModel documentCreateModel) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(documentCreateModel);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    String result = response.body().string();
                    return objectMapper.readValue(result, DocumentCreated.class);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DocumentCreated createDocumentSuccessor(String documentKey, SuccessorsDocumentData successorsDocumentData) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(successorsDocumentData);
            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents/" + documentKey + "/Successors")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    String result = response.body().string();
                    return objectMapper.readValue(result, DocumentCreated.class);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean belegUebernahme(String destinationDocumentKey, String sourceDocumentKey, PredecessorDocumentData predecessorDocumentData) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(predecessorDocumentData);
            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents/" + destinationDocumentKey + "/Predecessors/" + sourceDocumentKey)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .put(requestBody)
                    .build();
            try (Response ignored = client.newCall(request).execute()) {
                return true;
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DocumentPositionCreated createDocumentPositionWithArticleItemByDocumentKey(String documentKey, ArticlePositionItem articlePositionItem) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(articlePositionItem);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents/" + documentKey + "/ArticleItem")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    String result = response.body().string();
                    return objectMapper.readValue(result, DocumentPositionCreated.class);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Inventory> getInventories(String status, String standort) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();

            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Inventories?filter=Location EQ " + standort + " AND Status EQ " + status)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    String result = response.body().string();
                    return objectMapper.readValue(result, new TypeReference<>() {
                    });
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateInventoryRaiseArticleQuantity(String number, String warehouse, String article, InventoryArticleEdit inventoryArticleEdit) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(inventoryArticleEdit);
            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            //Log.e(TAG, "article: " + URLEncoder.encode(article, "UTF-8").replace("%", "~").replace("+", "~20"));
            Request request = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                request = new Request.Builder()
                        .url(BASE_ADDRESS + "Inventories/" + number + "/Warehouses/" + warehouse + "/Articles/" + URLEncoder.encode(article, StandardCharsets.UTF_8).replace("%", "~").replace("+", "~20"))
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                        .put(requestBody)
                        .build();
            }
            try {
                assert request != null;
                try (Response response = client.newCall(request).execute()) {
                    Log.e(TAG, "OK");
                    return response.isSuccessful();
                }
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void documentPrint(String documentKey, DocumentPrintInformation documentPrintInformation) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(documentPrintInformation);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents/" + documentKey + "/Print")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response ignored = client.newCall(request).execute()) {

            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<DocumentPositionReadModel> readDocumentPositionByDocumentKey(String documentKey) {
        try {
            OkHttpClient client = buildTrustAllClient();
            ObjectMapper objectMapper = new ObjectMapper();
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents/" + documentKey + "/Positions?Items=0")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    String result = response.body().string();
                    return objectMapper.readValue(result, new TypeReference<>() {
                    });
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JournalCreated addJournalToDocumentWithLink(String documentKey, DocumentJournalModelCreate documentJournalModelCreate) {
        try {
            OkHttpClient client = buildTrustAllClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(documentJournalModelCreate);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents/" + documentKey + "/Journals")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    objectMapper = new ObjectMapper();
                    String result = response.body().string();
                    return objectMapper.readValue(result, JournalCreated.class);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JournalAttachmentCreated addAttachment(String journalIdentifier, File file, String mimeType) {
        try {
            // ---------- 1. Trust-All-SSL-Kontext (wie in Ihren anderen Methoden) ----------
            OkHttpClient client = buildTrustAllClient();

            // ---------- 2. Multipart-Body aufbauen ----------
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            MediaType mediaType = MediaType.parse(mimeType);
            RequestBody fileBody = RequestBody.create(file, mediaType);
            bodyBuilder.addFormDataPart(
                    "Attachment",           // Name des Parts (frei wählbar)
                    file.getName(),                  // Content-Disposition FileName
                    fileBody                      // eigentlicher Daten-Body
            );
            RequestBody requestBody = bodyBuilder.build();

            // ---------- 3. Request bauen ----------
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Journals/" + journalIdentifier + "/Attachments")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .post(requestBody)
                    .build();

            // ---------- 4. Ausführen & Ergebnis deserialisieren ----------
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Fehler beim Upload – HTTP-Code: " + response.code());
                    return null;
                }
                ObjectMapper mapper = new ObjectMapper();
                String json = response.body().string();
                return mapper.readValue(json, JournalAttachmentCreated.class);
            }
        } catch (Exception ex) {
            Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }

    public static void updateDocumentPositionByDocumentKey(String documentKey, String documentPositionIdentifier, DocumentPositionUpdateModel documentPositionUpdateModel) {
        try {
            OkHttpClient client = buildTrustAllClient();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(documentPositionUpdateModel);

            RequestBody requestBody = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(BASE_ADDRESS + "Documents/" + documentKey + "/Positions/" + documentPositionIdentifier)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                    .put(requestBody)
                    .build();
            try (Response ignored = client.newCall(request).execute()) {

            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }

    public static void deleteDocumentPositionByDocumentKey(String documentKey, String documentPositionIdentifier) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("Positions")
                .addPathSegment(documentPositionIdentifier)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .delete()
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response " + response);
            }
        }
    }


    // Common
    private static OkHttpClient buildTrustAllClient() throws Exception {
        @SuppressLint("CustomX509TrustManager") TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new java.security.SecureRandom());
        return new OkHttpClient.Builder()
                .sslSocketFactory(ctx.getSocketFactory(), (X509TrustManager) trustAll[0])
                .hostnameVerifier((h, s) -> true)
                .build();
    }
}
*/

public class CommunicationSelectLine {

    private static final String TAG = "RIHNet";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static String BASE_ADDRESS;
    private static String ACCESS_TOKEN;

    private static final OkHttpClient CLIENT;

    static {
        try {
            CLIENT = buildTrustAllClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //   Login
    // ──────────────────────────────────────────────────────────────────────────────
    public static boolean login(String appKey, String baseAddress, String userName, String password) throws JSONException, NoSuchAlgorithmException, KeyManagementException {
        try {

//            Log.e(TAG, "baseAddress: " + baseAddress);
//            Log.e(TAG, "appKey: " + appKey);
//            Log.e(TAG, "userName: " + userName);
//            Log.e(TAG, "password: " + password);

            BASE_ADDRESS = baseAddress;
            //OkHttpClient client = buildTrustAllClient();

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
            try (Response response = CLIENT.newCall(request).execute()) {
                ObjectMapper objectMapper = new ObjectMapper();
                if (response.body() != null) {

                    //Token token = objectMapper.readValue(response.body().string(), Token.class);
                    //ACCESS_TOKEN = token.getAccessToken();

                    String responseBody = response.body().string();
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    if (jsonNode.has("AccessToken")) {
                        Token token = objectMapper.treeToValue(jsonNode, Token.class);
                        ACCESS_TOKEN = token.getAccessToken();
                        return true;
                    } else {
                            try {
                                String jsonString = objectMapper
                                        .writerWithDefaultPrettyPrinter()
                                        .writeValueAsString(jsonNode);
                                Log.e(TAG, jsonString);
                            } catch (JsonProcessingException e) {
                                Log.e(TAG, String.format("%s", e.getMessage()));
                            }

                        Log.e(TAG, "Response ist nicht vom Typ 'Token'!");
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    // ──────────────────────────────────────────────────────────────────────────────
    //   Manual Storage
    // ──────────────────────────────────────────────────────────────────────────────

    public static ManualStorageCreated createManualStorage(String standort) throws IOException {
        ManualStorage ms = new ManualStorage();
        ms.setWarehouseLocationNumber(standort);
        ms.setBusinessPartnerType("Customer");
        String payload = OBJECT_MAPPER.writeValueAsString(ms);

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("ManualStorages")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), ManualStorageCreated.class);
        }
    }

    public static ManualStorageCreated createManualStorage(String addressNumber, String standort, String lager) throws IOException {

        DocumentDetailAddress addr = new DocumentDetailAddress();
        addr.setNumber(addressNumber);
        BusinessPartnerDetails partner = new BusinessPartnerDetails();
        partner.setAddress(addr);

        ManualStorage ms = new ManualStorage();
        ms.setWarehouseNumber(lager);
        ms.setWarehouseLocationNumber(standort);
        ms.setBusinessPartnerType("Customer");
        ms.setBusinessPartner(partner);

        String payload = OBJECT_MAPPER.writeValueAsString(ms);

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("ManualStorages")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), ManualStorageCreated.class);
        }
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, int menge) throws IOException {
        return storePosition(manualStorageNumber, artikel, null, 0, (double) menge);
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, double menge) throws IOException {
        return storePosition(manualStorageNumber, artikel, null, 0, menge);
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, String zielLager, int zielLagerplatzId, int menge) throws IOException {
        return storePosition(manualStorageNumber, artikel, zielLager, zielLagerplatzId, (double) menge);
    }

    public static ManualStorageCreated storePosition(String manualStorageNumber, Artikel artikel, String zielLager, int zielLagerplatzId, double menge) throws IOException {
        DocumentPositionStoreInformation info = new DocumentPositionStoreInformation();
        info.setQuantity(menge);
        info.setArticleNumber(artikel.getArtikelnummer());
        info.setWarehouse(artikel.getLager());
        info.setStoragePlaceIdentifier(artikel.getLagerplatzId());
        if (zielLager != null) info.setTargetWarehouse(zielLager);
        if (zielLagerplatzId > 0) info.setTargetStoragePlaceIdentifier(zielLagerplatzId);
        if ("S".equals(artikel.getSerieCharge())) info.setSerialNumber(artikel.getSeriennummer());
        else if ("C".equals(artikel.getSerieCharge())) info.setSerialNumber(artikel.getCharge());

        String payload = OBJECT_MAPPER.writeValueAsString(List.of(info));

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("ManualStorages")
                .addPathSegment(manualStorageNumber)
                .addPathSegments("Positions/Store")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), ManualStorageCreated.class);
        }
    }

    public static void updateManualStorageAsync(String manualStorageNumber, String grund, String user) throws IOException {
        CustomFields1 cf = new CustomFields1();
        cf.setCustomText1(grund);
        cf.setCustomText2(user);
        ManualStorageUpdate upd = new ManualStorageUpdate();
        upd.setCustomFields(cf);

        String payload = OBJECT_MAPPER.writeValueAsString(upd);

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("ManualStorages")
                .addPathSegment(manualStorageNumber)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .put(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //   Documents
    // ──────────────────────────────────────────────────────────────────────────────

    public static DocumentCreated createDocument(DocumentCreateModel model) throws IOException {
        String payload = OBJECT_MAPPER.writeValueAsString(model);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), DocumentCreated.class);
        }
    }

    public static DocumentCreated createDocumentSuccessor(String documentKey, SuccessorsDocumentData data) throws IOException {
        String payload = OBJECT_MAPPER.writeValueAsString(data);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("Successors")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), DocumentCreated.class);
        }
    }

    public static boolean belegUebernahme(String destinationKey, String sourceKey, PredecessorDocumentData data) throws IOException {
        String payload = OBJECT_MAPPER.writeValueAsString(data);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(destinationKey)
                .addPathSegment("Predecessors")
                .addPathSegment(sourceKey)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .put(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
            return true;
        }
    }

    public static DocumentPositionCreated createDocumentPositionWithArticleItemByDocumentKey(String documentKey, ArticlePositionItem item) throws IOException {
        String payload = OBJECT_MAPPER.writeValueAsString(item);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("ArticleItem")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), DocumentPositionCreated.class);
        }
    }

    public static void documentPrint(String documentKey, DocumentPrintInformation info) throws IOException {
        String payload = OBJECT_MAPPER.writeValueAsString(info);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("Print")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //   Document‑Positions
    // ──────────────────────────────────────────────────────────────────────────────

    public static List<DocumentPositionReadModel> readDocumentPositionByDocumentKey(String documentKey) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("Positions")
                .addQueryParameter("Items", "0")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .get()
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), new TypeReference<>() {
            });
        }
    }

    public static void updateDocumentPositionByDocumentKey(String documentKey, String positionId, DocumentPositionUpdateModel model) throws IOException {
        String payload = OBJECT_MAPPER.writeValueAsString(model);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("Positions")
                .addPathSegment(positionId)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .put(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
        }
    }

    public static void deleteDocumentPositionByDocumentKey(String documentKey, String positionId) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("Positions")
                .addPathSegment(positionId)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .delete()
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //   Journals
    // ──────────────────────────────────────────────────────────────────────────────

    public static JournalCreated addJournalToDocumentWithLink(String documentKey, DocumentJournalModelCreate model) throws IOException {
        String payload = OBJECT_MAPPER.writeValueAsString(model);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Documents")
                .addPathSegment(documentKey)
                .addPathSegment("Journals")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), JournalCreated.class);
        }
    }

    public static JournalAttachmentCreated addAttachment(String journalId, File file, String mimeType) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Journals")
                .addPathSegment(journalId)
                .addPathSegment("Attachments")
                .build();
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Attachment", file.getName(), RequestBody.create(file, MediaType.parse(mimeType)))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .post(body)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), JournalAttachmentCreated.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //   Inventory
    // ──────────────────────────────────────────────────────────────────────────────

    public static List<Inventory> getInventories(String status, String standort) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Inventories")
                .addQueryParameter("filter", "Location EQ " + standort + " AND Status EQ " + status)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .get()
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response " + response);
            return OBJECT_MAPPER.readValue(response.body().string(), new TypeReference<>() {
            });
        }
    }

    public static boolean updateInventoryRaiseArticleQuantity(String number, String warehouse, String article, InventoryArticleEdit edit) throws IOException {
        String encodedArticle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            encodedArticle = URLEncoder.encode(article, StandardCharsets.UTF_8)
                    .replace("%", "~")
                    .replace("+", "~20");
        } else {
            encodedArticle = URLEncoder.encode(article, "UTF-8")
                    .replace("%", "~")
                    .replace("+", "~20");
        }

        String payload = OBJECT_MAPPER.writeValueAsString(edit);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE_ADDRESS))
                .newBuilder()
                .addPathSegment("Inventories")
                .addPathSegment(number)
                .addPathSegment("Warehouses")
                .addPathSegment(warehouse)
                .addPathSegment("Articles")
                .addPathSegment(encodedArticle)
                //.addEncodedPathSegment(encodedArticle)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "LoginId " + ACCESS_TOKEN)
                .put(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("RIHNet", "updateInventoryRaiseArticleQuantity failed: " + response);
                return false;                    // nicht crashen, sondern false zurückgeben
            }
            return true;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    //   SSL Helper
    // ──────────────────────────────────────────────────────────────────────────────

    private static OkHttpClient buildTrustAllClient() throws NoSuchAlgorithmException, KeyManagementException {
        @SuppressLint("CustomX509TrustManager") TrustManager[] trusts = new TrustManager[]{
                new X509TrustManager() {
                    @SuppressLint("TrustAllX509TrustManager")
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trusts, new java.security.SecureRandom());
        return new OkHttpClient.Builder()
                .sslSocketFactory(ctx.getSocketFactory(), (X509TrustManager) trusts[0])
                .hostnameVerifier((h, s) -> true)
                //.protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
}


