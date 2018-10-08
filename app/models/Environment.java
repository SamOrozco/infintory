package models;


import com.avaje.ebean.Model;
import helpers.ErrorHelper;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "environment")
public class Environment extends Model {

    @Column(name = "environment_id")
    private int environmentId;

    @Column(name = "environment_key")
    private String environmentKey;

    @Column(name = "create_date", updatable = false, insertable = false)
    private DateTime createDate;

    public static String newEnvironment() {
        String environmentKey = UUID.randomUUID().toString();
        try {
            new Environment(environmentKey).insert();
            return environmentKey;
        } catch (Exception e) {
            throw ErrorHelper.databaseInsertError("environment");
        }
    }

    public Environment(String environmentKey) {
        this.environmentKey = environmentKey;
    }

    public int getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(int environmentId) {
        this.environmentId = environmentId;
    }

    public String getEnvironmentKey() {
        return environmentKey;
    }

    public void setEnvironmentKey(String environmentKey) {
        this.environmentKey = environmentKey;
    }

    public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }
}
