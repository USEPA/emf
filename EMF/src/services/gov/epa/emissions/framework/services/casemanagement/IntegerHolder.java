package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

@SqlResultSetMapping(
        name = "IntegerHolderMapping",
        classes = @ConstructorResult(
                targetClass = IntegerHolder.class,
                columns = {
                    @ColumnResult(name = "id", type = Integer.class),
                    @ColumnResult(name = "userId", type = Integer.class)}))
public class IntegerHolder implements Serializable {
    private int id;
    
    private int userId;

    public IntegerHolder() {
        super();
    }

    public IntegerHolder(int userId) {
        super();
        this.userId = userId;
    }

    public IntegerHolder(int id, int userId) {
        super();
        this.id = id;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IntegerHolder other = (IntegerHolder) obj;
        if (id != other.id)
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }


}
