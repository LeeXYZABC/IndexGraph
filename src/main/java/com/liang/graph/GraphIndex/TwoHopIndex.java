package com.liang.graph.GraphIndex;

import org.apache.tinkerpop.gremlin.structure.Direction;

/**
 * Created by cnic-liliang on 2017/7/20.
 */

public class TwoHopIndex implements MultihopIndex{
    String indexLabel = null;
    String internalTypeValue = null;
    String preLabel = null;
    Direction preDirectoin = null;
    String label = null;
    Direction direction = null;

    public TwoHopIndex(String xindexLabel, String xinternalTypeValue, String xpreLabel, Direction xpreDirectoin, String xlabel, Direction xdirection) {
        indexLabel = xindexLabel;
        internalTypeValue = xinternalTypeValue;
        preLabel = xpreLabel;
        preDirectoin = xpreDirectoin;
        label = xlabel;
        direction = xdirection;
    }
    public String getInternalTypeValue() {
        return internalTypeValue;
    }
    public String getIndexLabel(){
        return indexLabel;
    }
    public String getPreLabel() {
        return preLabel;
    }
    public String getLabel() {
        return label;
    }
    public Direction getPreDirectoin(){
        return preDirectoin;
    }
    public Direction getDirection(){
        return direction;
    }
}