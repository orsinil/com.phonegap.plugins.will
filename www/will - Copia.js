WillPlugin.prototype.scan = 
		 cordova.exec(
					successCallback, // success callback function
					errorCallback, // error callback function
					'willPlugin', // mapped to our native Java class called "CalendarPlugin"
					'addWillEntry'
				); 

    };
	
/*var willPlugin = {
    open: function(successCallback, errorCallback) {
		 cordova.exec(
					successCallback, // success callback function
					errorCallback, // error callback function
					'willPlugin', // mapped to our native Java class called "CalendarPlugin"
					'addWillEntry'
				); 

    }
}*/
