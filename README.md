# Social Unrest Simulation Model

This model was built by the SURGE team at the University of Nebraska-Lincoln as part of their research on analyzing and anticipating and social unrest. In our framework, each individual social unrest event acts as an agent that can communicate with other agents within its predefined neighborhood.

The software is built upon the [Repast Symphony (RS) 2.6](https://repast.github.io/) framework. It is a open-source agent-based modelling and simulation platform, it's an interactive Java-based modeling system designed for use on workstations and small computing clusters. A getting started guide is can be found at: https://repast.github.io/docs/RepastJavaGettingStarted.pdf. It is reccomended that the user go through this guide before starting a new project or modifying existing ones. I will still try to cover the necessary details in this document.

We will be working with spatio-temporal data, this means the data has geospatial components (longitude, latitude) and a time element, among other variables. To make calculations in time, I am using the [Joda-Time API](https://www.joda.org/joda-time/). The spatial distances will be calculated using the [Haversine formula](https://en.wikipedia.org/wiki/Haversine_formula). In Repast Symphony framework, the enviroment is initalized in a class that extends the class `ContextBuilder`. In our project, the initializing class is called `UnrestBuilder`. This class returns a `Context` object, this the platform upon which all agents exist. Our agents are the `Event` objects. We also created an `Observer` object due to restrictions of the Repast Framework to store data (please see class description below for more details).

## `Event` Class

Each social unrest event is an agent, an object of the `Event` class. Each event in general has the following information.
| Parameters | Description |
| :--- |:---|
| Location | longitude and latitude |
| Event-date | date of occurrence of the event(day) |
| Intensity | Energy associated with it, representing its severity. |
| Socioeconomic variables and Area | Socioeconomic variables such as the literacy rates or employment rates are calculated for a region or area |
| Infrastructure variables and spatial radius | These variables are measures of how close infrastructure objects are to an event and how many of these infrastructure objects are within a certain radius of any event |
| Neighborhood and Radius | Any agent _e<sub>1</sub>_ that is within a distance of _R_ from another agent _e<sub>2</sub>_, is considered a neighbor of the agent _e<sub>2</sub>_. _R_ can only be between [0 - 1]. |

Repast allows us to save results from our simulation into a txt file, but the data must come from a single object. For the sake of data collection, I have had to add some seemingly unnecessary variables to this class such as `recoveryRate`, `influenceRate`, `neighborhoodSize`, `numberOfNeighbors`, `neighborsList`, `neighborhoodDensity`, `coefficientVar`, `avgNeighborhoodIntensity`, `sd` and `confidence`. Variable `markSize` and `intensityColor` were added to aid the visualization. Visualization of markers (representing agents) can have a size variation of [1-10]. We created the variable `markSize` for our `Event` class to declare what size we want our agents to be on the map. All events are initialized with a `markSize` of 1, once the event is alive it is changed to whatever the `MARKER_SIZE` is defined in the `UnrestBuilder` class.

The intensity for the next day is calculated by the `intensify()` method. Note that we use some constant values defined in the `UnrestBuilder` thoughout the calculations. Along with the intensity calculation, we now also calculate the confidence of preditions. The formulas and discussion for the intensity and confidence calculation can be found [HERE](https://github.com/sudbasnet/SocialUnrestMAS/confidenceFactor.pdf). If the resulting intensity is greater than the maximum intensity value possible (`UnrestBuilder.INTENSITY_CAP`), then the max value is returned, otherwise the impact is returned. This returned value is then assigned to the `nextIntensity` variable of the `Event` object instead of the `intensity` variable. On the next day (tick), the `intensity` variable is assigned the `nextIntensity` value for further calculations.

There are two scheduled methods in this class, `makeAlive()` and `calculate()`. These scheduled methods are run for each object of the `Event` class on each tick. The `makeAlive()` method sets the `alive` property of the object to `true` if the event's start date is same as the current day of the simulation. Note that the current date of the simulation is maintained by a scheduled method `moveDate()` in the `UnrestBuilder` class. The `calculate()` collects the neighborhood and calls `intensify()`. Scheduled methods are defined as:

```java
@ScheduledMethod(start=0, interval=1, priority = 4)
public void makeAlive() {
    ...
}
```

The `start` parameter indicates which tick should the method start executing at, `interval` parameter should be self-explanatory, higher `priority` means it gets executed first.

## `Distance` Class

The `Distance` class simply implements the distance formula for spatiotemporal, socioeconomic and infrastructural distances. Details regarding the distance formula can be found [HERE](https://github.com/sudbasnet/SocialUnrestMAS/distanceFunction.pdf). Note that we use Haversine formula here to calculate spatial distances in meters or kilometers. Some static values for weights and thresholds are defined during class instantiation. We are assigning equal weights to all distances and even in each sub-distance type calculation. The infrastructure weights are divided by 12 at the end because there are 12 variables in total, so imagine assigning a weight of 1/12 to each infrastructure variable. CHANGES SHOULD BE MADE IN DISTANCE WEIGHTING based on social theories or after extensive data-analysis.

## `UnrestBuilder` Class

As introduced earlier, the `UnrestBuilder` implements the Repast Symphony class `ContextBuilder`, it must contain a `build()` method that returns a `Context` object. I have described many constant/static variables in this class.

Variables `NEIGHBORHOOD_SIZE`, `RECOVERY`, `GAMMA`, `DIE_INTENSITY`, `MAXGAP`, `MOVE_STARTDAY_BY`, `HISTORYWINDOW`, and `NEIGHBORHOOD_THRESHOLD` are all assigned values through the Repast GUI. The descriptions of these paameters are written in the **parameters.xml** file in the **SocialUnrest.rs** folder. For example, the "Death Intensity" and "Recovery Rate" are written as:

```xml
<parameters>
	<parameter name="dieIntensity" displayName="Death Intensity" type="double"
        defaultValue="0.15"
        isReadOnly="false"
        converter="repast.simphony.parameter.StringConverterFactory$DoubleConverter"
	/>
    <parameter name="recoveryRate" displayName="Rate - Recovery" type="double"
        defaultValue="0.4"
        isReadOnly="false"
        converter="repast.simphony.parameter.StringConverterFactory$DoubleConverter"
    />
</parameters>
```

After giving the input, the parameters are pulled into the context

```java
Parameters params = RunEnvironment.getInstance().getParameters();

RECOVERY = (double)params.getValue("recoveryRate");
DIE_INTENSITY = (double)params.getValue("dieIntensity");
```

We can generate multiple contexts, so it is important to set the context id in the **context.xml** correctly. Also set the projection types. Please see official Repast documentation for details on projections. We use the geography-type projection.

```xml
<context id="SocialUnrest" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"                       xsi:noNamespaceSchemaLocation="http://repast.org/scenario/context">
  	<projection id="Geography" type="geography"/>
</context>
```

```java
context.setId("SocialUnrest");
GeographyParameters<Object> geoparams = new GeographyParameters<Object>();
GeographyFactory factory = GeographyFactoryFinder.createGeographyFactory(null);
Geography<Object> geography = factory.createGeography("Geography", context, geoparams);
GeometryFactory fac = new GeometryFactory();
```

In our project we import preprocessed datasets and create events out of it. This is also done in the `UnrestBuilder` using the `readData(String filePath)` method. The file is then read and all the events in the given file is added to the context as agents. The agents all have the `alive` property set to `false` and `markSize` set to `1` in the beginning.

## `Neighbor` Class

Neighbor is similar to an Event, I only created it to aid in calculations.

## `Observer` Class

My original idea for importing agents into the context was to save the details from the events data-file into a hash-map, where the event's date is the key and a list of events is the value. On each tick we simply pull the list of `Event`s from this hash-map and make their `alive` property `true`. But, I could not figure this out because the file needed to be read in the `UnrestBuilder`, and there's no hook that can grap the `context` within the `UnrestBuilder`. since I couldn't grab the `context` within the `UnrestBuilder` I could add agents to it. I can only create agents at the start. This is why during each calculation, I check for `if(this.alive) {...}`.

It was really cumbersome to calculate aggregate values from the agents, so I created an `Observer` agent type, a single `Observer` agent is added to the `context` at the start. Now it can constantly grab all the objects in the `context`, check which one of them is alive, and calculate the aggregate. It's not the most efficient way of doing things, but somethings are just limited.

# Run a simple simulation

1. Install the Repast Symphony software for Eclipse IDE. [Installation Docs Here](https://repast.github.io/download.html).
2. Open project **SocialUnrest** with Eclipse IDE.
3. Press the drop-down menu in the run buttton and choose "SocialUnrest Model", this will open up the RepastMain display.
4. The project includes the xml files for display setup.
5. Click the initialize button, this will load the data from the filePath, given in the `UnrestBuilder`.
6. Change different simuation parameters and click Play. The change in the intensties is shown as change in the color of the events.

You can add GIS layers from the display panel. Again, reading the getting-started document is reccomended.

# How to run this model in batch mode on a HPC server

Batch running this model on local or even on a remote machine is pretty straight-forward and you can just refer to the online guide to do so. But I struggled for a while to get this running on [HCC](https://hcc.unl.edu/) servers because of duo authentication and the fact that it was still my computer sending instructions to the remote server. I wanted to just run everything on the HCC and not use my pc at all. I considered converting the project to C++ and using another version of Repast that is especially designed for high performance computing. But I discovered that several packages required were missing or there were version conflicts with the HCC.
So we will continue to use the Java version, and do the following.

- set up the batch parameters.
- click on the **Create Model Archive for Batch Runs** button on the top (shown in figure below). This will create a **complete_model.jar** file.
- create a file **local_batch_run.properties** and put the info below. The information should mostly be self self-explanatory, you can change the instance count and other directories. I put this file in the same folder as the archive jar far that I get from the 2nd step.

```
unrolled.batch.parameter.file=./unrolledParamFile.txt
scenario.directory=./scenario.rs
working.directory=./
repast.lib.directory=./lib
instance.count = 4
batch.parameter.file = ./scenario.rs/batch_params.xml
vm.arguments = -Xmx512M
```

- now you can create and run a .slurm file such as **repastJob.slurm**

```
#!/bin/sh
#SBATCH --time=10:00:00          # Run time in hh:mm:ss
#SBATCH --ntasks-per-node=4
#SBATCH --nodes=4
#SBATCH --mem-per-cpu=16000       # Maximum memory required per CPU (in megabytes)
#SBATCH --job-name=RepastJob
#SBATCH --error=./batch.%J.err
#SBATCH --output=./batch.%J.out

module load java
jar xf complete_model.jar
java -cp "./lib/*" repast.simphony.batch.LocalDriver local_batch_run.properties
```
