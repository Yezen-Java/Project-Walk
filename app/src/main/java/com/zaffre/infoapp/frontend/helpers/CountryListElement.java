package com.zaffre.infoapp.frontend.helpers;

public class CountryListElement {

    private String name, icon;
    private boolean highlighted;

    public CountryListElement(String name, String icon) {
        this.name = name;
        this.icon = icon;
        highlighted = false;
    }

    /**
     * Get name of country
     * @return name of country
     */
    public String getName() {return name;}

    /**
     * Get country flag
     * @return flag string
     */
    public String getIcon() {return icon;}

    /**
     * Get highlighted state
     * @return highlight state
     */
    public boolean isHighlighted() {return highlighted;}

    /**
     * Set highlighted state
     * @param state new highlight state
     */
    public void setHighlighted(boolean state) {highlighted = state;}
}
