package socialUnrest;

import socialUnrest.Event;

public class Neighbor {
	private Event neighbor;
	private double distanceToNeighbor;

	public Neighbor(Event e, double d){
		this.neighbor = e;
		this.distanceToNeighbor = d;
	}
	
	public long getNeighborId() {
		return this.neighbor.getUniqueId();
	}
	
	public Event getNeighbor(){
		return this.neighbor;
	}
	
	public double getDistanceToNeighbor(){
		return this.distanceToNeighbor;
	}
	
	public double getNeighborIntensity(){
		return this.neighbor.getIntensity();
	}
}
