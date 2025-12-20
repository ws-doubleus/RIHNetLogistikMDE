package at.rihnet.rihnetlogistikmde.ui.paketlabel.beleg;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.R;

public class CameraActivity extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSIONS =
            Build.VERSION.SDK_INT >= 29
                    ? new String[]{ Manifest.permission.CAMERA }
                    : new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    //private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private MediaActionSound shutterSound;

    public static final String EXTRA_DOCUMENT_KEY = "documentKey";
    public static final String EXTRA_PHOTO_PATHS = "photoPaths";

    private final ArrayList<String> capturedPaths = new ArrayList<>();
    private String documentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        documentKey = getIntent().getStringExtra(EXTRA_DOCUMENT_KEY);

        previewView   = findViewById(R.id.pv_camera);
        FloatingActionButton shutter = findViewById(R.id.fab_camera);
        shutterSound = new MediaActionSound();
        shutterSound.load(MediaActionSound.SHUTTER_CLICK);

        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        shutter.setOnClickListener(v -> {
            shutterSound.play(MediaActionSound.SHUTTER_CLICK);
            takePhoto();
        });
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shutterSound != null) shutterSound.release();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] perms, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, perms, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS && hasCameraPermission()) {
            startCamera();
        } else {
            Toast.makeText(this, "Kamera-Berechtigung benötigt!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean hasCameraPermission() {
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;                    // mindestens eins fehlt
            }
        }
        return true;
    }

    // Für API 29+  (Scoped Storage)
    private ImageCapture.OutputFileOptions buildOutputOptions29(String fileName) {
        ContentValues v = new ContentValues();
        v.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName.replace(".jpg", ""));
        v.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "RIHNet");

        // Absoluten Pfad für Ergebnisliste berechnen
        File abs = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "RIHNet/" + fileName);
        capturedPaths.add(abs.getAbsolutePath());

        return new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v).build();
    }

    // Für API ≤ 28 (Legacy)
    /** @noinspection ResultOfMethodCallIgnored*/
    private ImageCapture.OutputFileOptions buildOutputOptionsLegacy(String fileName) {
        Log.e("RIHNet", "buildOutputOptionsLegacy()");
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/RIHNet");
        if (!dir.exists()) dir.mkdirs();
        File photo = new File(dir, fileName);
        capturedPaths.add(photo.getAbsolutePath());
        return new ImageCapture.OutputFileOptions.Builder(photo).build();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Camera-Initialisierung fehlgeschlagen", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture  == null) return;
        String fileName = documentKey + "_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg";

        ImageCapture.OutputFileOptions opts =
                (Build.VERSION.SDK_INT >= 29)
                        ? buildOutputOptions29(fileName)
                        : buildOutputOptionsLegacy(fileName);

        imageCapture.takePicture(
                opts,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults result) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Bild gespeichert!", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        runOnUiThread(() -> {
                            Log.e("RIHNet", "buildOutputOptionsLegacy(): " + exc.getMessage());
                            Toast.makeText(getApplicationContext(), "Fehler: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void finishWithResult() {
        Intent result = new Intent().putStringArrayListExtra(EXTRA_PHOTO_PATHS, capturedPaths);
        setResult(RESULT_OK, result);
        finish();
    }
}