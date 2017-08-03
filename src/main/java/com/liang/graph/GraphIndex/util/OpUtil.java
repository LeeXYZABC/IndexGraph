package com.liang.graph.GraphIndex.util;

import com.liang.graph.GraphIndex.TwoHopIndex;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cnic-liliang on 2017/7/19.
 */
public class OpUtil {
    static public String[] OpExtract(String query) {
        String[] opArray = query.split("\\).");
        // add ")" for each operation to remove the affection of split
        for (int i = 0; i < opArray.length; i++) {
            opArray[i] = opArray[i] + ')';
            opArray[i] = opArray[i].trim();
        }
        return opArray;
    }

    static public String OpKey(String Op) {
        return Op.split("\\(")[0];
    }

    static public String OpBracketContent(String Op) {
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(Op);
        if(matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }

    static public String FirstParameter(String Op) {
        String opKey = Op.split("\\(")[0];
        if(opKey.compareTo("has") !=0 ) return null;
        String para = OpBracketContent(Op).trim().split("\\,")[0].trim();
        if(para.length() > 0) return para.substring(1, para.length()-1);
        return null;
    }

    static public String SecondParameter(String Op) {
        String opKey = Op.split("\\(")[0];
        if(opKey.compareTo("has") !=0 ) return null;
        String para = OpBracketContent(Op).trim().split("\\,")[1].trim();
        if(para.length() > 0) return para.substring(1, para.length()-1);
        return null;
    }

    static public String AsVariable(String Op) {
        Pattern asPattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher asMatcher = asPattern.matcher(Op);
        if (asMatcher.find()) {
            String variable = asMatcher.group().trim();
            return variable.substring(1, variable.length() - 1);
        }
        return null;
    }

    static public String[] SelectVariable(String Op) {
        String VarStr = OpBracketContent(Op);
        if(VarStr == null) return null;
        String [] VarArray = VarStr.split("\\,");
        for(int i = 0; i < VarArray.length; i++) {
            String var = VarArray[i].trim();
            VarArray[i] = var.substring(1, var.length() - 1);
        }
        return VarArray;
    }

    static public Boolean IsVariableUsed(String var, String[] OpArray, int id) {
        int k = id + 2;
        while(k < OpArray.length - 1) {
            String kFun = OpArray[k].trim();
            String kFunKey = OpUtil.OpKey(kFun);
            if(kFunKey.compareTo("select")==0) {
                String [] vars = OpUtil.SelectVariable(kFun);
                for(int varCnt = 0; varCnt <= vars.length - 1; varCnt ++) {
                    if(var.compareTo(vars[varCnt])==0) {
                        //just break the multihop index optimization
                        return true;
                    }
                }
            }
            k++;
        }
        return false;
    }

    static public String EdgeLabel(String[] OpArray, int id) {
        if(id < 0) return null;
        String info = OpBracketContent(OpArray[id]).trim();
        return info.substring(1, info.length()-1);
    }

    static public Direction EdgeDirection(String[] OpArray, int id) {
        if(id < 0) return null;
        String opKey = OpKey(OpArray[id]).trim();
        return opKey.compareTo("in") == 0 ? Direction.IN :  Direction.OUT;
    }

    static public String GremlinGenerator(String [] OpArray) {
        String resultQuery = "";
        for(int i = 0; i < OpArray.length; i++) {
            if(OpArray[i] != null && OpArray.length > 0) {
                if(i > 0) resultQuery += ".";
                OpArray[i] = OpArray[i].replace("\"", "\'");
                resultQuery += OpArray[i];
            }
        }
        return resultQuery;
    }

    //optimize
    static public boolean IsToOptimize(int preID, int currentID, String[] opArray) {
        for( int cls = preID + 1; cls < currentID; cls ++) {
            String localFun = opArray[cls];
            String localfunKey = OpUtil.OpKey(localFun);
            if(localfunKey.compareTo("as")==0) {
                String asVar = OpUtil.AsVariable(localFun);
                if(OpUtil.IsVariableUsed(asVar, opArray, currentID)){
                    //should break the optimization process
                    return false;
                }
            }
        }
        return true;
    }

    //check
    static public boolean InternalTypeCheck(ArrayList<TwoHopIndex> indices, String typeValue) {
        boolean re = false;
        for(TwoHopIndex index : indices) {
            if(index.getInternalTypeValue().compareTo(typeValue) == 0) {
                return true;
            }
        }
        return  re;
    }

    static public String AllCheck(ArrayList<TwoHopIndex> indices, String prelabel, Direction preDirection,
                    String label, Direction direction, String intervalTypeValue) {
        String re = null;
        //System.out.println("index length " + indices.size());
        if(intervalTypeValue == null) return  null;
        for(TwoHopIndex index : indices) {
            if(index.getInternalTypeValue().compareTo(intervalTypeValue) == 0
                    && index.getPreLabel().compareTo(prelabel) == 0
                    && index.getPreDirectoin().compareTo(preDirection) == 0
                    && index.getLabel().compareTo(label) == 0
                    && index.getDirection().compareTo(direction)== 0) {
                return index.getIndexLabel();
            }
        }
        return  re;
    }

    //create
    static public ArrayList<TwoHopIndex> IndexConfiguration(String indexConf) {
        ArrayList<TwoHopIndex> indices =  new ArrayList<TwoHopIndex>();
        try{
            File file = new File(indexConf);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");
                int lengh = items.length;
                if(lengh<7) {
                    System.out.println("The index configuration is not correct for " + line);
                }
                TwoHopIndex index = new TwoHopIndex(items[6].trim(), items[3].trim(),
                        items[0].trim(), items[1].trim().compareTo("OUT") == 0 ? Direction.OUT: Direction.IN,
                        items[4].trim(), items[5].trim().compareTo("OUT") == 0 ? Direction.OUT: Direction.IN);
                indices.add(index);
            }
            reader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return indices;
    }
}
