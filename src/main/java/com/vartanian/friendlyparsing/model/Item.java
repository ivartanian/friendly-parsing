package com.vartanian.friendlyparsing.model;

/**
 * Created by super on 9/21/15.
 */
public class Item {

    private String url;
    private String score;
    private String pass;

    public Item(String url, String score, String pass) {
        this.url = url;
        this.score = score;
        this.pass = pass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return !(url != null ? !url.equals(item.url) : item.url != null);

    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("url=");
        sb.append(url);
        sb.append(", score='").append(score).append('\'');
        sb.append(", pass='").append(pass).append('\'');
        return sb.toString();
    }
}
