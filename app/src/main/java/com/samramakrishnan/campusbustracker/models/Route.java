package com.samramakrishnan.campusbustracker.models;

public class Route {
    private int serviceId, type, textColor, isBike;
    private String routeId, shortName, agencyId, serviceName, desc, url, routeColor;

    public Route(String routeId, int serviceId, String shortName, int type, int textColor, int isBike, String agencyId, String serviceName, String desc, String url, String routeColor) {
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.shortName = shortName;
        this.type = type;
        this.textColor = textColor;
        this.isBike = isBike;
        this.agencyId = agencyId;
        this.serviceName = serviceName;
        this.desc = desc;
        this.url = url;
        this.routeColor = routeColor;
    }

    public Route(String routeId, String shortName, String serviceName) {
        this.routeId = routeId;
        this.shortName = shortName;
        this.serviceName = serviceName;
    }

    public Route(String routeId, String shortName) {
        this.routeId = routeId;
        this.shortName = shortName;
    }

    // Getters
    public String getRouteId() {
        return routeId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getShortName() {
        return shortName;
    }

    public int getType() {
        return type;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getIsBike() {
        return isBike;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDesc() {
        return desc;
    }

    public String getUrl() {
        return url;
    }

    public String getRouteColor() {
        return routeColor;
    }


    // Setters

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setIsBike(int isBike) {
        this.isBike = isBike;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRouteColor(String routeColor) {
        this.routeColor = routeColor;
    }
}
