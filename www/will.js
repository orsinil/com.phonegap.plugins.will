var cordova.plugins.willPlugin = {
    createEvent: function(title, location, notes, startDate, endDate, successCallback, errorCallback) {
		 cordova.exec(
					successCallback, // success callback function
					errorCallback, // error callback function
					'willPlugin', // mapped to our native Java class called "CalendarPlugin"
					'addWillEntry'
				); 
     }
}