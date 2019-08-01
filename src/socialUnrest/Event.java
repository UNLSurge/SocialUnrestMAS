/**
 * 
 */
package socialUnrest;

import socialUnrest.Neighbor;
import socialUnrest.UnrestBuilder;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// joda time is used for DateTime calculations
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;

/**
 * @author sudbasnet
 */

public class Event {
	private int life = 0; 
	private boolean alive = false;
	private int markSize;
	private long uniqueId;
	
//	spatio-temporal variables
	double lat, lon;
	DateTime eventdate, eventEndDate;
	
//	socioeconomic variables
	double literacyRate, workerPopulation, population;
	
//	infrastructure variables
	double policeDensity, postalDensity,  hospitalDensity, schoolDensity, collegeDensity, universityDensity; 
	double policeClosest, postalClosest, hospitalClosest, schoolClosest, collegeClosest, universityClosest;
	
	private double intensity = 0;
	
//	calculations are done each day to determine what the intensity of any event should be on the next day
	private double nextIntensity = 0;
	
//	intensityColor was added to adjust the coloring during display
	private double intensityColor = 10;
	
/*	variables added for the purpose of data-collection during simulation
	these are not essential for the intensity calculation */
	private int numberOfNeighbors = 0;
	private List<Long> neighborsList = new ArrayList<Long>();
	private double recoveryRate, influenceRate, neighborhoodSize;
//	variables for calculating confidence factor
	private double neighborhoodDensity;
	private double coefficientVar;
	private double avgNeighborhoodIntensity;
	private double sd; // Standard Deviation in the intensities of neighbors
	private double confidence;
	
	public Event(
		double intensity, 
		double lon, 
		double lat, 
		long uniqueId, 
		String eventdate,
		double population,
		double literacyRate, 
		double workerPopulation,
		double policeClosest, 
		double postalClosest,
		double hospitalClosest, 
		double schoolClosest,
		double collegeClosest,
		double universityClosest,
		double policeDensity, 
		double postalDensity,
		double hospitalDensity, 
		double schoolDensity,
		double collegeDensity,
		double universityDensity
	) {
		this.intensity = intensity;
		this.lon = lon;
		this.lat = lat;
		this.eventdate = stringToDate(eventdate);
		this.eventEndDate = stringToDate(eventdate); // same as start-date in initialization
		this.uniqueId = uniqueId;
		this.population = population;
		this.literacyRate =  literacyRate;
		this.workerPopulation = workerPopulation;
		this.policeClosest =  policeClosest;
		this.postalClosest = postalClosest;
		this.hospitalClosest =  hospitalClosest;
		this.schoolClosest = schoolClosest;
		this.collegeClosest = collegeClosest;
		this.universityClosest = universityClosest;
		this.policeDensity =  policeDensity;
		this.postalDensity = postalDensity;
		this.hospitalDensity =  hospitalDensity;
		this.schoolDensity = schoolDensity;
		this.collegeDensity = collegeDensity;
		this.universityDensity = universityDensity;
		String currentDayString = UnrestBuilder.currentDay.toString(DateTimeFormat.shortDate());
		String eventDateString = this.eventdate.toString(DateTimeFormat.shortDate());
		if (currentDayString.equals(eventDateString)) {
			this.markSize = UnrestBuilder.MARKER_SIZE;
			this.alive = true;
		} else {
			this.markSize = 1;
		}
		this.setIntensityColor();
	}

	public Event() {
		// TODO Auto-generated constructor stub
	}

// --------	get methods ---------- //
	
	public long getUniqueId() {
		return this.uniqueId;
	}
	
	public boolean getAlive() {
		return this.alive;
	}
	
	public String getCurrentDay() {
		return UnrestBuilder.currentDay.toString(DateTimeFormat.shortDate());
	}

	public double getPop() {
		return this.population;
	}
	
	public double getlon() {
		return this.lon;
	}
	
	public double getlat() {
		return this.lat;
	}
	
	public String getEventdate() {
		return this.eventdate.toString(DateTimeFormat.shortDate());
	}
	
	public String getEventEnddate() {
		return this.eventEndDate.toString(DateTimeFormat.shortDate());
	}
	
	public int getLife() {
		return this.life;
	}
	
	public double getIntensity() {
		return this.intensity;
	}
	
	public int getMarkSize() {
		return this.markSize;
	}
	
	public double getIntensityColor() {
		return this.intensityColor;
	}
	
	public double getCoefficientVar() {
		return this.coefficientVar;
	}
	
	public double getConfidence() {
		return this.confidence;
	}
	
	public double getNeighborhoodDensity() {
		return this.neighborhoodDensity;
	}
	
	public int getNumberOfNeighbors() {
		return this.numberOfNeighbors;
	}
	
	public double getNextIntensity() {
		return this.nextIntensity;
	}

	public double getRecoveryRate() {
		return this.recoveryRate;
	}
	
	public double getInfluenceRate() {
		return this.influenceRate;
	}
	
	public double getNeighborhoodSize() {
		return this.neighborhoodSize;
	}
	
	public double getAvgNeighborhoodIntensity() {
		return this.avgNeighborhoodIntensity;
	}
	
	public double getSd() {
		return this.sd;
	}

	// returns a list of uniqueId's of all the neighbors, "" is done for formatting
	public String getNeighborList() {
		if (this.neighborsList.size() > 0) {
			return "" + this.neighborsList.toString() + "";
		} else {
			return "";
		}	
	}

// --------	set methods ---------- //
	
	public void setRecoveryRate(double r) {
		this.recoveryRate = r;
	}
	
	public void setInfluenceRate(double i) {
		this.influenceRate = i;
	}
	
	public void setNeighborhoodSize(double n) {
		this.neighborhoodSize = n;
	}
	
	public void setIntensity(double i) {
		this.intensity = i;
	}
	
	public void setNextIntensity(double n) {
		this.nextIntensity = n;
	}
	
	public void setNumberOfNeighbors(int n) {
		this.numberOfNeighbors = n;
	}
	
	public void setCoefficientVar(double c) {
		this.coefficientVar = c;
	}

	public void setIntensityColor() {
		if (this.intensity <= UnrestBuilder.INTENSITY_CAP) {
			this.intensityColor = UnrestBuilder.INTENSITY_CAP - this.intensity;
		} else 
			// everything outside the range of 1-10 is dark red
			this.intensityColor = 11;
	}
	
	public List<Neighbor> getNeighbors(double within) {
		List<Neighbor> neighbors = new ArrayList<Neighbor>();
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext(this);
		try {
			for (Object obj : context) {
				// making sure only objects of class Event are grabbed
				if (obj.getClass() == this.getClass()) {
					Event object = (Event)obj;
					if ((object.alive) && (object.getUniqueId() != this.uniqueId)) {
						double dist = Distance.distance(this, object);
						if (dist <= within) {
							Neighbor n = new Neighbor(object, dist);
							neighbors.add(n);
						}
					}
				}
			}
		} catch (NullPointerException e) {
				 e.printStackTrace();
			}
		return neighbors;
	}

// --------	Intensity Calculation  ---------- //
	
	public double intensify(Event e, List<Neighbor> neighborhood, boolean nPrincipleOne) {
		double summation = 0;
		int beta = neighborhood.size();
		if (nPrincipleOne) {
			beta = 1;
		}
		
		List<Double> intensityList = new ArrayList<Double>();
		this.neighborsList.clear(); 
		
		for (Neighbor n : neighborhood) {
			neighborsList.add(n.getNeighborId());
			double intensityWeight = 0;
			if(UnrestBuilder.NEIGHBORHOOD_SIZE > 0) {
				intensityWeight = (UnrestBuilder.NEIGHBORHOOD_SIZE - n.getDistanceToNeighbor())/UnrestBuilder.NEIGHBORHOOD_SIZE;
			} else {
				intensityWeight = 1;
			}
			intensityList.add(n.getNeighborIntensity() * intensityWeight);
			
//			calculating the total impact on each agent by the neighborhood
			double alpha = Math.pow((n.getNeighborIntensity() - e.getIntensity())/UnrestBuilder.MAXGAP, 2);
			if (n.getNeighborIntensity() > e.getIntensity()) {
				alpha = (n.getNeighborIntensity() - e.getIntensity())/UnrestBuilder.MAXGAP;
			}
			double impact = n.getNeighborIntensity() * ( alpha + Math.pow(1 - n.getDistanceToNeighbor(), 2));
			summation += impact;
		}
		
//		confidence calculation
		this.avgNeighborhoodIntensity = calculateAverage(intensityList);	
		this.neighborhoodDensity = Math.min((double)this.numberOfNeighbors/(double)UnrestBuilder.NEIGHBORDENSITY_THRESHOLD, 1.0);
		this.coefficientVar = getCV(intensityList);
		this.confidence = 0.5 * this.coefficientVar + 0.5 * this.neighborhoodDensity;

		double result = UnrestBuilder.RECOVERY * e.getIntensity() + UnrestBuilder.GAMMA * ((double)summation/(double)beta); 
		return Math.min(result, UnrestBuilder.INTENSITY_CAP);
	}
	
//-------- Scheduled Methods -----------//
	
	@ScheduledMethod(start=0, interval=1, priority = 4)
	public void makeAlive() {
		String currentDayString = UnrestBuilder.currentDay.toString(DateTimeFormat.shortDate());
		String eventDateString = this.eventdate.toString(DateTimeFormat.shortDate());	
		if (currentDayString.equals(eventDateString)){
			this.alive = true;
			this.markSize = UnrestBuilder.MARKER_SIZE;
		}
		if (this.getAlive()) {
			this.eventEndDate = UnrestBuilder.currentDay;
			this.intensity = this.nextIntensity;
		}
	}

	@ScheduledMethod(start=0, interval=1, priority=3)
	public void calculate() {
		if (this.alive) {
			@SuppressWarnings("unchecked")
			Context<Object> context = ContextUtils.getContext(this);
			this.life++;
			if (this.getIntensity() <= UnrestBuilder.DIE_INTENSITY) {
				context.remove(this); // completely removes agent from context
			} else {
				List<Neighbor> neighbors = this.getNeighbors(UnrestBuilder.NEIGHBORHOOD_SIZE);
				this.setNumberOfNeighbors(neighbors.size());
				if (neighbors.size() > 0) {
					this.nextIntensity = this.intensify(this, neighbors, false);
				} else {
					this.confidence = 0.5; // weight of the CV
					this.nextIntensity = UnrestBuilder.RECOVERY * this.getIntensity();
				}
				this.setIntensityColor();
			}
		}
	}
	
//	public void printNeighbors() {
//		List<Neighbor> neighbors = new ArrayList<Neighbor>();
//		neighbors = this.getNeighbors(UnrestBuilder.NEIGHBORHOOD_SIZE);
//		System.out.println("This event: " + this.getUniqueId());
//		for (Neighbor n: neighbors) {
//			System.out.println("Neighbor " + n.getNeighbor().getUniqueId());
//		} 
//	}
	
//------------- Static Helper methods -----------//
	
	public static DateTime stringToDate(String startdate) {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date d1;
		DateTime returnDate = new DateTime();
		try {
			d1 = df.parse(startdate);
			returnDate = new DateTime(d1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return returnDate;
	}

	public static double calculateAverage(List<Double> weightedI) {
		double sum = 0;
		for(double wI: weightedI) {
			sum += wI;
		}
		return sum/(double)weightedI.size();
	}
	
	public static double calculateVariance(List<Double> weightedI) {
		double avg = calculateAverage(weightedI);
		double sumDifferences = 0.0;
		for(double val: weightedI) {
			double diff = val - avg;
			diff *= diff;
			sumDifferences += diff;
		}
		return sumDifferences/(double)weightedI.size();
	}

//	standard deviation
	public static double calculateSd(List<Double> weightedI) {
		return Math.sqrt(calculateVariance(weightedI));
	}
	
//	coefficient of variance
	public static double getCV(List<Double> weightedI) {
		if(weightedI.size() == 0){
			return 0;
		}
		double sd = calculateSd(weightedI);
		double avg = calculateAverage(weightedI);
		return Math.min(sd/avg, 1.0);
	}
	
//	public void convertDate(String startdate) {
//		DateFormat df = new SimpleDateFormat("MM/dd/YY");
//		Date d1;
//		try {
//			d1 = df.parse(startdate);
//			eventdate = new DateTime(d1);
//			eventEndDate = eventdate;
//		} catch (ParseException e) {
//		    e.printStackTrace();
//		}
//	}
	
}
