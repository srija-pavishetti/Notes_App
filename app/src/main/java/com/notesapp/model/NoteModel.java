package com.notesapp.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class NoteModel implements Serializable {
    private String noteDesc;
    private String noteTitle;
    private String noteImage;
    private boolean completed;
    private Timestamp created;
    private String userId;

    public NoteModel() {
    }

    public NoteModel(String noteDesc, String noteTitle, String noteImage, boolean completed, Timestamp created, String userId) {
        this.noteDesc = noteDesc;
        this.noteTitle = noteTitle;
        this.noteImage = noteImage;
        this.completed = completed;
        this.created = created;
        this.userId = userId;
    }

    public String getNoteDesc() {
        return noteDesc;
    }

    public void setNoteDesc(String noteDesc) {
        this.noteDesc = noteDesc;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteImage() {
        return noteImage;
    }

    public void setNoteImage(String noteImage) {
        this.noteImage = noteImage;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "NoteModel{" +
                "noteDesc='" + noteDesc + '\'' +
                ", noteTitle='" + noteTitle + '\'' +
                ", noteImage='" + noteImage + '\'' +
                ", completed=" + completed +
                ", created=" + created +
                ", userId='" + userId + '\'' +
                '}';
    }
}
