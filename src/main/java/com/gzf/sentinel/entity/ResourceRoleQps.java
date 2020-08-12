package com.gzf.sentinel.entity;

import java.io.Serializable;

public class ResourceRoleQps implements Serializable {
    private Long id;

    private String appId;

    private String api;

    private Long limitQps;

    private Long createAt;

    private static final long serialVersionUID = 1L;

    public ResourceRoleQps(Long id, String appId, String api, Long limitQps, Long createAt) {
        this.id = id;
        this.appId = appId;
        this.api = api;
        this.limitQps = limitQps;
        this.createAt = createAt;
    }

    public ResourceRoleQps() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api == null ? null : api.trim();
    }

    public Long getLimitQps() {
        return limitQps;
    }

    public void setLimitQps(Long limitQps) {
        this.limitQps = limitQps;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ResourceRoleQps other = (ResourceRoleQps) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppId() == null ? other.getAppId() == null : this.getAppId().equals(other.getAppId()))
            && (this.getApi() == null ? other.getApi() == null : this.getApi().equals(other.getApi()))
            && (this.getLimitQps() == null ? other.getLimitQps() == null : this.getLimitQps().equals(other.getLimitQps()))
            && (this.getCreateAt() == null ? other.getCreateAt() == null : this.getCreateAt().equals(other.getCreateAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppId() == null) ? 0 : getAppId().hashCode());
        result = prime * result + ((getApi() == null) ? 0 : getApi().hashCode());
        result = prime * result + ((getLimitQps() == null) ? 0 : getLimitQps().hashCode());
        result = prime * result + ((getCreateAt() == null) ? 0 : getCreateAt().hashCode());
        return result;
    }
}