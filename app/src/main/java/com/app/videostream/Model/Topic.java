package com.app.videostream.Model;

public class Topic {

    String id;
    String thumbnailUrl;
    String title;
    String description;
    String date;
    String videoUrl;
    String duration;

    public Topic(String id, String thumbnailUrl, String title, String description, String date, String videoUrl , String duration) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.description = description;
        this.date = date;
        this.videoUrl = videoUrl;
        this.duration=duration;
    }

    public Topic(){}

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
