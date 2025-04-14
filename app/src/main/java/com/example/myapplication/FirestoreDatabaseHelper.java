package com.example.myapplication;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreDatabaseHelper {
    private FirebaseFirestore firestore;
    private CollectionReference audioFilesRef;

    public FirestoreDatabaseHelper() {
        firestore = FirebaseFirestore.getInstance();
        audioFilesRef = firestore.collection("audio_files");
    }


    private String normalizeFileType(String type) {
        if (type == null) return "unknown";
        type = type.toLowerCase();
        if (type.contains("mpeg")) {
            return "mp3";
        } else if (type.contains("wav")) {
            return "wav";
        }
        return type;
    }

    // uj audio file letrehozasa
    public void insertAudioFile(AudioFile file, OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileName", file.getFileName());
        data.put("filePath", file.getFilePath());
        data.put("tag", file.getTag());
        data.put("size", file.getSize());
        data.put("duration", file.getDuration());
        data.put("fileType", normalizeFileType(file.getFileType()));
        data.put("ownerId", file.getOwnerId());

        audioFilesRef.add(data)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // adott felhasznalokhoz tartozo fileok lekerdezese
    public void getUserAudioFiles(final String userId, final FirestoreCallback callback) {
        audioFilesRef.whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AudioFile> audioFiles = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String id = doc.getId();
                        String fileName = doc.getString("fileName");
                        String filePath = doc.getString("filePath");
                        String tag = doc.getString("tag");
                        Long size = doc.getLong("size");
                        Long durationLong = doc.getLong("duration");
                        String fileType = normalizeFileType(doc.getString("fileType"));
                        String ownerId = doc.getString("ownerId");

                        int duration = (durationLong != null) ? durationLong.intValue() : 0;
                        long fileSize = (size != null) ? size : 0;

                        audioFiles.add(new AudioFile(id, fileName, filePath, tag, fileSize, duration, fileType, ownerId));
                    }
                    callback.onCallback(audioFiles);
                })
                .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }

    public void getUserAudioFilesFiltered(String userId, Long minSize, Long maxSize, List<String> fileTypes, Long minDuration, Long maxDuration, final FirestoreCallback callback) {
        Query query = audioFilesRef.whereEqualTo("ownerId", userId);
        boolean sizeFilter = (minSize != null || maxSize != null);
        boolean durationFilter = (minDuration != null || maxDuration != null);

        if (sizeFilter) {
            if (minSize != null) {
                query = query.whereGreaterThanOrEqualTo("size", minSize);
            }
            if (maxSize != null) {
                query = query.whereLessThanOrEqualTo("size", maxSize);
            }
            query = query.orderBy("size");  // remote rendez meret szerint
        } else if (durationFilter) {
            if (minDuration != null) {
                query = query.whereGreaterThanOrEqualTo("duration", minDuration);
            }
            if (maxDuration != null) {
                query = query.whereLessThanOrEqualTo("duration", maxDuration);
            }
            query = query.orderBy("duration");  // remote hossz szerinti rendezes
        }
        if (fileTypes != null && !fileTypes.isEmpty()) {

            List<String> convertedTypes = new ArrayList<>();
            for (String type : fileTypes) {
                convertedTypes.add(normalizeFileType(type));
            }
            query = query.whereIn("fileType", convertedTypes);
        }
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<AudioFile> audioFiles = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                String id = doc.getId();
                String fileName = doc.getString("fileName");
                String filePath = doc.getString("filePath");
                String tag = doc.getString("tag");
                Long size = doc.getLong("size");
                Long durationLong = doc.getLong("duration");
                String fileType = normalizeFileType(doc.getString("fileType"));
                String ownerId = doc.getString("ownerId");

                int duration = (durationLong != null) ? durationLong.intValue() : 0;
                long fileSize = (size != null) ? size : 0;

                audioFiles.add(new AudioFile(id, fileName, filePath, tag, fileSize, duration, fileType, ownerId));
            }
            // lokalis szures
            if (sizeFilter && durationFilter) {
                List<AudioFile> filtered = new ArrayList<>();
                for (AudioFile file : audioFiles) {
                    if (minDuration != null && file.getDuration() < minDuration) continue;
                    if (maxDuration != null && file.getDuration() > maxDuration) continue;
                    filtered.add(file);
                }
                audioFiles = filtered;
            }
            callback.onCallback(audioFiles);
        }).addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }

    // cimke update
    public void updateTag(String fileId, String newTag, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        audioFilesRef.document(fileId)
                .update("tag", newTag)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // file torles
    public void deleteFile(String fileId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        audioFilesRef.document(fileId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
