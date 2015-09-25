package com.vartanian.testing.model;

/**
 * Created by super on 9/21/15.
 */
public class LevelLink {

    private String link;
    private int level;

    public LevelLink(String link, int level) {
        this.link = link;
        this.level = level;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LevelLink levelLink = (LevelLink) o;

        if (level != levelLink.level) return false;
        return !(link != null ? !link.equals(levelLink.link) : levelLink.link != null);

    }

    @Override
    public int hashCode() {
        int result = link != null ? link.hashCode() : 0;
        result = 31 * result + level;
        return result;
    }

    @Override
    public String toString() {
        return link;
    }
}
