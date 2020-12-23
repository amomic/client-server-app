# Objektorientierte Programmierung 2 KU Assignment 2020

# Overview

The goal of this course is to get familiar with advanced object oriented concepts by implementing a two part system for processing and visualizing real world sensor data.

The ecosystem you'll have to work in is comprised of two main components:

* An **Analysis Server** takes requests from one or more clients that are connected to it over the (internal) network and delivers processed data. The origin of the sensor data are .csv-files, each file containing data from one location, and should be cached in a useful way. The requested data will be processed on the Server and is sent to the requesting client for visualization.
* **Clients** are operated via comand line interface (CLI) and visualize the data in text, image or video.

Throughout this course, you will implement the two components using the Java 11 programming language along with a simple Framework being provided by us.

# Important

## Timeline

* **Assignment 0** (no due date, no points)
  * Reading the whole assignment description, setting up your workspace and getting ready.
* **Assignment 1** (Deadline 22.11.2020, 23:59, 50 points + 10 bonus points)
  * First functional version of the Client and Analysis Server. Retrieval and simple processing (sampling, min/max/median/mean) of data through the Analysis Server. Text and image visualizations on the client.
* **Assignment 2** (Deadline 10.01.2021, 23:59, 50 points + 10 bonus points)
  * Advanced processing on the Analysis Server by clustering with SOM-algorithm. Video visualizations on the client.

## Requirements
* you are allowed to work in a group up to 4 students. You should share the workload across every student evenly, in the end every student must know everything about your submission.
* plagiarism will not be tolerated
* every student has to push to the git, working from one computer only is not target-aimed
* you are not allowed to push the .csv files to the repository. Do not remove lines from the `.gitignore` file!

## Assignment 0 - Setting up Your Workspace

This assignment has no due date and won't be graded, but you should nevertheless get it done ASAP and contact us in case you encounter difficulties.

## Team Registration

By the time you read this, you should have already formed teams of up to 4 students. The teams stay the same throughout the course. If you are still looking for one, have a look at [Team mates search](https://tc.tugraz.at/main/mod/forum/view.php?id=137963). Please tell us if a teammate drops out.

## System Requirements

You should have the following programs installed on your machine:

* Git
* Docker (our script will create a network and load following images - see *Starting the System* below)
  * Java 11
  * Maven
* A Java IDE such as Eclipse, IntelliJ or NetBeans
* Download more RAM

Docker is not required but we recomend to use ist, since we are going to test your submission with the same docker images. You can install a local version of Java for better debugging, but do not underestimate the effort it takes to *correctly* upgrade/downgrade to Java 11 if you already use an other Java version!

## Checking out the Framework

By Monday you should have been granted access to a git repository designated to your team. All your progress/commits must be documented in this repository, so you aren't allowed to use SVN or another Git service. Our GitLab instance can be found at <https://student.cgv.tugraz.at/>. To sign in, click "Sign in with TUGRAZ online (Students)". Either use SSH (recommended) for authenticating yourself in Git or HTTPS. For the former you'll need to generate an SSH keypair (if you haven't already) and add it to your [GitLab Profile](https://student.cgv.tugraz.at/profile/keys).

Open a shell on your machine and issue the following commands:

```
# Clone your team repository, NOT the framework.
git clone git@student.cgv.tugraz.at:oop2_2020/XXX.git # [where XXX is your Assignment Team number]
cd XXX

# Set your Git credentials. Use "--global" instead of "--local" if you
# use Git exclusively at TUGraz.
git config --local user.name "Max Muster"
git config --local user.email "max.muster@student.tugraz.at"

# Add framework as second remote repository.
git remote add framework git@student.cgv.tugraz.at:oop2.2020/framework.git
git pull framework

# Push the framework to your own remote repository.
# This counts as the first partial contribution and results in getting a grade.
git push -u origin master
```

All other team members can access the team repository with:

```
# Clone your team repository, NOT the framework.
git clone git@student.cgv.tugraz.at:oop2_2020/XXX.git # [where XXX is your Assignment Team number]
cd XXX

# Set your Git credentials. Use "--global" instead of "--local" if you
# use Git exclusively at TUGraz.
git config --local user.name "Max Muster"
git config --local user.email "max.muster@student.tugraz.at"
```

If everything has worked so far, you should see the contents of the framework in your team repository in GitLab.

To pull changes from the framework, issue the command `git pull framework`. Changes to the framework will be announced on [TeachCenter Announcements](https://tc.tugraz.at/main/mod/forum/view.php?id=52959).

## Starting the System

The root directory of your repository contains a variety of scripts. Find out what they do and adapt them to your needs by opening them in a text editor before executing them! We recommend to run and build your project from the command line, since we'll do the same when grading.

* `mvn clean install` - Build the project. The resulting build artifacts are located in the directories `./analysis-server/target` and `./client/target`.
* `./docker/createDocker.sh` - Creates multiple Dockercontainer with the same Environment we use to test the assignments.
* `./docker/mvn.sh` - Executes `mvn clean install` inside Docker.
* `./docker/server.sh` - Starts the Analysis Server inside Docker.
* `./docker/client.sh <COMMAND> <ARGS>` - Connects to the Analysis Server inside Docker and performs one command.
* `./docker/client.sh` - Connects to the Analysis Server inside Docker and opens cli to enter commands.
* `./docker/debugclient.sh` - Same as client.sh with additional debugging functionality
* `./docker/debugserver.sh` - Same as server.sh with additional debugging functionality
Try running these commands to check if you can build the project and if you have installed all components correctly. Make sure the scripts are executable (`chmod +x ./docker/<filename>`).

For more docker information you can have a look [here](docker/DOCKER.md).

## Setting up Your IDE

Which IDE you should use depends on your personal preferences.

### Eclipse

Go to File -> Import -> Maven -> Existing Maven Projects -> Next and select the root of your repository as "Root Directory". Then click "Finish". You should see 4 new entries in the Package Explorer:

* analysis-server
* client
* shared
* oop2

The last one merely represents the maven container of the first three.

For each of the first three projects you should configure the following in *Right Click* -> Properties:

* Java Compiler -> Select Java Version 11
* Java Build Path -> Libraries -> JRE System Library -> Edit -> Select Java 11
* Java Editor -> Save Actions -> Configure Workspace Settings ... -> Check "Perform the selected actions on save", "Format source code", "Format edited lines", "Organize Imports", "Additional actions"
* Java Code Style -> Formatter -> Configure Workspace Settings ... -> Create a formatter profile in consistence with the code style of the framework

Now select the `oop2` project and click *Green Play Button* -> Run As -> Maven build -> Goals: "clean install" -> Apply, Run. If you have everything set up correctly, there shouldn't be any build errors and the current branch should be displayed next to the project names.

Eclipse also offers a convenient Git GUI. To see the commit history, *Right Click Project* -> Team -> Show in History. Select "Show All Branches and Tags", which is represented by an icon on the top right of the commit list.

### IntelliJ

We also recommend the Jetbrains IntelliJ IDE for develpment. You can get this IDE for free by applying at <https://www.jetbrains.com/shop/eform/students>. Install IntelliJ IDEA Ultimate with the default settings (make sure that Maven is installed alongside IntelliJ).

Using Jetbrains IntelliJ, click on *Open*, navigate to the root of your repository, select *pom.xml*, and click *ok*. You should be prompted whether you want to open this file as a project. Do so. For the Project settings, select Java 11 as the language level and configure JDK11 for your project. Using the maven targets *clean* and *install* you can build the project.

The framework is using Project Lombok to generate getters and setters for data classes. It will work without any additional setup on your side, but IntelliJ will show errors (which can be ignored) if you don't install the `Lombok` plugin.\
For a quick overview of what Lombok does check out https://projectlombok.org/

## When You're Done ...

... with setting everything up, help your teammates with doing the same and then get some sleep. If you encounter any problems, contact us ASAP.

# Framework

The framework is composed of a Maven container holding 3 child projects with different purposes. Both **analysis-server** and **client** depend on **shared**, which is designated for shared data classes such as DataPoint, DataSeries, utilities or packages. During the build process, a non-executable JAR is created from **shared**, which has no use for you. The other two projects are exactly what their name says. **analysis-server** and **client** compile to executable JARs and hold all business logic used either by the Analysis Server or the Client. Do not introduce additional dependencies between those 3 projects. If a piece of code is needed by both Analysis Server and Client, put it in **shared**. Many useful libraries are already included in **pom.xml**. In case you want to use other third party libraries, ask for our permission.

Do not change the general file structure given in the framework. I.e. do not move the root of the maven container and do not rename projects or change their version. You are free however to add new packages or source files as long as your submission compiles and fulfills its purpose.


There is a **Logger** class provided by us. Use each function as described in the comments in the `Logger.java` file. Analysis-Server and client have to call those functions correctly. You have to use the functions as described and you are not allowed to modify the parameters or create new functions, since we will replace this file for our internal testing and final grading. If you want to log additional stuff, create a development logging class that suits you, but please deactivate it in the final submission. Beware, if you execute the commands, calculations or caching correctly, but do not log them as required, you will get deductions.

## Shared

The **shared** projects contains a variety of helpful classes. `Util` contains mostly code for parsing and formatting timestamps. The immutable class `Sensor` represents the identifier for a sensor, which is described by the id, type, latitude, longitude, location and metric of the sensor. `DataPoint` holds a single sensor reading, consisting of a timestamp and a value.

The class `DataSeries` represents a time series of sensor data. Instead of being implemented as a `List<DataPoint>`, it has a more complex, but efficient structure. A data series has a start time (inclusive), an end time (exclusive) and a number of values spaced within a given interval. The time series might have gaps in it, where no sensor reading is present. The end time can be calculated from the start time and the length of the value array. For example, if the series starts at `2019-10-01 00:00:00`, has 24 values and an interval of 60 minutes, the end time is `2019-10-02 00:00:00`.


## Analysis Server

The entry point of **analysis-server** is the method `at.tugraz.oop2.server.ServerMain.main(String... args)`. The program takes 2 arguments:

* `<port>` - The port the Analysis Server should listen to for incoming connections from clients.
* `<data-folder>` - The path to the folder containing the sensors. Each sensor is represented as with the folder name `id_sensorType`. Each .csv file in this folder is named like `yyyy-mm-dd_sensorType_sensor_id.csv`. Our script `start.sh` will download the required files from TU Graz, their origin is from [luftdaten.info](https://luftdaten.info). Please execute `start.sh` only once.

The script `./server` already handles these arguments and starts the server. The server should run indefinitely and wait for incoming requests. In the unmodified framework however, the program prints the parsed arguments and terminates. Thus 99% of the server logic must be implemented by you. There is no CLI for the analysis-server!

## Client

The main method of **client** is located at `at.tugraz.oop2.client.ClientMain.main(String... args)` and contrary to the Analysis Server, a significant portion of the Client is already implemented. There are two distinct ways the client can function:

* `<ip> <port>` - Connects to the Analysis Server and starts the CLI. Done by the script `./cli`.
* `<ip> <port> <command> <args ...>` - Connects to the Analysis Server, executes one single request and terminates. Also done by `./cli`.

All command I/O is already implemented by the class `CommandHandler`. The class `ClientConnection` is responsible for maintaining a connection to the AnalysisServer and for sending and receiving requests. It has most parts missing and thus has to be implemented by you like connection handling and data visualization.

## Client Commands

The CLIs purpose is to interact with the analysis-server:

* `help` - Displays a list of client commands, so you don't have to look it up here.
* `ls` - Displays a list of all sensors (identified by their id and metric). The client must not directly access the data/sensors folder - everything must be handled by the analysis server.
* `data <sensorId> <metric> <from-time> <to-time> [operation [interval<s|m|h|d>]]` - Queries sensor data within the given  data range (optional with a DataSeries.Operation or with a DataSeries.Operation combined with interval-seconds) from the analysis-server and displays the data in a table. While *from* is inclusive, *to* is exclusive. Date/Time format is `yyyy-MM-dd'T'HH:mm:ss`. Operation could be {MIN, MAX, MEAN and MEDIAN}.
* `linechart <sensorId> <metric> <from-time> <to-time> <imagePath> [operation [interval<s|m|h|d>]]` - Queries sensor data within the given range and stores it as an linechart-image at imagePath. While *from* is inclusive, *to* is exclusive. Date/Time format is `yyyy-MM-dd'T'HH:mm:ss`. If operation and interval-seconds are given, the functions {MIN, MAX, MEAN, MEDIAN} should be applied for the given interval.
* `scatterplot <sensorId1> <metric1> <sensorId2> <metric2> <from-time> <to-time> <imagePath> [operation [interval<s|m|h|d>]]` - Queries sensor1 and sensor2 data for given metric1 and metric2 within the given range (and interval if requested) and stores it as an scatterplot-image at imagePath. Sensor1 should be dispalyed on the x-axis, sensor2 on the y-axis. While *from* is inclusive, *to* is exclusive. Date/Time format is `yyyy-MM-dd'T'HH:mm:ss`. If operation and interval are given, the functions {MIN, MAX, MEAN, MEDIAN} should be applied for the given interval.

More information on how these queries should be implemented can be found below.

## Logger

Every relevant action taken, received and sent data by the analysis-server and client must be logged using the Logger-class. Look up the documentation in the Logger.java file in shared in the upstream repository.

# General Remarks to Both Assignments

This document specifies the goals of the assignments and gives hints on how to implement solutions. It is deliberately not intended by us to completely define all specifics of the implementation. Students are required to do their own design decisions where needed, and this is actually one of the learning goals of the assignments. Assignment 2 builds upon Assignment 1, so you must use your code from Assignment 1 as a starting point for Assignment 2.

Aside from implementing the features described below, both submissions must fulfill a number of requirements:

* The system must be robust against introduction of new sensors, outages of sensors, data gaps or unreachability of sensor folder.
* The system shall be correct, which includes correct synchronization, error handling (timeouts!), detection of faulty data/input and closing of streams/connections to avoid resource leaks. The user/client must always get feedback when an error occurs! Error messages must be easy to follow. For example, "Can't connect to server." is better than `java.net.ConnectException: Connection refused` or "Please enter a positive number." is better than `java.lang.NumberFormatException: For input string: "four"`.
* The CLI shipped with the framework must not be altered in its functionality, since we use it to test parts of your submissions.
* Client and Analysis Server shall communicate via Sockets. However it is up to you how you design the protocol and whether you make it stateless or stateful.
* The Analysis Server performs all operations to get the data into final shape as requested by the Client. This means all computations must be performed at the Analysis Server (e.g., sampling, calculating median).

When your submission is graded, the last commit before the deadline on the **master** branch is checked out. Don't forget to write the file `SUBMISSION.md` in the root directory of your repository, where you document important design decisions, open issues or bonus tasks you implemented.

# Assignment 1 - Protocol, Caching, Simple Visualizations

In this assignment you will implement the logic necessary for the Client and Analysis Server to exchange data. You need to integrate a graph library to display line charts. To reduce the number of requests to the file system, Analysis Server must cache already fetched data from the .csv files.

The first assignment is comprised of these tasks, yielding 50 points in total:

## Connection Handling and Protocol (Required, 0P)

Implement the infrastructure that allows one or more clients to connect to the Analysis Server at the same time. Connected clients can send an arbitrary number of requests to the Analysis Server, where they are processed in parallel. Clients can connect without any authentication. Make sure to find a solution for queries that get answered in a different order than they were requested in. Since you need to implement one or more commands to show that your infrastructure works, we can't give you points for this task directly. We will however deduct points if your code lacks synchronization, basic security checks or doesn't handle errors.

You are obligated to use Java Sockets to implement the infrastructure. We recommend using serialization for encoding data, but you can design your own protocol if you want to do it the hard way.

## `ls` Command (**5P**)

The Client sends a request to the Analysis Server with no parameters given. The Analysis Server shall then query all known sensors (identified by sensorId and metric) from the file system and send the list of sensors with as much detail as possible back to the Client, which then displays them in the CLI.

## `data` Command (**15P**)

The Client sends a request containing a `Sensor` instance with `sensorId` and`metric`, a start (inclusive) and end time (exclusive), optional plus a DataSeries.Operation or a DataSeries.Operation and an interval, to the Analysis Server. The Analysis Server shall then query the data from the cache, or if not present from the file. The requested DataSeries.Operation should be calculated and returned to the client, if no DataSeries.Operation is provided, all data present should be returned. DataPoints from operations should have valid LocalDateTimes - come up with a reasonable solution. The client displays the data inline in a table. Think about meaningful columns and rows.
<a href="interpolation"></a>
Interval is given in `integer<s|m|h|d>` (seconds, minutes, hours or days>) and you have to fit the data by sampling, so one DataPoint is given in the requested interval.
If datapoints are missing because the .csv-file provides no data after sampling, do the following:
* if one DataPoint is given within the given time range, use this DataPoint
* if between two present DataPoints one is missing, interpolate this missing datapoint
* if more than one DataPoint in sequence is missing, show an error message on the client.

## `linechart` Command (**10P**)

The Client sends a request containing a `Sensor` instance with `sensorId` and `metric`, a start (inclusive) and end time (exclusive), optional plus a DataSeries.Operation and an interval, to the Analysis Server. The Analysis Server shall then query the data from the cache, or if not present from the file, and return the processed data to the client. The client stores the data in a linechart image containing all relevant information like time, sensor, metric, min, max, mean. The linechart axis have to be scaled meaningful.

## `scatterplot` Command (**10P**)

The Client requests the two time series from sensor1 and sensor2 from the Analysis Server and displays the data as a scatter plot image. The sensors and metrics could be the same or different and the axis should be scaled accordingly. Each point represents a point in time with the x-axis having the value of sensor1 and the y-axis that of sensor2.

## Caching (**10P**)

The goal of this task is to let the Analysis Server cache sensor data from previous requests in the RAM, so no more requests to the file system have to be made for similar queries. For example, the query `data ... 2020-10-01T00:00:00 2020-10-05T00:00:00 1d` is processed by first checking if all data in the requested interval is cached. If so, send the cached values to the Client. Otherwise fetch the needed data from the .csv files in the file system, write the data to the cache and send the result to the Client. Afterwards, the query `data ... 2020-10-01T00:00:00 2020-10-03T00:00:00 60s` must result in a cache-hit, while `data ... 2019-10-03T00:00:00 2019-10-07T00:00:00 60s` results in a (partial) cache-miss. Cache every data point from the .csv file and apply operations if needed every time. Partial cache-miss could be handled as cache-miss. If you come up with a beautiful solution bonus points could be rewarded. Please have a look at the bottom of this page.

# Assignment 2 - Clustering

The second assignment consists of implementing a clustering algorithm called self-organizing maps [SOM] or Kohonen maps. Clustering is used to automatically find patterns in a huge data set with very little user input given. The SOM algorithm uses a list of input curves and clusters these into different nodes which are connected in a grid shape. The result of this algorithm are different clusters consisting of a weight vector and zero or multiple of the curves which were given as input data.
Based on the members of each cluster additional [analysis](#heatmapoperations) may be done.

## Self-Organizing Maps

Self-organizing maps is a method of clustering data. During computation a set of input data, in our case curves, will be assigned into different _nodes_ of the underlying _grid_. In order to be able to assign one input curve to one of the grids nodes each node holds a prototype (or weight) vector. During the training steps the input curves will be sequentially processed and assigned to the so-called _best-matching-unit_ (which holds the best matching reference vector). This best match will then be adjusted to the input curve, as well as all of its neighbouring nodes, which are still within the _update radius_. The adjustment needs to be scaled by the current _learning rate_. Both the update radius and the learning rate need to be reduced as the training steps continue.

As soon as the training is concluded you can assign all input curves to their best matching units and report them as the final clusters.

For more details please refer to the [lecture slides](https://tc.tugraz.at/main/pluginfile.php/142869/mod_folder/content/0/OOP2VO-2020-11-05-Data-Analysis-Concepts.pdf) (starting from slide 27).


The second assignment is comprised of the following tasks, yielding 50 points in total:

## `cluster` Command (**25P**)
`cluster (all | <id>[,<id>]+) <metric> <from> <to> <interval> <operation> <length> <gridHeight> <gridWidth> <updateRadius> <learningRate> <iterationPerCurve> <resultID> <amountOfIntermediateResults>`

E.g.: `cluster 1503,1693 P1 2018-01-01 2018-01-05 1h mean 24 2 2 1 0.5 10000 0xCAFE 100`

The server starts clustering with the SOM algorithm using either `all` sensors or a list of sensors given by `<id>[,<id>]+` which are applicable for the given `<metric>`. You need to sample data points using the given `<interval>` and `<operation>` into curves with a fixed `<length>` starting at `<from>` until `<to>`. The query `cluster 1503,1693 P1 2018-01-01 2018-01-03 1h mean 24 ...` should therefore start the SOM algorithm with a total of 4 curves each consisting of 24 data points.

`<gridHeight>`, `<gridWidth>`, `<updateRadius>`, `<learningRate>` and `<iterationPerCurve>` define the parameters with which the SOM algorithm should be started. The `<resultID>` and `<amountOfIntermediateResults>` need to be a unique ID representing the query and the amount of intermediate results the server should send to the client.

Clustering is going to get interesting as soon as you cannot do it by hand anymore, in other words: The list of input data should be of noteworthy size. The server is going to need some time to execute such queries for which it will occasionally send an intermediate result to the client. If the client does not accept one of the intermediate results you may abort the computation on the server.

The intermediate results are to be stored in `<your-project-root>/clusteringResults/<resultID>` in json format.

Alternatively you can send all intermediate results as soon as the server has finished and postprocess those then. This will be easier to implement but lead to a _point deduction_.

_Edit 09. 12. 2020.:_
  - Your server must not accept any `cluster ...` requests where `<length>` is not a divisor of `(<to> - <from>)/<length>`, i.e. you should not process requests with which dangling data points would be left over after sampling and splitting the complete data range into `DataSeries` of length `<length>`. An example which is to be rejected: `cluster 1503 P1 2018-01-01 2018-01-02 1h mean 15 ...`. Your server needs to send an appropriate error message to the client. [TC-Posting](https://tc.tugraz.at/main/mod/forum/discuss.php?d=29308#p61523)
  - Your server will need to interpolate missing datapoints as results of empty intervals according to the requirements for [assignment one](#interpolation), with one additional requirement: If two missing datapoints represent the last and first interval of two consecutive `DataSeries` after splitting, these two values need to be filled with their preceding or their subsequent neighbour respectively. This requirement is a minimum, if you plan on implementing a more sophisticated version of interpolation (for gaps either within one `DataSeries` and/or spanning two consecutive `DataSeries`) please consult your tutor.
  - If your server is unable to interpolate values for a given `DataSeries` after splitting, only this exact `DataSeries` is to be removed from the input data. The `DataSeries` following the removed one need to abide to the implied begin of the split `DataSeries`. E.g. Given the following request: `cluster <id> P1 2018-01-01 2018-01-04 1h mean 24 ...` and missing values from `2018-01-02T12:00` up until `2018-01-02T15:00` the resulting set of `DataSeries` must contain _two_ `DataSeries` starting at `2018-01-01T00:00` and `2018-01-03T00:00` respectively.
  - If your server receives a request with a given list of sensor ids you will need to check the validity w.r.t. each single sensor. ([TC-Posting](https://tc.tugraz.at/main/mod/forum/discuss.php?d=29308#p61523)) Two cases may emerge here:
    * The sensor id is invalid. In this case your server must process the request with all valid id - metric combinations and warn the client about all invalid sensor ids.
    * The given sensor does not feature the given metric. The same rule as above applies. The client needs to be informed about the id of the sensor that could not be featured in the clustering algorithm.


## `listresults` Command (**1P**)

`listresults`

This command needs to list all finished clustering results.

_Edit 21. 12. 2020.:_

  - This command needs to be solely implemented on the client-side.

## `rm` Command (**1P**)

`rm <resultID>`

This command will remove the results of a finished clustering query.

_Edit 21. 12. 2020.:_

  - This command needs to be solely implemented on the client-side.

## `inspectcluster` Command (**2P**)

`inspectcluster <resultID> <heightIndex> <widthIndex> <boolVerbose>`

Prints information about the given cluster node indexed by `<heightIndex>` and `<widthIndex>`. An example can be seen below. `<boolVerbose>` defines whether information about all cluster members should be printed. In the example below this boolean would toggle the `List of all members:` either off or on.


```
+-------------------------------------+
| Node (i, j) from resultID           |
+--------------+----------------------+
| #Members     | (int)#member         |
+--------------+----------------------+
| #Error       | (float)Error         |
+--------------+----------------------+
| #Entropy     | (double)entropy      |
+--------------+----------------------+
| List of all members:                |
+--------+------+---------------------+
| <from> | <to> | <sensor>            |
| <from> | <to> | <sensor>            |
| <from> | <to> | <sensor>            |
| ...    | ...  | ...                 |
+--------+------+---------------------+

```


## `plotcluster` Command (**12P**)

`plotcluster <resultID> <clusterPlotHeight> <clusterPlotWidth> <boolPlotClusterMember> <heatMapOperation> <boolPlotAllFrames>`

This command plots either the final result of the query associated to `<resultID>` or the plots for each of the intermediate results (depending on `<boolPlotAllFrames> `). These can then be used for visualization in a video. Creating atleast one videos is mandatory in order to be able to get points from this part of the assignment. We will provide you with [commands](#videocommands) to create such videos.

Your frames should consist of a status bar containing information about:
  - the current iteration count,
  - the current update radius,
  - the current learning rate and
  - your *group number*.

Additional useful information is obviously welcome.

Besides the status bar each of the frames need to plot all the different clusters with respect to their cartesian coordinates in the grid, i.e. cluster `(0,0)` should be in the top left corner, `(0,1)` next to it on the right and so on. The height and width of each individual cluster plot is defined via `<clusterPlotHeight>` and `<clusterPlotWidth>`. `<boolPlotClusterMember>` defines whether all of the members or only the weight of the nodes should be plotted.

In order to easily use the `concat`-demuxer of ffmpeg, you may want to aggregate a *sorted* list of your plots as your are creating them. This will make it ease to use [ffmpeg](#videocommands) later.


## Operations for Heatmaps (**6P**) <a name="heatmapoperations"></a>

With the command described above we are able to visualize the behaviour and final result of the SOM algorithm. Additionally one can visualize some analysis results from the clusters, like
  - the normalized amount of members per cluster or
  - the normalized sum of errors with respect to the clusters weight vector.

To be able to actually plot a heatmap (e.g. as the background color of each cluster) you will need to normalize the values for each of your cluster. The color of the heatmap with respect to a given cluster can then be calculated for example as the interpolation between to colors or the alpha value of the background color (i.e. the more opaque the background color the higher the normalized value).


## Creating and Uploading Videos (**3P**) <a name="videocommands"></a>

Creating videos is possible with `ffmpeg` as follows:

`ffmpeg -r <framesPerSecond> -f concat -i <inputFile> -vcodec libx264 -crf 25 -pix_fmt yuv420p <videoName>.mp4`

where `<framesPerSecond>` are the frames to be displayed per second (about 10 - 15 is a good amount), `<inputFile>` is a *sorted* list of frames:

```
file 'iter_0_clusterplot.png'
file 'iter_1000_clusterplot.png'
...
```

and `<videoName>` is the name of the video to be created. You will need to upload at least one video with interesting findings to the teach center (do not upload the whole source code there!).

## Reference Videos <a href="referencevideos"></a>

  - [#1](https://www.youtube.com/watch?v=Ua-DINOeHFY)
  - [#2](https://www.youtube.com/watch?v=zQtQTtP1OzY)
  - [#3](https://www.youtube.com/watch?v=fGFd3-ULLOE)
  - [#4](https://www.youtube.com/watch?v=kIlkY8fyTfs)


## Bonus Tasks for Assignment 2

* _Different Kernel Functions_:

    The update function of the SOM usually works in a boolean fashion: A neighbouring node is either _within_ the radius or it is not. In order to make more use of the topological layout of the nodes one may also implement a 'gaussian kernel function' as the update function.

    A gaussian kernel will more heavily update nodes which are nearer to the best matching unit and will influence nodes which are further away less.

* _Distance Entropy_:

    Our provided data features the quite important location of the sensors. An interesting measure for a given cluster is the distribution of locations of its members sensors. We call this the distance entropy. If a cluster only features curves from the exact same sensor its entropy is 0, whereas clusters with members from multiple different sensors will have a very high distance entropy.

    In order to calculate the entropy of a cluster you will need to implement the [haversine](https://www.movable-type.co.uk/scripts/latlong.html) formula for distances on spheres. The entropy will then be the weighted sum of all distance between all members of a cluster. You may plot the normalized entropies as one of the heatmap operations.

* _U-Matrix_

    The _unified distance matrix_ is a `<gridHeight> - 1`x`<gridWidth> - 1`-matrix consisting of grayscale values which represents the euclidean distances between the connected nodes weight vectors of a SOM grid. The U-Matrix is therefore another way to cluster the input data, namely by clustering the final clusters. Light gray areas of the resulting grayscale image indicate areas of nodes which weight vectors are closer, whereas darker parts of the image indicate borders between these cluster of nodes.

# Bonus Tasks

There is a wide range of additional features to implement, advanced visualizations or performance optimizations. You are free to come up with your own ideas, but you should ask us for the number of points you can achieve for your idea before you hand in your submission. Please include a description of your bonus tasks in `SUBMISSION.md`. Here is a list of ideas with things you can implement as a bonus:

* *History of recent queries* - The Client keeps a list of queries recently made and an option to load the parameters into the input fields and to repeat the query. More points if the history is stored in a file.
* *Unit Tests* - Write tests using JUnit that are automatically run when building the project.
* *Protocol without serialization* - Instead of using serialization to convert between bytes and objects, find a more efficient way to encode and decode packets yourself.
* *Line chart with multiple sensors* - The user can select multiple sensors to be displayed in one line chart. If two metrics are selected, the scale of one metric is displayed on the left side of the chart and the other metric on the right side.

# Grading Scheme

There are a total of 100 points to be achieved, with each assignment counting 50 points. The points you get on an assignment including bonus points cannot exceed 60 points. The final grade depends on the overall number of points you achieve:

```
public static Grade getGrade(float ass1, float ass2) {
  ass1 = Math.min(ass1, 60);
  ass2 = Math.min(ass2, 60);
  float total = ass1 + ass2;
  if (total >= 50 && ass1 >= 10 && ass2 >= 10) {
    if (total >= 86) {
      return Grade.SEHR_GUT; // 1
    }
    if (total >= 74) {
      return Grade.GUT; // 2
    }
    if (total >= 62) {
      return Grade.BEFRIEDIGEND; // 3
    }
    return Grade.GENUEGEND; // 4
  }
  return Grade.NICHT_GENUEGEND; // 5
}
```

The code above implies that you need to have at least 10 points on both assignments and at least 50 points in total to pass the course. If you fail to achieve at least 25 points (excluding deductions at the Abgabegespräch) on an assignment, you have the chance to deliver a second submission. The assignment will then be graded with `points < 25 ? points : 25 + (points - 25) / 2` points.

For both of the assignments there will be assignment discussions (Abgabegespräche) for each team shortly after the deadline, where you must present your work and explain your code. All team members must be present. We'll also have a quick look into your Git commit statistics. If we find out that you didn't participate sufficiently, you'll get individual point deductions. Plagiarism is not tolerated and leads to a negative grade for the whole team and other teams involved.
