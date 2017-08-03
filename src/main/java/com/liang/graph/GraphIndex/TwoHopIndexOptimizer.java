package com.liang.graph.GraphIndex;

import com.liang.graph.GraphIndex.util.OpUtil;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.*;

/**
 * Created by cnic-liliang on 2017/7/19.
 * TwoHopIndexOptimizer
 */

public class TwoHopIndexOptimizer {
    static String canInternalTypeName = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static ArrayList<TwoHopIndex> indices = null;

    public TwoHopIndexOptimizer(String indexConf) {
        indices = OpUtil.IndexConfiguration(indexConf);
    }

    public String optimizer(String gremlinQuery) {
        String[] opArray = OpUtil.OpExtract(gremlinQuery);
        int preID = -1;
        for(int i = 0; i < opArray.length; i++) {
            String intervalTypeValue = null;
            if(OpUtil.OpKey(opArray[i]).compareTo("has") == 0) {
                if(OpUtil.FirstParameter(opArray[i]).compareTo(canInternalTypeName) == 0
                        && OpUtil.InternalTypeCheck(indices, OpUtil.SecondParameter(opArray[i]))) {
                    System.out.println("Internal Vertex matched!!! " + OpUtil.SecondParameter(opArray[i]));
                    intervalTypeValue = OpUtil.SecondParameter(opArray[i]);
                    //skip the as, dedup, select steps until the in/out step
                    i = i + 1;
                    while ((OpUtil.OpKey(opArray[i]).compareTo("as")==0 || OpUtil.OpKey(opArray[i]).compareTo("dedup")==0 || OpUtil.OpKey(opArray[i]).compareTo("select")==0)
                            &&  i < opArray.length - 1)  {
                        i = i + 1;
                    }
                }
            }
            //the function of the following part in this loop
            //1) record the preID, preLabel, preDirection of in/out opertions
            //2) optimize the multiple traversal
            //
            if(OpUtil.OpKey(opArray[i]).compareTo("in") == 0 || OpUtil.OpKey(opArray[i]).compareTo("out") == 0) {
                Direction direction = OpUtil.OpKey(opArray[i]).compareTo("in") == 0 ? Direction.IN : Direction.OUT;
                String indexLable = OpUtil.AllCheck(indices, OpUtil.EdgeLabel(opArray, preID), OpUtil.EdgeDirection(opArray, preID), OpUtil.EdgeLabel(opArray, i), direction, intervalTypeValue);
                if(indexLable != null && OpUtil.IsToOptimize(preID, i, opArray)) {
                    System.out.println("Found " + indexLable);
                    opArray[preID] =  "out(\"" + indexLable + "\")";
                    for(int xcls = preID + 1; xcls <= i; xcls ++)
                        opArray[xcls] = null;
                }
                else {
                    preID = i;
                }
            }
        }
        return OpUtil.GremlinGenerator(opArray);
    }

    public static void main(String[] args) {
        String indexFile = "./index.conf";
        String gremlinQuery = "g.V().has(\"vertexID\", \"http://gcm.wdcm.org/data/gcmAnnotation1/taxonomy/1270\").inE(\"http://gcm.wdcm.org/ontology/gcmAnnotation/v1/ancestorTaxid\").bothV().dedup().as(\"taxonid\").values(\"vertexID\").as(\"dsttaxonid\").select(\"taxonid\").in(\"http://gcm.wdcm.org/ontology/gcmAnnotation/v1/x-taxon\").has(\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \"http://gcm.wdcm.org/ontology/gcmAnnotation/v1/GeneNode\").as(\"geneid\").select(\"geneid\").out(\"http://gcm.wdcm.org/ontology/gcmAnnotation/v1/x-enzyme\").dedup().order().by(\"vertexID\", Order.incr).as(\"enzymeid\").values(\"vertexID\").as(\"dstenzymeid\").select(\"enzymeid\").select(\"enzymeid\").count().as(\"num\").select(\"num\");";
        TwoHopIndexOptimizer opt = new TwoHopIndexOptimizer(indexFile);
        String resultQuery = opt.optimizer(gremlinQuery);
        System.out.println("Origianl gremlin query: " + gremlinQuery);
        System.out.println("Resulted gremlin query: " + resultQuery);
    }

}
