package main;

public class Vector implements Cloneable {

	double[] weights;
	double MIN = -10.0;
	double MAX = 10.0;
	public Vector(double[] weights){
		this.weights = weights;
	}
	
	public Vector(Vector v){
		this.weights = v.weights;
	}
	
	public void addVector(Vector v){
    	double[] VFlat = v.weights;
    	double[] currFlat = this.weights;	
    	for (int i = 1 ; i <  currFlat.length; i++) {
    		currFlat[i] += VFlat[i]; 
    		if(currFlat[i] < this.MIN){
    			currFlat[i] = this.MIN;
    		}
    		if(currFlat[i] > this.MAX){
    			currFlat[i] = this.MAX;
    		}
    	}	
    	this.weights = (currFlat);
    }
    
    public void subVector(Vector v){	
     	double[] VFlat = v.weights;
    	double[] currFlat = this.weights;	
    	for (int i = 1 ; i <  currFlat.length; i++) {
    		currFlat[i] -= VFlat[i]; 
    		if(currFlat[i] < this.MIN){
    			currFlat[i] = this.MIN;
    		}
    		if(currFlat[i] > this.MAX){
    			currFlat[i] = this.MAX;
    		}
    	}
    	this.weights = (currFlat);
    }
    
    public void multVector(double d){
    	double[] currFlat = this.weights;
    	for (int i = 1 ; i <  currFlat.length; i++) {
    		currFlat[i] *= d;
    		if(currFlat[i] < this.MIN){
    			currFlat[i] = this.MIN;
    		}
    		if(currFlat[i] > this.MAX){
    			currFlat[i] = this.MAX;
    		}
    	}
    
    	this.weights = (currFlat);
    }
    public Vector clone() throws CloneNotSupportedException{  
    	return (Vector)super.clone();  
       }
}
