package models.shared;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EnvironmentModel extends Model {
    @Column(name = "env_id")
    private String environmentId;
    @Column(updatable = false, insertable = false)
    @JsonFormat(pattern = "mm:DD:yyyy")
    private DateTime createDate;
    @Column(updatable = false, insertable = false)
    private DateTime updateDate;

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }
}
