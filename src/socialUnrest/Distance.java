package socialUnrest;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class Distance {
	private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
	public static double SPATIOTEMPORAL_WEIGHT = 1.0/3;
	public static double SOCIOECONOMIC_WEIGHT = 1.0/3;
	public static double INFRASTRUCTURE_WEIGHT = 1.0/3;
	public static double TEMPORAL_THRESHOLD = 30.0;
	public static double SPATIAL_THRESHOLD = 100.0;
	
	public static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}
    
	public static double haversine_km(
		double startLat,
		double startLong,
		double endLat,
		double endLong
	) {
	double dLat  = Math.toRadians(endLat - startLat);
	double dLong = Math.toRadians(endLong - startLong);
	
	startLat = Math.toRadians(startLat);
	endLat   = Math.toRadians(endLat);
	
	double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	
//	returning values in meters
	return EARTH_RADIUS * c * 1000;
	}
	
	public static double socioeconomicDistance(Event e1, Event e2) {
		return (
			Math.abs(e1.literacyRate - e2.literacyRate)
			+ Math.abs(e1.workerPopulation - e2.workerPopulation)
		) / 2;
	}
	
	public static double infrastructureDistance(Event e1, Event e2) {
		double ret1 = (
			Math.abs(e1.hospitalDensity - e2.hospitalDensity) 
			+ Math.abs(e1.policeDensity - e2.policeDensity)
			+ Math.abs(e1.postalDensity - e2.postalDensity)
			+ Math.abs(e1.schoolDensity - e2.schoolDensity)
			+ Math.abs(e1.collegeDensity - e2.collegeDensity)
			+ Math.abs(e1.universityDensity - e2.universityDensity)
		);
		
		double ret2 = (
			Math.abs(e1.hospitalClosest - e2.hospitalClosest)
			+ Math.abs(e1.policeClosest - e2.policeClosest)
			+ Math.abs(e1.postalClosest - e2.postalClosest)
			+ Math.abs(e1.schoolClosest - e2.schoolClosest)
			+ Math.abs(e1.collegeClosest - e2.collegeClosest)
			+ Math.abs(e1.universityClosest - e2.universityClosest)
		);
		return (ret1 + ret2) / 12;
	}

public static double temporal(Event e1, Event e2) {
	
	double gap_startdate = Math.abs((double)Days.daysBetween(e1.eventdate.withTimeAtStartOfDay(), 
			e2.eventdate.withTimeAtStartOfDay()).getDays());

	double gap_enddate = Math.abs((double)Days.daysBetween(e1.eventEndDate.withTimeAtStartOfDay(), 
			e1.eventEndDate.withTimeAtStartOfDay()).getDays());
	
	double eucledian = Math.sqrt(Math.pow(gap_startdate, 2) + Math.pow(gap_enddate, 2));
	double manhattan = gap_startdate + gap_enddate;
	double span = (double)Days.daysBetween(
			minDate(e1.eventdate, e2.eventdate).withTimeAtStartOfDay(),
			maxDate(e1.eventEndDate, e2.eventEndDate).withTimeAtStartOfDay()
			).getDays() + 1.0;
	
	double gap = (double)Days.daysBetween(
			maxDate(e1.eventdate, e2.eventdate).withTimeAtStartOfDay(),
			minDate(e1.eventEndDate, e2.eventEndDate).withTimeAtStartOfDay()
			).getDays();
	if (maxDate(e1.eventdate, e2.eventdate).withTimeAtStartOfDay().isBefore(minDate(e1.eventEndDate, e2.eventEndDate).withTimeAtStartOfDay())) {
		gap = -1 * (gap + 1);
	} else {
		gap = (gap - 1);
	}
	double gap_metric = (gap/span);
	double closeness_metric = 0;
	if(manhattan == 0) {
		closeness_metric = 1;
	} else {
		closeness_metric = eucledian/manhattan;
	}
//	double closeness = Math.sqrt(eucledian/(manhattan * span));
	double temporal = (gap_metric + closeness_metric)/2;
	if (closeness_metric >= 0.0 & closeness_metric <= 1.0) {
	}else {
		System.out.println("eucledian: " + eucledian + ", manhattan: " + manhattan);
	}
	return (temporal);
}
	
	public static double spatiotemporal(Event e1, Event e2) {
		double spatial = haversine_km(e1.lat, e1.lon, e2.lat, e2.lon);
		double spatial_normalized, weight_temporal, spatiotemporal;
		// this portion assumes that 100000 (100km) is the threshold after
		// which all distances are considered equally separated (max distance)
		if (spatial > SPATIAL_THRESHOLD * 1000) spatial_normalized = 1;
		else spatial_normalized = spatial/(SPATIAL_THRESHOLD * 1000);
		double temp = 0;
		temp = temporal(e1, e2);
		// among all pairs of events
		// this portion simply makes the event that occurs first as the main event
		// and the event that occurs second as the second event
		if (e1.eventdate.isAfter(e2.eventdate)) {
			weight_temporal = e2.population / (e1.population + e2.population);
		} else if (e2.eventdate.isAfter(e1.eventdate)) {
			weight_temporal = e1.population / (e1.population + e2.population);
		} else if (e1.population > e2.population) {
			weight_temporal = e1.population / (e1.population + e2.population);
		} else {
			weight_temporal = e2.population / (e1.population + e2.population);
		}
		spatiotemporal = weight_temporal * temp + (1 - weight_temporal) * spatial_normalized;
		return spatiotemporal;
	}
	
	public static double distance(Event e1, Event e2) {
		double d_spatiotemporal = spatiotemporal(e1, e2);
		double d_socioeconomic = socioeconomicDistance(e1, e2);
		double d_infrastructure = infrastructureDistance(e1, e2);
		double d_final = SPATIOTEMPORAL_WEIGHT * d_spatiotemporal
			+ SOCIOECONOMIC_WEIGHT * d_socioeconomic
			+ INFRASTRUCTURE_WEIGHT * d_infrastructure;
		return Math.round(d_final * 10000.0) / 10000.0;
	}
	
	public static DateTime maxDate(DateTime d1, DateTime d2) {
		if (d1.isBefore(d2)){
			return d2;
		} else {
			return d1;
		}
	}
	
	public static DateTime minDate(DateTime d1, DateTime d2) {
		if (d1.isBefore(d2)){
			return d1;
		} else {
			return d2;
		}
	}
}