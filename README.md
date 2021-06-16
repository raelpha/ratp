<p align="center"> 
  <img src="img/ratp_logo.png" alt="Logo RATP" width="150px" height="181px">
</p>
<h1 align="center"> Paris metro simulation </h1>
<h3 align="center"> IA04 - Multi Agent Systems </h3>
<h5 align="center"> Project Assignment - <a href="https://www.utc.fr/">Université de Technologie de Compiègne</a> (Spring 2021) </h5>

<p align="center"> 
<img src="img/ratp_network.gif" alt="Animated network" height="382px">
</p>

<p>We developped a simulation of the whole parisian subway.
It includes simulation of subway train, traveller pathfinding, station failure, etc. </p>

<h2> :floppy_disk: Project Files Description</h2>

<p>There are 4 main directories for this projet:</p>
<ul>
  <li><b>data directory</b> - Includes all cleaned data originaly from <a href="https://data.iledefrance-mobilites.fr/pages/home/">IDFM Open Data</a> .</li>
  <li><b>img directory</b> - Includes all logos used in this documentation.</li>
  <li><b>lib directory</b> - Includes all libraries used in our project. None of these belong to us.</li>
  <li><b>src directory</b> - Includes all java files.</li>
</ul>

You can also find our slides used for our presentation here: [presentation slides](ratp_slides_presentation_15062021.pdf).

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/grass.png)

<h2> :tv: Video example</h2>

<p>You can find below a Youtube video that show how the simulation is working:</p>
<a href="https://www.youtube.com/watch?v=08HplBNFX04"> Youtube</a> <! --FIX ME -->



![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/grass.png)

<h2> :open_file_folder: Dependencies</h2>

<p> We used <a href="https://cs.gmu.edu/~eclab/projects/mason/"> Mason</a> for this project. </p>

<p> From their website: "MASON is a fast discrete-event multiagent simulation library core in Java, designed to be the foundation for large custom-purpose Java simulations, and also to provide more than enough functionality for many lightweight simulation needs. 
MASON contains both a model library and an optional suite of visualization tools in 2D and 3D." </p>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/grass.png)

<h2> :clipboard: Input files</h2>

<p> We used <a href="https://data.iledefrance-mobilites.fr/pages/home/"> IDFM OpenData</a> ressources to implement the model. </p>
<p> The <b>data</b> folder contains:</p>
<ul>
  <li><b>lines</b> - A shapefile, with each LineString as a line section, storing:</li>
    <ul>
      <li><i>color</i>: color of the line section</li>
      <li><i>origin</i>: starting point of the line section</li>
      <li><i>destination</i>: ending point of the line section</li>
      <li><i>line</i>: the line id</li>
    </ul>
  <li><b>schedule</b> - A csv, storing the different itineraries (multiple paths on the same line)</li>
    <ul>
      <li><i>order</i>: order</li>
      <li><i>station_name</i>: name of the station</li>
      <li><i>line</i>: line id</li>
      <li><i>direction</i>: heading of the train</li>
      <li><i>station_origin</i>: origin station</li>
      <li><i>station_destination</i>: destination station</li>
      <li><i>service</i>: the service name (usually origin -> destination)</li>
    </ul>
  <li><b>stations</b> - A list of all the stations</li>
    <ul>
      <li><i>name</i>: the name of the station</li>
    </ul>
</ul>

<p>The aforementionned files can be made out of <a href="https://data.iledefrance-mobilites.fr/pages/home/">IDFM</a> ressources using <a href=" www.qgis.org">QGIS</a>, or handmade with ETL tools such as Pentaho or Alteryx.</p>

<p>The system will automatically compute the connecting stations based on their names. Three singletons are build at startup onto these files and will store all the dynamic objects shared by the agents and their algorithms.</p>

<p>As little information as possible is <i>hardcoded</i> so you may adapt this simulation to your favorite transit system as long as the data is on these input files with little code adjustment.</p>


![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/grass.png)

<!-- CREDITS -->
<h2 id="contributors"> :scroll: Contributors</h2>



Raphael Jaures: [![GitHub Badge](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/raelpha)[![LinkedIn Badge](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/raphaeljaures/)


Yvain Raynaud: [![GitHub Badge](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Raynaudy)
[![LinkedIn Badge](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/yvain-raynaud/)

Clément Giummara

Jimmy Luong: [![GitHub Badge](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Neaus77)
[![LinkedIn Badge](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/jimmy-luong-3a050b179/)

Hugo Martin

