Heroku Deployment Setup
-----

[Application URL](http://damp-crag-7750.herokuapp.com/)

Installation
-----

* Download the [Heroku toolbelt](https://toolbelt.heroku.com/) 
* Add the Heroku git repo as a remote dependancy
  * `git add remote heroku git@heroku.com:damp-crag-7750.git`


Basic Setup
-----

The following files are used to configure the Heroku deployment

* [Procfile](https://github.com/piguy79/GalCon/blob/master/Procfile)
  * This file is used to describe the type of application Heroku is expecting to deploy. It also needs the path to the app.js in order to run it.
* [package.json](https://github.com/piguy79/GalCon/blob/master/package.json)
	* This file defines the dependencies needed for the application to run. Heroku downloads all dependencies during the deployment process.
* Environment variables. There are two environment variables passed to the process when it starts.
	* [app.js](https://github.com/piguy79/GalCon/blob/master/GalCon-Server/app.js) is making use of `process.env.PORT`
	* [mongooseConnection.js](https://github.com/piguy79/GalCon/blob/master/GalCon-Server/modules/model/mongooseConnection.js) makes use of `process.env.MONGOLAB_URI`


Deployment
-----

* Run a `git push heroku master`