package org.thingsboard.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;
import org.thingsboard.common.validation.Create;

@Data
public abstract class BaseData<I> implements Serializable, HasId<I> {
    public static final ObjectMapper mapper = new ObjectMapper();
    private static final long serialVersionUID = 5422817607129962637L;
    @NotNull(message = "Id不能为空", groups = Create.class)
    protected I id;
    protected Long createdTime;
    protected Long updatedTime;

}
