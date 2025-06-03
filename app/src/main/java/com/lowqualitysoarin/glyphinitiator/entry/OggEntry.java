package com.lowqualitysoarin.glyphinitiator.entry;

import android.net.Uri;

public class OggEntry {
    private String name;
    private String uriString; // Store URI as String for SharedPreferences

    public OggEntry(String name, String uriString) {
        this.name = name;
        this.uriString = uriString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUriString() {
        return uriString;
    }

    public Uri getUri() {
        return Uri.parse(uriString); // Convert back to Uri when needed
    }

    // It's good practice to override equals() and hashCode() if you're managing lists
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OggEntry oggEntry = (OggEntry) o;
        return name.equals(oggEntry.name) && uriString.equals(oggEntry.uriString);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + uriString.hashCode();
        return result;
    }
}
