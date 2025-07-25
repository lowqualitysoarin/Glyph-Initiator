package com.lowqualitysoarin.glyphinitiator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lowqualitysoarin.glyphinitiator.entry.OggEntry;
import com.lowqualitysoarin.glyphinitiator.services.ApplicationInitiatorService;
import com.lowqualitysoarin.glyphinitiator.utils.OggEntryAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OggEntryAdapter.OnItemInteractionListener {

    private static final String PREFS_NAME = "OggEntriesPrefs";
    private static final String KEY_OGG_ENTRIES = "ogg_entries_list";

    private OggEntryAdapter adapter;
    private ArrayList<OggEntry> oggEntriesList;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent appInitiatorService = new Intent(this, ApplicationInitiatorService.class);
        startService(appInitiatorService);

        setContentView(R.layout.activity_main);
        Button chooseFileButton = findViewById(R.id.button_choose_file);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_ogg_entries);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();
        oggEntriesList = loadEntries();

        adapter = new OggEntryAdapter(oggEntriesList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedFileUri = data.getData();
                            getContentResolver().takePersistableUriPermission(selectedFileUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            promptForName(selectedFileUri);
                        }
                    }
                });

        chooseFileButton.setOnClickListener(v -> openFilePicker());
        
        requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d("Glyph Initiator", "Notification permission granted.");
            } else {
                Log.e("Glyph Initiator", "Notification permission denied.");
            }
        });
        askNotificationPermissions();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/ogg");
        filePickerLauncher.launch(intent);
    }

    private void promptForName(final Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name for OGG File");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                addOggEntry(name, fileUri.toString());
            } else {
                Toast.makeText(MainActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addOggEntry(String name, String uriString) {
        OggEntry newEntry = new OggEntry(name, uriString);
        oggEntriesList.add(newEntry);
        adapter.notifyItemInserted(oggEntriesList.size() - 1);
        saveEntries();
    }

    private void saveEntries() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(oggEntriesList);
        editor.putString(KEY_OGG_ENTRIES, json);
        editor.apply();
    }

    private ArrayList<OggEntry> loadEntries() {
        String json = sharedPreferences.getString(KEY_OGG_ENTRIES, null);
        Type type = new TypeToken<ArrayList<OggEntry>>() {}.getType();
        ArrayList<OggEntry> loadedList = gson.fromJson(json, type);
        return (loadedList != null) ? loadedList : new ArrayList<>();
    }

    @Override
    public void onDeleteClicked(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete '" + oggEntriesList.get(position).getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    oggEntriesList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, oggEntriesList.size());
                    saveEntries();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRenameClicked(int position) {
        OggEntry entryToRename = oggEntriesList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Entry");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(entryToRename.getName()); // Pre-fill with current name
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(entryToRename.getName())) {
                entryToRename.setName(newName);
                adapter.notifyItemChanged(position);
                saveEntries();
            } else if (newName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onItemClicked(int position) {
        OggEntry entry = oggEntriesList.get(position);
        Toast.makeText(this, "Playing: " + entry.getName(), Toast.LENGTH_SHORT).show();
    }

    private void askNotificationPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            Toast.makeText(this, "Notification permission is required to keep the app running in the background.", Toast.LENGTH_LONG).show();
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}