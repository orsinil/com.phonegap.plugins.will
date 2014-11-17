var cordova.plugins.willPlugin = {
    open: function(successCallback, errorCallback) {
		 cordova.exec(
					successCallback, // success callback function
					errorCallback, // error callback function
					'willPlugin', // mapped to our native Java class called "CalendarPlugin"
					'addWillEntry'
				); 
     }
}