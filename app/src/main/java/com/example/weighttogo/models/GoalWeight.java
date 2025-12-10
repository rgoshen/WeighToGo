package com.example.weighttogo.models;

/**
 * Model class representing a weight goal.
 * Corresponds to the goal_weights table in the database.
 */
public class GoalWeight {

    private long goalId;
    private long userId;
    private double goalWeight;
    private String goalUnit;
    private double startWeight;
    private String targetDate;
    private int isAchieved;
    private String achievedDate;
    private String createdAt;
    private String updatedAt;
    private int isActive;

    /**
     * Default constructor.
     */
    public GoalWeight() {
    }

    public long getGoalId() {
        return goalId;
    }

    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public void setGoalWeight(double goalWeight) {
        this.goalWeight = goalWeight;
    }

    public String getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(String goalUnit) {
        this.goalUnit = goalUnit;
    }

    public double getStartWeight() {
        return startWeight;
    }

    public void setStartWeight(double startWeight) {
        this.startWeight = startWeight;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public int getIsAchieved() {
        return isAchieved;
    }

    public void setIsAchieved(int isAchieved) {
        this.isAchieved = isAchieved;
    }

    public String getAchievedDate() {
        return achievedDate;
    }

    public void setAchievedDate(String achievedDate) {
        this.achievedDate = achievedDate;
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

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "GoalWeight{" +
                "goalId=" + goalId +
                ", userId=" + userId +
                ", goalWeight=" + goalWeight +
                ", goalUnit='" + goalUnit + '\'' +
                ", startWeight=" + startWeight +
                ", targetDate='" + targetDate + '\'' +
                ", isAchieved=" + isAchieved +
                ", achievedDate='" + achievedDate + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
