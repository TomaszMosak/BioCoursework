package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

public class PSO {

	// size of population
	static int swarmsize = 480; // -----------------------------------------------------PSO HYPERPARAMATER HERE ---------------------------------
	static int iterations = 10000; // -----------------------------------------------------PSO HYPERPARAMATER HERE ---------------------------------
	int runs = 0;
	 // velocity to be retained
	static double velRetain = (double) 0.6; // -----------------------------------------------------LEARNING HYPERPARAMATER HERE ---------------------------------
	// personal best to retain (COGNITIVE COMPONENT)
	static double c1 = 1.8; // -----------------------------------------------------LEARNING HYPERPARAMATER HERE ---------------------------------
	// informants best to retain (SOCIAL COMPONENT)
	static double c2 = 0.8; // -----------------------------------------------------LEARNING HYPERPARAMATER HERE ---------------------------------
	// global best to retain
	static double c3 = 1.3; // -----------------------------------------------------LEARNING HYPERPARAMATER HERE ---------------------------------
	// jump size of particle
	double jumpsize = 1;
	double[] bestWeights;
	static double[] fitnessData;
	static double globalWorstFitness = 0.1;
	static int GlobalCount = 0;
	Random r = new Random();
	Network[] networks;
	double globalBestFitness = 100;
	Vector globalBestSettings;
	final static int NUM_INFORMANTS = 6;

	private void init(int length) {
		Network[] tempNetworkArr = new Network[swarmsize];
		for (int i = 0; i < swarmsize; i++) {
			tempNetworkArr[i] = new Network(length,10, 1); // -----------------------------------------------------NET HYPERPARAMATER HERE ---------------------------------
		}

		// give each particle 6 informants
		for (Network n : tempNetworkArr) {
			Network[] informants = new Network[NUM_INFORMANTS];
			for (int i = 0; i < NUM_INFORMANTS; i++) {

				int randomInt = r.nextInt(tempNetworkArr.length);
				Network tempN = tempNetworkArr[randomInt];
				// check the particle picked isnt the particle to which we are
				// assigning informants
				if (!tempN.equals(n)) {
					informants[i] = tempN;
				} else {
					if (randomInt >= tempNetworkArr.length - 1) {
						randomInt -= 1;
					} else {
						randomInt += 1;
					}
					informants[i] = tempNetworkArr[randomInt];
				}
			}
			n.informants = informants;
		}
		networks = tempNetworkArr;
	}

	public double assessFitness(Network n, List<double[]> inputs, double[] desired) {

		if (inputs.size() != desired.length) {
			System.out.println("Something went wrong");
			return 0.0;
		}
		double sumsq = 0;
		for (int i = 0; i < desired.length; i++) {
			sumsq += Math.pow(desired[i] - n.calculate(inputs.get(i)), 2);
		}

		 return sumsq / desired.length;

	}

	private boolean isBest() {
		if (globalBestSettings != null) {
			if (globalBestFitness <= 0.00000006) {
				System.out.println("Found Near Perfect Settings. Exiting.");
				return true;
			}
		}

		return false;
	}

	private double[] optimise(List<double[]> inputs, double[] outputs) throws CloneNotSupportedException {

		// algorithm based off of textbook given in course work specification
		// found here:
		// https://cs.gmu.edu/~sean/book/metaheuristics/Essentials.pdf

		// initialise the swarm
		init(inputs.get(0).length);

		while (!isBest() && runs < iterations) {
			if(runs > 0){
				for (Network n : this.networks) {
					/*
					 * here, we pass the vector of the global best to the network,
					 * as each network is "dumb" and doens't know about global best.
					 * We also pass in velocity inertia, cognitive, social, and
					 * global inertias
					 */
					Vector newVel = newVelocity(n);
					// make a copy of current position
					Vector newPos = n.getPos();
					// add velocity to position
					newPos.addVector(newVel);
					// make this the new position
					n.changePos(newPos);
				}
			}
			
			
			// get error of all networks, assign their personal bests
			for (Network n : this.networks) {
				double fitness = assessFitness(n, inputs, outputs);
				if (fitness < n.getPersonalBestFitness()) {
					n.setPersonalBest(n.getPos(), fitness);
					
				}
			}

			for (Network n : networks) {
				// check the network's fitness
				double fitness = n.getPersonalBestFitness();
				
				/*
				 * If the global best is null, or if the current network's
				 * fitness is better than the current best's fitness (i.e. the
				 * error is lower) then make the current network the best!
				 */
				if (this.globalBestSettings == null || fitness < this.globalBestFitness) {
					if(this.globalBestSettings != null)
					this.globalBestFitness = fitness;
					this.globalBestSettings = new Vector(n.getPersonalBest());
					//System.out.println(GlobalCount + ":FITNESS IS "+fitness); // UNCOMMENT TO SEE BEST FITNESS FOR EACH RUN
					if (globalWorstFitness < fitness) {
						globalWorstFitness = fitness;
					}
					fitnessData[GlobalCount] = fitness;
					GlobalCount++;
					bestWeights = Arrays.copyOf(globalBestSettings.weights, globalBestSettings.weights.length);
				}
			}

			runs++;
			
		}
		System.out.println("BEST FITNESS FOUND: "+ globalBestFitness);
		return bestWeights;
	}

	public Vector newVelocity(Network n) throws CloneNotSupportedException {
		Vector informantsBest = new Vector(n.getInformantsBest());
		Vector personalBest = new Vector(n.getPersonalBest());
		

		double[] newGlobalWeights = new double[globalBestSettings.weights.length];
		for(int i = 0; i < newGlobalWeights.length; i++){
			newGlobalWeights[i] = globalBestSettings.weights[i];
		}
		
		Vector globalBestPos = new Vector(newGlobalWeights);
		double b = c1 * r.nextFloat();
		double c = c2 * r.nextFloat();
		double d = c3 * r.nextFloat();

		Vector currPos = new Vector(n.getPos());
		// get a copy of the old velocity that we are mutating
		Vector changingVel = new Vector(n.getVelocity());

		// first part of equation
		changingVel.multVector(velRetain);

		// second part of equation
		personalBest.subVector(currPos);
		personalBest.multVector(b);
		// add to sum
		changingVel.addVector(personalBest);

		// third part of equation
		informantsBest.subVector(currPos);
		informantsBest.multVector(c);
		// add to sum
		changingVel.addVector(informantsBest);
		// fourth part of equation
		globalBestPos.subVector(currPos);
		globalBestPos.multVector(d);
		// add to sum
		changingVel.addVector(globalBestPos);

		// return the new velocity
		
	
		return changingVel;

	}

	public static void main(String[] args) throws CloneNotSupportedException {
		PSO pso = new PSO();
		FileHandler fh = new FileHandler();
		// get data
		List<double[]> data = fh.getData();
		// get list of outputs and inputs
		List<double[]> inputs = fh.getInputs(data);
		double[] outputs = fh.getOutputs(data);
		
		// create an array to the length of the dataset
		fitnessData = new double[outputs.length];
		
		// optimise till best is found
		Instant start = Instant.now();
		double[] best = pso.optimise(inputs, outputs);
		Instant end = Instant.now();

		
		Duration interval = Duration.between(start, end);
		
		// setting graph sizes
		double[] xData = new double[inputs.size()];
		double[] yData = new double[outputs.length];
		double[] fitnessGraph = new double[GlobalCount];


		System.out.println("BEST WEIGHTS FOUND: "+Arrays.toString(best));
		System.out.println("Drawing Graphs...");
		Vector bestVector = new Vector(best);
		Network bestNet = new Network(bestVector, inputs.get(0).length,10, 1); // ----------------------------------NET HYPERPARAMATER HERE -------------------------
		pso.globalBestSettings = bestVector;
		for (int i = 0; i < inputs.size(); i++) {
			double answer = bestNet.calculate(inputs.get(i));
			xData[i] = outputs[i];
			yData[i] = answer;
//			System.out.println("Input is " + Arrays.toString(inputs.get(i)) + " ," + "Expected is " + outputs[i]
//				+ ", answer achieved is " + answer);
			
		}
	
		for (int i = 0; i < GlobalCount; i++) {
			fitnessGraph[i] = fitnessData[i];
			
		}
		
		// Generate Graphs
		int numCharts = 3;
		 
	    List<XYChart> charts = new ArrayList<XYChart>();
	 
	    for (int i = 0; i < numCharts; i++) {
	      XYChart chart;
	      if (i == 0){
	    	  chart = new XYChartBuilder().title("Inertia: " + velRetain + " Cognative: " + c1 + " Social: " + c2).xAxisTitle("Desired Output").yAxisTitle("Values").width(600).height(400).build();
		      chart.getStyler().setYAxisMin((double) -1.5);
		      chart.getStyler().setYAxisMax((double) 1.5);
		      XYSeries series;
	    	  series = chart.addSeries("" + i, null, xData);
	    	  series.setLineColor(Color.GRAY);
	    	  series.setMarkerColor(Color.GREEN);
	    	  series.setMarker(SeriesMarkers.CROSS);
	      } else if (i == 1) {
	    	  chart = new XYChartBuilder().title("GOLDEN RATIO Network Size: 1-10-1").xAxisTitle("PSO Output - Time Taken: " + (interval.getSeconds()) + " seconds").yAxisTitle("Values").width(600).height(400).build();
		      chart.getStyler().setYAxisMin((double) -1.5); 
		      chart.getStyler().setYAxisMax((double) 1.5);
		      XYSeries series;
	    	  series = chart.addSeries("" + i, null, yData);
	    	  series.setLineColor(Color.GRAY);
	    	  series.setMarkerColor(Color.BLUE);
	    	  series.setMarker(SeriesMarkers.CROSS);
	      } else {
	    	  chart = new XYChartBuilder().title("Swarm Size: "+ swarmsize + " Iterations: " + iterations).xAxisTitle("Best Fitness Found: " + fitnessGraph[GlobalCount-1]).yAxisTitle("Mean Squared Error").width(600).height(400).build();
		      chart.getStyler().setYAxisMin((double) 0);
		      chart.getStyler().setYAxisMax((double) (globalWorstFitness+0.2));
		      XYSeries series;
		      
	    	  series = chart.addSeries("" + i, null, fitnessGraph);
	    	  series.getLabel();
	    	  series.setLineColor(Color.RED);
	    	  series.setMarker(SeriesMarkers.SQUARE);
	      }
	      ;
	      charts.add(chart);
	    }
	    new SwingWrapper<XYChart>(charts).displayChartMatrix();
	    System.out.println("Displaying Graphs...");
	}

}