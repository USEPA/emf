package gov.epa.emissions.commons.util;

import java.util.ArrayList;
import java.util.List;

public class EmfArrays {

    public static List<Integer> convert(int[] intArray) throws Exception {
        List<Integer> IntArray = new ArrayList<Integer>();
        
        for (int i : intArray)
            IntArray.add(new Integer(i));
        
        return IntArray;
    }
    
    public static int[] convert(List<Integer> integers) throws Exception {
        int[] array = new int[integers.size()];
        
        for (int i = 0; i < integers.size(); i++)
            array[i] = integers.get(i).intValue();
        
        return array;
    }
}
