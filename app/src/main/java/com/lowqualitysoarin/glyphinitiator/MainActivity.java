package com.lowqualitysoarin.glyphinitiator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson; // For easy serialization/deserialization of list
import com.google.gson.reflect.TypeToken;
import com.lowqualitysoarin.glyphinitiator.entry.OggEntry;
import com.lowqualitysoarin.glyphinitiator.utils.OggEntryAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OggEntryAdapter.OnItemInteractionListener {

    private static final String PREFS_NAME = "OggEntriesPrefs";
    private static final String KEY_OGG_ENTRIES = "ogg_entries_list";

    private Button chooseFileButton;
    private RecyclerView recyclerView;
    private OggEntryAdapter adapter;
    private ArrayList<OggEntry> oggEntriesList;
    private SharedPreferences sharedPreferences;
    private Gson gson; // For serializing the list to JSON

    // Modern way to handle activity results
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure you have activity_main.xml

        chooseFileButton = findViewById(R.id.button_choose_file);
        recyclerView = findViewById(R.id.recycler_view_ogg_entries);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();
        oggEntriesList = loadEntries(); // Load saved entries

        adapter = new OggEntryAdapter(oggEntriesList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize the ActivityResultLauncher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedFileUri = data.getData();
                            // Persist permission to access the URI across device restarts
                            getContentResolver().takePersistableUriPermission(selectedFileUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            promptForName(selectedFileUri);
                        }
                    }
                });

        chooseFileButton.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // More robust for persistent access
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/ogg"); // Filter for .ogg files
        // Optionally, if you want to allow picking from local storage only:
        // intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
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

    // --- OggEntryAdapter.OnItemInteractionListener Callbacks ---
    @Override
    public void onDeleteClicked(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete '" + oggEntriesList.get(position).getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Before removing, revoke persistable URI permission if this is the last reference
                    // This is a bit more complex as you need to check if other entries use the same URI
                    // For simplicity here, we are not revoking. In a real app, manage this carefully.
                    // Uri uriToRevoke = oggEntriesList.get(position).getUri();
                    // getContentResolver().releasePersistableUriPermission(uriToRevoke, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    oggEntriesList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, oggEntriesList.size()); // Update subsequent positions
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
        // Handle item click, e.g., play the OGG file
        OggEntry entry = oggEntriesList.get(position);
        Toast.makeText(this, "Playing: " + entry.getName(), Toast.LENGTH_SHORT).show();
        // You would typically use MediaPlayer here to play the audio
        // Example:
        // MediaPlayer mediaPlayer = MediaPlayer.create(this, entry.getUri());
        // mediaPlayer.start();
        // Remember to handle mediaPlayer.release() when done.
    }
}