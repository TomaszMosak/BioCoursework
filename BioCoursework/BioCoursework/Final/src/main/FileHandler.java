package main;

import java.io.*;
import java.util.*;


public class FileHandler{
    private static final File DATA_DIRECTORY = new File("data");
  
    public FileHandler(){}

    //get file name and figure out function
    
//    public String getFunctionType(){
//        String name = this.file.getName();
//        name = name.substring(name.indexOf("_") + 1, name.indexOf("."));
//        return name;
//
//    }

    public List<double[]> getData(){
        List<double[]> data = new ArrayList<double[]>();
        BufferedReader reader;
        String l;
        List<String[]> lines = new ArrayList<String[]>();
        try{

            reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/Attempt2/data/1in_cubic.txt"));
            while((l = reader.readLine()) != null){
                lines.add(l.replaceFirst("^\\s*", "").split("\\s+"));   
            }
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        
        for(String[] sarr :lines){
            double[] darr = new double[sarr.length];
            int count = 0;
            for(String s : sarr){
                double currdouble = Double.parseDouble(s);
                darr[count] = currdouble;
                count++;
            }
            data.add(darr);
        }
      //  for(int i = 0; i < lies.size(); i++){
       //     int lengthOfLine = lines.get(0).length();
        //    data = new double[count][lengthOfLine];
         //   String[] currLine = lines.get(i).split("\t");
          //  for(int y = 0; y < currLine.length; y++){
          //      data[i][y] = double.parsedouble(currLine[y]);
           // }
        //}
      
        return data;
        
    }
    
    
    public List<double[]> getInputs(List<double[]> list){
    	List<double[]> inputs = new ArrayList<double[]>();
    	
    	boolean oneInput = false;
        if(list.get(0).length == 2) oneInput = true;
    	
    	for(double[] d : list){
    		if(!oneInput){
    			double[] tmp = new double[] {d[0],d[1]};
    			inputs.add(tmp);
    		}else{
    			double[] tmp = new double[] {d[0]};
    			inputs.add(tmp);
    		}
    	} 	
    	return inputs;
    }
    
    public double[] getOutputs(List<double[]> list){
    	double[] outputs = new double[list.size()];
    	
    	for(int i = 0; i < list.size(); i++){
    		double[] d = list.get(i);
    		double tmp = d[d.length-1];
    		outputs[i] =tmp;
    	}
    	return outputs;
    }
    
    
}