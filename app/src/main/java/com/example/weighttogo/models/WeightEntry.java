package com.example.weighttogo.models;

/**
 * Model class representing a daily weight entry.
 * Corresponds to the daily_weights table in the database.
 */
public class WeightEntry {

    private long weightId;
    private long userId;
    private double weightValue;
    private String weightUnit;
    private String weightDate;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private int isDeleted;

    /**
     * Default constructor.
     */
    public WeightEntry() {
    }

    public long getWeightId() {
        return weightId;
    }

    public void setWeightId(long weightId) {
        this.weightId = weightId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getWeightValue() {
        return weightValue;
    }

    public void setWeightValue(double weightValue) {
        this.weightValue = weightValue;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getWeightDate() {
        return weightDate;
    }

    public void setWeightDate(String weightDate) {
        this.weightDate = weightDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "WeightEntry{" +
                "weightId=" + weightId +
                ", userId=" + userId +
                ", weightValue=" + weightValue +
                ", weightUnit='" + weightUnit + '\'' +
                ", weightDate='" + weightDate + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
