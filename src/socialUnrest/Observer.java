package socialUnrest;

//observer agent calculates the average intensity and total intensity
//we do this because we add all the agents into the context at the beginning
//so data collection methods of Repast will pull data from all of the existing agents

import socialUnrest.UnrestBuilder;
import socialUnrest.Distance;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Observer {
	private DateTime today, tomorrow;
	private double averageIntensity, expectedAverageIntensity;
	private double totalIntensity, expectedTotalIntensity; 
	private double averageNumberOfNeighbors, averageConfidence;
	private int eventCount;
	
	private static double weightSpatiotemporal = Distance.SPATIOTEMPORAL_WEIGHT;
	private static double weightSocioeconomic = Distance.SOCIOECONOMIC_WEIGHT;
	private static double weightInfrastructural = Distance.INFRASTRUCTURE_WEIGHT;
	private static double neighborhood_radius = UnrestBuilder.NEIGHBORHOOD_SIZE;
	private static double recoveryRate = UnrestBuilder.RECOVERY;
	private static double influenceRate = UnrestBuilder.GAMMA;
	private static int daysRealData = UnrestBuilder.MOVE_STARTDAY_BY;
	
	public void setTotalIntensity() {
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext(this);
		
		// had to add this just to check the class of other objects
		Event e1 = new Event();
		
		double total_intensity = 0.0;
		double total_confidence = 0.0;
		double expected_total_intensity = 0.0;
		int count = 0;
		int total_neighbors = 0;
		for (Object obj : context) {
			if(obj.getClass() == e1.getClass()){
				Event e = (Event)obj;
				if(e.getAlive()) {
					total_neighbors += e.getNumberOfNeighbors();
					total_intensity += e.getIntensity();
					expected_total_intensity += e.getNextIntensity();
					total_confidence += e.getConfidence();
					count += 1;
				}
			}
		}
		this.eventCount = count;
		this.totalIntensity = total_intensity;
		this.expectedTotalIntensity = expected_total_intensity;
		this.averageNumberOfNeighbors = (double)total_neighbors/(double)count;
		this.averageConfidence = (double)total_confidence/(double)count;
	}
	
	public void setAverageIntensity() {
		if (this.eventCount == 0) {
			this.averageIntensity = 0;
			this.expectedAverageIntensity = 0;
			this.averageNumberOfNeighbors = 0;
		} else {
			this.averageIntensity = (this.totalIntensity/(double)this.eventCount);
			this.expectedAverageIntensity = (this.expectedTotalIntensity/(double)this.eventCount);
		}
	}
	
	public void setGlobalVariables() {
		this.today = UnrestBuilder.currentDay;
		this.tomorrow = UnrestBuilder.currentDay.plusDays(1);
		Observer.weightSpatiotemporal = Distance.SPATIOTEMPORAL_WEIGHT;
		Observer.weightSocioeconomic = Distance.SOCIOECONOMIC_WEIGHT;
		Observer.weightInfrastructural = Distance.INFRASTRUCTURE_WEIGHT;
		Observer.neighborhood_radius = UnrestBuilder.NEIGHBORHOOD_SIZE;
		Observer.recoveryRate = UnrestBuilder.RECOVERY;
		Observer.influenceRate = UnrestBuilder.GAMMA;
		Observer.daysRealData = UnrestBuilder.MOVE_STARTDAY_BY;
	}
	
	public void set_date() {
		this.today = UnrestBuilder.currentDay;
		this.tomorrow = UnrestBuilder.currentDay.plusDays(1);
	}
	
	public void setToday() {
		this.today = UnrestBuilder.currentDay;
	}
	
	public void setTomorrow(){
		this.tomorrow = UnrestBuilder.currentDay.plusDays(1);
	}
	
	public String getToday() {
		DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy");
		return UnrestBuilder.currentDay.toString(format);
	}
	
	public String getTomorrow(){
		DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy");
		return UnrestBuilder.currentDay.plusDays(1).toString(format);
	}
	
	public double getAverageNumberOfNeighbors(){
		return this.averageNumberOfNeighbors;
	}
	
	public double getAverageIntensity() {
		return this.averageIntensity;
	}
	
	public double getExpectedAverageIntensity() {
		return this.expectedAverageIntensity;
	}
	
	public double getTotalIntensity() {
		return this.totalIntensity;
	}
	
	public double getExpectedTotalIntensity() {
		return this.expectedTotalIntensity;
	}
	
	public int getEventCount() {
		return this.eventCount;
	}
	
	public double getRecoveryRate() {
		return Observer.recoveryRate;
	}
	
	public double getInfluenceRate() {
		return Observer.influenceRate;
	}
	
	public int getDaysRealData() {
		return Observer.daysRealData;
	}
	
	public void setDaysRealData(int n) {
		Observer.daysRealData = n;
	}
	
	public double getNeighborhoodRadius() {
		return Observer.neighborhood_radius;
	}
	
	public double getWeightSpatiotemporal() {
		return Observer.weightSpatiotemporal;
	}
	
	public double getWeightSocioeconomic() {
		return Observer.weightSocioeconomic;
	}
	
	public double getWeightInfrastructural() {
		return Observer.weightInfrastructural;
	}
	
	public double getConfidence() {
		return this.averageConfidence;
	}
	
	@ScheduledMethod(start=0, interval=1, priority = 1)
	public void update_values() {
		this.setGlobalVariables();
		this.setTotalIntensity();
		this.setAverageIntensity();
	}
}
