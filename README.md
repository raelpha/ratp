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
  <li><b>img directory</b> - Includes all logo used in this documentation.</li>
  <li><b>lib directory</b> - Includes all libraries used in our project. None of these belong to us.</li>
  <li><b>src directory</b> - Includes all java files.</li>
</ul>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)

<h2> :book: Naive Bayes</h2>

<p>In machine learning, naive Bayes classifiers are a family of simple "probabilistic classifiers" based on applying Bayes' theorem with strong (naive) independence assumptions between the features.
Abstractly, naive Bayes is a conditional probability model: given a problem instance to be classified, represented by a vector
<img src="image/1.png" alt="Formula 1" style="max-width:100%;"></p>

<p>representing some n features (independent variables), it assigns to this instance probabilities
<img src="image/2.png" alt="Formula 2" style="max-width:100%;"></p>

<p>The problem with the above formulation is that if the number of features n is large or if a feature can take on a large number of values, then basing such a model on probability tables is infeasible. We therefore reformulate the model to make it more tractable. Using Bayes' theorem, the conditional probability can be decomposed as
<img src="image/3.png" alt="Formula 3" style="max-width:100%;"></p>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)

<h2> :clipboard: Execution Instruction</h2>
<p>The order of execution of the program files is as follows:</p>
<p><b>1) spam_detector.py</b></p>
<p>First, the spam_detector.py file must be executed to define all the functions and variables required for classification operations.</p>
<p><b>2) train.py</b></p>
<p>Then, the train.py file must be executed, which leads to the production of the model.txt file. 
At the beginning of this file, the spam_detector has been imported so that the functions defined in it can be used.</p>
<p><b>3) test.py</b></p>
<p>Finally, the test.py file must be executed to create the result.txt and evaluation.txt files.
Just like the train.py file, at the beginning of this file, the spam_detector has been imported so that the functions defined in it can be used.</p>


![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)

<!-- CREDITS -->
<h2 id="credits"> :scroll: Credits</h2>

Raphael Jaures:

[![GitHub Badge](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/...)
[![LinkedIn Badge](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/.../)


Yvain Raynaud:

[![GitHub Badge](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Raynaudy)
[![LinkedIn Badge](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/yvain-raynaud/)