SUBMISSION GROUP 215

Important design decisions:
For client and server communication we used socket. Main idea for recognizing when a request is sent/a response should be given was using .writeObject and .readObject. In this way server and
client can communicate through entered commands.  
1. ls command -> since there were no parameters to pass, we started with writing queryLS to client output so the server knows which command needs to be handled and to send the response.
Next step was to read csv files. Given 3 sensors in the task (SDS011, DHT22, BME280) we distinguish between 3 possible cases based on parameter index and type of metrics. As long as we need to read csv file, we are ignoring the header line
and starting from the second one. Going to csv file we just listed sensors and printed the table on client and server side.

2. data command -> client requesting data event will be recognized based on DataQueryParameters. In our implementation base case is distinguishing different type of operations entered in command line (none, min, max, mean, average).
Core concept was to consider data points between from-to interval and devide them into smaller TreeSets according to interval (if entered). If no interval exists the whole set of DataPoints will be taken and corresponding operation will be performed.
To check if we have to interpolate, report an error or proceed with calculations we created a counter which will be increased every time no DataPoints occur in an interval (if it equals 2 we will report an error). 
For interpolation we used 3 different calculations:
-if first point needs to be calculated we will sum up 0 (no point before) and next point we stored in TreeSet and devide with 2.
-if points within interval are mission we will sum up previous and next value from result TreeSet and take their mean
-if the last point in interval is missing and there is no next value that is included in TreeSet we will take the mean value from 2 previous elements.
We created also a list of found values so we can use elements with previous and next index (interpolation - case 2 and 3). Since we wanted to store interpolation result between two mentioned points, we used boolean variable to set it to true and wait until 
next DataPoint is calculated so we can store it first in a list to be able to use it for interpolation mean value calculation and then store it in TreeSet after interpolated point was added.

3. linechart ->  Linechart receives data from DataSeries and uses such data for further calculations. Arguments are passed, we get the timestamp and metric values and and have to find minimum, maximum and mean value of both of them. After iterating through DataSeries and finding them, we have to draw graph using proper calculations. We have to firstly normalize metric and timestamp to be able to properly draw graph. Then we use draw x and y axis according to our min and max values and calculate linechart according to normalized values. 


4. scatterplot -> It receives data from DataSeries and uses such data for further calculations. The logics behind scatterplot is similar to the linechart except that it receives additional sensor and metric. We have to normalize min and max values of these two elements and draw graph accordingly. We create 2 DataQueries in order to store 2 sensors and 2 metrics. 

Issues: 
We implemented missing parts of assignment based on what we understood from description. The cases we tested were 
working withour any problems. As an open issue we can mention missing implementation, since there is nothing in our code which will handle caching. 
Linechart issue with scaling: Graph that was drawn is too big, scaling issues.
Scatterplot: Graph



Bonus tasks: 
Eventhough there is a wide range of additional features to implement, we do not have any (additional) implementation for bonus tasks. 