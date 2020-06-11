package main;

import java.util.Arrays;

/**
 * @author Tomasz Mosak
 */

public class Network implements Cloneable{

	
    public final int[] NETWORK_LAYER_SIZES; // Array for the amount of neurons on each layer.
    public final int INPUT_SIZE; // Size of the input layer
    public final int OUTPUT_SIZE; // Size of the output layer
    public final int NETWORK_SIZE; // How many layers we have
    public int activationInt;
	
    private double[][] outputs; // Stores outputs of each neuron.  Layer-Neuron
    private double[][][] weights; // Stores weights of each neuron. Layer-Neuron-PrevNeuron
    
    double error;
    Vector bestPos, personalBest, velocity;
    Network[] informants;
    double personalBestFitness;
    //constructor for random weight.
    public Network(int... NETWORK_LAYER_SIZES) {
        this.NETWORK_LAYER_SIZES = NETWORK_LAYER_SIZES;
        this.INPUT_SIZE = NETWORK_LAYER_SIZES[0];
        this.NETWORK_SIZE = NETWORK_LAYER_SIZES.length;
        this.OUTPUT_SIZE = NETWORK_LAYER_SIZES[NETWORK_SIZE - 1];


        this.activationInt = 2; // -----------------------------------------------------HYPERPARAMATER HERE ---------------------------------
        this.personalBestFitness = 1000;
        this.velocity = this.vectorRandomiser();
        
        this.outputs = new double[NETWORK_SIZE][];
        this.weights = new double[NETWORK_SIZE][][];
        
        
        
        //random weights
        for(int i = 0; i < NETWORK_SIZE; i++) {
            this.outputs[i] = new double[NETWORK_LAYER_SIZES[i]]; //The size is the amount of neurons at that layer.
       
            if(i == 0)
            	 this.weights[i] = NetTools.createRandomArray(this.NETWORK_LAYER_SIZES[i], this.NETWORK_LAYER_SIZES[i],  0, 0);
            // Here I check to see if i > 0. Make weight for every neuron EXCEPT those in the first layer due to input layer
            if(i > 0) {
                this.weights[i] = NetTools.createRandomArray(NETWORK_LAYER_SIZES[i], NETWORK_LAYER_SIZES[i - 1], (double) -10,(double)  10);
            }
        }
        this.personalBest = new Vector(this.flatWeights());
    }
    
    public Network(Vector vector, int... NETWORK_LAYER_SIZES){
    	this.NETWORK_LAYER_SIZES = NETWORK_LAYER_SIZES;
        this.INPUT_SIZE = NETWORK_LAYER_SIZES[0];
        this.NETWORK_SIZE = NETWORK_LAYER_SIZES.length;
        this.OUTPUT_SIZE = NETWORK_LAYER_SIZES[NETWORK_SIZE - 1];
    
        
        this.activationInt = 2; // -----------------------------------------------------HYPERPARAMATER HERE ---------------------------------
        this.personalBest = vector;
        this.personalBestFitness = 1000;
        this.velocity = this.vectorRandomiser();
        //set weights
        this.outputs = new double[NETWORK_SIZE][];
        this.weights = new double[NETWORK_SIZE][][];
        double[] flat = vector.weights;
        this.set3DWeight(flat);
        
        for(int i = 0; i < NETWORK_SIZE; i++) {
            this.outputs[i] = new double[NETWORK_LAYER_SIZES[i]];
        }
        
    }
    
    //from flat weight array to [][][] weights
    public void set3DWeight(double[] flat){
    	int flatcount;
    	if (this.INPUT_SIZE == 0){
    		flatcount = this.INPUT_SIZE+2;
    	} else {
    		flatcount = this.INPUT_SIZE;
    	}
      //  System.out.println(flat.length);
        //keep looping until the end of the flat double array
        
        	this.weights[0] = NetTools.createRandomArray(NETWORK_LAYER_SIZES[0], NETWORK_LAYER_SIZES[0], (double) 0.0, (double) 0.0);
         //step through layers
        	for(int layer = 1; layer < NETWORK_SIZE; layer++) {
        		
             //this is just to make sure that the weights returned are just the flat array values
             this.weights[layer] = NetTools.createRandomArray(NETWORK_LAYER_SIZES[layer], NETWORK_LAYER_SIZES[layer - 1], (double) 0.0, (double) 0.0);
         //step through neurons
             for(int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++) {
             //subtract weights
            	 for(int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZES[layer - 1]; prevNeuron++) {
                    this. weights[layer][neuron][prevNeuron] = flat[flatcount];
                     flatcount++;
                     if(flatcount == flat.length){
                    	 return;
                     }
                 }
             }
         }
        
  
  
    }

    
    public Vector vectorRandomiser(){
    	double[][][] randomWeights = new double[this.NETWORK_SIZE][][];
   	  for(int i = 0; i < this.NETWORK_SIZE; i++) {

   		  		if(i == 0)
   		  		 randomWeights[i] = NetTools.createRandomArray(this.NETWORK_LAYER_SIZES[i], this.NETWORK_LAYER_SIZES[i],  0, 0);
   		  	 // Here I check to see if i > 0. This is because creation of weight for every neuron EXCEPT those in the first layer.
             if(i > 0) {
           	  randomWeights[i] = NetTools.createRandomArray(this.NETWORK_LAYER_SIZES[i], this.NETWORK_LAYER_SIZES[i - 1],  -10, 10);
             }
         }
       
   	  double[] flat = Arrays.stream(
			randomWeights)
	                      .flatMap(Arrays::stream)
	                      .flatMapToDouble(Arrays::stream)
	                      .toArray();
      
   		return new Vector(flat);
    }
    
    
    //from [][][] weights to flat weights 
  public double[] flatWeights(){
    	
    	double[] flat = Arrays.stream(
    			weights)
    	                      .flatMap(Arrays::stream)
    	                      .flatMapToDouble(Arrays::stream)
    	                      .toArray();

    	return flat;
  }
    /**
     * Function to calculate the output of neurons.
     * @param input - Array of inputs
     * @return Values of the output layer after calculation
     */
        
    public double calculate(double... input) {
        if(input.length != this.INPUT_SIZE) return 0.0; // If we have too few, or too many, inputs then we will return null;

        this.outputs[0] = input; // If we are on the first layer, we have no output to calculate so the output becomes the input we are given.

        /*
            Here we iterate through the layers and then the neurons. For every neuron we iterate through the neurons in the previous layer.
            We then sum up the output of the neuron at the previous layer, multiplied by the weight connecting that neuron to our current working neuron.
            This sum is then applied to our activation function and its output is saved in the outputs array.
            The function then returns the final layer outputs.
         */
        for(int layer = 1; layer < NETWORK_SIZE; layer++) {
            for(int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++) {

                double sum = 0;
                for(int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZES[layer - 1]; prevNeuron++) {
                    sum += outputs[layer - 1][prevNeuron] * weights[layer][neuron][prevNeuron];
                }
                outputs[layer][neuron] = Activation(sum, this.activationInt);
            }
        }
        return outputs[NETWORK_SIZE - 1][0];
    }

    public enum ActivationEnum{
        Sigmoid,
        Undefined,
        hyperbolicTangtent,
        Cosine,
        Gaussian,
        Linear
    }

    
    public String printEnumValue(int x) {
    	return java.util.Arrays.asList(ActivationEnum.values()).get(x).toString();
    }

    //Select the right activation function defined in the hyperparameter
    public double Activation(double x, int enumIndex)
    {
    String enumValue = printEnumValue(enumIndex);
      if (enumValue.equals("Sigmoid"))
        return sigmoid(x);
      else if (enumValue.equals("hyperbolicTangtent"))
        return hyperbolicTangtent(x);
      else if (enumValue.equals("Undefined"))
        return undefined(x);
      else if (enumValue.equals("Cosine"))
        return cosine(x);
      else if (enumValue.equals("Gaussian"))
        return gaussian(x);
      else if (enumValue.equals("Linear"))
    	  return x;
      else
        return (double) 0.0;
    }


    private double sigmoid(double x) { // ---------------------------------- BEST ACTIVATION FUNCTION TO USE FOR POSITIVE ONLY OUTPUTS (xor dataset)------------------
        return (double) (1D / (1 + Math.exp(-x)));
    }

    private double hyperbolicTangtent(double x){ // ---------------------------------- BEST ACTIVATION FUNCTION TO USE ------------------
        return (double) Math.tanh(x);
    }
    
    private double undefined(double x){
        return 0.0;
    }

    private double cosine (double x){
        return (double) Math.cos(x);
    }

    private double gaussian (double x){
        return  (double) Math.exp(-((Math.pow(x, 2)/(2.0))));
    }

    
    public void changePos(Vector v){
    	
    	this.set3DWeight(v.weights);
    }
    
 
    
    
    
    public Vector getPos(){
    	return new Vector(this.flatWeights());
    }
    
    public void setInformants(Network[] networks){
    	this.informants = networks;
    }
    
    public Vector getInformantsBest(){
    	double bestFitness = this.getPersonalBestFitness();
    	Vector bestPos = this.getPersonalBest();
    	for(Network n : this.informants){
    		if(n.getPersonalBestFitness() < bestFitness){
    			bestFitness = n.getPersonalBestFitness();
    			bestPos = n.getPersonalBest();
    		}
    	}
    	
    	return bestPos;
    }
    
    public Vector getPersonalBest(){
    	return this.personalBest;
    }
    
    public Vector getVelocity(){
    	return this.velocity;
    }
    
    public double getPersonalBestFitness(){
    	return this.personalBestFitness;
    }
    
    public void setPersonalBest(Vector v, double fitness){
    	this.personalBest = v;
    	this.personalBestFitness = fitness;
    }
    public Network clone() throws CloneNotSupportedException{  
    	return (Network)super.clone();  
       }
    
}