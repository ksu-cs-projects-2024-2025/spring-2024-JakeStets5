# Herd Alert

## Description

Herd Alert is an early warning app for cattle owners. It computes the Comprehensive Climate Index using a formula created by Hayati Koknaroglu and asseses the "heat threat" to cattle. If there is a high threat, cattle may die from the heat. 

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)

## Installation

Because this app has not been deployed, cloning the repository and opening it in android studio is the best way to run it.     
Cloning the databases may pose problems. Here are the following reccomendations:

* Clone the repository. Open the project and run. If you are able to search the city London, great! The app has been cloned successfully.
  
* If the app cannot find the table to query London from, or you would like to implement the larger database of cities, follow these steps:
  * Download the 
  * Click on the hamburger menu in the top left -> click on "View" -> click on "Tool Windows" -> click on "Device Explorer"
  * Click on the Device Explorer, (it should show up on the right hand side of Android Studio; a phone under a magnifying glass), click on "data" -> "data" -> "jakestets5.ksu.heatstressapp" (or your package name if you change it)
  * Right-click on "databases".
  * Select "upload" and upload settings.db, SavedLocations.db, and either Cities.db (around 40,000 cities) or TrimmedCities.db (14 specific cities) from the directory the project is in.
    
* As of May 10th, 2024, the API keys used to gather weather and location details will be deleted. To create and implement your own Open Weather Map key, follow these steps:
  * Click on the following link: https://openweathermap.org.
  * After creating an account or signing in, click on your account name in the top right corner -> "My API keys" -> "Generate".
  * When your api key is generated copy it and replace the value of the variable "weatherApiKey" within the "CurrentWeatherApiHelper" and "ForecastApiHelper" with your API key.
  * Click on the API option on the top ribbon. Subscribe to the One Call API 3.0 (costs may apply).
    
* To create and implement your own geocoding API key, follow these steps:
  * Click on the following link: https://cloud.google.com.
  * After creating an account or signing in, select the hamburger menu in the top left -> "Google Maps Platform" -> "APIs and services".
  * Search for "geocoding" in the search bar at the top -> click on "Geocoding API" -> "Enable".
  * Click on the hamburger menu again -> "APIs & Services" -> "Credentials".
  * Click on "Create Credentials" -> "API key".
  * Replace the value of the variable "geocodeApiKey" within the "CurrentWeatherApiHelper" with your API key.
    
## Usage

After installation, using the app is quite straightforward. Clicking the green arrow in the top right of Android Studio runs the app. There's 4 options once the app is running. 

* 7-day forecast button - shows the 7 day forecast with relevant weather details.
* Saved Locations button - shows any cities saved by the user. Also serves as the list of cities that get the threat assessed for the push notifications.
* Settings button - displays settings that the user can adjust to get a more specific heat threat analysis.
* Search bar - allows the user to query any database. Clicking the floppy disk (save) icon on the results from a user query saves the city to the Saved Locations database.

## Contributing

Instructions for how others can contribute to the project.
