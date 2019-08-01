package socialUnrest;

import socialUnrest.Distance;
import socialUnrest.Event;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import socialUnrest.Observer;

public class UnrestBuilder implements ContextBuilder<Object> {
	public static DateTime startDay = Event.stringToDate("01/01/2014");
	public static DateTime currentDay;
	public static DateTime lastDay = Event.stringToDate("12/31/2014");
	public static int MARKER_SIZE = 10;
	public static double NEIGHBORHOOD_SIZE;
	public static double INTENSITY_CAP = 10.0;
	public static double RECOVERY;
	public static double GAMMA;
	public static double DIE_INTENSITY;
	public static double MAXGAP;
	public static int MOVE_STARTDAY_BY;
	public static int HISTORYWINDOW;
	public static int NEIGHBORDENSITY_THRESHOLD;
	
	/* (non-Javadoc)
	 * @see repast.simphony.dataLoader.ContextBuilder#build(repast.simphony.context.Context)
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		
//		should match with /SocialUnrest.rs/parameters.xml 
		Parameters params = RunEnvironment.getInstance().getParameters();
		NEIGHBORHOOD_SIZE = (double)params.getValue("neighborhood");
	    Distance.SPATIOTEMPORAL_WEIGHT = (double)params.getValue("w_spatiotemporal");
	    Distance.SOCIOECONOMIC_WEIGHT = (double)params.getValue("w_socioeconomic");
	    Distance.INFRASTRUCTURE_WEIGHT = (double)params.getValue("w_infrastructure");
	    
	    GAMMA = (double)params.getValue("gamma");
	    RECOVERY = (double)params.getValue("recoveryRate");
	    DIE_INTENSITY = (double)params.getValue("dieIntensity");
	    MAXGAP = INTENSITY_CAP - DIE_INTENSITY;
	    MOVE_STARTDAY_BY = (int)params.getValue("moveStartDayBy");
	    HISTORYWINDOW = (int)params.getValue("historyWindow");
	    NEIGHBORDENSITY_THRESHOLD = (int)params.getValue("neighborDensityThreshold");
	    
	    startDay = Event.stringToDate("01/01/2014").plusDays(MOVE_STARTDAY_BY);
		currentDay = startDay;
	  
//	    should match with /SocialUnrest.rs/context.xml 
		context.setId("SocialUnrest");
		
//		Create a Geography based context
		GeographyParameters<Object> geoparams = new GeographyParameters<Object>();
		GeographyFactory factory = GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> geography = factory.createGeography("Geography", context, geoparams);
		GeometryFactory fac = new GeometryFactory();
		
//		Specify how long the simulation should run here (100 ticks)
		RunEnvironment.getInstance().endAt(100);
		
//		Create an observer agent
		Observer o = new Observer();
		context.add(o);

//		Read the file and create events (agents) inside the context
		List<Event> newEvents = readData("./data/data_gdelt_india_2014_clean_repast_ready_karnataka.csv");
		for(Event evt : newEvents) {
			context.add(evt); 
			Coordinate coord = new Coordinate(evt.lon, evt.lat);
			Point geom = fac.createPoint(coord);
			geography.move(evt, geom);
		}
		
		return context;
	}

//	import the event data file, and return array of events
	public List<Event> readData(String filePath) {
		List<Event> events= new ArrayList<Event>();
		BufferedReader br = null;	
		try {
			String sCurrentLine;
			String dir = filePath;
			File eventFile = new File(dir);
			br = new BufferedReader(new FileReader(eventFile));
			br.readLine();
			while ((sCurrentLine = br.readLine()) != null) {
				String[] sCurrentEvent = sCurrentLine.replace("\"", "").split(",");
				double lon = Double.parseDouble(sCurrentEvent[0]);
				double lat = Double.parseDouble(sCurrentEvent[1]);
				long uniqueID = Integer.parseInt(sCurrentEvent[2]);
				String eventdate = sCurrentEvent[3];
				String eventCategory = sCurrentEvent[4];
				double population = Double.parseDouble(sCurrentEvent[5]);
				double literacyRate = Double.parseDouble(sCurrentEvent[6]) ;
				double workerPopulation = Double.parseDouble(sCurrentEvent[7]) ;
				double policeClosest = Double.parseDouble(sCurrentEvent[8]) ;
				double postalClosest = Double.parseDouble(sCurrentEvent[9]) ;
				double hospitalClosest = Double.parseDouble(sCurrentEvent[10]) ;
				double universityClosest = Double.parseDouble(sCurrentEvent[11]);
				double collegeClosest = Double.parseDouble(sCurrentEvent[12]) ;
				double schoolClosest = Double.parseDouble(sCurrentEvent[13]) ;
				double policeDensity = Double.parseDouble(sCurrentEvent[14]) ;
				double postalDensity = Double.parseDouble(sCurrentEvent[15]) ;
				double hospitalDensity = Double.parseDouble(sCurrentEvent[16]) ;
				double universityDensity = Double.parseDouble(sCurrentEvent[17]) ;
				double collegeDensity = Double.parseDouble(sCurrentEvent[18]) ;
				double schoolDensity = Double.parseDouble(sCurrentEvent[19]) ;	
				double intensity = 0;
				
//				this assignment is based on my analysis shown in my thesis
				switch(eventCategory) {
				case "Appeal":
					intensity = 1.178; 
					break;
				case "Demand":
					intensity = 4.655; 
					break;
				case "Threaten":
					intensity = 4.655; 
					break;
				case "Protest":
					intensity = 4.655; 
					break;
				case "Coerce":
					intensity = 5.159; 
					break;
				case "Assault":
					intensity = 6.569; 
					break;
				case "Fight":
					intensity = 3.761; 
					break;
				case "Engage in UMV":
					intensity = 10.1840; 
					break;
				} 
				
				Event e = new Event(
					intensity, 
					lon, 
					lat, 
					uniqueID, 
					eventdate, 
					population,
					literacyRate, 
					workerPopulation,
					policeClosest, 
					postalClosest,
					hospitalClosest, 
					schoolClosest,
					collegeClosest,
					universityClosest,
					policeDensity, 
					postalDensity,
					hospitalDensity, 
					schoolDensity,
					collegeDensity,
					universityDensity
				);
				e.setInfluenceRate(GAMMA);
				e.setRecoveryRate(RECOVERY);
				e.setNeighborhoodSize(NEIGHBORHOOD_SIZE);
				e.setNextIntensity(intensity);
				events.add(e);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return events;
	}
	
//	move the current day in the context forward
	@ScheduledMethod(start=1, interval=1, priority = 5)
	public static void moveDate() {
		currentDay = currentDay.plusDays(1);
	}	
}