package com.example.raid_bot_2.oauth;

public class Tweet {

    private String[] edit_history_tweet_ids;
    private String id;
    private PublicMetrics public_metrics;
    private String text;

    public String[] getEdit_history_tweet_ids() {
        return edit_history_tweet_ids;
    }

    public void setEdit_history_tweet_ids(String[] edit_history_tweet_ids) {
        this.edit_history_tweet_ids = edit_history_tweet_ids;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicMetrics getPublic_metrics() {
        return public_metrics;
    }

    public void setPublic_metrics(PublicMetrics public_metrics) {
        this.public_metrics = public_metrics;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Tweet() {
    }

    public Tweet(String[] edit_history_tweet_ids, String id, PublicMetrics public_metrics, String text) {
        this.edit_history_tweet_ids = edit_history_tweet_ids;
        this.id = id;
        this.public_metrics = public_metrics;
        this.text = text;
    }
}
