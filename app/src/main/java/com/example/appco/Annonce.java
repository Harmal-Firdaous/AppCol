package com.example.appco;

public class Annonce {
    private String id;
    private String imageBase64;
    private String imageUrl;
    private String titre;
    private String description;
    private String annotation;
    private float rating;
    private double latitude;
    private double longitude;
    private String ownerName;
    private String ownerEmail;
    private String ownerId; // Added ownerId field
    private long timestamp;

    // Empty constructor needed for Firestore
    public Annonce() {
    }

    // Constructor for Firestore data - updated to include ownerName and ownerEmail
    public Annonce(String id, String imageBase64, String imageUrl, String titre, String description,
                   float rating, double latitude, double longitude, long timestamp,
                   String ownerId, String ownerName, String ownerEmail) {
        this.id = id;
        this.imageBase64 = imageBase64;
        this.imageUrl = imageUrl;
        this.titre = titre;
        this.description = description;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}