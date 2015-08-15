package com.koolew.mars.notification;

/**
 * Created by jinchangzhu on 8/15/15.
 */
public class NotificationKeeper {
    private static int feeds;
    private static int assignment;
    private static int suggestion;
    private static int comment;
    private static int me;


    public static void set(NotificationEvent notification) {
        feeds = notification.getFeeds();
        assignment = notification.getAssignment();
        suggestion = notification.getSuggestion();
        comment = notification.getComment();
        me = notification.getMe();
    }

    public static int getAssignment() {
        return assignment;
    }

    public static void setAssignment(int assignment) {
        NotificationKeeper.assignment = assignment;
    }

    public static int getComment() {
        return comment;
    }

    public static void setComment(int comment) {
        NotificationKeeper.comment = comment;
    }

    public static int getFeeds() {
        return feeds;
    }

    public static void setFeeds(int feeds) {
        NotificationKeeper.feeds = feeds;
    }

    public static int getMe() {
        return me;
    }

    public static void setMe(int me) {
        NotificationKeeper.me = me;
    }

    public static int getSuggestion() {
        return suggestion;
    }

    public static void setSuggestion(int suggestion) {
        NotificationKeeper.suggestion = suggestion;
    }
}
