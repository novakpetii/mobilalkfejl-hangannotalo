package com.example.myapplication;

public class AudioFile {

    private String id;
    private String fileName;
    private String filePath;
    private String tag;
    private long size;
    private int duration;
    private String fileType;
    private String ownerId;  // feltolto felhasznalo uid-je

    public AudioFile(String id, String fileName, String filePath, String tag, long size, int duration, String fileType, String ownerId) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.tag = tag;
        this.size = size;
        this.duration = duration;
        this.fileType = fileType;
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTag() {
        return tag;
    }

    public long getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }

    public String getFileType() {
        return fileType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
