package at.tugraz.oop2.server;

import at.tugraz.oop2.data.DataQueryParameters;
import lombok.Getter;

public class CacheDataQueryParameters {
    private final @Getter
    DataQueryParameters dataObject;

    public CacheDataQueryParameters(DataQueryParameters dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;

        if(obj == null || obj.getClass()!= this.getClass())
            return false;

        CacheDataQueryParameters requestDataObject = (CacheDataQueryParameters) obj;
        return (this.dataObject.getOperation() == requestDataObject.getDataObject().getOperation() &&
                (requestDataObject.getDataObject().getFrom().isBefore(this.dataObject.getFrom()) ||
                requestDataObject.getDataObject().getFrom().isEqual(this.dataObject.getFrom())) &&
                (requestDataObject.getDataObject().getTo().isAfter(this.dataObject.getTo()) ||
                requestDataObject.getDataObject().getTo().isEqual(this.dataObject.getTo())));
    }

}
